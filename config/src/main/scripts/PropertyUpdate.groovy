/**
 * Helper class for updating property files.  Example usage:
 * <pre><code>
new PropertyUpdate(
&nbsp;&nbsp;path: 'maven.filter',
&nbsp;&nbsp;from: 'defaults/security-proxy', 
&nbsp;&nbsp;to: 'security-proxy').update { properties ->
&nbsp;&nbsp;&nbsp;&nbsp;properties['shared.server.name'] = host
&nbsp;&nbsp;&nbsp;&nbsp;properties['shared.default.log.level'] = logLevel
&nbsp;&nbsp;&nbsp;&nbsp;properties['application_home'] = applicationHome
&nbsp;&nbsp;&nbsp;&nbsp;properties['shared.ldapUrl'] = ldapUrl
}
</code></pre>
 * <strong>Note:</strong> source file is not modified.  It is read and the updated file
 * is written to the directory containing generated files
 */
class PropertyUpdate extends AbstractUpdater {
  /** 
   * Perform the updating.  Note: source file is not modified
   * <p>
   * Groovy has special syntax for Maps (Properties are map objects so it applies to them)
   * See <a href="http://groovy.codehaus.org/JN1035-Maps">http://groovy.codehaus.org/JN1035-Maps</a>
   * </p><p>
   * Probably the most usefule syntax for our purposes is:
   * <ul>
   * <li>properties[key] = newValue</li>
   * </ul>
   * @param closure that takes a java.util.Properties object and updates the properties.
   */
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

	  toFile.withWriter('UTF-8'){ w -> properties.store(w,"updated by config's GenerateConfig class")}
	}
}
