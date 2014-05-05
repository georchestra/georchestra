package org.georchestra.extractorapp.ws.extractor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import junit.framework.Assert;

import org.georchestra.extractorapp.ws.extractor.task.ExtractionManager;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.sun.net.httpserver.Authenticator.Success;

public class ExtractorControllerTest {

	// TODO: Duplicated from ExtractorController
	private static final String UUID_PARAM = "uuid";

	private ExtractorController ec;

	@Before
	public void setUp() {
		ec = new ExtractorController();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testValidateConfig() {
		// no extractionManager
		try {
			ec.validateConfig();
		} catch (AssertionError e) {
			// expected
		} catch (Throwable e) {
			fail("unexpected: " + e.getMessage());
		}

		// creates an ExtractionManager and retries
		ExtractionManager em = new ExtractionManager();
		ec.setExtractionManager(em);

		// the validateConfig should now run without raising
		// any exception
		try {
			ec.validateConfig();
		} catch (Throwable e) {
			fail("Unexpected: " + e.getMessage());
		}
		return;
	}

	@Test
	public void testResultsNotExistingFile() {
		try {
			MockHttpServletRequest msr = new MockHttpServletRequest();
			MockHttpServletResponse msresp = new MockHttpServletResponse();
			ec.results(msr, msresp);
			int httpRetCode = msresp.getStatus();

			// since our request does not contain any parameter, id should be
			// 404
			if (httpRetCode != 404) {
				fail("Http return code should be = 404, found " + httpRetCode);
			}
		} catch (IOException e) {
			fail("Unexpected: " + e.getMessage());
		}
	}

	@Test
	public void testValidateConfigUnothaurizedDirectory() {
		// Tests are not supposed to be run as root
		String storageDirProp = null;
		boolean assertionErrorRaised = false;

		try {
			storageDirProp = System.getProperty("extractor.storage.dir");
			// this should not be accessible by a regular user
			// which runs the testsuite
			storageDirProp = System.setProperty("extractor.storage.dir",
					"/root/extratorappStorage");
			ExtractionManager em = new ExtractionManager();
			ec.setExtractionManager(em);
			try {
				ec.validateConfig();
			} catch (AssertionError e) {
				assertionErrorRaised = true;
			}
		} catch (Throwable e) {
			fail("Unexpected: " + e.getMessage());
		} finally {
			// restores value for storage.dir
			if (storageDirProp != null) {
			System.setProperty("extractor.storage.dir", storageDirProp);
			} else {
				System.clearProperty("extractor.storage.dir");
			}
		}
		Assert.assertTrue(assertionErrorRaised);
	}

	@Test
	public void testResultsExistingFile() {
		try {
			MockHttpServletRequest msr = new MockHttpServletRequest();
			MockHttpServletResponse msresp = new MockHttpServletResponse();
			msr.setParameter(UUID_PARAM, "extractortest");
			// creating a test file
			File testfile = FileUtils.storageFile("extractortest"
					+ ExtractorController.EXTRACTION_ZIP_EXT);
			testfile.deleteOnExit();
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
					testfile));
			out.putNextEntry(new ZipEntry("test.txt"));
			out.write("Helloworld test".getBytes());
			out.close();

			ec.results(msr, msresp);
			int httpRetCode = msresp.getStatus();

			// should be 200 - OK
			if (httpRetCode != 200) {
				fail("Http return code should be = 404, found " + httpRetCode);
			}
			// contentType should be application/zip
			if (!"application/zip".equalsIgnoreCase(msresp.getContentType())) {
				fail("Content-type 'application/zip' expected, got "
						+ msresp.getContentType());
			}
		} catch (IOException e) {
			fail("Unexpected: " + e.getMessage());
		}
	}

	@Test
	public void testGetTaskQueue() {
		MockHttpServletRequest msr = new MockHttpServletRequest();
		MockHttpServletResponse msresp = new MockHttpServletResponse();
		ExtractionManager em = new ExtractionManager();
		ec.setExtractionManager(em);
		try {
			ec.getTaskQueue(msr, msresp);
		} catch (Exception e) {
			fail("Unexpected: " + e.getMessage());
		}
		Assert.assertEquals(msresp.getContentType(), "application/json");
	}

	@Test
	public void testUpdateTask() {
		MockHttpServletRequest msr = new MockHttpServletRequest();
		MockHttpServletResponse msresp = new MockHttpServletResponse();
		ExtractionManager em = new ExtractionManager();
		ec.setExtractionManager(em);
		boolean invalidJsonThrown = false;
		boolean correctlyFormattedJson = false;
		// invalid JSON as input
		try {
			msr.setContent("Invalid JSON content .. ??//".getBytes());
			ec.updateTask(msr, msresp);
		} catch (IllegalArgumentException e) {
			invalidJsonThrown = true;
		} catch (Throwable e) {
			fail("Unexpected: " + e.getMessage());
		}
		// correctly formated JSON
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			JSONObject jObj = new JSONObject();
			jObj.put("uuid", 1);
			jObj.put("priority", 0);
			jObj.put("status", 	"RUNNING");
			jObj.put("requestor", "pmauduit");
			jObj.put("spec", new JSONObject());
			jObj.put("request_ts", dateFormat.format(new Date()));
			jObj.put("begin_ts", dateFormat.format(new Date()));
			jObj.put("end_ts", dateFormat.format(new Date()));

			msr.setContent(jObj.toString().getBytes());
			ec.updateTask(msr, msresp);
			correctlyFormattedJson = true;
		} catch (Throwable e) {
			fail("Unexpected: " + e.getMessage());
		}

		// creates a dummy task in the extractionmanager
		// TODO finish to test ...

		assertTrue(invalidJsonThrown);
		assertTrue(correctlyFormattedJson);
	}
}
