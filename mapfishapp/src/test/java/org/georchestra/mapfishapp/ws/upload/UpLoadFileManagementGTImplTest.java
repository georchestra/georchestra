/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

	public UpLoadFileManagementGTImplTest() {
		System.setProperty("org.geotools.referencing.forceXY", "true");
	}
	/**
	 * Test method for {@link mapfishapp.ws.upload.UpLoadFileManagement#getFeatureCollectionAsJSON()}.
	 * @throws IOException 
	 */
	@Test
	public void testSHPAsJSON() throws Exception {
		
		String fileName = "points-4326.shp";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName, null);
	}
	
	@Test
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
	@Test
	public void testSHPCoordinatesEPSG4326() throws Exception {
		
		String fileName = "shp_4326_accidents.shp";
		String fullName = makeFullName(fileName);
		
		String json = testGetGeofileToJSON(fullName, "EPSG:4326");
		
		assertCoordinateContains(-2.265330624649336, 48.421434814828025, json );
	}

	/** 
	 * Tests the coordinates order.
	 * <p>
	 * The retrieved features aren't reprojected. They will be in the base srs.
	 * </p>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSHPCoordinatesNoReprojected() throws Exception {
		
		String fileName = "shp_4326_accidents.shp";
		String fullName = makeFullName(fileName);
		
		String json = testGetGeofileToJSON(fullName, null);
		
		assertCoordinateContains(-2.265330624649336, 48.421434814828025, json );
	}
	
	@Test
	public void testKMLAsJSON() throws Exception {

		String fileName = "regions.kml";
		String fullName = makeFullName(fileName);
		
		String regions = testGetGeofileToJSON(fullName, null);
		
		JSONObject list = new JSONObject(regions);
		JSONArray jsonArray = list.getJSONArray("features");
		JSONObject reg = jsonArray.getJSONObject(0);
		String id = reg.getString("id");
		
		JSONObject properties = reg.getJSONObject("properties");// FIXME REMOVE
		String  p1 = getJsonFieldValue(properties,  "open");
		String  p2 = getJsonFieldValue(properties,  "visibility");
		String  name = getJsonFieldValue(properties,  "name");
		String  desc = getJsonFieldValue(properties,  "description");
		String  osm_id = getJsonFieldValue(properties,  "osm_id");
		String  geom = getJsonFieldValue(properties,  "geometry");
		
	}
	
	@Test
	public void testKMLExtendedData() throws Exception {

		String fileName = "kml_4326_accidents.kml";
		String fullName = makeFullName(fileName);
		
		String regions = testGetGeofileToJSON(fullName, null);
		
		JSONObject list = new JSONObject(regions);
		JSONArray jsonArray = list.getJSONArray("features");
		JSONObject reg = jsonArray.getJSONObject(0);
		String id = reg.getString("id");
		
		JSONObject properties = reg.getJSONObject("properties");
		String  p1 = getJsonFieldValue(properties,  "id");
		String  p2 = getJsonFieldValue(properties,  "date");
		String  p3 = getJsonFieldValue(properties,  "plage_hora");
		String  p4 = getJsonFieldValue(properties,  "jour_nuit");
		String  p5 = getJsonFieldValue(properties,  "meteo");
		String  p6 = getJsonFieldValue(properties,  "voie_type");
		String  p7 = getJsonFieldValue(properties,  "milieu");
		String  p8 = getJsonFieldValue(properties,  "voie_type");
		String  p9 = getJsonFieldValue(properties,  "tues_nb");
		String  p10 = getJsonFieldValue(properties,  "milieu");
		String  p11 = getJsonFieldValue(properties,  "tues_18_24");
		String  p12 = getJsonFieldValue(properties,  "tues_moto_");
		String  p13 = getJsonFieldValue(properties,  "tues_pieto");
		String  p14 = getJsonFieldValue(properties,  "tues_velo_");
		String  p15 = getJsonFieldValue(properties,  "vehicules_");
		String  p16 = getJsonFieldValue(properties,  "pl_impliqu");
		String  p17 = getJsonFieldValue(properties,  "commune");
		String  p18 = getJsonFieldValue(properties,  "departemen");
		String  p19 = getJsonFieldValue(properties,  "commentair");
		String  p20 = getJsonFieldValue(properties,  "consolide");
		String  p21 = getJsonFieldValue(properties,  "anciennete");
		String  p22 = getJsonFieldValue(properties,  "f_mois");
		String  p23 = getJsonFieldValue(properties,  "f_annee");
		
	}
	
	private String getJsonFieldValue(JSONObject properties, String field) {
		 
		String value;
		try {
			value = properties.getString(field);
		} catch (JSONException e) {
			value = null;
		} 
		 
		 return value;
	}
	/** 
	 * Tests the coordinates order.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testKMLCoordinatesEPSG4326() throws Exception {
		
		String fileName = "kml_4326_accidents.kml";
		String fullName = makeFullName(fileName);
		
		String json = testGetGeofileToJSON(fullName, "EPSG:4326");
		
		assertCoordinateContains(-2.265330624649336, 48.421434814828025, json );
	}
	
	
	/**
	 * Read features no reprojected
	 * @throws Exception
	 */
	@Test
	public void testGMLAsJSON() throws Exception {
		
		String fileName = "border.gml";
		String fullName = makeFullName(fileName);

		testGetGeofileToJSON(fullName, null);
		
	}


	@Test 
	public void testMIFAsJSON() throws Exception {
		
		String fileName = "pigma_regions_POLYGON.mif";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName, null);
	}
	
	/** 
	 * Tests the coordinates order.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMIFCoordinatesEPSG4326() throws Exception {
		
		String fileName = "mif_4326_accidents.mif";
		String fullName = makeFullName(fileName);
		
		String json = testGetGeofileToJSON(fullName, "EPSG:4326");
		
		assertCoordinateContains(-2.265330624649336, 48.421434814828025, json );
	}
	
	
	@Test 
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
	public void testGMLCoordinatesEPSG4326() throws Exception {
		
		String fileName = "gml_4326_accidents.gml";
		String fullName = makeFullName(fileName);
		
		String json = testGetGeofileToJSON(fullName, "EPSG:4326");
		
		assertCoordinateContains( -2.265330624649336, 48.421434814828025, json );
	}

	/** 
	 * Tests the coordinates order. The input layer is projected in epsg:4326,
	 * the result is reprojected to epsg:3857.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGMLCoordinatesFrom4326to3857() throws Exception {
		
		String fileName = "gml_4326_accidents.gml";
		String fullName = makeFullName(fileName);
		
		String json = testGetGeofileToJSON(fullName, "EPSG:3857");
		
		assertCoordinateContains( -252175.451614371791948, 6177255.152005254290998, json );
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
		
		assertTrue(iter.hasNext()); // the test data only contains one feature

		SimpleFeature f = iter.next();

		Point geom = (Point) f.getDefaultGeometry();

		assertEquals(x, geom.getCoordinate().x, 10e-14);

		assertEquals(y, geom.getCoordinate().y, 10e-14);
	}
	
	
	protected String testGetGeofileToJSON(final String fileName, final String epsg) throws Exception{
		
		assertTrue(fileName.length() >0 );
		
		String jsonFeatures = getFeatureCollectionAsJSON(fileName, epsg);
		assertNotNull(jsonFeatures); 
		
		return jsonFeatures;
	}
	
	/**
	 * Sets the bridge {@link AbstractFeatureGeoFileReader} with the {@link GeotoolsFeatureReader} implementation, then
	 * retrieves the file as Json collection. The return value will be null if the file is empty.
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
		
		StringWriter out = new StringWriter();
		if(epsg != null){
			fm.writeFeatureCollectionAsJSON(out, CRS.decode(epsg));
		} else{
			fm.writeFeatureCollectionAsJSON(out, null);
		}
		return  out.toString();
	}
	
	/**
	 * @return UpLoadFileManagement set with geotools implementation
	 */
	protected UpLoadFileManagement create() throws IOException{
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
