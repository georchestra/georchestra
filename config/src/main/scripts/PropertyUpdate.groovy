/**
 * Helper class for updating property files.  Example usage:
 * 
 * new PropertyUpdate(
      path: 'maven.filter',
		  from: 'defaults/security-proxy', 
		  to: 'security-proxy').update { properties ->
		    properties['shared.server.name'] = host
		    properties['shared.default.log.level'] = logLevel
		    properties['application_home'] = applicationHome
		    properties['shared.ldapUrl'] = ldapUrl
	  }
 * 
 */
class PropertyUpdate extends AbstractUpdater {
	def update(closure) {
		def properties = new Properties()
		def fromFile = getFromFile()
		if(fromFile != null) {
		  params.log.info("Loading parameters to update from $fromFile")
		  fromFile.withReader { r -> 
			  properties.load(r)
		  }
	  }
		
		closure(properties)

		def toFile = getToFile()
		params.log.info("Writing updated parameters to $toFile")
		params.log.debug("writing "+properties+" to "+toFile)

	  toFile.withWriter('UTF-8'){ w -> properties.store(w,"updated by pigma's GenerateConfig class")}
	}
}
