/**
 * Downloads a jar from one of the maven repositories to the 
 * provided location
 *
 * new MavenDownloader(
      artifact: ['com.vividsolutions','jts','1.13],
		  to: 'geoserver-webapp/WEB-INF/lib').download()
  *
  * Example will download the jts jar to the geoserver configuration
  * new MavenDownloader(
      artifacts: [
        ['org.geoserver.extension','control-flow','2.2.4'],
        ['com.vividsolutions','jts','1.13]
      ],
      to: 'geoserver-webapp/WEB-INF/lib').download()
  * 
  * Only one of artifact and artifacts can be declared
  * the to parameter is always relative to the params.outputDir
  */
class MavenDownloader{
  def artifacts, artifact, to
  
  def download () {
    if(artifacts != null && artifact != null) {
      throw new AssertionError("Only one of artifact or artifacts may be declared")
    }
    if(artifacts == null && artifact == null) {
      throw new AssertionError("One of artifact or artifacts must be declared")
    }
    if (artifact != null) {
      artifacts = [artifact]
    }

    artifacts.each {nextArtifact ->
      def groupId = nextArtifact[0]
      def artifactId = nextArtifact[1]
      def version = nextArtifact[2]
      def success = false
      def params = Parameters.get
      
      def fileName = "${artifactId}-${version}"
      if(nextArtifact.size() == 4) {
        fileName += "-${nextArtifact[3]}.jar"
      } else {
        fileName += ".jar"
      }
      def path = "${groupId.replace('.','/')}/$artifactId/$version/$fileName"
      def toFile = new File(new File(params.outputDir, to), fileName)
      toFile.parentFile.mkdirs()
      
  	  for ( repo in params.project.repositories ) {
        def url = repo.url
        if(url.endsWith("/")) {
          url += path
        } else {
          url += "/"+path
        }
  	    try {
  	      params.log.info("Attempting to download $url to "+toFile)
  	      toFile << new URL(url).openStream()
  	      success = true
  	      break;
        } catch (e) {
          params.log.info("Failed to download $url: "+e)
        }
  	  }
  	  if (!success) {
  	    throw new AssertionError("Failed to download artifact: ${groupId}.${artifactId}.${version}")
  	  }
    }
  }

}