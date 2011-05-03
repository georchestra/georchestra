package extractorapp.ws.extractor

import SampleData._
import java.io.File
/**
 * The test fixture.  Not a test but is passed to each test
 *
 * (notice the fixture : ExtractorTestFixture in each test)
 */
class StorageTestFixture (specName:String) {
  /* a directory sandbox that will be deleted after test */
  val storageDir = {
    val base = File.createTempFile(specName, "")
    base.delete()
    base.mkdirs()
    base
  }
  val requests = (ExtractorLayerRequest parseJson sampleJSON)
  
  def cleanup() = {
    def delete(file:File) : Unit = {
      if(file.isDirectory) { 
        file.listFiles foreach {delete _}
      }
      require (file.delete == true)
    }
    delete(storageDir)
  }
  
}