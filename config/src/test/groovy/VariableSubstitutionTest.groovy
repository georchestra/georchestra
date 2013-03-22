/**
 * This is ran after the configuration files have been processed and are in the output directory.  It
 * verifies that all variables have been correctly substituted
 */
public class VariableSubstitutionTest {
	def ignorables = [".png", ".gif", ".ico", ".bmp", ".jpg", ".odg", ".pdf", ".swf", ".doc", ".jar", ".class"]
    def illegalVariableChars = """!@#{}\$%^&*()+=/|\\"';:,"""

	def execute(def params) {
		def failures = []
		params.outputDir.eachFileRecurse{ file ->
			def extIndex = file.name.lastIndexOf(".")
			def ignorable = file.name.equals("favicon")
			if(extIndex > -1) {
				def ext = file.name.substring(extIndex).toLowerCase()
				ignorable = ignorables.contains(ext)
			}

			if(file.isFile() && !ignorable) {
			  params.log.debug("Testing "+file+" for unsubstituted substitutions")
				def m = file.getText("UTF-8") =~ /@\S*@/
				if(m.find()) {
				  m.each { failure ->
				    if(!failure.endsWith('\\@') && !failure.findAll{!illegalVariableChars.contains(it)}) {
					    failures << "${m[0]} was found as an unmatched variable in ${file.path.substring(outputDir.path.length())}.  Define the variable in a shared.maven.filters"
				    }
				  }
				}
			}
		}
		if(!failures.isEmpty()) {
			throw new AssertionError(failures.join("\n"))
		}
	}
}