package extractorapp.ws.extractor.wcs

import java.net.{
  Socket, URL, ServerSocket
}

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
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
class WcsReaderRequestSpec extends WordSpec with MustMatchers with ParallelTesting {
  val sharedRequest = WcsReaderRequestFactory.create("1.0.0", "layername", 0.0, 5.0, 10.0, 15.0, WGS84, WGS84, .25, "geotiff")

  "WcsReaderRequest" should {
    "be convertable to a Geotools parameters" in {
      val request = sharedRequest
      val params = request.getParameters
      params must have size (7)

      params("version") must have ('value ("1.0.0"))
      params("coverage") must have ('value ("layername"))
      params("format") must have ('value ("geotiff"))
      params("crs") must have ('value ("EPSG:4326"))
      params("imgParams").values must have length (1)
      params("imgParams/res").values must have length (2)
      params("imgParams/res/resx") must have ('value (.25))
      params("imgParams/res/resy") must have ('value (.25))
      params("extent").values must have length (1)
      params("extent/bbox").values must have length (5)
      params("extent/bbox/minx") must have ('value (0))
      params("extent/bbox/miny") must have ('value (5))
      params("extent/bbox/maxx") must have ('value (10))
      params("extent/bbox/maxy") must have ('value (15))
      params("extent/bbox/crs") must have ('value ("EPSG:4326"))
      params("usePost") must have ('booleanValue (true))
    }
    "be able to convert Geotools parameters to a Request Object" in {
      val request = WcsReaderRequestFactory.create("1.0.0", "layername", 0.0, 5.0, 10.0, 15.0, WGS84, WGS84, .25, "geotiff", false)
      val params = request.getParameters
      val newRequest = WcsReaderRequestFactory.create(params)

      newRequest.groundResolutionX must equal (0.25)
      newRequest must have (
        'version ("1.0.0"),
        'coverage ("layername"),
        'format ("geotiff"),
        'usePost (false)
      )
    }
    def describeServer = new MockWcsServer() accepting (DescribeCoverage{
      params => params ("REQUEST") must be ("DESCRIBECOVERAGE")
    })

    "getCapabilities document" in {
      def capabilitiesServer = new MockWcsServer() accepting (GetCoverage{
            params => params ("REQUEST") must be ("GETCAPABILITIES")
        })
      (pending)
    }

    "get the describeCoverage document" in {
      val server = describeServer
      parallel(
        fThread("server")(server),
        thread("reader request"){
          val boundRequest = sharedRequest.bind(new URL("http://localhost:"+server.port))
          val describeString = boundRequest.getDescribeCoverage
          val describe = xml.XML.loadString(describeString)
          describe must not be (null)
          (describe \\ "CoverageOffering") must have size (1)
        }
      )
    }
    "list supported formats for the layer" in {
      val server = describeServer
      parallel(
        fThread("server")(server),
        thread("reader request"){
          val boundRequest = sharedRequest.bind(new URL("http://localhost:"+server.port))

          val formats = boundRequest.getSupportedFormats
          formats must (
            contain("jpeg") and
            contain("geotiff")
            )
          formats forall (format => format.toLowerCase == format) must be (true)
        }
      )
    }
    "list supported CRS for the layer in requestResponse describeCaps" in {
      val server = describeServer
      parallel(
        fThread("server")(server),
        thread("reader request"){
          val boundRequest = sharedRequest.bind(new URL("http://localhost:"+server.port))
          val responseCrs = boundRequest.getSupportedResponseCRSs
          responseCrs must contain("EPSG:32633")
          responseCrs must contain("EPSG:4326")
          responseCrs must have size (2)

          responseCrs forall (crs => crs.toUpperCase == crs) must be (true)

          val requestCrs = boundRequest.getSupportedResponseCRSs
          requestCrs must contain("EPSG:32633")
          responseCrs must contain("EPSG:4326")
          requestCrs must have size (2)

          requestCrs forall (crs => crs.toUpperCase == crs) must be (true)
        }
      )
    }
    "list supported CRS for the seperated request response describeCaps" in {
      val server = new MockWcsServer() accepting (DescribeCoverage(file(this.getClass, "../describe-coverage-1.0.0-sep-supported-crs.xml")))
      parallel(
        fThread("server")(server),
        thread("reader request"){
          val boundRequest = sharedRequest.bind(new URL("http://localhost:"+server.port))
          val responseCrs = boundRequest.getSupportedResponseCRSs
          responseCrs must contain("EPSG:4321")
          responseCrs must contain("EPSG:4326")
          responseCrs must have size (2)

          responseCrs forall (crs => crs.toUpperCase == crs) must be (true)

          val requestCrs = boundRequest.getSupportedRequestCRSs
          requestCrs must contain("EPSG:4567")
          requestCrs must contain("EPSG:4326")
          requestCrs must have size (2)

          requestCrs forall (crs => crs.toUpperCase == crs) must be (true)
        }
      )
    }
    "cache the describeCoverage result" in {
      val server = describeServer
      parallel(
        fThread("server")(server),
        thread("reader request"){
          val boundRequest = sharedRequest.bind(new URL("http://localhost:"+server.port))
          boundRequest.getDescribeCoverage
          boundRequest.getDescribeCoverage
        }
      )
    }
  }

  /* ---------- Support implicits  -------------
   *  Makes a nicer syntax for some tests by automatically coverting
   *  params to a Map[String,GeneralParameterValue]
   */

  implicit def valueToGroup(value : GeneralParameterValue):ParameterValueGroup = value.asInstanceOf[ParameterValueGroup]
  implicit def paramsToMap(params : Seq[GeneralParameterValue]) : Map[String,GeneralParameterValue] = {
    val entries = params.flatMap {case p:ParameterValueGroup =>
                                    val name = p.getDescriptor.getName.getCode
                                    val children = paramsToMap(p.values).toList map {case (key,value) => (name + "/" + key, value)}
                                    (name,p) :: children
                                  case p => (p.getDescriptor.getName.getCode, p) :: Nil
                                 }
    Map(entries:_*)
  }
}