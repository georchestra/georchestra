/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.junit.Assert;
import org.junit.Ignore;
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

	private AbstractFeatureGeoFileReader reader = new AbstractFeatureGeoFileReader(new GeotoolsFeatureReader());


	/**
	 * Tests that the geotools implementation is used when the current reader cannot read the file format.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSwitchToGeotoolsReaderImpl() throws Exception {
		
		AbstractFeatureGeoFileReader reader = new AbstractFeatureGeoFileReader(new MockReader());

		String fullName = makeFullName("points-4326.shp");
		File file = new File(fullName);
		
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.shp);
		
		assertFeatureCollection(fc,  2, 4326);

		assertTrue( reader.readerImpl instanceof GeotoolsFeatureReader );
	}
	
	@Test
	public void testSHPFormat() throws Exception {
		
		String fullName = makeFullName("points-4326.shp");
		File file = new File(fullName);
		
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.shp);
		
		assertFeatureCollection(fc,  2, 4326);
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

		CoordinateReferenceSystem crs = t.getCoordinateReferenceSystem();
 		assertTrue( expectedEPSG ==CRS.lookupEpsgCode( crs, true) );
	}
	
	private void assertGeometryCRS(final GeometryDescriptor geom, final int expectedEPSG) throws FactoryException{
		
		CoordinateReferenceSystem crs = geom.getType().getCoordinateReferenceSystem();
		Integer code = CRS.lookupEpsgCode(crs, true);
		assertTrue(code == expectedEPSG);
		
//		assertTrue( expectedEPSG == geom.getGeometryType().;
//		assertTrue( expectedEPSG == geom.getSRID());
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
	 * This test checks that there is not inversion of coordinates (long/lat) 
	 */
	@Test
	public void testGMLCoordinates() throws Exception {
		
		String fullName = makeFullName("gml_4326_accidents.gml");
		File file = new File(fullName);
		
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.gml);
		
		double x = -4.566578;
		double y = 48.585601;
		int id = 141;
		int crs = 4326;
		assertCoordinatedOrder(fc, id, x,  y, crs);
		
		
	}
	
	/**
	 * Checks that the coordinates are in the expected order (x,y).
	 * 
	 * @param fc
	 * @param requiredFeatureID feature to search
	 * @param xCoordExpected
	 * @param yCoordExpected
	 * @param expectedEPSG
	 */
	private void assertCoordinatedOrder(SimpleFeatureCollection fc, final int requiredFeatureID, final double xCoordExpected, final double yCoordExpected, final int expectedEPSG) {
		
		CoordinateReferenceSystem schemaCRS = fc.getSchema().getCoordinateReferenceSystem();
		
		assertNotNull(schemaCRS);
		
		SimpleFeatureIterator iter = fc.features();
		boolean OK = false;
		try{
			while(iter.hasNext()){
				
				SimpleFeature f = iter.next();
				
				int id = Integer.valueOf(f.getAttribute("id").toString());
				if(id == requiredFeatureID ){
					Geometry geom = (Geometry) f.getDefaultGeometry();
					assert(geom.getSRID() == expectedEPSG);

					Coordinate[] coordinates = geom.getCoordinates();
					
					assertTrue(coordinates[0].x == xCoordExpected);
					assertTrue(coordinates[0].y == yCoordExpected);
					
					OK = true;

					break;
				}
			}
			
		} finally {
			iter.close();
			assertTrue(OK);
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

			Assert.assertTrue( gtRequiredFormats.contains(formats[i]) );
			
		}
		
	}

}
