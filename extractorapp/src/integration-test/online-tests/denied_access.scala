import java.net.URL

new Test (args) {
  val jsonRequest = """{ 
      "emails":["jesse.eichar@camptocamp.com"], 
      "globalProperties":{ 
        "projection":"EPSG:27562", 
        "resolution":25, 
        "rasterFormat":"geotiff", 
        "vectorFormat":"shp", 
        "bbox":{ 
          "srs":"EPSG:2154", 
          "value":[203198.15059445,6808903.5667107,233025.0990753,6835614.2668428] 
        } 
      }, 
      "layers":[ 
        { 
          "projection":null, 
          "resolution":null, 
          "format":null, 
          "bbox":null, 
          "owsUrl":"%s", 
          "owsType":"WFS", 
          "layerName":"geob_edit:tasmania_cities" 
        } 
      ] 
    }""".resolve("geoserver.url")

 "extractor should not be permitted to access a restricted layer" inService "extractorapp-private/ws/extractor/test-initiate" withPost jsonRequest should (result => {
       result has ('responseCode -> 200)
       val url = new java.net.URL((result.xml \\ "link")(0).text)
       val nonProxyURL = new URL(url.toString.replaceFirst("extractorapp", "extractorapp-private"))
       val zip = Curl(nonProxyURL).zipFile
       zip has ('size -> 1)
       assert(zip.entries.find{_.getName.endsWith("failures.html")}.isDefined, "a failure is expected")
    })
}

