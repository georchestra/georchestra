.. _`georchestra.documentation.installation_en`:

==================
Installation Guide
==================

While the goal of the project is to be able to publish raw built artifacts to a central repository and have a configuration system that modifies their configuration for a particular deployment platform, that is not currently the situation.  As it stands the configurations are built along with the artifacts and as such one must do a full build for each platform you want to deploy to.  

Configuration
=============

Each of the projects require a configuration to customize that project for a particular deployment platform.  In general the configurations are stored in: <project>/src/<platform_id>.  Normally there is a maven.filters file which defines the primary configuration parameters (there are more in the src/main/webapp/WEB-INF folder as well but normally they do not need to be modified and are different for each project.)  It is recommended for each project to copy the configuraiton parameters of another deployment platform and modify it for the new platform.  For example update the host files.

After adding the configuration files for each project a '''profile''' section must be added to the root pom.xml as follows (note that platform_id needs to correspond to the identifier you chose to identify the target deploy platform.):

{{{
	<profile>
		<id>platform_id</id>
		<properties>
			<server>platform_id</server>
		</properties>
	</profile>
}}}

Once all configuration files have been updated one can build all projects.  

Building
========

From the root directory of the checkout execute maven specifying the server you want to build for and the task (typically install)

{{{
  mvn install -P<configurationkey>
}}}

When executed in the root directory all project will be build.  When the command is executed in a submodule (like extractorapp) only that module will be built.  

This will take a very long time.  After all the projects are built each webapp will have a war file published to your local maven repository with the platform_id appended as a classifier.  For example if you build {{{mvn install -Pdev}}} in the cas-server-webapp directory, the artifact {{{cas-server-webapp-1.0-dev.war}}} will be put in your local maven repository.  In my case the file is in {{{/home/username/.m2/repository/org/georchestra/cas-server-webapp/1.0/cas-server-webapp-1.0-dev.war}}}

Once all of the artifacts are built then they can be deployed with the server-deploy module.

Deploy
======

The first step that needs to be done is 

=====================
Technical Information
=====================

Deployment Mechanism
====================

The deploy mechanism consists of 2 modules:
 * server-deploy
 * server-deploy-support

The server-deploy-support module contains Java and Groovy classes (which are platform independent) to make it simple to write deploy scripts for deploying to any system of servers and web containers.  Examples of classes are:
 * SSH - provide scp and ssh commands in a platform independent manner so one can deploy to an ssh compliant server from windows or linux.
 * SSHTomcatDeployer - Allows delivering one or more war files to a tomcat with around 3 lines of code.  It includes copying the files to the remote server, updating the existing wars and restarting the tomcat if necessary.

The server-deploy module contains the actual scripts for performing the deployment.  It has profiles for performing a full deploy, and upgrade of a single module or a upgrade of all modules.  The scripts are very simple to write for example:

::
    
  def ssh = new SSH(log:log,settings:settings,host:"c2cpc83.camptocamp.com")

  def deploy = new C2CDeploy(project,ssh)
  deploy.deploy()

The above code deployed using the default C2CDeploy configuration which consists of 2 tomcat servers.  Naturally that is not applicable to all situations so the following is an example of deploying Geoserver to one server and all other applications to another server.  

::
    
	def artifacts = new Artifacts(project, Artifacts.standardGeorchestraAliasFunction)
	def ssh = new SSH(log:log,settings:settings,host:"server1")
	def server1Deployer = new SSHWarDeployer(
	        log: log,
	        ssh: ssh,
	        projectProperties: projectProperties,
	        webappDir: "/srv/tomcat/tomcat1/webapps",
	        startServerCommand: "sudo /etc/init.d/tomcat-tomcat1 start",
	        stopServerCommand: "sudo /etc/init.d/tomcat-tomcat1 stop"
	)
	server1Deployer.deploy(artifacts.findAll{!it.name.contains("geoserver")})

	def geoserverArtifact = artifacts.find{it.name.contains("geoserver")}
	if (geoserverArtifact != null) {
	  def geoserverSSH = ssh.changeHost("server2")
	  def geoserverDeployer = tomcat1Deployer.copy(ssh: geoserverSSH)
	  geoserverDeployer.deploy()
	}

The above code is trunk/server-deploy/exampleDeployScript.groovy and has many comments describing the purpose of each line.

To reiterate.  The server-deploy module provides a way to very simply write deploy scripts for deploying the entire system to one or more servers.  The design goals of this module are as follows:

 * Provide a way to very simply write deploy scripts
 * Be platform independent so the one script will work on any platform
 * Require no additional installations other than maven and java
 * Be extremely flexible so that it is easy to write script that deploy all wars to a single server even deploy the same artifact to several servers for scaling and the other artifacts to other servers.

Currently the server-deploy-support provides only rudimentary support but extra classes can be added to assist in writing deploy scripts for other types of server configurations.