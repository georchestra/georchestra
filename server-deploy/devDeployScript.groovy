
def ssh = new SSH(log:log,settings:settings,host:"drebretagne-geobretagne.int.lsn.camptocamp.com")
def artifacts = new Artifacts(project)
//ssh.scp("devDeployScript.groovy","/tmp/devDeployScript.groovy")

def deployer = new SSHWarDeployer(
  log:log,
  ssh:ssh,
  projectProperties:properties,
  webappDir:"/srv/tomcat/tomcat1/webapps/",
  startServerCommand:"sudo /etc/init.d/tomcat-tomcat1 start",
  stopServerCommand:"sudo /etc/init.d/tomcat-tomcat1 stop"
  )
  
  println(artifacts.simpleNameMap)
deployer.deploy(artifacts.simpleNameMap['security-proxy-1.0'])