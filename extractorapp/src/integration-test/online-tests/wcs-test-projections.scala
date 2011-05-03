import java.net.URL

new Test (args) {
  val jsonRequestTemplate = properties("generic.wcs.request").resolve("geoserver.url")

  val results = for {
    crs <- List(27562,27572,2154,3948,4171,4326)
    jsonRequest = jsonRequestTemplate.replace ("{crs}","EPSG:"+crs.toString)
    } yield {
        "extract wcs with crs="+crs inService "extractorapp-private/ws/extractor/test-initiate" withPost jsonRequest should (result => {
             println("testing EPSG:"+crs)
             result has ('responseCode -> 200)
             val url = new java.net.URL((result.xml \\ "link")(0).text)
             val nonProxyURL = new URL(url.toString.replaceFirst("extractorapp", "extractorapp-private"))
             val zip = Curl(nonProxyURL).zipFile
             val imageEntry = zip.entries.find{_.getName.endsWith(".tif")}

             zip.entries foreach { e =>
               assert(e.getName forall {c => !(""":\|<>*?""" contains c)}, e + " contains an illegal character")
             }

             assert(imageEntry.isDefined, "tif image was not found")
             val image = javax.imageio.ImageIO.read(zip.getInputStream(imageEntry.get))
             assert(image != null, "Image file is corrupt") 
             
         })
           None
    }
}

