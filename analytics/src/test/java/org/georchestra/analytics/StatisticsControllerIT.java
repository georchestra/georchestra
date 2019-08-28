package org.georchestra.analytics;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/ws-servlet.xml" })
public class StatisticsControllerIT {

    private @Autowired StatisticsController controller;

    private @Value("${dlJdbcUrlOGC}") String dlJdbcUrlOGC;

    private @Autowired BasicDataSource ds;

    protected final Log logger = LogFactory.getLog(getClass().getPackage().getName());

    private static boolean dataLoaded = false;

    public @Before void before() throws Exception {
        if (!dataLoaded) {
            URL res = StatisticsControllerIT.class.getResource("fixtures.sql");
            assertNotNull(res);
            String fixtures = FileUtils.readFileToString(new File(res.toURI()));
            Connection c = ds.getConnection();
            for (String stmt : fixtures.split("\n")) {
                logger.info(stmt);
                PreparedStatement ps = c.prepareStatement(stmt);
                ps.execute();
            }
            dataLoaded = true;
        }
    }

    public @Test void testFilteringByOrg() throws Exception {
        String payload = "{\"service\":\"layersUsage.json\",\"startDate\":\"2019-07-23\","
                + "\"endDate\":\"2019-08-23\",\"role\":\"GT_TELECOM\",\"limit\":10}";
        HttpServletResponse response = new MockHttpServletResponse();

        String ret = controller.combinedRequests(payload, "json", response);

        JSONObject jsRet = new JSONObject(ret);
        JSONArray results = jsRet.getJSONArray("results");
        assertTrue("GT_TELEKOM role should have only one hit in the statistics table",
                results.length() == 1 && results.getJSONObject(0).getInt("count") == 1);
    }

    public @Test void testNoFilteringByOrg() throws Exception {
        String payload = "{\"service\":\"layersUsage.json\",\"startDate\":\"2019-07-23\","
                + "\"endDate\":\"2019-08-23\",\"limit\":10}";
        HttpServletResponse response = new MockHttpServletResponse();

        String ret = controller.combinedRequests(payload, "json", response);

        JSONObject jsRet = new JSONObject(ret);
        JSONArray results = jsRet.getJSONArray("results");
        assertTrue("With no filter on the role, we expect more than 1 hit in the statistics table",
                results.length() > 1);
    }
}
