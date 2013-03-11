/**
 * 
 */
package mapfishapp.ws.upload;

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
	public void testGetFeatureCollectionAsJSON() throws IOException {
		
		String directory = getWorkingDirectory();
		String fileName = getTestData();
		
		FileDescriptor fd = new FileDescriptor(fileName);
		fd.listOfFiles.add(fileName);
		fd.listOfExtensions.add(FilenameUtils.getExtension(fileName));

		UpLoadFileManagement fm = new UpLoadFileManagement(fd, directory);
		
		String jsonFeatures = fm.getFeatureCollectionAsJSON( );
		
		Assert.assertNotNull(jsonFeatures); 
	}
	
	
	private String getWorkingDirectory() throws IOException{
		String current = new File( "." ).getCanonicalPath();
		String directory = current + "/src/test/resources/upload/";

		return directory;
	}
	private String getTestData() throws IOException{
		
		String fileName = getWorkingDirectory() + "points-4326.shp";

		return fileName;
	}

}
