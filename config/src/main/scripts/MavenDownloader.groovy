/**
 * Downloads a jar from one of the maven repositories to the 
 * provided location
 *<pre><code>
new MavenDownloader(
&nbsp;&nbsp;artifact: ['com.vividsolutions','jts','1.13],
&nbsp;&nbsp;to: 'geoserver-webapp/WEB-INF/lib').download()
</code></pre>
  * Example will download the jts jar and the geoserver control-flow jar to the geoserver configuration
  * <pre><code>
new MavenDownloader(
&nbsp;&nbsp;artifacts: [
&nbsp;&nbsp;&nbsp;&nbsp;['org.geoserver.extension','control-flow','2.2.4'],
&nbsp;&nbsp;&nbsp;&nbsp;['com.vividsolutions','jts','1.13]
&nbsp;&nbsp;],
&nbsp;&nbsp;to: 'geoserver-webapp/WEB-INF/lib').download()
</code></pre>
  * Only one of artifact and artifacts can be declared
  * the to parameter is always relative to the params.outputDir
  */
class MavenDownloader {
  /** The artifacts to download.  This is a collections of
   * lists(or arrays) that list the artifacts to download.
   * <p>This property is used when one wants to download many artifacts</p>
   * <p>See artifact complete details on how to define an artifact for download</p>
   * <p><strong>Note:</strong> One and only one of artifacts or artifact is required.</p>
   */
  def artifacts
  /** An artifact to download. This is a list (or array) that provides the groupId, artifactId and version
   * of the artifact to download.  In the future maps will also be permitted so that optional artifact characteristics
   * like classifier and type.
   *<p>The format for declaring an artifact is:</p>
   * <pre>{@code
[groupId,artifactId,version]
}</pre>
   * For example:
   * <pre>{@code
['org.geoserver.extension','control-flow','2.2.4']
}</pre>
   * <p><strong>Note:</strong> One and only one of artifacts or artifact is required.</p>
   */
  def artifact
  /** The directory to download the file to. Can be a string or file object */
  def to
  
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
	  if (!toFile.parentFile.exists()) {
		  if (!toFile.parentFile.mkdirs()) {
			throw new AssertionError("Unable to create directory: "+toFile.parentFile+".  Check permissions on file and parent directories")
		  }
	  }
      def errors = ""
  	  for ( repo in params.project.repositories ) {
        def url = repo.url
        if(url.endsWith("/")) {
          url += path
        } else {
          url += "/"+path
        }
  	    try {
		  if (toFile.exists()) {
			if (!toFile.delete()) {
				throw new AssertionError("Unable to delete: "+toFile+".  Check permissions on file and parent directories")
			}				
		}
		  
  	      toFile << new URL(url).openStream()
  	      params.log.info("Downloaded $url to "+toFile)
  	      success = true
  	      break;
        } catch (e) {
			errors != "\n"+repo.url +"->"+ e.message;
        }
  	  }
  	  if (!success) {
		Log.error("Unable to download artifacts: "+errors)
  	    throw new AssertionError("Failed to download artifact: ${groupId}.${artifactId}.${version}")
  	  }
    }
  }

}