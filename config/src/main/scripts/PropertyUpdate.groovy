/**
 * Helper class for updating property files.  Example usage:
 * 
 * 	  new PropertyUpdate(
		  from: 'defaults'+SEP+'security-proxy'+SEP+'maven.filter', 
		  to: 'security-proxy'+SEP+'maven.filter').update { properties ->
        println("pp\n\n"+properties)
		    properties['shared.server.name'] = host
		    properties['shared.default.log.level'] = logLevel
		    properties['application_home'] = applicationHome
		    properties['shared.ldapUrl'] = ldapUrl
	  }
	  
 * The 'from' parameter is a string and is relative to the root of the config project directory
 * The 'to' parameter is also a string but is relative to the directory that contains generated files.  These files override the
 * files in the normal configuration
 * The update method is called with a closure that will update the properties read from the 'from' file and writes the updated properties
 * to the 'to' file.  
 * 
 * Note: if the 'from' parameter is not defined then the properties will be empty when passed to the update method.
 * Note: if the 'from' parameter is defined then the file must exist or an error will be thrown
 */
class PropertyUpdate {
	String from, to
	def update(closure) {
	  def params = Parameters.get
		def properties = new Properties()
		if(from != null) {
		  def fromFile = new File(params.basedirFile, from)
		  if (!fromFile.exists()) {
		    throw new IllegalStateException("$fromFile does not exist.")
		  }
		  params.log.info("Loading parameters to update from $fromFile")
		  fromFile.withReader { r -> 
			  properties.load(r)
		  }
	  }
		
		closure(properties)

		def toFile = new File(params.outputDir,to)
		toFile.parentFile.mkdirs()
		params.log.info("Writing updated parameters to $toFile")
		params.log.debug("writing "+properties+" to "+toFile)

	  toFile.withWriter('UTF-8'){ w -> properties.store(w,"updated by pigma's GenerateConfig class")}
	}
}
