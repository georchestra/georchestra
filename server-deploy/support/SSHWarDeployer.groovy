class SSHWarDeployer {
  private def config
  def log
  def ssh
  def tmpDir = "/tmp/georchestra_deploy_tmp"
  def webappDir
  def startServerCommand
  def stopServerCommand
  def projectProperties
  
  /**
   * Config parameter description:
   * tmpDir - a temporary directory to store uploaded resources to
   * webappdir - directory to copy war files to
   * startServerCommand - the command to use to restart the webserver
   * stopServerCommand - the command to use to stop the webserver
   * projectProperties - the project.properties object obtained from a gmaven script
   * ssh - optional SSH object.  If not present then the config should have the parameters required to create a SSH object
   * @config the map of parameters. 
   */
  SSHWarDeployer(config) {
    this.config=config
    ssh = config['ssh']
    if(ssh == null) {
      ssh = new SSH(config)
    }
    log = ssh.log
    log = config['log']
    set 'projectProperties',{projectProperties=it}
    set 'tmpDir',{tmpDir=it}
    set 'webappDir',{webappDir=it}
    set 'startServerCommand',{startServerCommand=it}
    set 'stopServerCommand',{stopServerCommand=it}
    
    validateNonNull('log','webappDir', 'startServerCommand', 'stopServerCommand')
  }
  
  def deploy(Object... warFiles) {
    if(warFiles == null) {
      throw new AssertionError("At least one file must be declared to be deployed")
    }
    warFiles.each {file -> 
      if(!(file instanceof File)) {
        file = new File(file.toString())
      }
      
      ssh.exec "mkdir -p $tmpDir",ssh.zeroCode
      ssh.scp file, tmpDir
    }
    
    ssh.exec stopServerCommand,ssh.zeroCode
    ssh.exec "cp $tmpDir/* $webappDir",ssh.zeroCode
    ssh.exec startServerCommand,ssh.zeroCode
    ssh.exec "rm -f $tmpDir/*"
  }
  
  private def set(param,setClosure) {
    if(config[param] != null) {
      setClosure(config[param])
      return true
    } else if(System.properties["SSHWarDeployer.$param"] != null) {
      setClosure(System.properties["SSHWarDeployer.$param"])
      return true
    } else if(projectProperties[param] != null) {
      setClosure(projectProperties[param])
      return true
    }
    return false
  }
  
  private def validateNonNull(Object... required) {
    def missing = required.findAll {param -> properties[param] == null}
    if(missing.size > 0) throw new AssertionError("Caller of SSHWarDeployer did not specify the required parameters: $missing")
  }
}