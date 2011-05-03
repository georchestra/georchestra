package extractorapp.ws.extractor.wcs

import java.net.{
  Socket, URL, ServerSocket
}

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.fixture.FixtureWordSpec
import org.scalatest.concurrent.Conductor
import org.scalatest.matchers.MustMatchers

import scala.io.Source
import scala.collection.jcl.Conversions._

import org.geotools.referencing.CRS
import org.opengis.parameter.{
  GeneralParameterValue, ParameterValueGroup
}
import org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;

import java.nio.channels.Channels
import java.net._
import MockWcsServer._
import GeotoolsMocks._
import IoSupport._

@RunWith(classOf[JUnitRunner])
class WcsCoverageReaderSpec extends FixtureWordSpec with MustMatchers with ParallelTesting {

  "WcsFormat" should {
    
    val format = new WcsFormat(999999)
    "accept url input objects" in { fixture =>
      format accepts new URL("http://xys") must be (true)
      format getReader new URL("http://xys") must not be (null)
      format getReader new URL("http://xys") must not be (null)
    }
    "accept url string input objects" in { fixture =>
      format accepts "http://xys" must be (true)
      format getReader "http://xys" must not be (null)
      format getReader "http://xys" must not be (null)
    }
    "throw exception with bad inputs" in { fixture =>
      format accepts "hi" must be (false)
      evaluating { format getReader "hi" } must produce [IllegalArgumentException]

      format accepts 1 must be (false)
      evaluating { format getReader 1 } must produce [IllegalArgumentException]
    }
  }
  "WcsCoverageReader" should {
    "should output bmp even when server does not support it" in { fixture =>
      val server = new MockWcsServer().accepting (
          DescribeCoverage (),
          DescribeCoverage (),
		      GetCoverage()
		    )
      parallel (
        fThread ("mock wcs server") (server),
        thread ("reader thread") {
          Thread.sleep(200)

          val url = new URL("http://localhost:"+server.port)
          val reader = new WcsCoverageReader(url)
          val request = WcsReaderRequestFactory.create("1.0.0", "layername", 0.0, 5.0, 10.0, 15.0, WGS84, WGS84, .25, "bmp")

          val bound = request.bind(url)
          bound.getSupportedFormats must not contain ("bmp")
          bound.getSupportedFormats must contain ("png")

          import fixture._
          val file = reader.readToFile(storageDir, "WcsReaderSpec", request.getParameters)
          file.getName must be ("WcsReaderSpec.bmp")
			  }
			)
		}
    "should output geotiff even when server does not support it" in { fixture =>
      val server = new MockWcsServer().accepting (
          DescribeCoverage (file(this.getClass,"../no-geotiff-describe-coverage-1.0.0.xml")),
          DescribeCoverage (file(this.getClass,"../no-geotiff-describe-coverage-1.0.0.xml")),
		      GetCoverage()
		    )
      parallel (
        fThread ("mock wcs server") (server),
        thread ("reader thread") {
          Thread.sleep(200)

          val url = new URL("http://localhost:"+server.port)
          val reader = new WcsCoverageReader(url)
          val request = WcsReaderRequestFactory.create("1.0.0", "layername", 0.0, 5.0, 10.0, 15.0, WGS84, WGS84, .25, "geotiff")

          val bound = request.bind(url)
          bound.getSupportedFormats must not contain ("geotiff")
          bound.getSupportedFormats must contain ("png")

          import fixture._
          val file = reader.readToFile(storageDir, "WcsReaderSpec", request.getParameters)
          file.getName must be ("WcsReaderSpec.tif")
			  }
			)
    }
    "should reproject locally if server will not" in { fixture =>
      val request = WcsReaderRequestFactory.create("1.0.0", "layername", 0.0, 5.0, 10.0, 15.0, WGS84, CRS.decode("EPSG:21781"), .25, "jpeg")
      val server = new MockWcsServer() accepting (
          DescribeCoverage{ params => params("REQUEST") must equal ("DESCRIBECOVERAGE") },
          GetCoverage {params => params("CRS") must equal ("EPSG:4326")}
        )
  		parallel (
        fThread ("mock wcs server")(server),
        thread ("client") {
        	Thread.sleep(200)
          val reader = new WcsCoverageReader(new URL("http://localhost:"+server.port))
          val coverage = request.execute(reader)
          coverage must not be (null)
          CRS.lookupEpsgCode(coverage.getCoordinateReferenceSystem, false) must be (21781)
        }
      )
    }
    "should reproject geotiffs locally if server will not" in { fixture =>
      val request = WcsReaderRequestFactory.create("1.0.0", "layername", 0.0, 5.0, 10.0, 15.0, WGS84, CRS.decode("EPSG:21781"), .25, "geotiff")
      val img = file(getClass, "../geotiff4326.tif")
      val server = new MockWcsServer() accepting (
          DescribeCoverage{ params => params("REQUEST") must equal ("DESCRIBECOVERAGE") },
          GetCoverage (img, 
                       params => params("CRS") must equal ("EPSG:4326")
                      )
        )
  		parallel (
        fThread ("mock wcs server")(server),
        thread ("client") {
        	Thread.sleep(200)
          val reader = new WcsCoverageReader(new URL("http://localhost:"+server.port))
          val coverage = request.execute(reader)
          coverage must not be (null)
          CRS.lookupEpsgCode(coverage.getCoordinateReferenceSystem, false) must be (21781)
        }
      )
    }
    "make a POST WCS request for a requested layer" in { fixture =>
      val request = WcsReaderRequestFactory.create("1.0.0", "layername", 0.0, 5.0, 10.0, 15.0, WGS84, WGS84, .25, "jpeg", true)
      wcsRequest(request)
    }
    "make a GET WCS request for a requested layer" in { fixture =>
      val request = WcsReaderRequestFactory.create("1.0.0", "layername", 0.0, 5.0, 10.0, 15.0, WGS84, WGS84, .25, "jpeg", false)
      wcsRequest(request)
    }
    "alias jpg to jpeg is server supports jpg but not jpeg" in { fixture =>
      val server = new MockWcsServer().accepting (
          DescribeCoverage {params => params("FORMAT") must equal ("JPG")},
		      GetCoverage {params => params("FORMAT") must equal ("JPEG")},
		    )
      parallel (
        fThread ("mock wcs server") (server),
        thread ("reader thread") {
          Thread.sleep(200)

          val url = new URL("http://localhost:"+server.port)
          val reader = new WcsCoverageReader(url)
          val request = WcsReaderRequestFactory.create("1.0.0", "layername", 0.0, 5.0, 10.0, 15.0, WGS84, WGS84, .25, "jpg")
          val coverage = request.execute(reader)
          coverage must not be (null)
			  }
			)
		}
    "resy is based on ratio of request" in { fixture =>
      val server = new MockWcsServer().accepting (
          DescribeCoverage (),
		      GetCoverage {params =>
		        assert(params("RESX").toDouble - (2.2609) < 0.01)
		        assert(params("RESY").toDouble - (4.5218) < 0.01)
		      },
		    )
      parallel (
        fThread ("mock wcs server") (server),
        thread ("reader thread") {
          Thread.sleep(200)

          val url = new URL("http://localhost:"+server.port)
          val reader = new WcsCoverageReader(url)
          val request = WcsReaderRequestFactory.create("1.0.0", "layername", 0.0, 5.0, 10.0, 25.0, WGS84, WGS84, .25, "jpg")
          val coverage = request.execute(reader)
          coverage must not be (null)
			  }
			)
		}
    "request geotiff layers" in { fixture =>
      val request = WcsReaderRequestFactory.create("1.0.0", "layername", 0.0, 5.0, 10.0, 15.0, WGS84, WGS84, .25, "geotiff")
      wcsRequest(request)
    }
  }

  // -------------------- Support methods --------------------- //

  def stdServer(request:WcsReaderRequest) = {
    val img = if(request.format == "geotiff") file(this.getClass, "../geotiff4326.tif")
              else file(this.getClass, "../image4326.jpg")
    new MockWcsServer() accepting (
      DescribeCoverage{ params => params("REQUEST") must equal ("DESCRIBECOVERAGE") },
      GetCoverage (img, params => {
        params("SERVICE") must equal ("WCS")
        params("VERSION") must equal ("1.0.0")
        params("REQUEST") must equal ("GETCOVERAGE")
        params("COVERAGE") must equal ("LAYERNAME")
        params("CRS") must equal ("EPSG:4326")
        params("BBOX") must equal ("0.0,5.0,10.0,15.0")
        assert(params("RESX").toDouble - (2.2609) < 0.01)
        assert(params("RESY").toDouble - (2.2609) < 0.01)
        params("FORMAT") must equal (request.format.toUpperCase)
      })
    )
  }
  def wcsRequest(request:WcsReaderRequest) = {
    val server = stdServer(request)
		parallel (
      fThread ("mock wcs server")(server),
      thread ("client") {
      	Thread.sleep(200)
        val reader = new WcsCoverageReader(new URL("http://localhost:"+server.port))
        val coverage = request.execute(reader)
        coverage must not be (null)
      }
    )
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