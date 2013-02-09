/**
 * 
 */
package mapfishapp.ws.upload;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

/**
 * @author Mauricio Pazos
 *
 */
public class UpLoadFileManegementTest {

	/**
	 * Test method for {@link mapfishapp.ws.upload.UpLoadFileManegement#featureCollectionToJSON(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	public void testFeatureCollectionToJSON() throws IOException {
		
		
		String current = new File( "." ).getCanonicalPath();
		String directory = current + "/src/main/resources/mapfishapp/ws/upload/";
		String fileName = directory + "points-4326.shp";
		FileDescriptor fd = new FileDescriptor(fileName);
		fd.listOfFiles.add(fileName);
		fd.listOfExtensions.add(FilenameUtils.getExtension(fileName));

		UpLoadFileManegement fm = new UpLoadFileManegement(fd, directory);
		
		String jsonFeatures = fm.getFeatureCollectionAsJSON( );
		
		System.out.println("RESULT: " +  jsonFeatures);
	}

}
