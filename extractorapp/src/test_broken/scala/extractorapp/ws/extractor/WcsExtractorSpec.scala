package extractorapp.ws.extractor

import extractorapp.ws.extractor.wcs._

import java.io.File
import java.net.URL

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.fixture.FixtureWordSpec
import org.scalatest.matchers.MustMatchers
import scala.collection.jcl.Conversions._
import scala.io.Source

import org.geotools.referencing.CRS._
import org.opengis.parameter.GeneralParameterValue

import SampleData.sampleJSON

@RunWith(classOf[JUnitRunner])
class WcsExtractorSpec extends FixtureWordSpec with MustMatchers with GeotoolsMatchers {

  "WcsExtractor" should {
    "parse request and make request" in { fixture =>
      import fixture._
      val url = new URL("http://drebretagne-geobretagne.int.lsn.camptocamp.com:80/geoserver/wfs/WfsDispatcher2")
      val mockWcsReader = new WcsCoverageReader (url) {
        
        override def readToFile(containingDirectory:File, baseFilename:String, 
                                parameters:Array[GeneralParameterValue] ) : File = {
          val request = WcsReaderRequestFactory.create(parameters)
        
          request.groundResolutionX must equal (0.25)
          request must have (
            'version ("1.0.0"),
            'coverage (SampleData.rasterLayerName),
            'format ("png")
          )

          request.requestBbox must have (
            'minX (111335.2),
            'maxX (400594.5),
            'minY (6704491.9),
            'maxY (6881488.6)            
          )

          request.responseCRS must be (epsg (27572))

          containingDirectory.getParentFile must be (storageDir)
          return new File("xyz")
        }
      }
      
      val mockWcsFormat = new WcsFormat{override def getReader(obj:Object) = mockWcsReader}

      val extractor = new WcsExtractor(storageDir, mockWcsFormat)
      val result = extractor.extract (requests (1))

      result must not be (null)
    }
  }
  
   // the fixture setup code.
  type FixtureParam = StorageTestFixture

  override def withFixture(test: OneArgTest) {
    val fixture = new StorageTestFixture("WcsExtractorSpec")
    try { test(fixture) }
    finally {
      fixture.cleanup
    }
  }
}