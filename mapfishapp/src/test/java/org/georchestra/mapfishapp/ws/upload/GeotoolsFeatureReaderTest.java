/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;

import org.geotools.data.DefaultFeatureResults;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Unit test for {@link GeotoolsFeatureReader}
 * 
 * @author Mauricio Pazos
 *
 */
public class GeotoolsFeatureReaderTest {

	private AbstractFeatureGeoFileReader reader;

	@Before
	public void setup() {
		reader = new AbstractFeatureGeoFileReader();
		reader.readerImpl = new GeotoolsFeatureReader();
	}
	
	public GeotoolsFeatureReaderTest() {
		System.setProperty("org.geotools.referencing.forceXY", "true");
	}
	
	@Test
	public void testSHPFormat() throws Exception {
		
		String fullName = makeFullName("points-4326.shp");
		File file = new File(fullName);
		
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.shp);
		
		assertFeatureCollection(fc,  2, 4326);
	}
	
	@Test
	public void testSHPCoordinatesEPSG4326() throws Exception {
		
		String fullName = makeFullName("shp_4326_accidents.shp");
		File file = new File(fullName);
		
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.shp);
		
		double x = -2.265330624649336;
		double y = 48.421434814828025;
		int id = 205;
		int crs = 4326;
		assertCoordinatedOrder(fc, id, x,  y, crs);
	}

	@Test
	public void testSHPCoordinatesEPSG3857() throws Exception {
		
		String fullName = makeFullName("shp_4326_accidents.shp");
		File file = new File(fullName);
		
		final int CRS_ID = 3857;
		CoordinateReferenceSystem crs = CRS.decode("EPSG:"+CRS_ID);
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.shp, crs);
		
		
		double x = -252175.451614371791948; 
		double y = 6177255.152005254290998; 
		
		int id = 205;
		assertCoordinatedOrder(fc, id, x,  y, CRS_ID);
	}

	/**
	 * Transform the features from 4326 to 2154
	 * @throws Exception
	 */
	@Test
	public void testSHPFormatTo2154() throws Exception {
		
		final int epsgCode = 2154;
		
		String fullName = makeFullName("points-4326.shp");
		File file = new File(fullName);
		
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.shp, CRS.decode("EPSG:"+ epsgCode) );
		
		assertFeatureCollection(fc,  2, epsgCode);
	}
	
	private void assertFeatureCollectionFromGML(SimpleFeatureCollection fc, final int countExpected, final int expectedEPSG) throws Exception {
		
		CoordinateReferenceSystem schemaCRS = fc.getSchema().getCoordinateReferenceSystem();
		
		assertNotNull(schemaCRS);
		
 		assertTrue( expectedEPSG ==CRS.lookupEpsgCode( schemaCRS, true) );
		
		SimpleFeatureIterator iter = fc.features();
		try{
			int i= 0;
			while(iter.hasNext()){
				
				SimpleFeature f = iter.next();
				
				Geometry geom = (Geometry) f.getDefaultGeometry();
				assert(geom.getSRID() == expectedEPSG);
				
				
				i++;
			}
			assertTrue(countExpected == i);
			
		} finally {
			iter.close();
		}
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
				
				assertFeatureTypeCRS(f.getFeatureType(), expectedEPSG);
				
				i++;
			}
			assertTrue(countExpected == i);
			
		} finally {
			iter.close();
		}
	}

	private void assertFeatureTypeCRS(final SimpleFeatureType t, final int expectedEPSG) throws Exception {
		// from qgis
		CoordinateReferenceSystem crs = t.getCoordinateReferenceSystem();
 		assertTrue( expectedEPSG ==CRS.lookupEpsgCode( crs, true) );
	}
	
	private void assertGeometryCRS(final GeometryDescriptor geom, final int expectedEPSG) throws FactoryException{
		
		CoordinateReferenceSystem crs = geom.getType().getCoordinateReferenceSystem();
		Integer code = CRS.lookupEpsgCode(crs, true);
		assertTrue(code == expectedEPSG);
		
	}
	
	
	@Test
	public void testMIFFormat() throws Exception {

		String fullName = makeFullName("pigma_regions_POLYGON.mif");
		File file = new File(fullName);
		
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.mif);
		
		assertFeatureCollection(fc,  93, 4326);
	}
	
	
	@Test
	public void testMIFFormatTo2154() throws Exception {
		
		final int epsgCode = 2154;

		String fullName = makeFullName("pigma_regions_POLYGON.mif");
		File file = new File(fullName);
		
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.mif, CRS.decode("EPSG:"+ epsgCode));
		
		assertFeatureCollection(fc,  93, epsgCode);
	}

	@Test 
	public void testGML2Format() throws Exception {

		String fullName = makeFullName("border.gml");
		File file = new File(fullName);
		
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.gml);
		
		assertFeatureCollectionFromGML(fc,  50, 4326);
	}
	
	/**
	 * Tests gml projected no reprojected
	 * @throws Exception
	 */
	@Test
	public void testGMLCoordinates() throws Exception {
		
		String fullName = makeFullName("gml_4326_accidents.gml");
		File file = new File(fullName);
		
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.gml, null);
		
		double x = -2.265330624649336; 
		double y = 48.421434814828025; 
		int id = 205;
		assertCoordinatedOrder(fc, id, x,  y);
	}
	
	
	/**
	 * Tests gml projected using EPSG:4326
	 * @throws Exception
	 */
	@Test
	public void testGMLCoordinatesTransformToEPSG4326() throws Exception {
		
		String fullName = makeFullName("gml_4326_accidents.gml");
		File file = new File(fullName);
		
		final int CRS_ID = 4326;
		CoordinateReferenceSystem crs = CRS.decode("EPSG:"+CRS_ID);
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.gml, crs);
		
		double x = -2.265330624649336; 
		double y = 48.421434814828025; 
		int id = 205;
		assertCoordinatedOrder(fc, id, x,  y, CRS_ID);
	}
	
	/**
	 * Tests gml projected using EPSG:3857
	 * @throws Exception
	 */
	@Test
	public void testGMLCoordinatesTransformToEPSG3857() throws Exception {
		
		String fullName = makeFullName("gml_4326_accidents.gml");
		File file = new File(fullName);
		
		final int CRS_ID = 3857;
		CoordinateReferenceSystem crs = CRS.decode("EPSG:"+CRS_ID);
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.gml, crs);
		
		double x = -252175.451614371791948; 
		double y = 6177255.152005254290998; 
		
		int id = 205;
		assertCoordinatedOrder(fc, id, x,  y, CRS_ID);
	}

	/**
	 * Tests gml projected using EPSG:2154
	 * @throws Exception
	 */
	@Test
	public void testGMLCoordinatesTransformToEPSG2154() throws Exception {
		
		String fullName = makeFullName("gml_4326_accidents.gml");
		File file = new File(fullName);
		
		final int CRS_ID = 2154;
		CoordinateReferenceSystem crs = CRS.decode("EPSG:"+CRS_ID);
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.gml, crs);
		
		double x = 310725.620248031220399;
		double y = 6826444.585061171092093; 
		
		int id = 205;
		assertCoordinatedOrder(fc, id, x,  y, CRS_ID);
	}
	

	private void assertCoordinatedOrder(SimpleFeatureCollection fc, int id,
			double x, double y) throws Exception{
		assertCoordinatedOrder(fc, id, x,  y, 0);
		
	}

	/**
	 * Checks that the coordinates are in the expected order (x,y).
	 * 
	 * @param fc
	 * @param requiredFeatureID feature to search
	 * @param xExpected
	 * @param yExpected
	 * @param expectedEPSG
	 */
	private void assertCoordinatedOrder(SimpleFeatureCollection fc, final int requiredFeatureID, final double xExpected, final double yExpected, final int expectedEPSG) 
			throws Exception{
		
		CoordinateReferenceSystem schemaCRS = fc.getSchema().getCoordinateReferenceSystem();
		assertNotNull(schemaCRS);
		if(expectedEPSG != 0l){
			int code =CRS.lookupEpsgCode(schemaCRS, true);
			assertTrue( expectedEPSG == code);
		} 

		double x;
		double y;
		SimpleFeatureIterator iter = fc.features();
		try{
			while(iter.hasNext()){
				
				SimpleFeature f = iter.next();
				
				int id = Integer.valueOf(f.getAttribute("id").toString());
				if(id == requiredFeatureID ){
					Geometry geom = (Geometry) f.getDefaultGeometry();


					Coordinate[] coordinates = geom.getCoordinates();
					int significantDigit = geom.getPrecisionModel().getMaximumSignificantDigits();
					
					assertEquals(xExpected, coordinates[0].x , significantDigit);
					assertEquals(yExpected, coordinates[0].y, significantDigit);
					
					assertTrue(true);
					break;
				}
				fail("the feature id: " + requiredFeatureID + " wan't found" );
			}
			
		} finally {
			iter.close();
		}
	}
	
	@Test
	public void testGML2FormatTo2154() throws Exception {

		String fullName = makeFullName("border.gml");
		File file = new File(fullName);
		
		final int epsgCode = 2154;
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.gml, CRS.decode("EPSG:"+ epsgCode));
		
		assertFeatureCollectionFromGML(fc,  50, epsgCode);
	}
	
	
	

	@Test 
	public void testGML3Format() throws Exception {

		String fullName = makeFullName("states-3.gml");
		File file = new File(fullName);
		
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.gml);
		
		Assert.assertNotNull(fc);
		Assert.assertTrue(!fc.isEmpty());
	}
	
	@Test
	public void testKMLFormat() throws Exception {

		String fullName = makeFullName("regions.kml");
		File file = new File(fullName);
		
		SimpleFeatureCollection featureCollection = reader.getFeatureCollection(file, FileFormat.kml);
		
		Assert.assertTrue(!featureCollection.isEmpty());
	}
	
	@Test
	public void testKMLExtendedData() throws Exception {

		String fullName = makeFullName("kml_4326_accidents.kml");
		File file = new File(fullName);
		
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.kml);
		
		Assert.assertTrue(!fc.isEmpty());
		
		SimpleFeature f = fc.features().next();
		
		Assert.assertNotNull( f.getProperty("id") );
		Assert.assertNotNull( f.getProperty("date") );
		Assert.assertNotNull( f.getProperty("plage_hora") );
		Assert.assertNotNull( f.getProperty("jour_nuit") );
		Assert.assertNotNull( f.getProperty("meteo") );
		Assert.assertNotNull( f.getProperty("voie_type") );
		Assert.assertNotNull( f.getProperty("milieu") );
		Assert.assertNotNull( f.getProperty("tues_nb") );
		Assert.assertNotNull( f.getProperty("tues_18_24") );
		Assert.assertNotNull( f.getProperty("tues_moto_") );
		Assert.assertNotNull( f.getProperty("tues_pieto") );
		Assert.assertNotNull( f.getProperty("tues_velo_") );
		Assert.assertNotNull( f.getProperty("vehicules_") );
		Assert.assertNotNull( f.getProperty("vehicules_") );
		Assert.assertNotNull( f.getProperty("commune") );
		Assert.assertNotNull( f.getProperty("departemen") );
		Assert.assertNotNull( f.getProperty("commentair") );
		Assert.assertNotNull( f.getProperty("consolide") );
		Assert.assertNotNull( f.getProperty("anciennete") );
		Assert.assertNotNull( f.getProperty("f_mois") );
		Assert.assertNotNull( f.getProperty("f_annee") );
		
	}

	/**
	 * Returns path+fileName
	 * @param fileName
	 * @return path+fileName
	 * @throws Exception
	 */
	private String makeFullName(String fileName) throws Exception{
		
		URL url= this.getClass().getResource(fileName);
		
		String fullFileName = url.toURI().getPath();

		return fullFileName;
	}

	/**
	 * Test method for {@link org.georchestra.mapfishapp.ws.upload.AbstractFeatureGeoFileReader#getFormatList()}.
	 * @throws IOException 
	 */
	@Test
	public void testGetFormatList() throws IOException {

		
		EnumSet<FileFormat> gtRequiredFormats = EnumSet.of(FileFormat.shp, FileFormat.mif, FileFormat.gml, FileFormat.kml);
		
		FileFormat[] formats = reader.getFormatList();
		for (int i = 0; i < formats.length; i++) {

			assertTrue( gtRequiredFormats.contains(formats[i]) );
			
		}
		
	}

}
