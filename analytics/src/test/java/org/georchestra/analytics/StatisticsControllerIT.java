/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@SpringJUnitConfig(locations = { "classpath:ws-servlet.xml" })
public class StatisticsControllerIT {

    private @Autowired StatisticsController controller;
    private @Autowired @Qualifier("dataSource") DataSource dataSource;

    protected final Log logger = LogFactory.getLog(getClass().getPackage().getName());

    private static String[] SCRIPT = {
            "INSERT INTO ogcstatistics.ogc_services_log VALUES ('admin', '2019-08-18 16:44:54.160501', 'WMS', 'fond_gip', 1861, 'getmap', '', '{ROLE_ADMINISTRATOR}');",
            "INSERT INTO ogcstatistics.ogc_services_log VALUES ('admin', '2019-08-19 17:25:11.305113', 'WMS', 'ign', 541, 'describelayer', '', '{ROLE_ADMINISTRATOR}');",
            "INSERT INTO ogcstatistics.ogc_services_log VALUES ('admin', '2019-08-20 18:03:25.296614', 'WMS', 'fond_gip', 1620, 'getmap', '', '{ROLE_ADMINISTRATOR}');",
            "INSERT INTO ogcstatistics.ogc_services_log VALUES ('admin', '2019-08-21 18:19:51.142684', 'WMS', 'fond_gip', 1653, 'getmap', '', '{ROLE_ADMINISTRATOR}');",
            "INSERT INTO ogcstatistics.ogc_services_log VALUES ('admin', '2019-08-22 19:07:36.502652', 'WMS', 'fond_gip', 295, 'getmap', '', '{ROLE_ADMINISTRATOR}');",
            "INSERT INTO ogcstatistics.ogc_services_log VALUES ('telek', '2019-08-22 19:21:35.283377', 'WMS', 'fond_gip', 1458, 'getmap', '', '{ROLE_GT_TELECOM}');" };

    private static boolean scriptExecuted;

    public @BeforeEach void before() throws SQLException {
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
        assertEquals(1, results.length(), "GT_TELEKOM role should have only one hit in the statistics table");
        assertEquals(1, results.getJSONObject(0).getInt("count"), "count json property for GT_TELEKOM role mismatch");
    }

    public @Test void testNoFilteringByOrg() throws Exception {
        String payload = "{\"service\":\"layersUsage.json\",\"startDate\":\"2019-07-23\","
                + "\"endDate\":\"2019-08-23\",\"limit\":10}";
        HttpServletResponse response = new MockHttpServletResponse();

        String ret = controller.combinedRequests(payload, "json", response);

        JSONObject jsRet = new JSONObject(ret);
        JSONArray results = jsRet.getJSONArray("results");
        assertTrue(results.length() == 5,
                "With no filter on the role, we expect more than 1 hit in the statistics table");
    }
}
