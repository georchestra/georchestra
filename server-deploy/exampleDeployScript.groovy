/*
  Reads all war dependencies of of server-deploy module to determine which webapps need to be deployed
  the second param is the Alias function.  The war files that are supplied to the script have the full 
  module name and version as defined in their pom.  For example extractorapp-1.0.war is the 
  actual file, the aliasFunction will map that to extractorapp.war which is what the deployed
  system expects.  The artifacts can be mapped to any desired name.  
  
  The Artifacts object uses the maven dependencies to look up the artifacts that need to be deployed
  and depending on the maven profile those dependencies can be controlled.  For example the extractorapp profile
  only has a dependency on extractorapp so artifacts will only contain that one artifact.
  
  This class is only for assisting deployment.  It is not required to be used but it is recommended
  so that deploy scripts are all similar
*/
def artifacts = new Artifacts(project, Artifacts.standardGeorchestraAliasFunction)

/*
  Create a SSH option that operates on server: server1.
  The authentication information is read from home/deploy_user/.m2/settings.xml
  Passwords do not have to be in this file.  
  
  If the passwords/passphrases are the deploy will require no interaction with a user.  
  
  If they are not a user will have to enter the password.  
  
  The options are privateKey/passphrase or username/password
  
For the record, here is a typical settings.xml file:

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
	<servers>
		<server>
			<id>server1</id>
			<username>deploy</username>
			<privateKey>/home/deploy/.ssh/id_rsa</privateKey>
		</server>
		<server>
			<id>server2</id>
			<username>deploy</username>
			<privateKey>/home/deploy/.ssh/id_rsa</privateKey>
		</server>
	</servers>
</settings>
  
*/
def ssh = new SSH(log:log, settings:settings, host:"server1")

// create an object for deploying wars to a unix-based machine using SSH
def server1Deployer = new SSHWarDeployer(
    log: log,
    ssh: ssh,
    projectProperties: project.properties,
    webappDir: "/srv/tomcat/webapps",
    startServerCommand: "sudo /etc/init.d/tomcat start",
    stopServerCommand: "sudo /etc/init.d/tomcat stop"
)

// deploy all artifacts except the geoserver artifact using the server1Deployer 
server1Deployer.deploy(artifacts.findAll{!it.name.contains("geoserver")})

// Find the geoserver artifact (will return null if there is not geoserver artifact for the current deploy profile)
def geoserverArtifact = artifacts.find{it.name.contains("geoserver")}
if (geoserverArtifact != null) {
  // get a copy of the previous SSH object that redirects to a new server.  
  // username and passwords are not copied as they are read once again from the maven 
  // settings.xml file
  def geoserverSSH = ssh.changeHost("server2")
  // create a deployer based on the tomcat1Deployer but use the new ssh object
  // (since this will deploy to a different server)
  def geoserverDeployer = server1Deployer.copy(ssh: geoserverSSH)
  // finally deploy
  geoserverDeployer.deploy(geoserverArtifact)
}
