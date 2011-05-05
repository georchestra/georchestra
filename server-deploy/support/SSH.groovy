import com.jcraft.jsch.*;
import java.io.*;

class SSH {
  final def log
  final def host
  final def jsch
  private def usingSSHPrivateKey
  private def password
  private def username
  final def sessionConfig

  SSH(config) {
    this(config['log'],
      config['settings'],
      config['host'],
      config['username'],
      config['privateKey'],
      config['passphrase'],
      config['password'],
      config['sessionConfig'])
  }
  
  SSH(log,settings,host,username=null,privateKey=null,passphrase=null,password=null,sessionConfig=null) {
    this.log=log
    this.host=host
    this.jsch=new JSch();
    this.username=username
    this.password=password
    this.sessionConfig = sessionConfig
    if(this.sessionConfig==null) {
      this.sessionConfig = new java.util.Properties(); 
      this.sessionConfig.put("StrictHostKeyChecking", "no");
    }
    
    if(username==null || (privateKey==null && password==null)) {
      def serverSettings = settings.properties['servers'].find{server -> server.id == host}
      if(serverSettings == null) {
          throw new AssertionError("""
Unable to find server settings for $host in the maven settings.xml (typically in ~/.m2/settings.xml) 
See: http://maven.apache.org/settings.html for details about settings.

Another option is to provide the username and either path privateKey or a password
""")

      }
      if(serverSettings.privateKey != null) {
        privateKey = serverSettings.privateKey
        passphrase = serverSettings.passphrase
      }
      if(this.password==null) this.password=serverSettings.password
      if(this.username==null) this.username=serverSettings.username
    }
    
    if(privateKey != null) {
      usingSSHPrivateKey = true
      if(passphrase==null) {
        char[] passwd;
        Console cons;
        if ((cons = System.console()) != null && (passwd = cons.readPassword("Enter passphrase for keystore %s: ",privateKey)) != null) {
          passphrase = new String(passwd)
        }       
      }
      if(passphrase==null) {
        jsch.addIdentity(privateKey)
      }else {
        jsch.addIdentity(privateKey,passphrase)
      }
    }
    
  }
  /**
   * @param src either a string or file of the file (not directory) to copy
   * @param dest either a string or file of the file (not directory) to copy to
   */
  def scp(src,dest) {
    final File srcFile,destFile
    if(src instanceof File) {
      srcFile = src
    } else {
      srcFile = new File(src.toString())
    }
    if(dest instanceof File) {
      destFile = dest
    } else {
      destFile = new File(dest.toString())
    }
    log.info("scp $srcFile $username@$host:$destFile")
    
    boolean ptimestamp = false;
    
    def channelFac = {session ->
      // exec 'scp -t destFile' remotely
      String command="scp " + (ptimestamp ? "-p" :"") +" -t "+destFile;
      Channel channel=session.openChannel("exec");
      ((ChannelExec)channel).setCommand(command);
      
      return channel
    }
    
    withSession channelFac,{session,channel ->

      // get I/O streams for remote scp
      def out=channel.getOutputStream()
      def input=channel.getInputStream()

      if(checkAck(input)!=0){
        throw new AssertionError("An host did not respond with an ACK after intializing transfer")
      }
      String command
      if(ptimestamp){
        command="T "+(srcFile.lastModified()/1000)+" 0";
        // The access time should be sent here,
        // but it is not accessible with JavaAPI ;-<
        command+=(" "+(srcFile.lastModified()/1000)+" 0\n"); 
        out.write(command.getBytes()); out.flush();
        if(checkAck(input)!=0){
          throw new AssertionError( "An host did not respond with an ACK after setting timestamp")
        }
      }

      // send "C0644 filesize filename", where filename should not include '/'
      long filesize=srcFile.length();
      command="C0644 "+filesize+" ";
      if(srcFile.path.lastIndexOf('/')>0){
        command+=srcFile.path.substring(srcFile.path.lastIndexOf('/')+1);
      }
      else{
        command+=srcFile.path;
      }
      command+="\n";
      out.write(command.getBytes()); out.flush();
      if(checkAck(input)!=0){
         throw new AssertionError("An host did not respond with an ACK after setting file size")
      }

      // send a content of srcFile
      byte[] buf=new byte[1024];

      srcFile.withInputStream { fis ->
        def written = 0
        def total = srcFile.length()
        def start = System.currentTimeMillis()
        def last = start
        
          while(true){
            int len=fis.read(buf, 0, buf.length);
            if(len<=0) break;
            out.write(buf, 0, len);
            written += len
            if((System.currentTimeMillis() - last) > 5000) {
              last = System.currentTimeMillis()
              log.info("${Math.round(written / total * 1000) / 100}% written: ${Math.round(written/1000)}/${Math.round(total/1000)}KB time: ${Math.round(last - start)}ms" )
            }
          }
      }
    
      // send '\0'
      buf[0]=0; 
      out.write(buf, 0, 1); out.flush();
      if(checkAck(input)!=0){
        throw new AssertionError("An error occurred when trying to copy "+srcFile+" to "+host+":"+destFile)
      }
      out.close();

    }
    
    log.info("scp complete")
  }
  
  private int checkAck(input) throws IOException{
    int b=input.read();
    // b may be 0 for success,
    //          1 for error,
    //          2 for fatal error,
    //          -1
    if(b==0) return b;
    if(b==-1) return b;

    if(b==1 || b==2){
      StringBuffer sb=new StringBuffer();
      int c = input.read();
      sb.append((char)c);
      while(c!='\n') {
	      c=input.read();
	      sb.append((char)c);
      }
      
      if(b==1){ // error
	      log.error(sb.toString());
      }
      if(b==2){ // fatal error
	      log.error(sb.toString());
      }
    }
    return b;
  }
  static def zeroCode = {result -> return result['code'] == 0}
  static def nonZeroCode = {result -> return result['code'] != 0}
  static def acceptAll = {result -> return true}

  /**
   * @param command the command to execute on remote server
   * @param validation a closure that checks the result and returns true if the result is acceptable otherwise false 
   *                    and an error will be reported with the result and responseCode
   * @return Map(code:<Response code>, sysout:<system out data>)
   */
  def exec(command,validation=acceptAll) {
    log.info("executing "+command)
    
    def channelFac = {session ->
      Channel channel=session.openChannel("exec");
      ((ChannelExec)channel).setCommand(command);

      channel.setInputStream(null);

      //channel.setOutputStream(System.out);
      ((ChannelExec)channel).setErrStream(System.err);
      
      return channel
    }
    withSession channelFac,{session,channel ->
      InputStream input=channel.getInputStream();
      
      def response = ""
      def responseCode = 0
      
      byte[] tmp=new byte[1024];
      while(true){
        while(input.available()>0){
          int i=input.read(tmp, 0, 1024);
          if(i<0)break;
          response = new String(tmp, 0, i)
          log.debug("ssh exec response:\n$response\n");
        }
        if(channel.isClosed()){
          log.debug("exit-status: "+channel.getExitStatus());
          def returnVal = [code:channel.getExitStatus(),response:response]
          if(!validation(returnVal)) {
            throw new AssertionError("ssh exec command: '$command' completed but with an unacceptable result: $returnVal")
          } 
          return returnVal
        }
        try{Thread.sleep(500);}catch(Exception ee){}
      }
    }
  }
  
  def withSession(channelFactory,closure=acceptAll) {
    Session session=jsch.getSession(username, host, 22);
    session.setConfig(sessionConfig)
    if(!usingSSHPrivateKey) {
      if(this.password == null) {
        char[] passwd;
        Console cons;
        if ((cons = System.console()) != null && (passwd = cons.readPassword("Enter password for user %s: ",username)) != null) {
          this.password = new String(passwd)
        }
      }
      session.setPassword(this.password)
    }
    session.connect();
    Channel channel
    try {
      channel = channelFactory(session)
      channel.connect()
      return closure(session,channel)
    } finally {
      if(channel != null) {
        channel.disconnect()
      }
      session.disconnect()
    }
  }
}
