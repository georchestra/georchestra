= Georchestra configuration =

== Introduction ==

The files for configuring Georchestra are all contained in the module conf.  When building a Georchestra module the conf module must first be built with the appropriate profile enabled.  If the full project is being built the conf module will be automatically built before all other modules.  

Consider the scenario where only mapfishapp is built:

{{{
cd conf
mvn install -P<server>

cd ../mapfishapp
mvn install -P<server>

cd ../server-deploy
mvn -P<server>,mapfishapp
}}}

When conf is built it is build for the declared server the configuration files are packaged into a war and installed into the local maven repository (with the classifier that equals the serverId).  Then when mapfishapp is build the conf module is obtained from the local repository and expanded into mapfishapp/target/conf.  Mapfishapp then uses the configuration files in mapfishapp/target/conf/mapfishapp for configuration.  If there are files other than configuration files in the configuration, they will override the ones used by the mapfishapp normally.  This allows a module to contain all the defaults and overrides for a specific project can be in the configuration.

The last step deploys mapfishapp to the server.  The configuration contains a file DeployScript.groovy which contains the script for deploying to that server.

== Configuration module structure ==

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
			- DeployScript.groovy
			- <optionally module overrides>
	- defaults
		- build_support
		- <each module's configuration>
	- src

There are three different methods for customizing a configuration.  The simplest, and the one desired most of the time is to override the properties in shared.maven.filters.  The next is by including files that override those in the defaults configuration or in the actual module.  The last (and most flexible) method is to define a DeployScript.groovy that somehow generates some or all of the configuration files.  

== Config build process ==

When the config module builds it goes through several steps:

 1. Execute GenerateConfig.groovy for the target server/project.  The GenerateConfig.groovy script can generate configuration files that will be given the highest priority overall other files in the system. This is very handy when you have integration and production servers and only a few properties should be changed.  (See the GenerateConfig section for more information.)
 1. 

== Future work ==
 * It is currenty difficult to override individual properties in the maven.filter files because only the entire file can be overridden.  The current solution is to write a GenerateConfig.groovy script that does the following:
{{{
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
}}}
  A better solution is to add a maven.filter.overrides file that is empty by default, but each module will use to source properties from, with higher priority than the other maven.filter file.  This is easy.  In each pom.xml a new filter needs to be defined BEFORE the maven.filter filter.  And an empty file is added to each module (or the build script can generate an empty file)