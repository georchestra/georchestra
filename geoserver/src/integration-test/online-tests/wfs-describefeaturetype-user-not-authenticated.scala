// a test for the GeoServer bug where any DescriveFeatureType return a 401
// when layer security is enabled in "mixed" mode
new Test (args) {
  val queryParams = Map(
    "request" -> "describefeaturetype",
    "featuretype" -> "geob:communes_geofla")

  "wfs DescribeFeatureType" inService "geoserver/wfs" withGet queryParams should (result => {
    result has ('responseCode -> 200)
  })
}
