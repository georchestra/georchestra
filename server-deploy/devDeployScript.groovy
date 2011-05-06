
def ssh = new SSH(log:log,settings:settings,host:"drebretagne-geobretagne.int.lsn.camptocamp.com")
def aliasFunction = Artifacts.versionNumToPrivateMapping {artifact ->
    if (artifact.name.startsWith ("cas-server-webapp")) return "cas.war"
    else if (artifact.name.startsWith ("security-proxy")) return "ROOT.war"
    else return null
}

def artifacts = new Artifacts(project,aliasFunction)

/*
def deployer = new SSHWarDeployer(
  log:log,
  ssh:ssh,
  projectProperties:properties,
  webappDir:"/srv/tomcat/tomcat1/webapps/",
  startServerCommand:"sudo /etc/init.d/tomcat-tomcat1 start",
  stopServerCommand:"sudo /etc/init.d/tomcat-tomcat1 stop"
  )
  
deployer.deploy(artifacts.simpleNameMap['security-proxy-1.0'])
*/