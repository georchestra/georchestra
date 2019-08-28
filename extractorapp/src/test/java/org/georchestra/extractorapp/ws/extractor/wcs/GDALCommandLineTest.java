package org.georchestra.extractorapp.ws.extractor.wcs;

import org.geotools.referencing.CRS;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GDALCommandLineTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private URL sampletif = GDALCommandLineTest.class.getResource("/latlong.tif");

    @Test
    public final void testGdalTransformation() throws Exception {
        try {
            final Process exec = Runtime.getRuntime().exec("gdalwarp --help-general");
            exec.destroy();
        } catch (IOException e) {
            Assume.assumeNoException("Test aborted because gdalwarp is not on PATH", e);
        }
        File tmpFile = folder.newFile("gdalCmdlineTmpFile.tif");
        // This should fail
        boolean npeCaught = false;
        try {
            GDALCommandLine.gdalTransformation(new File(sampletif.getFile()), tmpFile, null, null);
        } catch (NullPointerException e) {
            npeCaught = true;
        } catch (Throwable e) {
            fail("Unexpected: " + e.getMessage());
        }
        assertTrue("Should have caught a NullPointerException", npeCaught);

        // now creates "legit" WcsReaderRequests
        @SuppressWarnings("static-access")
        WcsReaderRequest executedReq = new WcsReaderRequestFactory().create(WcsReaderRequest.DEFAULT_VERSION,
                "latlong.tif", -180, -90, 180, 90, CRS.decode("EPSG:4326"), CRS.decode("EPSG:2154"), 1, "geotiff", true,
                true, true, "dummy", "dummy");
        GDALCommandLine.gdalTransformation(new File(sampletif.getFile()), tmpFile, executedReq, executedReq);
    }

}
