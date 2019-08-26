package org.georchestra.extractorapp.ws.extractor.task;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

import org.georchestra.extractorapp.ws.extractor.ExtractorLayerRequest;
import org.georchestra.extractorapp.ws.extractor.RequestConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ReflectionUtils;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:task-context.xml" })
public class ExtractionTaskTest {

	@Autowired
	private ComboPooledDataSource dataSource;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private boolean pgAvailable;
	private int beforeCount;

	@Before
	public void setUp() throws Exception {
		try {
			Connection c = dataSource.getConnection();
			this.beforeCount = getLayerLogCount(c);
		} catch (Exception e) {
			this.pgAvailable = false;
			return;
		}
		this.pgAvailable = true;
	}

	private int getLayerLogCount(Connection c) throws Exception {
		Statement s = c.createStatement();
		ResultSet r = s.executeQuery("SELECT COUNT(*) AS rowCount FROM extractorapp.extractor_log");
		r.next();
		int ret = r.getInt("rowCount");
		r.close();
		return ret;
	}

	@Test
	public void testAnonymousExtractionRequestStatSetRunning() throws Exception {
		assumeTrue("No postgresql available for this test", this.pgAvailable);
		File testDir = tempFolder.newFolder();
		RequestConfiguration rc = new RequestConfiguration(new ArrayList<ExtractorLayerRequest>(), UUID.randomUUID(),
				null, null, true, null, null, null, null, "localhost", testDir.toString(), 10000000, true, false, null,
				null);
		ExtractionTask et = new ExtractionTask(rc, this.dataSource);
		Method m = ReflectionUtils.findMethod(et.getClass(), "statSetRunning");
		m.setAccessible(true);

		ReflectionUtils.invokeMethod(m, et);

		int afterCount = getLayerLogCount(dataSource.getConnection());
		assertTrue("Expected to have saved at least one record in DB", afterCount > this.beforeCount);
	}
}
