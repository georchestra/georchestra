/**
 * Encapsulates parameters of build.  This class is a singleton and
 * provides a way to access all the parameters from anywhere in the scripts
 * without having to pass all parameters through the entire system.
 * <p>
 * To get the Parameter class from anywhere one can do:
 * </p>
 * <code>Parameters.get</code>
 */
class Parameters {
  /** static reference to the parameters */
  static Parameters get
  /** The config/target directory */
  File targetDir
  /** If the script is a GenerateConfig script then outputDir is the the 'generated' directory.  This is where generated files should go.
	  Otherwise it is project.build.directory/target
   */
	File outputDir
	/** The maven base directory of the config module */
	File basedirFile
	/** A string indicating the target configuration.  This is the value of the server java property and
	    the name of the configuration directory 
	*/
	String target
	/** Optional subtarget normally indicating the server that is being built for.  For example 
	    integration, test, prod, int, etc... */
	String subTarget
	/** If the script is a GenerateConfig script the directory containing the configuration files.  For example:
	    config/configuration/<target>
		
		Otherwise this will be project.build.directory/conf/project.artifactId
	*/
	File projectDir
	/** The directory: config/configuration/<target>/build_support if the script is a GenerateConfig script.  
		Otherwise it is null */
	File buildSupportDir
	/** The A SLF4J Logger instance to use for writing logs during the build process<br/>
	    Example usage: <code>log.info("building")</code><br/>
	*/
	def log
	/**
	 * A Groovy ant builder object that can be used to execute many ant tasks:
	 * <p><a href="http://groovy.codehaus.org/Using+Ant+from+Groovy">See Using Ant from Groovy</a></p>
	 */
	def ant
	/**
	 * The Maven project object.  objects like Repositories and version number and artifact name
	 * can be accessed through the project
	 */
	def project
	
	/**
	 * Do not call.  This is called by framework
	 */
	def init(def delete) {
	  get = this
	  targetDir = new File(project.build.directory)
	  if (project.artifactId == 'config') {
		outputDir = new File(project.build.directory, "generated")
		buildSupportDir = new File(project.basedir, "configurations/${target}/build_support")
		projectDir = new File(project.basedir, "configurations/${target}")
	  } else {
		outputDir = new File(project.build.directory, "classes")
		projectDir = new File(project.build.directory, "conf/"+project.artifactId)
	  }
	  basedirFile = project.basedir
	
	  if(delete) {
		log.info("cleaning target directory: ${targetDir}")
		targetDir.deleteDir()
		targetDir.mkdirs()
		log.info("cleaning output directory: ${outputDir}")
		outputDir.deleteDir()
		outputDir.mkdirs()
		new File(outputDir, "shared.maven.filters").write("#place holder")
	  }
	}
	
	String toString() {
	  return "Params[targetDir=${targetDir}, outputDir=${outputDir}, basedirFile=${basedirFile}, target=${target}, \n\tsubTarget=${subTarget}, projectDir=${projectDir}, buildSupportDir=${buildSupportDir}]"
	}
}