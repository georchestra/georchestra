/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Point;

/**
 * Unit Test for {@link UpLoadFileManagement}
 * 
 * Test the geotools implementation {@link GeotoolsFeatureReader}.  
 * 
 * @author Mauricio Pazos
 *
 */
public class UpLoadFileManagementGTImplTest {

	/**
	 * Test method for {@link mapfishapp.ws.upload.UpLoadFileManagement#getFeatureCollectionAsJSON()}.
	 * @throws IOException 
	 */
	@Ignore
	public void testSHPAsJSON() throws Exception {
		
		String fileName = "points-4326.shp";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName);
	}
	
	@Ignore
	public void testSHPAsJSONReporjectedTo2154() throws Exception {
		
		String fileName = "points-4326.shp";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName,"EPSG:2154");
	}
	
	/** 
	 * Tests the coordinates order.
	 * 
	 * @throws Exception
	 */
	@Ignore
	public void testSHPCoordinates() throws Exception {
		
		String fileName = "shp_4326_accidents.shp";
		String fullName = makeFullName(fileName);
		
		String json = testGetGeofileToJSON(fullName, "EPSG:4326");
		
		assertCoordinateContains(-2.265330624649336, 48.421434814828025, json );
	}

	
	@Ignore 
	public void testKMLAsJSON() throws Exception {

		String fileName = "regions.kml";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName);
	}
	
	/** 
	 * Tests the coordinates order.
	 * 
	 * @throws Exception
	 */
	@Ignore
	public void testKMLCoordinates() throws Exception {
		
		String fileName = "kml_4326_accidents.kml";
		String fullName = makeFullName(fileName);
		
		String json = testGetGeofileToJSON(fullName, "EPSG:4326");
		
		assertCoordinateContains(-2.265330624649336, 48.421434814828025, json );
	}
	
	
	@Ignore
	public void testGMLAsJSON() throws Exception {
		
		String fileName = "border.gml";
		String fullName = makeFullName(fileName);

		testGetGeofileToJSON(fullName);
		
	}


	@Ignore 
	public void testMIFAsJSON() throws Exception {
		
		String fileName = "pigma_regions_POLYGON.mif";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName);
	}
	
	/** 
	 * Tests the coordinates order.
	 * 
	 * @throws Exception
	 */
	@Ignore
	public void testMIFCoordinates() throws Exception {
		
		String fileName = "mif_4326_accidents.mif";
		String fullName = makeFullName(fileName);
		
		String json = testGetGeofileToJSON(fullName, "EPSG:4326");
		
		assertCoordinateContains(-2.265330624649336, 48.421434814828025, json );
	}
	
	
	@Ignore 
	public void testMIFAsJSONReprojectedTo2154() throws Exception {
		
		String fileName = "pigma_regions_POLYGON.mif";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName, "EPSG:2154" );
	}

	
	/** 
	 * Tests the coordinates order.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGMLCoordinates() throws Exception {
		
		String fileName = "gml_4326_accidents.gml";
		String fullName = makeFullName(fileName);
		
		String json = testGetGeofileToJSON(fullName, "EPSG:4326");
		
		assertCoordinateContains( -2.265330624649336, 48.421434814828025, json );
	}

	/**
	 * Assert that the feature in json syntax contains its coordinate in the order x, y.
	 * 
	 * @param x
	 * @param y
	 * @param json
	 * @throws Exception
	 */
	protected void assertCoordinateContains(final double x, final double y, final String json) throws Exception{
		
		FeatureJSON featureJSON = new FeatureJSON();
		
		FeatureIterator<SimpleFeature> iter = featureJSON.streamFeatureCollection(json);
		
		assertTrue(iter.hasNext());

		SimpleFeature f = iter.next();

		Point geom = (Point) f.getDefaultGeometry();

		assertEquals(x, geom.getCoordinate().x, 10e-15);

		assertEquals(y, geom.getCoordinate().y, 10e-15);
	}
	/**
	 * Sets the default crs as epsg:4326, before test.
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	protected void testGetGeofileToJSON(final String fileName) throws Exception{
	
		testGetGeofileToJSON(fileName, "EPSG:4326");
		
	}
	
	
	protected String testGetGeofileToJSON(final String fileName, final String epsg) throws Exception{
		
		String jsonFeatures = getFeatureCollectionAsJSON(fileName, epsg);
		assertNotNull(jsonFeatures); 
		
		return jsonFeatures;
	}
	
	/**
	 * Sets the bridge {@link AbstractFeatureGeoFileReader} with the {@link GeotoolsFeatureReader} implementation, then
	 * retrieves the file as Json collection.
	 * 
	 * @param fileName
	 * @param epsg
	 * @return
	 * @throws Exception
	 */
	protected String getFeatureCollectionAsJSON(final String fileName, final String epsg) throws Exception{
		
		FileDescriptor fd = new FileDescriptor(fileName);
		fd.listOfFiles.add(fileName);
		fd.listOfExtensions.add(FilenameUtils.getExtension(fileName));

		UpLoadFileManagement fm = create();
		
		fm.setWorkDirectory(FilenameUtils.getFullPath(fileName));
		fm.setFileDescriptor(fd);
		
		return  fm.getFeatureCollectionAsJSON(CRS.decode(epsg));
	}
	
	/**
	 * @return UpLoadFileManagement set with geotools implementation
	 */
	protected UpLoadFileManagement create(){
		return UpLoadFileManagement.create(UpLoadFileManagement.Implementation.geotools);
	}
	

	/**
	 * Returns path+fileName
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	protected String makeFullName(String fileName) throws Exception{
		
		URL url= this.getClass().getResource(fileName);
		
		String fullFileName = url.toURI().getPath();

		return fullFileName;
	}
}
