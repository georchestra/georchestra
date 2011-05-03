new Test (args) {
  val jsonRequest = """|{
                       |   "globalProperties": {
                       |       "projection": "EPSG:2154",
                       |       "rasterFormat": "geotiff",
                       |       "vectorFormat": "shp",
                       |       "bbox": {
                       |           "srs": "EPSG:2154",
                       |           "value": [111335.2,6704491.9,400594.5,6881488.6]
                       |       }
                       |   },
                       |   "layers": [
                       |       {
                       |           "owsUrl": "http://pariou.wrk.cby/cgi-bin/mapserv_sextant?",
                       |           "owsType": "WCS",
                       |           "layerName": "rasteur",
                       |           "projection": "EPSG:32633",
                       |           "resolution": 1,
                       |           "bbox": {
                       |               "srs":"EPSG:27572",
          					   |               "value":[
          					   |                  96432.941292876,
					             |                  2397924.8548813,
	          				   |                  97899.854221636,
					             |                  2398926.6490765
           					   |                ]
                       |           }
                       |       }
                       |   ],
                       |  "emails" : [
                       |      "jesse.eichar@camptocamp.com"
                       |   ],
                       |}""".stripMargin

 "extractor performs wcs extraction" inService "extractorapp/ws/extractor/test-initiate" withPost jsonRequest should (result => {
       result has ('responseCode -> 200)
       val url = new java.net.URL((result.xml \\ "link")(0).text)
       val zip = Curl(url).zipFile
       
       val failures = zip.entries.find{_.getName.endsWith("failures.html")}
       
       assert(failures.isEmpty, "Failures: \n\n"+zip.asString(failures.get)+"\n\n")
       
       zip has ('size -> 1)
              
       val imageEntry = zip.entries.find{_.getName.endsWith(".tif")}
       
       assert(imageEntry.isDefined, "tif image was not found")
       val image = javax.imageio.ImageIO.read(zip.getInputStream(imageEntry.get))
       assert(image != null, "Image file is corrupt") 
    })
}

