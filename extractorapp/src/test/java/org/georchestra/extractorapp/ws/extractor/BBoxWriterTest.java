/**
 *
 */
package org.georchestra.extractorapp.ws.extractor;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author Mauricio Pazos
 *
 */
public class BBoxWriterTest {

    /**
     * Test method for
     * {@link org.georchestra.extractorapp.ws.extractor.BBoxWriter#generateFiles()}.
     */
    @Test
    public void testBBoxSHP() throws Exception {

        CoordinateReferenceSystem crs = CRS.decode("EPSG:2154");

        ReferencedEnvelope bbox = new ReferencedEnvelope(227532.435762, 260426.450558, 6704235.126328, 6768637.529969,
                crs);
        File baseDir = FileUtils.createTempDirectory();
        BBoxWriter writer = new BBoxWriter(bbox, baseDir, crs);

        List<File> generateFiles = writer.generateFiles();

        assertFalse(generateFiles.isEmpty());

        FileUtils.delete(baseDir);
    }

}
