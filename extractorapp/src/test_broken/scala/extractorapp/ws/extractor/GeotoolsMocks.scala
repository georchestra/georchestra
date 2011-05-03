package extractorapp.ws.extractor

import org.scalatest.mock.MockitoSugar
import org.mockito.Matchers._
import org.mockito.Mockito._

import extractorapp.ws.extractor.wcs._

import org.geotools.data.{
    DataStore, FeatureSource,
    DataUtilities, DataStoreFactorySpi,
    AbstractDataStoreFactory, Query
}
import org.geotools.data.DataAccessFactory.Param
import org.geotools.feature.{
  FeatureCollection, AttributeTypeBuilder
}
import org.opengis.feature.simple.{
    SimpleFeature, SimpleFeatureType
}
import org.geotools.feature.simple.{
  SimpleFeatureBuilder, SimpleFeatureTypeBuilder
}
import org.geotools.referencing.CRS.decode
import org.opengis.filter.Filter

import com.vividsolutions.jts.geom.{
  Geometry, GeometryFactory, Coordinate
}

import java.io.Serializable
import java.util.{Map => javaMap}
import SampleData._

/**
 * Several methods for creating mocks and sample geotools objects for mocking up a
 * test fixture for specs
 */
object GeotoolsMocks extends MockitoSugar {
    /**
     * A mock DataStoreFactorySpi that can be used to create the mockDataStore.
     */
    def mockDataStoreFactory : DataStoreFactorySpi = {
        val factory = new AbstractDataStoreFactory(){
            def createDataStore(params : javaMap[String, Serializable]) = mockDataStore
            def createNewDataStore(params : javaMap[String, Serializable]) = mockDataStore
            def getParametersInfo:Array[Param] = Array[Param]()
            def getDescription = "Mock factory"
        }

        factory
    }

    /**
     * Create a mock DataStore.
     *
     * The datastore will have one FeatureType with a couple of features
     */
    def mockDataStore : DataStore = {
        import DataUtilities.collection

        val dataStore = mock[DataStore]
        val featureSource = mock[FeatureSource[SimpleFeatureType, SimpleFeature]]
        val line1 = point(2,46)
        val line2 = line( (0,50), (0,43))
        val featureCollection = collection (Array (feature ("1", line1, line1.getCentroid, "1", 1:Integer), feature("2",line2, line2.getCentroid,"2",2:Integer)))

        // configure mock object
        // these are the only supported method calls for now
        when (dataStore getFeatureSource (anyString)).thenReturn (featureSource)
        when (dataStore getSchema(vectorLayerName)).thenReturn (featureType)
        when (featureSource getFeatures ()).thenReturn (featureCollection)
        when (featureSource getFeatures (anyObject():Filter)).thenReturn (featureCollection)
        when (featureSource getFeatures (anyObject():Query)).thenReturn (featureCollection)

        dataStore
    }

    /**
     * The feature type used to by the mockDatastore and to create
     * Features in the feature() method.
     */
    val featureType : SimpleFeatureType = {
      val builder = new SimpleFeatureTypeBuilder()
      builder setName vectorLayerName
      builder add ("GEOM", classOf[Geometry], decode("EPSG:4326"))
      builder add ("GEOM2", classOf[Geometry])
      builder add ("ID", classOf[String])
      builder add ("id", classOf[Integer])

      builder buildFeatureType ()
    }

    val DEFAULT_ATTRIBUTES = List[Object](point(1,1),null,"ID", 1:Integer)

    /**
     * Creates a feature with specified attributes.  See featureType for the
     * featureType declaration
     *
     * @param id the feature id to assign to the feature
     * @param attributes.
     *          The attributes to be used.  They can be in any order and if any are missing the
     *          attributes from DEFAULT_ATTRIBUTES will be used.
     *          For examples 2 will result in the attributes: List(point(1,1), null, "ID", 2)
     */
    def feature(id:String,
                attributes : Object*) : SimpleFeature = {
      // fill up attributes and correct order of Attributes
      val attributesAndDefaults = for( (e,i) <- DEFAULT_ATTRIBUTES.zipWithIndex ) yield {
        val found = i match {
          case 0 => attributes find {_.isInstanceOf[Geometry]}
          case 1 => (attributes filter {_.isInstanceOf[Geometry]} drop 1).firstOption
          case 2 => attributes find {_.isInstanceOf[String]}
          case 3 => attributes find {_.isInstanceOf[Integer]}
        }
        found getOrElse e
      }
      SimpleFeatureBuilder build (featureType, attributesAndDefaults.toArray, id)
    }

    // this has to be a lazy val so that when DEFAULT_ATTRIBUTES is initialized this will not be null
    lazy val GEOM_FAC = new GeometryFactory()

    // convenience methods for creating geometries using the GEOM_FAC
    def point(x:Double, y:Double) = GEOM_FAC.createPoint(new Coordinate(x,y))
    def line(points:(Double,Double)*) = {
      val coordinates = points map { case (x,y) => new Coordinate(x,y)}
      GEOM_FAC.createLineString(coordinates.toArray)
    }

}