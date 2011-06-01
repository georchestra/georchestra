import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

class SSH {
    final def log
    final def host
    final def settings
    final def jsch
    private def usingSSHPrivateKey
    private def password
    private def username
    final def sessionConfig
    final def mb = 1024*1024

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

    SSH(log, settings, host, username = null, privateKey = null, passphrase = null, password = null, sessionConfig = null) {
        this.log = log
        this.host = host
        this.settings = settings
        this.jsch = new JSch();
        this.username = username
        this.password = password
        this.sessionConfig = sessionConfig
        if (this.sessionConfig == null) {
            this.sessionConfig = new java.util.Properties();
            this.sessionConfig.put("StrictHostKeyChecking", "no");
        }

        def serverSettings
        if (username == null || (privateKey == null && password == null)) {
            serverSettings = settings.properties['servers'].find {server -> server.id == host}
            if (serverSettings == null) {
                throw new AssertionError("""
Unable to find server settings for $host in the maven settings.xml (typically in ~/.m2/settings.xml) 
See: http://maven.apache.org/settings.html for details about settings.

Another option is to provide the username and either path privateKey or a password
""")

            }
            if (serverSettings.privateKey != null) {
                privateKey = serverSettings.privateKey
                passphrase = serverSettings.passphrase
            }
            if (this.password == null) this.password = serverSettings.password
            if (this.username == null) this.username = serverSettings.username
        }

        if (privateKey != null) {
            usingSSHPrivateKey = true
            if (passphrase == null) {
                char[] passwd;
                Console cons;
                if ((cons = System.console()) != null && (passwd = cons.readPassword("Enter passphrase for keystore %s: ", privateKey)) != null) {
                    passphrase = new String(passwd)
                }
            }
            if (passphrase == null) {
                jsch.addIdentity(privateKey)
            } else {
                jsch.addIdentity(privateKey, passphrase)
            }
        }

    }
    /**
     * @param src either a string or file of the file (not directory) to copy
     * @param dest either a string or file of the file (not directory) to copy to
     */
    def scp(src, dest) {
        final File srcFile
        if (src instanceof File) {
            srcFile = src
        } else if (src instanceof Artifact) {
            srcFile = src.file
        } else {
            srcFile = new File(src.toString())
        }
        log.info("scp $srcFile $username@$host:$dest")
        def written = streamCopy(srcFile.newInputStream(),dest, srcFile.length(), srcFile.lastModified(), true)
        if(written < mb) {
            log.info("scp complete: ${written} bytes copied")
        } else {
            log.info("scp complete: ${Math.round(written / mb * 10.0) / 10.0}MB copied")
        }
    }

    /**
     * Copy a file from the classpath to the remote server.  Note the file should be small enough to fit into memory
     *
     * @param resource the path of the file to load from the provided class loader
     * @param dest the destination path
     * @param classLoader the classloader to use to get the resource.  By default it is the SSH classloader
     */
    def scpResource(resource,dest,substitutions =[:], classLoader = SSH.class.classLoader) {
        def stream = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(resource)))
        def all = new StringBuilder()
        def next = stream.readLine()

        while(next != null) {
            substitutions.each{ key,value ->
                next = next.replaceAll(key,value)
            }
            all.append(next).append("\n")
            next = stream.readLine()
        }

        def a = all.toString().getBytes("UTF-8")
        log.info("scp $resource $username@$host:$dest")
        streamCopy(new ByteArrayInputStream(a),dest,a.length, System.currentTimeMillis(), true)
        if(a.length < mb) {
            log.info("scp complete: ${a.length} bytes copied")
        } else {
            log.info("scp complete: ${Math.round(a.length / (mb * 10.0)) / 10.0}MB copied")
        }
    }

    /**
     * Copy a stream to a file on the remote server
     *
     * @param src an input stream to copy to dest file
     * @param dest a string or file (cannot be a directory it must be the file)
     * @param lastModified the lastModified date to sent.  Default is current time
     * @param length length of stream, this is required so that the the remote file can be correctly created
     * @param hideStartStopLog If the start and end messages should not be logged (in case of scp it already makes the logs)
     */
    def streamCopy(src, dest, length ,lastModified = System.currentTimeMillis(),  hideStartStopLog=false) {

        if(src == null){
            throw new IllegalArgumentException("src is not permitted to be null")
        }

        if(dest instanceof File) {
            dest = dest.path
        } else if(dest instanceof Artifact) {
            dest = dest.file.path
        }

        if(!hideStartStopLog) {
            log.info("streamCopy to $username@$host:$dest")
        }

        boolean ptimestamp = false;
        def written = 0


        def channelFac = {session ->
            // exec 'scp -t dest' remotely
            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + dest;
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            return channel
        }

        withSession channelFac, {session, channel ->

            // get I/O streams for remote scp
            def out = channel.getOutputStream()
            def input = channel.getInputStream()

            if (checkAck(input) != 0) {
                throw new AssertionError("An host did not respond with an ACK after intializing transfer")
            }
            String command
            if (ptimestamp) {
                command = "T " + (lastModified / 1000) + " 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (lastModified / 1000) + " 0\n");
                out.write(command.getBytes()); out.flush();
                if (checkAck(input) != 0) {
                    throw new AssertionError("An host did not respond with an ACK after setting timestamp")
                }
            }

            // send "C0644 filesize filename", where filename should not include '/'
            if(length > -1) {
                long filesize = length;
                command = "C0644 " + filesize + " ";
                if (dest.lastIndexOf('/') > 0) {
                    command += dest.substring(dest.lastIndexOf('/') + 1);
                }
                else {
                    command += dest.path;
                }
                command += "\n";
                out.write(command.getBytes()); out.flush();
                if (checkAck(input) != 0) {
                    throw new AssertionError("An host did not respond with an ACK after setting file size")
                }
            }

            // send a content of srcFile
            byte[] buf = new byte[1024];

            try {

                def total = length;
                def start = System.currentTimeMillis()
                def last = start

                while (true) {
                    int len = src.read(buf, 0, buf.length);
                    if (len <= 0) break;
                    out.write(buf, 0, len);
                    written += len
                    if ((System.currentTimeMillis() - last) > 5000) {
                        last = System.currentTimeMillis()
                        def percentageWritten
                        if(length < 0) {
                            percentageWritten = "?"
                        } else {
                            percentageWritten = Math.round(written / total * 1000) / 10
                        }
                        log.info("${percentageWritten}% written: " +
                                "${Math.round(written / mb * 10) / 10}/${total < 0 ? "?" : Math.round(total / mb * 10 ) / 10}MB " +
                                "time: ${Math.round((last - start) / 100) / 10}s")
                    }
                }

            } finally {
                src.close()
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1); out.flush();
            if (checkAck(input) != 0) {
                println("An error occurred when trying to copy to " + host + ":" + dest)
                throw new AssertionError("An error occurred when trying to copy to " + host + ":" + dest)
            }
            out.close();

        }

        if(!hideStartStopLog) {
            log.info("streamCopy complete: ${Math.round(written / mb * 10) / 10}MB copied")
        }

        return written
    }

    private int checkAck(input) throws IOException {
        int b = input.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c = input.read();
            sb.append((char) c);
            while (c != -1) {
                c = input.read();
                sb.append((char) c);
            }

            if (b == 1) { // error
                log.error(sb.toString());
            }
            if (b == 2) { // fatal error
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
     * @return Map ( code : < Response code > , sysout : < system out data > )
     */
    def exec(command, validation = acceptAll) {
        log.info("executing " + command)

        def channelFac = {session ->
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            channel.setInputStream(null);

            //channel.setOutputStream(System.out);
            ((ChannelExec) channel).setErrStream(System.err);

            return channel
        }
        withSession channelFac, {session, channel ->
            InputStream input = channel.getInputStream();

            def response = ""
            def responseCode = 0

            byte[] tmp = new byte[1024];
            while (true) {
                while (input.available() > 0) {
                    int i = input.read(tmp, 0, 1024);
                    if (i < 0) break;
                    response = new String(tmp, 0, i)
                    log.debug("ssh exec response:\n$response\n");
                }
                if (channel.isClosed()) {
                    log.debug("exit-status: " + channel.getExitStatus());
                    def returnVal = [code: channel.getExitStatus(), response: response]
                    if (!validation(returnVal)) {
                        throw new AssertionError("ssh exec command: '$command' completed but with an unacceptable result: $returnVal")
                    }
                    return returnVal
                }
                try {Thread.sleep(500);} catch (Exception ee) {}
            }
        }
    }

    def withSession(channelFactory, closure = acceptAll) {
        Session session = jsch.getSession(username, host, 22);
        session.setConfig(sessionConfig)
        if (!usingSSHPrivateKey) {
            if (this.password == null) {
                char[] passwd;
                Console cons;
                if ((cons = System.console()) != null && (passwd = cons.readPassword("Enter password for user %s: ", username)) != null) {
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
            return closure(session, channel)
        } finally {
            if (channel != null) {
                channel.disconnect()
            }
            session.disconnect()
        }
    }
    
    def changeHost(newHost) {
      return new SSH(
        log:log,
        settings:settings,
        host:newHost,
        sessionConfig:sessionConfig
      )
      
    }
}
