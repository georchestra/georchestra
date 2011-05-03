new Test (args) {
  val queryParams = Map(
    "request" -> "GetCapabilities")

  "wms getmap" inService "geoserver/wms" withGet queryParams should (result => {
    result has ('responseCode -> 200)
    
    val found = (result.xml \\ "Layer").exists { layer =>
      val name = (layer \ "Name").text
      name == "geob_test:tasmania_cities"
    }
    assert(!found, "geob_test:tasmania_cities is in getcaps doc")
  })
}
