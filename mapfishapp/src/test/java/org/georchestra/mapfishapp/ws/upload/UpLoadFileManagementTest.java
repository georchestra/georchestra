/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
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
	public void testSHPAsJSON() throws IOException {
		
		String directory = getWorkingDirectory();
		String fileName = directory + "points-4326.shp";
		
		testGetGeofileToJSON(directory, fileName);
	}
	
	@Test 
	public void testKMLAsJSON() throws IOException {
		
		String directory = getWorkingDirectory();
		String fileName = directory + "regions.kml";
		
		testGetGeofileToJSON(directory, fileName);
	}
	@Test 
	public void testGMLAsJSON() throws IOException {
		
		String directory = getWorkingDirectory();
		String fileName = directory + "regions.gml";
		
		testGetGeofileToJSON(directory, fileName);
	}

	@Test 
	public void testGPXAsJSON() throws IOException {
		
		String directory = getWorkingDirectory();
		String fileName = directory + "wp.gpx";
		
		testGetGeofileToJSON(directory, fileName);
	}
	
	@Test 
	public void testTABAsJSON() throws IOException {
		
		String directory = getWorkingDirectory();
		String fileName = directory + "pigma_regions_POLYGON.tab";
		
		testGetGeofileToJSON(directory, fileName);
	}

	@Test 
	public void testMIFAsJSON() throws IOException {
		
		String directory = getWorkingDirectory();
		String fileName = directory + "pigma_regions_POLYGON.mif";
		
		testGetGeofileToJSON(directory, fileName);
	}

	private void testGetGeofileToJSON(final String directory, final String fileName) throws IOException{
		
		FileDescriptor fd = new FileDescriptor(fileName);
		fd.listOfFiles.add(fileName);
		fd.listOfExtensions.add(FilenameUtils.getExtension(fileName));

		UpLoadFileManagement fm = new UpLoadFileManagement();
		fm.setWorkDirectory(directory);
		fm.setFileDescriptor(fd);
		
		
		String jsonFeatures = fm.getFeatureCollectionAsJSON( );
		
		Assert.assertNotNull(jsonFeatures); 
	}
	
	
	private String getWorkingDirectory() throws IOException{
		String current = new File( "." ).getCanonicalPath();
		String directory = current + "/src/test/resources/org/georchestra/mapfishapp/ws/upload/";

		return directory;
	}
	

}
