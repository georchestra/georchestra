import java.net.URL

new Test (args) {
  val jsonRequest = properties("generic.wfs.request").resolve("geoserver.url").replace("{crs}","EPSG:27562").replace("shp","mif")

 "extractor performs mif extraction" inService "extractorapp-private/ws/extractor/test-initiate" withPost jsonRequest should (result => {
       result has ('responseCode -> 200)
       val url = new java.net.URL((result.xml \\ "link")(0).text)
       val nonProxyURL = new URL(url.toString.replaceFirst("extractorapp", "extractorapp-private"))
       val zip = Curl(nonProxyURL).zipFile
       zip has ('size -> 2)
       zip.entries foreach { e =>
         assert(e.getName forall {c => !(""":\|<>*?""" contains c)}, e + " contains an illegal character")
       }
       assert(zip.entries.find{_.getName.endsWith(".mif")}.isDefined, "mif file is missing")
       
    })
}

