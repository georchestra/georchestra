/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assume.assumeNoException;
/**
 * Test case for {@link UpLoadFileManagement} set to use the OGR implementation  
 * 
 * <p>
 * This test case depend on the gdal/ogr module. It could fail, if gdal module was compiled without the support for the required file format.
 * </p>
 * 
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
	@Before
	public void setUp() {
		try {
			// First, the bindings should be available in the classpath
			Class.forName("org.gdal.ogr.ogr");
			// Second, the JVM should be able to load the native lib.
			System.loadLibrary("ogrjni");
		} catch (Throwable e) {
			assumeNoException(e);
		}
	}
	@Test
	public void testGPXAsJSON() throws Exception {

		String fileName = "wp.gpx";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName, null);
	}
	
	@Test
	public void testTABAsJSON() throws Exception {
		
		String fileName = "pigma_regions_POLYGON.tab";
		String fullName = makeFullName(fileName);
		
		testGetGeofileToJSON(fullName, null);
	}
	

}
