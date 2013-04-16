/**
 * 
 */
package org.geotools.data.mif;

import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.referencing.CRS;
import org.junit.AfterClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Unit test for MIFDataStoreFactory
 * 
 * 
 * @author Mauricio Pazos
 *
 */
public class MIFDataStoreFactoryTest extends MIFDataStoreFactory {

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	
	@Test
	public void readFeatures() throws Exception{

		
		URL url= this.getClass().getResource("pigma_regions_POLYGON.mif");  
		String file = url.toURI().getPath();

		HashMap<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(MIFDataStoreFactory.PARAM_PATH.key, file);

		MIFDataStoreFactory storeFactory = new MIFDataStoreFactory();
		DataStore store = storeFactory.createDataStore(params);
		SimpleFeatureType type = store.getSchema("pigma_regions_POLYGON");
		assertNotNull( type.getCoordinateReferenceSystem() ) ;

		SimpleFeatureSource featureSource = store.getFeatureSource(type.getTypeName());

		SimpleFeatureCollection features = featureSource.getFeatures();
		assertNotNull( features.getSchema().getCoordinateReferenceSystem());
		
		assertNotNull(features);

	}
	
	@Test
	public void reprojectedFeatures() throws Exception{
		
		HashMap<String, Serializable> params = new HashMap<String, Serializable>();

		// sets the mif file as parameter
		URL url= this.getClass().getResource("pigma_regions_POLYGON.mif");  
		String file = url.toURI().getPath();
		params.put(MIFDataStoreFactory.PARAM_PATH.key, file);

		// sets the crs as parameter
		MIFProjReader prjReader = new MIFProjReader();
		
		CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
		String mifcrs = prjReader.checkSRID(crs);
		params.put(MIFDataStoreFactory.PARAM_COORDSYS.key, mifcrs);
		
		// retrieves the features
		MIFDataStoreFactory storeFactory = new MIFDataStoreFactory();
		DataStore store = storeFactory.createDataStore(params);
		SimpleFeatureType type = store.getSchema("pigma_regions_POLYGON");
		assertNotNull( type.getCoordinateReferenceSystem() ) ;

		SimpleFeatureSource featureSource = store.getFeatureSource(type.getTypeName());

		SimpleFeatureCollection features = featureSource.getFeatures();
		assertNotNull( features.getSchema().getCoordinateReferenceSystem());
		
		assertNotNull(features);
	}
	


}
