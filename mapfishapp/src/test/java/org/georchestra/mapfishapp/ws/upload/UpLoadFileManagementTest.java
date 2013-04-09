/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
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
		String directory = getWorkingDirectory(fileName);
		String fullName = directory + fileName;
		
		testGetGeofileToJSON(directory, fullName);
	}
	
	@Ignore 
	public void testKMLAsJSON() throws Exception {

		String fileName = "regions.kml";
		String directory = getWorkingDirectory(fileName);
		String fullName = directory + fileName;
		
		testGetGeofileToJSON(directory, fullName);
	}
	
	@Test
	public void testGMLAsJSON() throws Exception {
		
		String fileName = "regions.gml";
		String directory = getWorkingDirectory(fileName);
		String fullName = directory + fileName;
		
		testGetGeofileToJSON(directory, fullName);
	}

	@Ignore // FIXME
	public void testGPXAsJSON() throws Exception {
		
		String fileName = "wp.gpx";
		String directory = getWorkingDirectory(fileName);
		String fullName = directory + fileName;
		
		testGetGeofileToJSON(directory, fullName);
	}
	
	@Ignore // FIXME
	public void testTABAsJSON() throws Exception {
		

		String fileName = "pigma_regions_POLYGON.tab";
		String directory = getWorkingDirectory(fileName);
		String fullName = directory + fileName;
		
		testGetGeofileToJSON(directory, fullName);
	}

	@Test 
	public void testMIFAsJSON() throws Exception {
		
		String fileName = "pigma_regions_POLYGON.mif";
		String directory = getWorkingDirectory(fileName);
		String fullName = directory + fileName;
		
		testGetGeofileToJSON(directory, fullName);
	}

	private void testGetGeofileToJSON(final String directory, final String fileName) throws Exception{
		
		FileDescriptor fd = new FileDescriptor(fileName);
		fd.listOfFiles.add(fileName);
		fd.listOfExtensions.add(FilenameUtils.getExtension(fileName));

		UpLoadFileManagement fm = new UpLoadFileManagement();
		fm.setWorkDirectory(directory);
		fm.setFileDescriptor(fd);
		
		
		String jsonFeatures = fm.getFeatureCollectionAsJSON(CRS.decode("EPSG:4326"));
		
		Assert.assertNotNull(jsonFeatures); 
	}
	

	private String getWorkingDirectory(String fileName) throws Exception{
		
		String resource = this.getClass().getResource(fileName).toString();
		
		String directory = FilenameUtils.getFullPath(resource);
		int i = directory.indexOf(":");
		directory = directory.substring(i + 1);

		return directory;
	}
}
