/**
 * 
 */
package org.georchestra.extractorapp.ws.extractor;

import static org.junit.Assert.*;

import java.io.File;

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

		testBBox(FileFormat.shp);
	}

	private void testBBox(FileFormat format) throws Exception {

		CoordinateReferenceSystem crs = CRS.decode("EPSG:2154");

		ReferencedEnvelope bbox = new ReferencedEnvelope(227532.435762, 260426.450558, 6704235.126328, 6768637.529969,
				crs);
		File baseDir = FileUtils.createTempDirectory();
		BBoxWriter writer = new BBoxWriter(bbox, baseDir, format, crs, null);

		File[] generateFiles = writer.generateFiles();

		assertTrue(generateFiles.length > 0);

		FileUtils.delete(baseDir);
	}

}
