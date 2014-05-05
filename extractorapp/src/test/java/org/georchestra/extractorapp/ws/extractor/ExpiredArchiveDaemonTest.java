package org.georchestra.extractorapp.ws.extractor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.georchestra.extractorapp.ws.extractor.task.ExtractionManager;
import org.junit.Assert;
import org.junit.Test;

public class ExpiredArchiveDaemonTest {

    private static final long   SECOND = 1000;
    private static final long   MINUTE = 60 * SECOND;
    private static final long   HOUR   = 60 * MINUTE;
    private static final long   DAYS   = 24 * HOUR;

	@Test
	public void testExpiredArchiveDaemon() {
		try {
			ExpiredArchiveDaemon ead = new ExpiredArchiveDaemon();
			ead.startup();

			// we need an extractionManager before trying to call run()
			ead.setExtractionManager(new ExtractionManager());

			ead.run();
		} catch (Throwable e) {
			//TODO: debug
			fail("Exception running ExpiredArchiveDaemon: " +e.getMessage());
		}
	}

	@Test
	public void testExpiredArchiveDaemonFileDeletion() {
		ExpiredArchiveDaemon ead = new ExpiredArchiveDaemon();
		ead.startup();
		ead.setExtractionManager(new ExtractionManager());
		File storageFile = FileUtils.storageFile("");
		File testFile = new File(storageFile + File.separator + "sample" + ExtractorController.EXTRACTION_ZIP_EXT);
		try {
			testFile.createNewFile();
		} catch (IOException e) {
			fail("Unexpected exception: " + e.getMessage());
		}
		testFile.deleteOnExit();
		ead.run();
		// With default params, the file should be still here after run
		assertTrue(testFile.exists());

		// With expiry = 0, it should be removed
		ead.setExpiry(0);
		ead.run();
		assertFalse(testFile.exists());
	}

	@Test
	public void testExpiredArchiveDaemonExpiry() {
		ExpiredArchiveDaemon ead = new ExpiredArchiveDaemon();
		// should raise NPE before setting it
		try {
			ead.getExpiry();
		} catch (NullPointerException e) {}

		ead.setExpiry(1000);
		Assert.assertEquals(1000, ead.getExpiry());
	}

	@Test
	public void testExpiredArchiveDaemonPeriod() {
		ExpiredArchiveDaemon ead = new ExpiredArchiveDaemon();
		try {
			ead.getPeriod();
		} catch (NullPointerException e) {}
		ead.setPeriod(1000);

		Assert.assertEquals(1000, ead.getPeriod());
	}





}
