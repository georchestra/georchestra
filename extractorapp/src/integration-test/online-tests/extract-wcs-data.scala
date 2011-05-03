import java.net.URL

new Test (args) {
  val jsonRequest = properties("generic.wcs.request").
                    replace("{crs}", "EPSG:27562").
                    resolve("geoserver.url")

 "extractor performs wcs extraction" inService "extractorapp-private/ws/extractor/test-initiate" withPost jsonRequest should (result => {
       result has ('responseCode -> 200)
       val url = new java.net.URL((result.xml \\ "link")(0).text)

       val nonProxyURL = new URL(url.toString.replaceFirst("extractorapp", "extractorapp-private"))
       val zip = Curl(nonProxyURL).zipFile
       
       val failures = zip.entries.find{_.getName.endsWith("failures.html")}
       
       assert(failures.isEmpty, "Failures: \n\n"+zip.asString(failures.get)+"\n\n")
       
       zip.entries foreach { e =>
         assert(e.getName forall {c => !(""":\|<>*?""" contains c)}, e + " contains an illegal character")
       }
       
       zip has ('size -> 1)
              
       val imageEntry = zip.entries.find{_.getName.endsWith(".tif")}
       
       assert(imageEntry.isDefined, "tif image was not found")
       val image = javax.imageio.ImageIO.read(zip.getInputStream(imageEntry.get))
       assert(image != null, "Image file is corrupt") 
    })
}

