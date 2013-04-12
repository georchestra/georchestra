/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.geotools.referencing.CRS;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit Test for {@link UpLoadFileManagement}
 * 
 * @author Mauricio Pazos
 *
 */
public class UpLoadFileManagementTest {

	/**
	 * Test method for {@link mapfishapp.ws.upload.UpLoadFileManagement#getFeatureCollectionAsJSON()}.
	 * @throws IOException 
	 */
	@Test
	public void testSHPAsJSON() throws Exception {
		
		String fileName = "points-4326.shp";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName);
	}
	
	@Test 
	public void testKMLAsJSON() throws Exception {

		String fileName = "regions.kml";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName);
	}
	
	@Ignore // FIXME
	public void testGMLAsJSON() throws Exception {
		
		String fileName = "border.gml";
		String fullName = makeFullName(fileName);

		testGetGeofileToJSON(fullName);
		
	}

	@Ignore // FIXME
	public void testGPXAsJSON() throws Exception {
		
		String fileName = "wp.gpx";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName);
	}
	

	@Test 
	public void testMIFAsJSON() throws Exception {
		
		String fileName = "pigma_regions_POLYGON.mif";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName);
	}

	private void testGetGeofileToJSON(final String fileName) throws Exception{
		
		FileDescriptor fd = new FileDescriptor(fileName);
		fd.listOfFiles.add(fileName);
		fd.listOfExtensions.add(FilenameUtils.getExtension(fileName));

		UpLoadFileManagement fm = new UpLoadFileManagement();
		fm.setWorkDirectory(FilenameUtils.getFullPath(fileName));
		fm.setFileDescriptor(fd);
		
		
		String jsonFeatures = fm.getFeatureCollectionAsJSON(CRS.decode("EPSG:4326"));
		
		Assert.assertNotNull(jsonFeatures); 
	}
	

	/**
	 * Returns path+fileName
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private String makeFullName(String fileName) throws Exception{
		
		URL url= this.getClass().getResource(fileName);
		
		String fullFileName = url.toURI().getPath();

		return fullFileName;
	}
}
