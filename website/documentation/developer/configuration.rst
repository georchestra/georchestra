=========================
Georchestra configuration
==========================

Introduction
=============

The files for configuring Georchestra are all contained in the module conf.  When building a Georchestra module the conf module must first be built with the appropriate profile enabled.  If the full project is being built the conf module will be automatically built before all other modules.  

Consider the scenario where only mapfishapp is built:

::
	
	cd conf
	mvn install -P<server>
	
	cd ../mapfishapp
	mvn install -P<server>
	
	cd ../server-deploy
	mvn -P<server>,mapfishapp

When conf is built it is build for the declared server the configuration files are packaged into a war and installed into the local maven repository (with the classifier that equals the serverId).  Then when mapfishapp is build the conf module is obtained from the local repository and expanded into mapfishapp/target/conf.  Mapfishapp then uses the configuration files in mapfishapp/target/conf/mapfishapp for configuration.  If there are files other than configuration files in the configuration, they will override the ones used by the mapfishapp normally.  This allows a module to contain all the defaults and overrides for a specific project can be in the configuration.

The last step deploys mapfishapp to the server.  The configuration contains a file DeployScript.groovy which contains the script for deploying to that server.

Configuration module structure
================================

There were two important design goals for the configuration section

 1. Keep all configuration together in a single location so that new configurations can easily be created.
 1. Keep the amount of duplication and copy/paste to a minimum.

The first goal was satisfied automatically simply by having the configuration module.  The second is a much trickier issue because one project can be very different from another, including different CSS, icons, proxies, etc...  In addition a project can have several target servers. (integration, production, dev, etc...).  Thus the following structure/build was devised:

- conf
	- shared.maven.filters
	- configurations
		- <server/project>
			- build_support
				- GenerateConfig.groovy
				- shared.maven.filters
			- [optional DeployScript.groovy]
			- <optionally module overrides>
	- defaults
		- build_support
		- <each module's configuration>
	- src

There are three different methods for customizing a configuration.  The simplest, and the one desired most of the time is to override the properties in shared.maven.filters.  The next is by including files that override those in the defaults configuration or in the actual module.  The last (and most flexible) method is to define a DeployScript.groovy that somehow generates some or all of the configuration files.  

Config build process
======================

When the config module builds it goes through several steps:

 1. Execute GenerateConfig.groovy for the target server/project.  The GenerateConfig.groovy script can generate configuration files that will be given the highest priority overall other files in the system. This is very handy when you have integration and production servers and only a few properties should be changed.  (See the GenerateConfig section for more information.)
 1. Copy files from defaults to conf/target/classes (replacing all @tag@ tags in the each file with the property in conf/target/generated/shared.maven.filters, conf/configurations/<server>/shared.maven.filters or conf/shared.maven.filters).  Files in build_support are not copied
 1. Copy files from conf/configurations/<server> to conf/target/classes (replacing tags in the same way as for defaults).  Files in build_support are not copied
 1. Copy files from conf/target/generated/ to conf/target/classes (replacing tags in the same way as for defaults).
 1. Run a script to look for any @tag@ tags in any of the files in conf/target/classes to make sure that the configuration has all the substitutions replaced correctly.  It is easy to forget to add a configuration parameter or for someone to add a new one and not update all existing configurations.  
 
Once conf is built then the module is built.  This is also done in several steps:

 1. Expand the conf jar into <module>/target/conf.
 1. normal build steps
 1. When building the war, resoures in <module>/src/main/filtered-resources/ are copied to war but all ${tag} tags are replaced with the properties in <module>/target/conf/<module>/maven.filter

shared.maven.filters
======================

When copying files from conf/defaults or conf/configurations/<server/project> or conf/target/generated each non-binary file is processed and each tag of the form @tagname@ is replaced with a property in either 

 * conf/target/generated/shared.maven.filters
 * conf/configurations/<server>/shared.maven.filters
 * conf/shared.maven.filters.  

The order I have listed is very important.  The order is the order in which the properties are searched for.  If a property is found in conf/target/generated/shared.maven.filters the other files will not be checked.  This allows properties generated by GenerateConfig.groovy to override all other settings.

Overriding files 
================== 

In the same way that shared.maven.filters are have a priority as do files.  Although slightly differently.  When a war is being built the files in the module are first copied into the war then files from config are copied into the war (overwriting the module files if they conflict.)

With regards to the configuration, when the config war is built the files from defaults are copied, then the files from conf/configurations/<server/project>/ and finally the files from conf/target/generated. Again files in one of the later sources will overwrite the previously added files.

Generate Config
================

Generate Config is likely only rarely used but it can be useful when a special situation occurs or when dealing with a project that has several target servers with virtually identical configurations.  As a way of explanation, the following is an example of such a case.

Lets call the project, project MTS and it has a integration server and a production server.  One can use the directory structure:
- conf
	-configurations
		- MTS
			- build_support
				- GenerateConfig.groovy
				- int.DeployScript.groovy
				- prod.DeployScript.groovy
				- resources
					- <common resources and overrides to defaults>
				- shared.maven.filters

The GenerateConfig.groovy can be as follows:

::
	
  class GenerateConfig {
	def SEP = File.separator
	/**
	 * @param project The maven project.  you can get all information about the project from this object
	 * @param log a logger for logging info
	 * @param ant an AntBuilder (see groovy docs) for executing ant tasks
	 * @param basedirFile a File object that references the base directory of the conf project
	 * @param target the server property which is normally set by the build profile.  It indicates the project that is being built
	 * @param subTarget the "subTarget" that the project is being deployed to.  For example integration or production
	 * @param targetDir a File object referencing the targetDir
	 * @param buildSupportDir a File object referencing the build_support dir of the target project
	 * @param outputDir the directory to copy the generated configuration files to
	**/
	def generate(def project, def log, def ant, def basedirFile, 
							def target, def subTarget, def targetDir, 
							def buildSupportDir, def outputDir) {
		def resources = new File(buildSupportDir, "resources")

		// copy all resource files (configuration files)
		ant.copy(todir:outputDir.canonicalPath) {
			fileset(dir:resources.canonicalPath)
		}
		
		// copy correct deploy script for subTarget
		def filters = new File(buildSupportDir, "${subTarget}.DeployScript.groovy").getText("UTF-8")
		new File(outputDir, "DeployScript.groovy").write(filters, "UTF-8")

		// copy correct shared.maven.filters for subTarget
		def host = "shared.server.name="
		switch (subTarget) {
			case "int": 
				host += "georchestra-int.net"
				break
			default: 
				host += "georchestra-prod.net"
				break
		}
		
		new File(outputDir, "shared.maven.filters").write(host, "UTF-8")
	}
  }

Future work
============

 * It is currenty difficult to override individual properties in the maven.filter files because only the entire file can be overridden.  The current solution is to write a GenerateConfig.groovy script that does the following:
::
	
	def spMavenFilter = new Properties()
	new File(basedirFile,'defaults'+SEP+'security-proxy'+SEP+"maven.filter").withReader { r -> 
		spMavenFilter.load(r)
	}
	
	spMavenFilter.put('proxy.mapping',"""
		<entry key="extractorapp" value="http://localhost:8081/extractorapp-private/" />
		<entry key="mapfishapp" value="http://localhost:8081/mapfishapp-private/" />
		<entry key="geonetwork" value="http://localhost:8081/geonetwork-private/" />
		<entry key="catalogapp" value="http://localhost:8081/catalogapp-private/" />
		<entry key="geoserver" value="http://localhost:8181/geoserver/" />
		<entry key="geowebcache" value="http://localhost:8081/geowebcache-private/" />""".replaceAll("\n|\t",""))
	
	def spDir = new File(outputDir,'security-proxy')
	spDir.mkdirs()
    new File(spDir, "maven.filter").withWriter{ w -> spMavenFilter.store(w,"updated by pigma's GenerateConfig class")}

  A better solution is to add a maven.filter.overrides file that is empty by default, but each module will use to source properties from, with higher priority than the other maven.filter file.  This is easy.  In each pom.xml a new filter needs to be defined BEFORE the maven.filter filter.  And an empty file is added to each module (or the build script can generate an empty file)
