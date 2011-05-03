package extractorapp.ws.extractor

//import org.geotools.data.shapefile.ShapefileDataStore
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.fixture.FixtureWordSpec
import org.scalatest.matchers.MustMatchers
import java.io.File
import java.util.UUID

import scala.collection.jcl.Conversions._

import org.geotools.data.shapefile.ShapefileDataStore
import com.vividsolutions.jts.geom.{
  Point, LineString
}
import SampleData.sampleJSON
import GeotoolsMocks._

@RunWith(classOf[JUnitRunner])
class ExtractorThreadSpec extends FixtureWordSpec with MustMatchers with GeotoolsMatchers {
  "ExtractorThread" should {
    "handle wfs extraction failure by writing an error message in the zip file" in { fixture =>
      import fixture._
      
      val archiveName = "testuuid.zip"
      val uuid = UUID.randomUUID()
      val tmpBundleDir = new File(storageDir, "dir")
      val archive = new File(storageDir, archiveName)
      
      tmpBundleDir.mkdirs
      tmpBundleDir.exists must be (true)
      
      val wfsRequest = new java.util.ArrayList[ExtractorLayerRequest]()
      wfsRequest.add(requests(0))
      
      val thread = new ExtractorThread(true, wfsRequest, null, uuid, null, null, null, Int.MaxValue){
        override def mkTmpBundleDir(name:String) = {
          if(name == uuid.toString) {
            tmpBundleDir
          } else{
            val f = new File(storageDir, "name")
            f.mkdirs
            f
          }
        }
        override def archiveExtraction(tmpExtractionBundle:File) = {
          tmpExtractionBundle.listFiles must have size (1)
          
          FileUtils.archiveToZip(tmpExtractionBundle, archive);
          archive
        }
      }
      thread.run();
      
      archive.exists must be (true)
      val entries = FileUtils.listZip(archive)
      entries must have size (1)
      println(entries)
      
      FileUtils.getZipEntryAsString(archive, "dir/failures.html") != null must be (true)
    }
  }
  
  // the fixture setup code.
  type FixtureParam = StorageTestFixture

  override def withFixture(test: OneArgTest) {
      val fixture = new StorageTestFixture("ExtractorThreadSpec")
      try { test(fixture) }
      finally {
          fixture.cleanup()
      }
  }
}