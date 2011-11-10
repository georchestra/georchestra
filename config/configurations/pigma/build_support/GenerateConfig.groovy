/* 
 * This file can optionally generate configuration files.  The classic example
 * is when a project has both a integration and a production server.  
 * 
 * The configuration my be in a subdirectory of build_support (which is not copied into the configuration by default)
 * Depending on serverId, this script can copy the files to the outputDir and copy a shared.maven.filters with the parameters that
 * are needed depending on serverId.  More can be done but that is the classic example
 */
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
	 */
	def generate(def project, def log, def ant, def basedirFile, 
							def target, def subTarget, def targetDir, 
							def buildSupportDir, def outputDir) {
		log.debug("pigma generate script starting")
		def resources = new File(buildSupportDir, "resources")
		// copy all resource files (configuration files)
		log.debug("Running groovy configuration generation scripts")
		
		ant.copy(todir:outputDir.canonicalPath) {
			fileset(dir:resources.canonicalPath)
		}
		
		// set shared.server.name dependent on the prod or int server
		def host = "shared.server.name="
		switch (subTarget) {
			case "prod": 
				host += "ids.pigma.org"
				break
			default: 
				host += "ns383241.ovh.net"
				break
		}
		
		def sharedMavenFilters = new File(outputDir, "shared.maven.filters")
		log.debug("writing "+host+" to "+sharedMavenFilters)
		sharedMavenFilters.write(host, "UTF-8")
		
		// need to modify the targets of the security-proxy to also proxy to geowebcache
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
    def spMavenFilterFile = new File(spDir, "maven.filter")
		log.debug("writing "+spMavenFilter+" to "+spMavenFilterFile)

	  spMavenFilterFile.withWriter{ w -> spMavenFilter.store(w,"updated by pigma's GenerateConfig class")}
		log.debug("pigma generate script finished")
	}
}