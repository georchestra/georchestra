package extractorapp.ws.extractor

//import org.geotools.data.shapefile.ShapefileDataStore
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.fixture.FixtureWordSpec
import org.scalatest.matchers.MustMatchers
import java.io.File
import scala.collection.jcl.Conversions._

import org.geotools.data.shapefile.ShapefileDataStore
import com.vividsolutions.jts.geom.{
  Point, LineString
}

import SampleData.sampleJSON
import GeotoolsMocks._

@RunWith(classOf[JUnitRunner])
class WfsExtractorSpec extends FixtureWordSpec with MustMatchers with GeotoolsMatchers {

    class WfsExtractorTestFixture extends StorageTestFixture("wfsExtractorSpec") {
      val extractor = new WfsExtractor(storageDir, mockDataStoreFactory)
    }

    "WFS extractor" should {
        "construct Query from ExtractorLayerRequest" in {  (fixture : WfsExtractorTestFixture) =>
            import fixture._
            val query = extractor.createQuery(requests(0), featureType)
            query must not be (null)
            query.getFilter must be (intersectsFilter (0, 45.0, 6.0, 55.0))
            query.getPropertyNames must have length (3)
            query.getPropertyNames must not contain ("GEOM2")
            query.getCoordinateSystemReproject must be (epsg(2154))
        }
        "throw exception when request is not WFS request" in { (fixture : WfsExtractorTestFixture) =>
            import fixture._
            intercept[IllegalArgumentException] {
                extractor extract requests(1)
            }
        }
        "extract to shapefile" in { (fixture : WfsExtractorTestFixture) =>
            import fixture._
            val shpFiles = extractor extract requests(0)

            shpFiles must have length (2)
            shpFiles foreach { file =>
                file must be a ('file)
                file.getParentFile must have (
                    'parentFile (storageDir)
                )
               val ds = new ShapefileDataStore (file.toURI.toURL)
               val featureCount = ds.getFeatureSource().getFeatures.size
               featureCount must equal (1)

               val expectedGeomType = if (file.getName.endsWith("POINT.shp")) "MultiPoint"
                                      else "MultiLineString"

               val geomType = ds.getSchema.getGeometryDescriptor.getType.getBinding
               geomType.getSimpleName must equal (expectedGeomType)
               val crs = ds.getSchema.getCoordinateReferenceSystem
               crs must be (epsg(2154))
            }
        }
    }

// the fixture setup code.
    type FixtureParam = WfsExtractorTestFixture

    override def withFixture(test: OneArgTest) {
        val fixture = new WfsExtractorTestFixture()
        try { test(fixture) }
        finally {
            fixture.cleanup()
        }
    }
}