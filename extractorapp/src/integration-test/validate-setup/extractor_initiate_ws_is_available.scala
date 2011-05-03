new Test (args) {
  val emptyRequest =  """|{
                         |  "emails" : [
                         |      "address1",
                         |      "address2"
                         |   ],
                         |   "globalProperties": {
                         |       "projection": "EPSG:2154",
                         |       "rasterFormat": "png",
                         |       "vectorFormat": "shp",
                         |       "bbox": {
                         |           "srs": "EPSG:2154",
                         |           "value": [111335.2,6704491.9,400594.5,6881488.6]
                         |       }
                         |   },
                         |   "layers": []
                         |}""".stripMargin

  "extractor be available" inService "extractorapp-private/ws/extractor/initiate" withPost emptyRequest should (result => {
    result has ('responseCode -> 200)
  }) 
}