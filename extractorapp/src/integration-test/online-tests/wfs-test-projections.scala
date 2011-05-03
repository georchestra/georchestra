import java.net.URL

new Test (args) {
  val jsonRequestTemplate = properties("generic.wfs.request").resolve("geoserver.url")
  

  val results = for {
    crs <- List(27562,27572,2154,3948,4171,4326)
    jsonRequest = jsonRequestTemplate.replace ("{crs}","EPSG:"+crs.toString)
    } yield {
        "extract wcs with crs="+crs inService "extractorapp-private/ws/extractor/test-initiate" withPost jsonRequest should (result => {
             println("testing EPSG:"+crs)
             result has ('responseCode -> 200)
             val url = new URL((result.xml \\ "link")(0).text)
             val nonProxyURL = new URL(url.toString.replaceFirst("extractorapp", "extractorapp-private"))
             val zip = Curl(nonProxyURL).zipFile
             zip.entries foreach { e =>
               assert(e.getName forall {c => !(""":\|<>*?""" contains c)}, e + " contains an illegal character")
             }
             zip has ('size -> 4)
         })
           None
    }
}

