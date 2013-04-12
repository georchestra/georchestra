/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mauricio Pazos
 *
 */
public class GeotoolsFeatureReaderTest {

	private FeatureFileReader reader = new FeatureFileReader(new GeotoolsFeatureReader());

	
	@Test
	public void testSHPFormat() throws Exception {
		

		String fullName = makeFullName("points-4326.shp");
		File file = new File(fullName);
		
		SimpleFeatureCollection featureCollection = reader.getFeatureCollection(file, FileFormat.shp);
		
		Assert.assertTrue(!featureCollection.isEmpty());
	}
	
	@Test
	public void testMIFFormat() throws Exception {
		

		String fullName = makeFullName("pigma_regions_POLYGON.mif");
		File file = new File(fullName);
		
		SimpleFeatureCollection featureCollection = reader.getFeatureCollection(file, FileFormat.mif);
		
		Assert.assertTrue(!featureCollection.isEmpty());
	}

	@Test
	public void testGMLFormat() throws Exception {
		

		String fullName = makeFullName("border.gml");
		File file = new File(fullName);
		
		SimpleFeatureCollection featureCollection = reader.getFeatureCollection(file, FileFormat.gml);
		
		Assert.assertTrue(!featureCollection.isEmpty());
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
