/**
 * 
 */
package org.geotools.data.ogr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.junit.After;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

/**
 * This test case write a feature collection in different formats
 * 
 * @author Mauricio Pazos
 *
 */
public class OGRDataStoreFormatTest {


    private List<File> tmpFiles = new LinkedList<File>() ;

	/**
     * MIF format Point geometry
     * 
     * Test for {@link OGRDataStore#createSchema(SimpleFeatureCollection, boolean, String[])}
     * @throws Exception
     */
	@Test
    public void testCreate_MIF_Polygon() throws Exception{

    	WKTReader r = new WKTReader();
    	Polygon geom = (Polygon) r.read("POLYGON((-158 -55, 113 10, 2 -130, -158 -55))");

        SimpleFeatureCollection features = (SimpleFeatureCollection) DataProvider.createFeatureCollection(geom, "EPSG:4326"); 
        File baseDir = createTestDir("test-mif-polygon");
        File tmpFile = File.createTempFile("test-mif", ".mif", baseDir);
        
        // create the tab file
        OGRDataStore ds1 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
       
		ds1.createSchema(features, false, new String[]{"FORMAT=MIF"});
        
        // read from tab
        OGRDataStore ds2 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);

        StringTokenizer tokens = new StringTokenizer(tmpFile.getName(), ".");
		String layerName = tokens.nextToken();
		
		testGeometry(layerName, ds2, Polygon.class, DataProvider.FEATURES_AMOUNT);
		
    }
	
    /**
     * MIF format MultiPolygon geometry
     * 
     * Test for {@link OGRDataStore#createSchema(SimpleFeatureCollection, boolean, String[])}
     * @throws Exception
     */
	@Test
    public void testCreate_MIF_MultiPolygon() throws Exception{

    	WKTReader r = new WKTReader();
    	MultiPolygon geom = (MultiPolygon) r.read("MultiPolygon(((-158 -55, 113 10, 2 -130, -158 -55)),((-168 -65, 123 20, 22 -140, -168 -65)))");

        SimpleFeatureCollection features = (SimpleFeatureCollection) DataProvider.createFeatureCollection( geom, "EPSG:4326"); 
        File baseDir = createTestDir( "test-mif-multipolygon");
        File tmpFile = File.createTempFile("test-mif", ".mif", baseDir);
        
        // create the tab file
        OGRDataStore ds1 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
       
		ds1.createSchema(features, false, new String[]{"FORMAT=MIF"});
        
        // read from tab
        OGRDataStore ds2 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);

        StringTokenizer tokens = new StringTokenizer(tmpFile.getName(), ".");
		String layerName = tokens.nextToken();
		
		testGeometry(layerName, ds2, MultiPolygon.class, DataProvider.FEATURES_AMOUNT);
		
    }
	
	
	@After
	public void deleteTemporalFile(){
		
		for(File file: this.tmpFiles ){
			delete(file);
		}
		this.tmpFiles.clear();
	}

	/**
     * MIF format Point geometry
     * 
     * Test for {@link OGRDataStore#createSchema(SimpleFeatureCollection, boolean, String[])}
     * @throws Exception
     */
	@Test
    public void testCreate_MIF_Point() throws Exception{

    	WKTReader r = new WKTReader();
    	Point geom = (Point) r.read("Point(-158 -55)");

        SimpleFeatureCollection features = (SimpleFeatureCollection) DataProvider.createFeatureCollection( geom, "EPSG:4326"); 
        File baseDir = createTestDir("test-mif-point");
        File tmpFile = File.createTempFile("test-mif", ".mif", baseDir);
        
        // create the tab file
        OGRDataStore ds1 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
       
		ds1.createSchema(features, false, new String[]{"FORMAT=MIF"});
        
        // read from tab
        OGRDataStore ds2 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);

        StringTokenizer tokens = new StringTokenizer(tmpFile.getName(), ".");
		String layerName = tokens.nextToken();
		
		testGeometry(layerName, ds2, Point.class, DataProvider.FEATURES_AMOUNT);
		
    }
	
    /**
     * MIF format MultiPoint geometry. It generate Point features
     * 
     * Test for {@link OGRDataStore#createSchema(SimpleFeatureCollection, boolean, String[])}
     * @throws Exception
     */
	@Test
    public void testCreate_MIF_MultiPoint() throws Exception{

    	WKTReader r = new WKTReader();
    	MultiPoint geom = (MultiPoint) r.read("MultiPoint((-158 -55),(113 10),(2 -130))");

        SimpleFeatureCollection features = (SimpleFeatureCollection) DataProvider.createFeatureCollection( geom, "EPSG:4326"); 
        File baseDir = createTestDir( "test-mif-multipoint");
        File tmpFile = File.createTempFile("test-mif", ".mif", baseDir);
        
        // create the tab file
        OGRDataStore ds1 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
       
		ds1.createSchema(features, false, new String[]{"FORMAT=MIF"});
        
        // read from tab
        OGRDataStore ds2 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);

        StringTokenizer tokens = new StringTokenizer(tmpFile.getName(), ".");
		String layerName = tokens.nextToken();
		
		testGeometry(layerName, ds2, Point.class, DataProvider.FEATURES_AMOUNT * 3);// each MultipPint feature has 3 points
		
    }

    /**
     * MIF format MultiPoint geometry. It generate Point features
     * 
     * Test for {@link OGRDataStore#createSchema(SimpleFeatureCollection, boolean, String[])}
     * @throws Exception
     */
	@Test
    public void testCreate_MIF_MultiLineString() throws Exception{

    	WKTReader r = new WKTReader();
    	MultiLineString geom = (MultiLineString) r.read("MultiLineString((-158 -55, 113 10),(2 -130, -158 -55))");

        SimpleFeatureCollection features = (SimpleFeatureCollection) DataProvider.createFeatureCollection( geom, "EPSG:4326"); 
        File baseDir = createTestDir("test-mif-multilinestring");
        File tmpFile = File.createTempFile("test-mif", ".mif", baseDir);
        
        // create the tab file
        OGRDataStore ds1 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
       
		ds1.createSchema(features, false, new String[]{"FORMAT=MIF"});
        
        // read from tab
        OGRDataStore ds2 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);

        StringTokenizer tokens = new StringTokenizer(tmpFile.getName(), ".");
		String layerName = tokens.nextToken();
		
		testGeometry(layerName, ds2, MultiLineString.class, DataProvider.FEATURES_AMOUNT);
		
    }

	/**
     * MIF format Point geometry
     * 
     * Test for {@link OGRDataStore#createSchema(SimpleFeatureCollection, boolean, String[])}
     * @throws Exception
     */
	@Test
    public void testCreate_MIF_LineString() throws Exception{

    	WKTReader r = new WKTReader();
    	LineString geom = (LineString) r.read("LineString(-158 -55, 113 10, 2 -130)");

        SimpleFeatureCollection features = (SimpleFeatureCollection) DataProvider.createFeatureCollection( geom, "EPSG:4326"); 
        File baseDir = createTestDir("test-mif-line");
        File tmpFile = File.createTempFile("test-mif", ".mif", baseDir);
        
        // create the tab file
        OGRDataStore ds1 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
       
		ds1.createSchema(features, false, new String[]{"FORMAT=MIF"});
        
        // read from tab
        OGRDataStore ds2 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);

        StringTokenizer tokens = new StringTokenizer(tmpFile.getName(), ".");
		String layerName = tokens.nextToken();
		
		testGeometry(layerName, ds2, LineString.class, DataProvider.FEATURES_AMOUNT);
		
    }

	/**
     * MIF alfanumeric types
     * 
     * Test for {@link OGRDataStore#createSchema(SimpleFeatureCollection, boolean, String[])}
     * @throws Exception
     */
	@Test
    public void testCreate_MIF_alfanuericTypes_Point() throws Exception{

    	WKTReader r = new WKTReader();
    	Point geom = (Point) r.read("Point(-158 -55)");

        SimpleFeatureCollection features = (SimpleFeatureCollection) DataProvider.createFeatureCollectionFullFieldTypes(geom, "EPSG_4326"); 
        File baseDir = createTestDir("test-mif-alfanumericTypes-point");
        File tmpFile = File.createTempFile("test-mif", ".mif", baseDir);
        
        // create the tab file
        OGRDataStore ds1 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
        
        ds1.createSchema(features, false, new String[]{"FORMAT=MIF"});
        
        // read from mif
        OGRDataStore ds2 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
        StringTokenizer tokens = new StringTokenizer(tmpFile.getName(), ".");
		String layerName = tokens.nextToken();

		testGeometry(layerName, ds2, Point.class, DataProvider.FEATURES_AMOUNT); 

		testSchema(ds2, features.getSchema()); 
    }
	/**
     * MIF alfanumeric types
     * 
     * Test for {@link OGRDataStore#createSchema(SimpleFeatureCollection, boolean, String[])}
     * @throws Exception
     */
	@Test
    public void testCreate_MIF_alfanuericTypes_MultiPoint() throws Exception{

    	WKTReader r = new WKTReader();
    	MultiPoint geom = (MultiPoint) r.read("MultiPoint((-158 -55),(113 10),(2 -130))");
    	

        SimpleFeatureCollection features = (SimpleFeatureCollection) DataProvider.createFeatureCollectionFullFieldTypes(geom, "EPSG_4326"); 
        File baseDir = createTestDir("test-mif-alfanumericTypes-multipoint");
        File tmpFile = File.createTempFile("test-mif", ".mif", baseDir);
        
        // create the tab file
        OGRDataStore ds1 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
        
        ds1.createSchema(features, false, new String[]{"FORMAT=MIF"});
        
        // read from mif
        OGRDataStore ds2 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
        StringTokenizer tokens = new StringTokenizer(tmpFile.getName(), ".");
		String layerName = tokens.nextToken();

		testGeometry(layerName, ds2, Point.class, DataProvider.FEATURES_AMOUNT * 3); 

		testSchema(ds2, features.getSchema()); 
    }

	private void testSchema(OGRDataStore dataStore, SimpleFeatureType expectedSchema) throws IOException {
		
		
		String[] typeNames = dataStore.getTypeNames();
		
		for (int i = 0; i < typeNames.length; i++) {
			FeatureType type = dataStore.getSchema(typeNames[i]);
			
			assertEquals( expectedSchema.getDescriptors().size(), type.getDescriptors().size());
			
			for (PropertyDescriptor descriptor: type.getDescriptors() ) {
				
				// exists the field and has equal type than the expected 
				String propName = descriptor.getName().toString();
				boolean found = false;
				for (PropertyDescriptor expDescriptor : expectedSchema.getDescriptors()) {
					
					String expPropName = expDescriptor.getName().toString();
					if(propName.equals(expPropName)){
						found =true;
						assertEquals("Expected property type is ", expDescriptor.getType().getClass(), descriptor.getType().getClass());
						break;
					}
				}
				assertTrue("Property not found " + propName, found);
			}
			
		}
	}

	/**
     * TAB alfanumeric types
     * 
     * Test for {@link OGRDataStore#createSchema(SimpleFeatureCollection, boolean, String[])}
     * @throws Exception
     */
	@Test
    public void testCreate_TAB_AlfanuericTypes() throws Exception{

    	WKTReader r = new WKTReader();
    	Point geom = (Point) r.read("Point(-158 -55)");

    	SimpleFeatureCollection features = (SimpleFeatureCollection) DataProvider.createFeatureCollectionFullFieldTypes(geom, "EPSG:4326"); 
        File tmpFile = createTestDir( "test-tab-alfanum-types");
        
        // create the tab file
        OGRDataStore ds = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
        
        ds.createSchema(features, true, new String[]{});
        
        // read from tab
        OGRDataStore ds2 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
        final String layerName = features.getSchema().getTypeName();

		testGeometry(layerName, ds2, Point.class, DataProvider.FEATURES_AMOUNT);  

		testSchema(ds2, features.getSchema());
    }
	
	@Test
    public void testCreate_TAB_Point() throws Exception{

    	WKTReader r = new WKTReader();
    	Point geom = (Point) r.read("Point(-158 -55)");

        SimpleFeatureCollection features = (SimpleFeatureCollection) DataProvider.createFeatureCollection(geom, "EPSG:4326");
        File tmpFile = createTestDir("test-tab-point");
        
        // create the tab file
        OGRDataStore ds = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
        
        ds.createSchema(features, true, new String[]{});
        
        // read from tab
        OGRDataStore ds2 = new OGRDataStore(tmpFile.getAbsolutePath(), "MapInfo File", null);
        final String layerName = features.getSchema().getTypeName();
		testGeometry(layerName, ds2, Point.class, DataProvider.FEATURES_AMOUNT); 

    }


	/**
	 * Test that the data source has the geometry type expected
	 * @param layerName
	 * @param dataSource
	 * @param expectedGeometryClass
	 * @throws IOException
	 */
    private void testGeometry(
    		final String layerName, 
    		final OGRDataStore dataSource,
			final Class<?> expectedGeometryClass,
			final int expectedFeatures) 
					throws IOException {

    	Query query = new Query(layerName, Filter.INCLUDE);
    	FeatureReader<?, ?> reader = dataSource.getFeatureReader(query , Transaction.AUTO_COMMIT);
		int i = 0;
		while( reader.hasNext() ){
			SimpleFeature f = (SimpleFeature)reader.next();
			assertTrue( f.getDefaultGeometry().getClass().equals(expectedGeometryClass));
			assertNotNull(f.getType().getCoordinateReferenceSystem());
			
			i++;
		}
		assertEquals(expectedFeatures,i++);
	}

    private File createTestDir(final String testDir){
		
		File baseDir = new File(".", testDir);
		if(baseDir.exists()){
			
			delete(baseDir);
		
		}
		baseDir.mkdir();
		
		this.tmpFiles.add(baseDir);
		
		return baseDir;
		
	}
	
    private void delete(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
        }
        file.delete();
    }	
	
	
		
}
