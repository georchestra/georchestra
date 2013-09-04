/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link UpLoadFileManagement} set to use the OGR implementation  
 * @author Mauricio Pazos
 *
 */
public class UpLoadFileManagementOGRImplTest extends UpLoadFileManagementGTImplTest {


	/**
	 * @return UpLoadFileManagement set with geotools implementation
	 * @throws IOException 
	 */
	@Override
	protected UpLoadFileManagement create() throws IOException{
		return UpLoadFileManagement.create(UpLoadFileManagement.Implementation.ogr);
	}
	
	@Test
	public void testGPXAsJSON() throws Exception {
		
		String fileName = "wp.gpx";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName, null);
	}
	

}
