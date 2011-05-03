new Test (args) {
  var queryParams = Map(
    "bbox" -> "47680.03566835344,2267644.974909656,349781.01121069863,2444833.9703504895",
    "Format" -> "image/png",
    "request" -> "GetMap",
    "layers" -> "geob:communes_geofla",
    "width" -> "550",
    "height" -> "250",
    "srs" -> "EPSG:27572")
  "wms getmap" inService "geoserver/wms" withGet queryParams should (result => {
    result has ('responseCode -> 200)
    assert(result.image.isDefined, "image is corrupt")
  })
}
