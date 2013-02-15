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
 * Unit Test for {@link UpLoadFileManegement}
 * 
 * @author Mauricio Pazos
 *
 */
public class UpLoadFileManegementTest {

	/**
	 * Test method for {@link mapfishapp.ws.upload.UpLoadFileManegement#getFeatureCollectionAsJSON()}.
	 * @throws IOException 
	 */
	@Test 
	public void testGetFeatureCollectionAsJSON() throws IOException {
		
		String directory = getWorkingDirectory();
		String fileName = getTestData();
		
		FileDescriptor fd = new FileDescriptor(fileName);
		fd.listOfFiles.add(fileName);
		fd.listOfExtensions.add(FilenameUtils.getExtension(fileName));

		UpLoadFileManegement fm = new UpLoadFileManegement(fd, directory);
		
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
