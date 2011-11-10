/**
 * This is ran after the configuration files have been processed and are in the output directory.  It
 * verifies that all variables have been correctly substituted
 */
public class VariableSubstitutionTest {
	def ignorables = [".png", ".gif", ".ico", ".bmp", ".jpg", ".odg", ".pdf", ".swf", ".odg"]
	/**
	 * @param project The maven project.  you can get all information about the project from this object
	 * @param log a logger for logging info
	 * @param ant an AntBuilder (see groovy docs) for executing ant tasks
	 * @param basedirFile a File object that references the base directory of the conf project
	 * @param target the server property which is normally set by the build profile.  It indicates the project that is being built
	 * @param subTarget the "subTarget" that the project is being deployed to.  For example integration or production
	 * @param targetDir a File object referencing the targetDir
	 * @param buildSupportDir a File object referencing to the build_support dir of the target project
	 * @param outputDir The File object referencing to the directory containing all the processed configuraiton files
	 */
	def execute(def project, def log, def ant, def basedirFile, 
							def target, def subTarget, def targetDir, 
							def buildSupportDir, def outputDir) {
		def failures = []
		outputDir.eachFileRecurse{ file ->
			def extIndex = file.name.lastIndexOf(".")
			def ignorable = file.name.equals("favicon")
			if(extIndex > -1) {
				def ext = file.name.substring(extIndex).toLowerCase()
				ignorable = ignorables.contains(ext)
			}

			if(file.isFile() && !ignorable) {
				def m = file.getText("UTF-8") =~ /@\S*@/
				if(m.find()) {
					failures << "${m[0]} was found as an unmatched variable in ${file.path.substring(outputDir.path.length())}.  Define the variable in a shared.maven.filters"
				}
			}
		}
		if(!failures.isEmpty()) {
			throw new AssertionError(failures.join("\n"))
		}
	}
}