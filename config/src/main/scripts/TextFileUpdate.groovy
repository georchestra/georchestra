/**
 *
 * Updates a text file by replacing all sections of the file that match the patterns
 * and outputs it to the configuration
 * Example Usage:
 *
 *  new TextFileUpdate(
	    path:  'apps/georchestra/js/Settings.js',
	    fromProject: "geonetwork",
	    from: 'web-client/src/main/resources/',
	    to: 'geonetwork-client/',
	    patternsToReplace: [ /GeoNetwork\.Util\.defaultLocale\s*=\s*'eng'/: "GeoNetwork.Util.defaultLocale = 'fre'"]
	    ).update()
	*
	* for a description of path, fromProject, from and to see AbstractUpdater
  */
class TextFileUpdate extends AbstractUpdater {
  def patternsToReplace = [:]
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