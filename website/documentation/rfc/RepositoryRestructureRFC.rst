.. _`georchestra.documentation.rfc.rfc1`:

===========================================
RFC 1: SVN Repository Restructure Proposal
===========================================

The SVN Repository Restructure Proposal is intended to fix several issues:

 1. The inability to easily tag and branch a project that works together
 2. The inability to easily build the entire project with a single command
 3. Remove platform dependent shell scripts
 4. Replace the cockpit module with a more extensible and platform independent option
 5. Control configuration by using maven profiles. 

Sandbox
========

The following changes have already been made in the sandbox.  So the changes can be reviewed in sandbox/trunk

Restructure design
===================

Currently the structure of the repository is essentially as follows:

::
    
    svnroot/application/trunk
                       /branches
                       /tags
	 /anotherApp/trunk
                    /branches
                    /tags

There are two main problems with this structure:

 1. The applications have interdependencies and as such should be tagged and branches as one so that it is easy to track the versions that work together correctly
 2. Maven has a very hard time with this structure and a very large amount of work would be required to make a build system to handle this structure (for example suppose you want to build all applications from a specific tag)

The recommended restructuring is as follows:

::
    
    svnroot/trunk/application
                 /anotherApp
	   /branches/application
	            /anotherApp
	   /tags/application
		/anotherApp

With this structure it is a simple command to tag or branch and maven can be easily configured to build a branch tag or trunk.

Remove Shell scripts
=====================

There are three major sets of shell scripts in georchestra:

 1. Javascript minification (this proposal will not address this issue)
 2. Cockpit
 3. geonetwork patching

Geonetwork Patching
---------------------

One of the goals in Georchestra is to be able to track and stay close to geonetwork latest releases.  We have made 2 attempts until now.  

 * Maintain patches of all changes.  
   This turned out to be a major problem because applying patches does not take into account history, in other words all patches are essentially applied as one major change, so when geonetwork changed it ended up being a nightmare trying to change the patches so they would work with the new version.
 * Because the patching strategy was not maintainable we made a Git mirror of Geonetwork and made one branch which was GeOrchestra. That branch had all the GeOrchestra changes and tracked the latest stable Geonetwork branch (currently 2.6.x).  From that branch we would create a single patch that would be applied to a geonetwork svn checkout. 

In both cases most of the work for checking out, applying patches and building GN were shell scripts.

This proposal will modify the process by making the GN checkout a simple svn externals property.  GitHub has both a git API and a SVN API. So development on Geonetwork can be done either using GIT or SVN. GIT is required to merge in changes from geonetwork trunk or stable branch but changes only affecting Georchestra can be done using either GIT or SVN.  

With regards to the maven process, now Geonetwork is a simple SVN:Externals link instead of patches and several shell scripts. After the restructuring and proposed changes it will be a normal maven module with no special handling.

The justification for not keeping the Geonetwork code in the Georchestra SVN is that having Geonetwork in a GIT repository makes it much easier to follow the current development branches and trunk of the "core" Geonetwork developers.

Cockpit
--------

The cockpit is a deployment mechanism that handles compiling all the projects in the correct order and publishing them to the target servers.

The main problems with the cockpit is as follows:
 * It is a set of shell scripts and therefore platform dependent
 * It is very hard to add new deployment options.

In the new proposal the cockpit is not needed.  For building the project a simple mvn command is required to be ran from the ROOT of the branch/trunk/tag
mvn install -Pconfiguration

However for deployment I have added 2 new modules:
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

The above code is sandbox/trunk/server-deploy/exampleDeployScript.groovy and has many comments describing the purpose of each line.

To reiterate.  The server-deploy module provides a way to very simply write deploy scripts for deploying the entire system to one or more servers.  The design goals of this module are as follows:

 * Provide a way to very simply write deploy scripts
 * Be platform independent so the one script will work on any platform
 * Require no additional installations other than maven and java
 * Be extremely flexible so that it is easy to write script that deploy all wars to a single server even deploy the same artifact to several servers for scaling and the other artifacts to other servers.

Currently the server-deploy-support provides only rudimentary support but extra classes can be added to assist in writing deploy scripts for other types of server configurations.

Javascript Minification
------------------------

This issue is not addressed by this RFC but a future one can be created to no longer use shell scripts and a pre-installed Python. Perhaps Jython can be used to run the minification script. I have heard of a minification process for maven that has been created by opengeo as well that perhaps can be used. YUI compressor is another option but the include annotations used in the current build is not supported by that.

Maven Profiles for configuration control
==========================================

Currently when a build is initiated the parameter -Dserver=<key> is required to be added.  <key> essentially refers to a set of configurations to be used for configuring each webapp.  The idea is to use profiles so that hudson and other tools can easier discover the configuration options.  In the sandbox the root pom has several profiles configured which essentially set the server param.  This leaves all projects alone (reducing amount of work required to satisfy this change) but still allows profile activations to control which configuration is enabled.  In the sandbox one can build all projects using the command:

mvn install -Pdev

The -Pdev indicates that the dev configuration will be used by each project for configuration.  

Since profiles are inherited a submodule can also be built by executing the same command but in a subproject.

Future work
============

Patching wars on deploy
------------------------

The problem with the current system is that each time one wants to deploy to a new server all projects must be rebuilt even though only configuration files have changed.  I would like to not perform configuration until deploy.  My plan is to copy all configuration files that must be modified during build to their own maven artifact (problably a jar but perhaps just a normal zip file).  

The configuration files in the config artifact would have the substitution parameters within them (${variable}) so they can be configured by performing an update as we do now with the maven.filters file.  Indeed the maven.filters (or equivalent file) file can be shared between several project reducing duplication.  However a shared configuration does not have to be used.  A single artifact can be configured by deploy script several times with several different configurations if desired.  

This strategy has the following benefits:

 * Much quicker since a single build is required for different deploy configurations
 * Configuration can be better shared
 * The deploy script can more easily deploy the same artifact to different servers since the project does not need to be rebuilt to reconfigure the project.

