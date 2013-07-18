/**
 * 
 */
package org.geotools.data.mif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Unit test for MIFDataStoreFactory
 * 
 * 
 * @author Mauricio Pazos
 *
 */
public class MIFDataStoreFactoryTest extends MIFDataStoreFactory {

	@Test
	public void readFeatures() throws Exception{

		
		URL url= this.getClass().getResource("points-4326.mif");  
		String file = url.toURI().getPath();

		HashMap<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(MIFDataStoreFactory.PARAM_PATH.key, file);

		MIFDataStoreFactory storeFactory = new MIFDataStoreFactory();
		DataStore store = storeFactory.createDataStore(params);
		SimpleFeatureType type = store.getSchema("points-4326");
		assertNotNull( type.getCoordinateReferenceSystem() ) ;

		SimpleFeatureSource featureSource = store.getFeatureSource(type.getTypeName());

		SimpleFeatureCollection features = featureSource.getFeatures();
		assertNotNull( features.getSchema().getCoordinateReferenceSystem());
		
		assertNotNull(features);
	}
	
	@Ignore
	public void crsNotFound() throws URISyntaxException {

		final String layerName = "SBB-NETZ-12-res20_line-train_120919";
		URL url= getClass().getResource(layerName + ".mif");  
		String file = url.toURI().getPath();

		try{
			HashMap<String, Serializable> params = new HashMap<String, Serializable>();
			params.put(MIFDataStoreFactory.PARAM_PATH.key, file);

			MIFDataStoreFactory storeFactory = new MIFDataStoreFactory();
			storeFactory.createDataStore(params);
			
			fail();
		} catch (IOException e){
			
			assertTrue(true);
		}
	}
	
	
	/** 
	 * Retrieves the features using the CRS registered in the mif file
	 * @throws Exception
	 */
	@Test
	public void retrieveFeaturesUsingBaseCRS() throws Exception{
		
		SimpleFeatureCollection fc = retrieveFeature("points-4326");
		
		Integer epsgCode = CRS.lookupEpsgCode(fc.getSchema().getCoordinateReferenceSystem(), true);
		
		assertFeatureCollection(fc, 2, epsgCode);
	}

	/** 
	 * The features are reprojected from 4326 to 2154 
	 * @throws Exception
	 */
	@Test
	public void retrieveFeaturesReprojectedTo2154() throws Exception{
		
		final int epsgCode = 2154;
		CoordinateReferenceSystem crs = CRS.decode("EPSG:" + epsgCode);
		SimpleFeatureCollection fc = retrieveFeature("points-4326", crs);
		
		assertFeatureCollection(fc, 2, epsgCode ); 
	}

	
	/**
	 * test the getBounds method
	 * 
	 * @throws Exception 
	 * 
	 */
	@Test
	public void getBounds() throws Exception{
		
		SimpleFeatureCollection fc = retrieveFeature("points-4326");
		ReferencedEnvelope bounds = fc.getBounds();
		
		assertNotNull(bounds);

		Integer epsgCode = CRS.lookupEpsgCode( bounds.getCoordinateReferenceSystem(), true );
		assertEquals( new Integer(4326), epsgCode);
	}
	
	/**
	 * Test getBounds from a FeatureCollection reprojected to 2154.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getBoundsReprojected() throws Exception{
		
		final int targetCRS = 2154;
		
		CoordinateReferenceSystem crs = CRS.decode("EPSG:" + targetCRS);
		SimpleFeatureCollection fc = retrieveFeature("points-4326", crs);

		ReferencedEnvelope bounds = fc.getBounds();
		Integer epsgCode = CRS.lookupEpsgCode( bounds.getCoordinateReferenceSystem(), true );
		assertEquals( new Integer(targetCRS), epsgCode);
	}
	
	private void assertFeatureCollection(SimpleFeatureCollection fc, final int countExpected, final int expectedEPSG) throws Exception {
		
		CoordinateReferenceSystem schemaCRS = fc.getSchema().getCoordinateReferenceSystem();
		
		assertNotNull(schemaCRS);
		
 		assertTrue( expectedEPSG ==CRS.lookupEpsgCode( schemaCRS, true) );
		
		SimpleFeatureIterator iter = fc.features();
		try{
			int i= 0;
			while(iter.hasNext()){
				
				SimpleFeature f = iter.next();
				assertFeatureCRS(f, expectedEPSG);
				
				i++;
			}
			assertTrue(countExpected == i);
			
		} finally {
			iter.close();
		}
	}

	private void assertFeatureCRS(final SimpleFeature f, final int expectedEPSG) throws Exception {

		CoordinateReferenceSystem crs = f.getFeatureType().getCoordinateReferenceSystem();
 		assertEquals( new Integer(expectedEPSG), CRS.lookupEpsgCode( crs, true) );
	}


	private SimpleFeatureCollection  retrieveFeature(String layerName) throws Exception{
		return retrieveFeature(layerName, null); 
	}
	
	private SimpleFeatureCollection  retrieveFeature(String layerName, CoordinateReferenceSystem crs) throws Exception{
		
		HashMap<String, Serializable> params = new HashMap<String, Serializable>();

		// sets the mif file as parameter
		URL url= this.getClass().getResource(layerName+".mif");  
		String file = url.toURI().getPath();
		params.put(MIFDataStoreFactory.PARAM_PATH.key, file);
		
		MIFDataStoreFactory storeFactory = new MIFDataStoreFactory();
		DataStore store = storeFactory.createDataStore(params);
		
		SimpleFeatureType type = store.getSchema(layerName);
		assertNotNull( type.getCoordinateReferenceSystem() ) ;

		SimpleFeatureSource featureSource = store.getFeatureSource(type.getTypeName());

		Query query = new Query(layerName, Filter.INCLUDE);
		if(crs != null){
			query.setCoordinateSystemReproject(crs);
		}
		
		SimpleFeatureCollection featureCollection = featureSource.getFeatures(query);
		
		
		return featureCollection;
	}

}
