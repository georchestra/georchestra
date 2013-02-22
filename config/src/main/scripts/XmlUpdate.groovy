/**
 * Helper class for updating xml files.  Example usage:
 * 
 * new XmlUpdate(
      path: 'file.xml',
		  from: 'defaults/security-proxy', 
		  to: 'security-proxy').update { xml ->
		    xml.category.findAll {it.@class.contains("gn")}. each {cat ->
		      cat.@class = s.@class + " geor" // add new class to element
		    }
	  }
 * 
 * The method write passes a MarkupBuilder to the closure:
 *    http://groovy.codehaus.org/Creating+XML+using+Groovy%27s+MarkupBuilder
 * The method update passes the xml from XmlParser to the closure:
 *    http://groovy.codehaus.org/Reading+XML+using+Groovy%27s+XmlParser
 */
class XmlUpdate extends AbstractUpdater {
  def write(closure) {
    def xml, writer
		def fromFile = getFromFile()
		if(fromFile == null) {
		  params.log.info("Creating new file")
	    writer = new StringWriter()
	    xml = new MarkupBuilder(writer)
	  } else {
	    params.log.info("Overwriting $fromFile")
	  }
	  closure(doc)
	  
		doUpdate(writer)
  }
	def update(closure) {
		def xml, writer
		def fromFile = getFromFile()
		if(fromFile != null) {
		  params.log.info("Loading parameters to update from $fromFile")
		  xml = new XmlParser().parse(fromFile)
	  } else {
      throw new AssertionError("$fromFile does not exist.  Perhaps you want to use write to create the file")
	  }
		
		def writer = new StringWriter()
    new XmlNodePrinter(new PrintWriter(writer)).print(xml)
    writer(writer)
	}

  private def write (writer) {
    def text = writer.toString()
		def toFile = getToFile()
		params.log.info("Writing updated xml to $toFile")

	  toFile.withWriter('UTF-8'){ w -> w.write(text)}
  }
}
