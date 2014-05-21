/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeTrue;
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


    @Override
    @Test
    @Ignore("OGR GML reprojection is currently broken out of Mapfishapp's scope")
    public void testGMLCoordinatesEPSG4326() {}
    
    @Override
    @Test
    @Ignore("OGR GML reprojection is currently broken out of Mapfishapp's scope")
    public void testGMLCoordinatesFrom4326to3857() throws Exception {}
    
    @Override
	@Test
	@Ignore("KML OGR Implementation is currently broken out of Mapfishapp's scope")
    public void testKML22ExtendedData() throws Exception {}

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
