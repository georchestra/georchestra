/**
 * 
 */
package org.geotools.data.mif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;

import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.referencing.CRS;
import org.junit.AfterClass;
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

		SimpleFeatureCollection featureCollection = featureSource.getFeatures();
		assertNotNull( featureCollection.getSchema().getCoordinateReferenceSystem());
		
		assertNotNull(featureCollection);

		int i= 1;
		SimpleFeatureIterator iter = featureCollection.features();
		while(iter.hasNext()){
			iter.next();
			i++;
		}
		assertEquals(94, i);
	}
	
	/** 
	 * Retrieves the features using the CRS registered in the mif file
	 * @throws Exception
	 */
	@Test
	public void retrieveFeaturesUsingBaseCRS() throws Exception{
		
		SimpleFeatureCollection fc = retrieveFeature("pigma_regions_POLYGON");
		
		Integer epsgCode = CRS.lookupEpsgCode(fc.getSchema().getCoordinateReferenceSystem(), true);
		
		assertFeatureCollection(fc, 93, epsgCode);
	}

	/** 
	 * The features are reprojected from 4326 to 2154 
	 * @throws Exception
	 */
	@Test
	public void retrieveFeaturesReprojectedToCRS() throws Exception{
		
		final int epsgCode = 2154;
		CoordinateReferenceSystem crs = CRS.decode("EPSG:" + epsgCode);
		SimpleFeatureCollection fc = retrieveFeature("pigma_regions_POLYGON", crs);
		
		assertFeatureCollection(fc, 93, epsgCode ); 
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
 		assertTrue( expectedEPSG ==CRS.lookupEpsgCode( crs, true) );
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
			query.setCoordinateSystem(crs);

			// in order to reproject the collection the crs provided should be different to the base crs
			int baseCRS = CRS.lookupEpsgCode(type.getCoordinateReferenceSystem(), true);
			int requiredCRS = CRS.lookupEpsgCode(crs, true);
			assertFalse( baseCRS == requiredCRS);
		}
		
		SimpleFeatureCollection featureCollection = featureSource.getFeatures(query);
		
		
		return featureCollection;
	}

}
