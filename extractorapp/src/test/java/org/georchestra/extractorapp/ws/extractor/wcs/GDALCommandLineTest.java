package org.georchestra.extractorapp.ws.extractor.wcs;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.geotools.referencing.CRS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GDALCommandLineTest {

	private URL sampletif = GDALCommandLineTest.class.getResource("/latlong.tif");

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGdalTransformation() throws Exception {
		System.out.println(System.getProperty("java.library.path"));
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile("gdalCmdlineTmpFile", ".tif");
			// javadoc is not clear, making sure we sweep it off at JVM end
			tmpFile.deleteOnExit();
		} catch (IOException e) {
			fail("Unexpected: " +e.getMessage());
		}

		// This should fail
		boolean npeCaught = false;
		try {
			GDALCommandLine.gdalTransformation(new File(sampletif.getFile()), tmpFile, null, null);
		}catch (NullPointerException e) {
			npeCaught = true;
		} catch (Throwable e) {
			fail("Unexpected: " +e.getMessage());
		}
		assertTrue("Should have caught a NullPointerException", npeCaught);


		// now creates "legit" WcsReaderRequests
		@SuppressWarnings("static-access")
		WcsReaderRequest executedReq = new WcsReaderRequestFactory().create(WcsReaderRequest.DEFAULT_VERSION, "latlong.tif",
				-180, -90, 180, 90, CRS.decode("EPSG:4326"),
				CRS.decode("EPSG:2154"), 1, "geotiff", true,
				true, true, "dummy", "dummy");
		GDALCommandLine.gdalTransformation(new File(sampletif.getFile()), tmpFile, executedReq, executedReq);
	}

}
