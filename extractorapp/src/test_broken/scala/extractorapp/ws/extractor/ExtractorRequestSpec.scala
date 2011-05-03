package extractorapp.ws.extractor

import org.geotools.referencing.CRS;
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

import scala.collection.jcl.Conversions._

import SampleData._
import GeotoolsMocks._

@RunWith(classOf[JUnitRunner])
class ExtractorRequestSpec extends WordSpec with MustMatchers {

    "JSON Request Parser" can {
        val json = " %s  ".format(sampleJSON)
        val layers = ExtractorLayerRequest.parseJson(json)


        "parse a json array of layers" in {
            layers must have size (2)
        }

        "parse the parameters layer1" in {
            val layer1 = layers(0)
            import layer1._

            _url must have ('toString (wfsURL))
            _epsg must equal ("EPSG:2154")
            _projection must equal (CRS.decode(_epsg))
            _layerName must equal ("geob:communes_geofla")
            _format must equal ("shp")
            _owsType must equal (OWSType.WFS)
            _bbox must have (
                'minX (0),
                'minY (45),
                'maxX (6),
                'maxY (55),
                'coordinateReferenceSystem (CRS.decode("EPSG:4326"))
            )
            _emails must have length (2)
            _emails must contain ("address1")
            _emails must contain ("address2")
            
        }

        "construct a capabilities URL" in {
          val layer1 = layers(0)
          import layer1._
          
          val query = capabilitiesURL("WFS","1.0.0").getQuery
          query.contains ("REQUEST=GETCAPABILITIES") must be (true)
          query.contains ("SERVICE=WFS") must be (true)
          query.contains ("VERSION=1.0.0") must be (true)
        }

        "parse the parameters layer2" in {
            val layer2 = layers(1)
            import layer2._

            _url must have { 'toString (wcsURL) }
            _epsg must equal ("EPSG:27572")
            _projection must equal (CRS.decode(_epsg))
            _layerName must equal ("geob_loc:PAYS_region")
            _format must equal ("png")
            _owsType must equal (OWSType.WCS)
            _bbox must have (
                'minX (111335.2),
                'minY (6704491.9),
                'maxX (400594.5),
                'maxY (6881488.6),
                'coordinateReferenceSystem (CRS.decode("EPSG:2154"))
            )
        }
    }
}