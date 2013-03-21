/**
 *
 * Updates a text file by replacing all sections of the file that match the patterns
 * and outputs it to the configuration<br/>
 * Example Usage:
 *
 * <pre><code>
 * new TextUpdate(
 * &nbsp;&nbsp;path:  'apps/georchestra/js/Settings.js',
 * &nbsp;&nbsp;fromProject: "geonetwork",
 * &nbsp;&nbsp;from: 'web-client/src/main/resources/',
 * &nbsp;&nbsp;to: 'geonetwork-client/',
 * &nbsp;&nbsp;patternsToReplace: [ /GeoNetwork\.Util\.defaultLocale\s*=\s*'eng'/: "GeoNetwork.Util.defaultLocale = 'fre'"]
 * ).update()
 * </code></pre>
	* for a description of path, fromProject, from and to see AbstractUpdater
	* 
	* <strong>Note:</strong> unlike many other updaters a from file is required
  * <strong>Note:</strong> source file is not modified.  It is read and the updated file
  * is written to the directory containing generated files
  */
class TextUpdate extends AbstractUpdater {
  /** The text replacement definitions */
  def patternsToReplace = [:]
  /**
   * Load file update text (in-memory) and write results to the generated files directory
   */
  def update() {
    def fromFile = getFromFile()
    def toFile = getToFile()
    assert fromFile.exists(), "$fromFile does not exist"

    params.log.info ("updating text file.\n\tfrom: $fromFile\n\tto:$toFile")

    def text = fromFile.text
    patternsToReplace.each {entry ->
      assert text =~ entry.key, "Unable to find any matches for ${entry.key}"
      params.log.info("Performing following replacement: $entry")
      text = text.replaceAll(entry.key, entry.value)
    }

    toFile << text.getBytes("UTF-8")
  }
}
