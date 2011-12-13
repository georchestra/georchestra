/**
 * 
 */
package extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 * The class that implements this interface are responsible of writing a 
 * set of features in a given format. 
 * </p>
 * @author Mauricio Pazos
 *
 */
interface FeatureWriterStrategy {
	
	/**
	 * Generates a files that maintain the vector data correspondent to a layer  
	 * @return the generated files
	 * @throws IOException
	 */
	File[] generateFiles() throws IOException;
	

}
