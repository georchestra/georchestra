/**
 * 
 */
package org.geotools.data.mif;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URI;
import java.util.HashMap;

import org.geotools.data.DataStore;
import org.geotools.data.ServiceInfo;
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
	public void read() throws Exception{

		HashMap params = new HashMap();
		
		File directory = new File(".");  
//		String file = directory.getAbsolutePath() +"/src/test/java/org/geotools/data/mif/pigma_regions_POLYGON.mif";
		String file = "/home/mauro/devel-box/projects/georchestra/mpazos-georchestra/gt-mif/src/test/resources/org/geotools/data/mif/pigma_regions_POLYGON.mif";
		params.put(MIFDataStoreFactory.PARAM_PATH.key, file);

//		CoordinateReferenceSystem crs = CRS.parseWKT("EPSG:4326");
//		Integer code = CRS.lookupEpsgCode(crs, true);
//		params.put(MIFDataStoreFactory.PARAM_COORDSYS.key, code);

		MIFDataStoreFactory storeFactory = new MIFDataStoreFactory();
		DataStore store = storeFactory.createDataStore(params);

//		ServiceInfo info = store.getInfo();
//		URI uriSchema = info.getSchema();
//		SimpleFeatureType schema = store.getSchema(uriSchema.toString());

		SimpleFeatureType type = store.getSchema("pigma_regions_POLYGON");

		SimpleFeatureSource featureSource = store.getFeatureSource(type.getTypeName());

		SimpleFeatureCollection features = featureSource.getFeatures();
		
		assertNotNull(features);

	}


}
