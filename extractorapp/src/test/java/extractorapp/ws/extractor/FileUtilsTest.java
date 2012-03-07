/**
 * 
 */
package extractorapp.ws.extractor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;

import org.junit.Test;

/**
 * @author Mauricio Pazos
 *
 */
public class FileUtilsTest {

	@Test
	public void deleteTempDir() throws Exception{
		
		String dir = "/home/mauro/devel-box/projects/georchestra/trunk/extractorapp/target/tmp";
		File deleteFile = new File(dir);
		FileUtils.delete(deleteFile);
		
		File f = new File(dir);
		
		assertFalse(f.exists());
		
	}

}
