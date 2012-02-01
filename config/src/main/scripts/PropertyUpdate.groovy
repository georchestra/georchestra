class PropertyUpdate {
	String from, to
	def update(closure) {
	  def params = Parameters.get
		def properties = new Properties()
		new File(params.basedirFile, from).withReader { r -> 
			properties.load(r)
		}
		
		closure(properties)

		def toFile = new File(params.outputDir,to)
		toFile.parentFile.mkdirs()
		params.log.debug("writing "+properties+" to "+toFile)

	  toFile.withWriter('UTF-8'){ w -> properties.store(w,"updated by pigma's GenerateConfig class")}
	}
}
