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

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author Mauricio Pazos
 *
 */
public class GeotoolsFeatureReaderTest {

	private FeatureFileReader reader = new FeatureFileReader(new GeotoolsFeatureReader());


	@Test
	public void testSwitchToGeotoolsReaderImpl() throws Exception {
		
		FeatureFileReader reader = new FeatureFileReader(new MockReader());

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
	public void testMIFFormatReporjected() throws Exception {
		
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
	
	@Test
	public void testGML2FormatTo2154() throws Exception {

		String fullName = makeFullName("border.gml");
		File file = new File(fullName);
		
		final int epsgCode = 2154;
		SimpleFeatureCollection fc = reader.getFeatureCollection(file, FileFormat.gml, CRS.decode("EPSG:"+ epsgCode));
		
		assertFeatureCollectionFromGML(fc,  50, epsgCode);
	}
	

	@Ignore 
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
	 * Test method for {@link org.georchestra.mapfishapp.ws.upload.FeatureFileReader#getFormatList()}.
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
