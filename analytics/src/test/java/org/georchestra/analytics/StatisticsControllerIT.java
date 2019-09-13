package org.georchestra.analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:ws-servlet.xml" })
public class StatisticsControllerIT {

    private @Autowired StatisticsController controller;
    private @Autowired @Qualifier("dataSource") DataSource dataSource;

    protected final Log logger = LogFactory.getLog(getClass().getPackage().getName());

    private static String[] SCRIPT = {
            "INSERT INTO ogcstatistics.ogc_services_log VALUES ('admin', '2019-08-22 16:44:54.160501', 'WMS', 'fond_gip', 1861, 'getmap', '', '{ROLE_ADMINISTRATOR}');",
            "INSERT INTO ogcstatistics.ogc_services_log VALUES ('admin', '2019-08-22 17:25:11.305113', 'WMS', 'ign', 541, 'describelayer', '', '{ROLE_ADMINISTRATOR}');",
            "INSERT INTO ogcstatistics.ogc_services_log VALUES ('admin', '2019-08-22 18:03:25.296614', 'WMS', 'fond_gip', 1620, 'getmap', '', '{ROLE_ADMINISTRATOR}');",
            "INSERT INTO ogcstatistics.ogc_services_log VALUES ('admin', '2019-08-22 18:19:51.142684', 'WMS', 'fond_gip', 1653, 'getmap', '', '{ROLE_ADMINISTRATOR}');",
            "INSERT INTO ogcstatistics.ogc_services_log VALUES ('admin', '2019-08-22 19:07:36.502652', 'WMS', 'fond_gip', 295, 'getmap', '', '{ROLE_ADMINISTRATOR}');",
            "INSERT INTO ogcstatistics.ogc_services_log VALUES ('telek', '2019-08-22 19:21:35.283377', 'WMS', 'fond_gip', 1458, 'getmap', '', '{ROLE_GT_TELECOM}');" };

    private static boolean scriptExecuted;

    public @Before void before() throws SQLException {
        if (!scriptExecuted) {
            try (Connection c = dataSource.getConnection()) {
                for (String sstatement : SCRIPT) {
                    c.createStatement().execute(sstatement);
                }
            }
            scriptExecuted = true;
        }
    }

    public @Test void testFilteringByOrg() throws Exception {
        String payload = "{\"service\":\"layersUsage.json\",\"startDate\":\"2019-07-23\","
                + "\"endDate\":\"2019-08-23\",\"role\":\"GT_TELECOM\",\"limit\":10}";
        HttpServletResponse response = new MockHttpServletResponse();

        String ret = controller.combinedRequests(payload, "json", response);

        JSONObject jsRet = new JSONObject(ret);
        JSONArray results = jsRet.getJSONArray("results");
        assertEquals("GT_TELEKOM role should have only one hit in the statistics table", 1, results.length());
        assertEquals("count json property for GT_TELEKOM role mismatch", 1, results.getJSONObject(0).getInt("count"));
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
