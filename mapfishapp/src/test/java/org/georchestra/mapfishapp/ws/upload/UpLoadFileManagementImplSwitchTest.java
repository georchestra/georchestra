/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Mauricio Pazos
 *
 */
public class UpLoadFileManagementImplSwitchTest {


	/**
	 * Tests that the implemantation created provides the minimum set of geoFile formats
	 * 
	 * Test method for {@link org.georchestra.mapfishapp.ws.upload.UpLoadFileManagement#getFormatList()}.
	 */
	@Test
	public void testGetFormatList() {
		
		UpLoadFileManagement manager = UpLoadFileManagement.create();
		
		FileFormat[] formatList = manager.getFormatList();
		
		FileFormat[] minimumSet = getMinimumFormatSet();
		
		for (FileFormat reqFormat: minimumSet) {
			
			boolean found = false;
			for (FileFormat formatImpl : formatList) {
				
				if(formatImpl.equals(reqFormat)){
					found = true;
					break;
				}
			}
			if(!found){
				fail(reqFormat + " is a required format");
				break;
			}
		}
	}
	
	/**
	 * minimum format set is the geotools implementation
	 * @return Minimum format
	 */
	private FileFormat[] getMinimumFormatSet() {
		
		GeotoolsFeatureReader reader = new GeotoolsFeatureReader();
		
		return reader.getFormatList();
	}

}
