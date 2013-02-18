/**
 * Helper class for updating xml files.  Example usage:
 * 
 * new XmlUpdate(
      path: 'maven.filter',
		  from: 'defaults/security-proxy', 
		  to: 'security-proxy').update { properties ->
		    properties['shared.server.name'] = host
		    properties['shared.default.log.level'] = logLevel
		    properties['application_home'] = applicationHome
		    properties['shared.ldapUrl'] = ldapUrl
	  }
 * 
 * If no from file is defined a MarkupBuilder object is passed to closure:
 *    http://groovy.codehaus.org/Creating+XML+using+Groovy%27s+MarkupBuilder
 * Otherwise the xml from XmlParser is passed to closure:
 *    http://groovy.codehaus.org/Reading+XML+using+Groovy%27s+XmlParser
 */
class XmlUpdate extends AbstractUpdater {
	def update(closure) {
		def xml, writer
		def fromFile = getFromFile()
		if(fromFile != null) {
		  params.log.info("Loading parameters to update from $fromFile")
		  xml = new XmlParser().parse(fromFile)
	  } else {
	    writer = new StringWriter()
	    xml = new MarkupBuilder(writer)
	  }
		
		closure(xml)

    def text = writer.toString()
		def toFile = getToFile()
		params.log.info("Writing updated xml to $toFile")

	  toFile.withWriter('UTF-8'){ w -> w.write(text)}
	}
}
