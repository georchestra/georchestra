class Parameters {
  static Parameters get
  def targetDir
	def outputDir
	def basedirFile
	def target
	def subTarget
	def projectDir
	def buildSupportDir
	def log
	def ant
	
	def init() {
	  get = this
		targetDir.deleteDir()
		targetDir.mkdirs()
	  outputDir.deleteDir()
		outputDir.mkdirs()
		new File(outputDir, "shared.maven.filters").write("#place holder")
	}
	
	String toString() {
	  return "Params[targetDir=${targetDir}, outputDir=${outputDir}, basedirFile=${basedirFile}, target=${target}, \n\tsubTarget=${subTarget}, projectDir=${projectDir}, buildSupportDir=${buildSupportDir}]"
	}
}