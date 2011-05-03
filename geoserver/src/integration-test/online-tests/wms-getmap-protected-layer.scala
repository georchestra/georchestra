new Test (args) {
  var queryParams = Map(
    "bbox" -> "-4.614646073422986,-42.851001816890005,147.2910004483,47.74552119985795",
    "Format" -> "image/png",
    "request" -> "GetMap",
    "layers" -> "geob_test:tasmania_cities",
    "width" -> "550",
    "height" -> "250",
    "srs" -> "EPSG:4326")
  "wms getmap" inService "geoserver/wms" loginWith BasicLogin("jeichar", "jeichar") doGet queryParams should (result => {
    result has ('responseCode -> 200)
    assert(result.image.isDefined, "image is corrupt")
  })
}
