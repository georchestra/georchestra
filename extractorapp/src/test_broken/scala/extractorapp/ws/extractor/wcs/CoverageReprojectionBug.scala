package extractorapp.ws.extractor.wcs

import org.geotools.referencing.CRS;

import java.io.File

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.fixture.FixtureWordSpec
import org.scalatest.concurrent.Conductor
import org.scalatest.matchers.MustMatchers

import IoSupport._

@RunWith(classOf[JUnitRunner])
class CoverageReprojectionBug extends FixtureWordSpec with MustMatchers with ParallelTesting {
  "Geotiff must be able to be reprojected from EPSG2154 to EPSG27562" in { fixture =>
    val f = new File(fixture.storageDir, "image.tif")
    copy(file(this.getClass, "../EPSG2154-to-EPSG27562-test.tif"), f)
    val fromCRS = CRS.decode("EPSG:2154")
    val toCRS = CRS.decode("EPSG:27562")
    
    // only the from and to CRS parameters matter in this test
    val mockDesiredRequest = WcsReaderRequestFactory.create("1.0.0","unimportant", 0,0,1,1,fromCRS, toCRS, 10, "geotiff")
    val mockActualRequest = WcsReaderRequestFactory.create("1.0.0","unimportant", 0,0,1,1,fromCRS, fromCRS, 10, "geotiff")
    WcsCoverageReader.transformCoverage(f,mockDesiredRequest, mockActualRequest)
    
    // no exception? good!
  }
  
  // the fixture setup code.
  type FixtureParam = StorageTestFixture

  override def withFixture(test: OneArgTest) {
      val fixture = new StorageTestFixture("WcsCoverageReaderSpec")
      try { test(fixture) }
      finally {
          fixture.storageDir.listFiles foreach {_.delete}
          require (fixture.storageDir.delete == true)
      }
  }
}