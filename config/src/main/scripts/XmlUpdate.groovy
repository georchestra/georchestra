/**
 * Helper class for updating xml files.  Example usage:
 * 
 * <pre><code>
 *  new XmlUpdate(
 *  &nbsp;&nbsp;path: 'file.xml',
 *  &nbsp;&nbsp;from: 'defaults/security-proxy', 
 *  &nbsp;&nbsp;to: 'security-proxy').update { xml ->
 *  &nbsp;&nbsp;&nbsp;&nbsp;xml.category.findAll {it.@class.contains("gn")}. each {cat ->
 *  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;cat.@class = s.@class + " geor" // add new class to element
 *  &nbsp;&nbsp;&nbsp;&nbsp;}
 *  &nbsp;&nbsp;}
 *</code></pre>
 * <strong>Note:</strong> source file is not modified.  It is read and the updated file
 * is written to the directory containing generated files
 * <p>
 * The method write passes a MarkupBuilder to the closure:
 * <p>
  * <ul><li><a href="http://groovy.codehaus.org/Creating+XML+using+Groovy%27s+MarkupBuilder">http://groovy.codehaus.org/Creating+XML+using+Groovy%27s+MarkupBuilder</a></li></ul>
 * </p></p><p>
 * The method update passes the xml from XmlParser to the closure:
 * <p>
 * <ul><li><a href="http://groovy.codehaus.org/Reading+XML+using+Groovy%27s+XmlParser">http://groovy.codehaus.org/Reading+XML+using+Groovy%27s+XmlParser</a></li></ul>
 * </p></p>
 */
class XmlUpdate extends AbstractUpdater {
  /**
   * Create a new xml file with the xml created by the closure
   *
   * @param closure a closure that takes a <a href="http://groovy.codehaus.org/Creating+XML+using+Groovy%27s+MarkupBuilder">MarkupBuilder</a> and uses the builder to create XML
   */
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
  /**
   * Load an existing xml file update the xml in-memory and output updated file to generated directory
   *
   * @param closure a closure that takes an <a href="http://groovy.codehaus.org/Reading+XML+using+Groovy%27s+XmlParser">XmlParser</a> and updates the xml using
   the parser.  (See link for examples)
   */
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
