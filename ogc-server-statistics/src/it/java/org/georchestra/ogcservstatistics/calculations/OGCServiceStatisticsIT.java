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

/**
 * 
 */
package org.georchestra.ogcservstatistics.calculations;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.awaitility.Duration;
import org.georchestra.ogcservstatistics.OGCServStatisticsException;
import org.georchestra.ogcservstatistics.dataservices.DataCommandException;
import org.georchestra.ogcservstatistics.log4j.OGCServiceMessageFormatter;
import org.georchestra.ogcservstatistics.log4j.OGCServicesAppender;
import org.georchestra.ogcservstatistics.util.IntegrationTestSupport;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Test case for OGCServiceStatistics class
 * <p>
 * WARNING this test will initialize the log table.
 * </p>
 * 
 * @author Mauricio Pazos
 *
 */
public class OGCServiceStatisticsIT {

    // There's no logger configured for this package, use OGCServicesAppender's
    private static Logger LOGGER;

    private final static Date time = Calendar.getInstance().getTime();

    public static @ClassRule IntegrationTestSupport support = new IntegrationTestSupport();

    public static @BeforeClass void before() throws ClassNotFoundException, SQLException, DataCommandException {
        support.deleteAllEntries();

        LOGGER = Logger.getLogger(OGCServicesAppender.class);
        // add the initial logs

        // user101: one request layer1 (WFS) and two for layer2 (using WFS and WMS)
        String request;
        String[] roles = new String[] { "ROLE1", "ROLE2" };

        request = "http://www.someserver.com/wfs.cgi&SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeature&TYPENAME=layer1&FILTER=<Filter><Within><PropertyName>InWaterA_1M/wkbGeom<PropertyName><gml:Envelope><gml:lowerCorner>10 10<gml:lowerCorner><gml:upperCorner>20 20</gml:upperCorner></gml:Envelope></Within></Filter>";
        LOGGER.info(OGCServiceMessageFormatter.format("user101", request, "", roles));

        request = "http://www.someserver.com/wfs.cgi&SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeature&TYPENAME=layer2&FILTER=<Filter><Within><PropertyName>InWaterA_1M/wkbGeom<PropertyName><gml:Envelope><gml:lowerCorner>10 10<gml:lowerCorner><gml:upperCorner>20 20</gml:upperCorner></gml:Envelope></Within></Filter>";
        LOGGER.info(OGCServiceMessageFormatter.format("user102", request, "", roles));

        request = "http://www.someserver.com/geoserver/wms?SERVICE=WMS&LAYERS=layer2&TRANSPARENT=true&VERSION=1.1.1&FORMAT=image%2Fpng&REQUEST=GetMap&STYLES=&SRS=EPSG%3A2154&BBOX=358976.61292821,6395407.8064641,430656.57422103,6467087.7677569&WIDTH=512&HEIGHT=512";
        LOGGER.info(OGCServiceMessageFormatter.format("user101", request, "", roles));

        // user102: one request layer1 using WFS and other using WMS
        request = "http://www.someserver.com/wfs.cgi&SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeature&TYPENAME=layer1&FILTER=<Filter><Within><PropertyName>InWaterA_1M/wkbGeom<PropertyName><gml:Envelope><gml:lowerCorner>10 10<gml:lowerCorner><gml:upperCorner>20 20</gml:upperCorner></gml:Envelope></Within></Filter>";
        LOGGER.info(OGCServiceMessageFormatter.format("user101", request, "", roles));

        request = "http://www.someserver.com/geoserver/wms?SERVICE=WMS&LAYERS=layer1&TRANSPARENT=true&VERSION=1.1.1&FORMAT=image%2Fpng&REQUEST=GetMap&STYLES=&SRS=EPSG%3A2154&BBOX=358976.61292821,6395407.8064641,430656.57422103,6467087.7677569&WIDTH=512&HEIGHT=512";
        LOGGER.info(OGCServiceMessageFormatter.format("user101", request, "", roles));

        await().atMost(Duration.TWO_SECONDS).untilAsserted(() -> assertEquals(5, OGCServiceStatistics.list().size()));
    }

    /**
     * Populates the log table with the set of record required by the test methods
     */
    public OGCServiceStatisticsIT() {

    }

    /**
     * Test method for
     * {@link org.georchestra.ogcservstatistics.calculations.OGCServiceStatistics#list()}.
     * 
     * @throws OGCServStatisticsException
     */
    @Test
    public void testList() throws Exception {

        List<Map<String, Object>> list = OGCServiceStatistics.list();
        assertTrue(list.size() > 0);

        for (Map<String, Object> row : list) {

            assertTrue(row.containsKey("service"));
            assertNotNull(row.get("service"));

            assertTrue(row.containsKey("layer"));
            assertNotNull(row.get("layer"));

            assertTrue(row.containsKey("user_name"));
            assertNotNull(row.get("user_name"));

            assertTrue(row.containsKey("date"));
            assertNotNull(row.get("date"));
        }
    }

    private int getYear(Date date) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(this.time);
        return calendar.get(Calendar.YEAR);
    }

    private int getMonth(Date date) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(this.time);
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * Test method for
     * {@link ogr.georchestra.ogcservs)tatistics.calculations.OGCServiceStatistics#retrieveConnectionsForLayer()}.
     * 
     * @throws OGCServStatisticsException
     */
    @Test
    public void testRetrieveConnectionsForLayer() throws Exception {

        List<Map<String, Object>> list = OGCServiceStatistics.retrieveConnectionsForLayer(getYear(this.time),
                getMonth(this.time));
        assertTrue(list.size() > 0);

        for (Map<String, Object> row : list) {

            assertTrue(row.containsKey("layer"));
            assertNotNull(row.get("layer"));

            assertTrue(row.containsKey("user_name"));
            assertNotNull(row.get("user_name"));

            assertTrue(row.containsKey("connections"));
            assertNotNull(row.get("connections"));
            assertTrue(((Integer) row.get("connections")) > 0);
        }
    }

    /**
     * Test method for
     * {@link ogr.georchestra.ogcservs)tatistics.calculations.OGCServiceStatistics#retrieveConnectionsForLayer()}.
     * 
     * @throws OGCServStatisticsException
     */
    @Test
    public void testRetrieveConnectionsForLayerForYear() throws Exception {

        List<Map<String, Object>> list = OGCServiceStatistics.retrieveConnectionsForLayer(getYear(this.time));
        for (Map<String, Object> row : list) {

            assertTrue(row.containsKey("layer"));
            assertNotNull(row.get("layer"));

            assertTrue(row.containsKey("user_name"));
            assertNotNull(row.get("user_name"));

            assertTrue(row.containsKey("connections"));
            assertNotNull(row.get("connections"));
            assertTrue(((Integer) row.get("connections")) > 0);
        }
    }

    /**
     * Test wrong parameters exceptions
     * 
     * @throws OGCServStatisticsException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWrongParameters_month() throws IllegalArgumentException, OGCServStatisticsException {
        OGCServiceStatistics.retrieveConnectionsForLayer(getYear(this.time), 13); // wrong month
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongParameters_year() throws IllegalArgumentException, OGCServStatisticsException {
        OGCServiceStatistics.retrieveConnectionsForLayer(0, 13); // wrong year
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongParameters_limit() throws IllegalArgumentException, OGCServStatisticsException {
        OGCServiceStatistics.retrieveMostActiveUsers(getYear(this.time), 12, 0); // wrong limit
    }

    /**
     * Test method for
     * {@link org.georchestra.ogcservstatistics.calculations.OGCServiceStatistics#retrieveMostActiveUsers()}.
     * 
     * @throws OGCServStatisticsException
     */
    @Test
    public void testRetrieveMostActiveUsers() throws Exception {

        final int limit = 5;
        List<Map<String, Object>> list = OGCServiceStatistics.retrieveMostActiveUsers(getYear(this.time),
                getMonth(this.time), limit);
        assertTrue(list.size() > 0);
        assertTrue(list.size() <= limit);

        for (Map<String, Object> row : list) {

            assertTrue(row.containsKey("user_name"));
            assertNotNull(row.get("user_name"));

            assertTrue(row.containsKey("connections"));
            assertNotNull(row.get("connections"));
            assertTrue(((Integer) row.get("connections")) > 0);
        }

    }

    /**
     * Test method for
     * {@link org.georchestra.ogcservstatistics.calculations.OGCServiceStatistics#retrieveUserConnectionsForLayer()}.
     * 
     * @throws OGCServStatisticsException
     */
    @Test
    public void testRetrieveUserConnectionsForLayer() throws OGCServStatisticsException {

        List<Map<String, Object>> list = OGCServiceStatistics.retrieveUserConnectionsForLayer(getYear(this.time),
                getMonth(this.time));
        assertTrue(list.size() > 0);

        for (Map<String, Object> row : list) {
            assertTrue(row.containsKey("user_name"));
            assertNotNull(row.get("user_name"));

            assertTrue(row.containsKey("layer"));
            assertNotNull(row.get("layer"));

            assertTrue(row.containsKey("connections"));
            assertNotNull(row.get("connections"));
            assertTrue(((Integer) row.get("connections")) > 0);
        }
    }

    /**
     * Test method for
     * {@link org.georchestra.ogcservstatistics.calculations.OGCServiceStatistics#retrieveUserConnectionsForLayer()}.
     * 
     * @throws OGCServStatisticsException
     */
    @Test
    public void testRetrieveUserConnectionsForLayerForYear() throws OGCServStatisticsException {

        List<Map<String, Object>> list = OGCServiceStatistics.retrieveUserConnectionsForLayer(getYear(this.time));
        assertTrue(list.size() > 0);

        for (Map<String, Object> row : list) {
            assertTrue(row.containsKey("user_name"));
            assertNotNull(row.get("user_name"));

            assertTrue(row.containsKey("layer"));
            assertNotNull(row.get("layer"));

            assertTrue(row.containsKey("connections"));
            assertNotNull(row.get("connections"));
            assertTrue(((Integer) row.get("connections")) > 0);
        }
    }

    /**
     * Test method for
     * {@link org.georchestra.ogcservstatistics.calculations.OGCServiceStatistics#retrieveMostConsultedLayers()}.
     * 
     * @throws OGCServStatisticsException
     */
    @Test
    public void testRetrieveMostConsultedLayers() throws OGCServStatisticsException {

        final int limit = 3;
        List<Map<String, Object>> list = OGCServiceStatistics.retrieveMostConsultedLayers(getYear(this.time),
                getMonth(this.time), limit);
        assertTrue(list.size() > 0);
        assertTrue(list.size() <= limit);

        for (Map<String, Object> row : list) {
            assertTrue(row.containsKey("layer"));
            assertNotNull(row.get("layer"));

            assertTrue(row.containsKey("connections"));
            assertNotNull(row.get("connections"));
            assertTrue(((Integer) row.get("connections")) > 0);
        }

    }

    @Test
    public void testRetrieveMostConsultedLayersForYear() throws OGCServStatisticsException {

        final int limit = 3;
        List<Map<String, Object>> list = OGCServiceStatistics.retrieveMostConsultedLayers(getYear(this.time), limit);
        assertTrue(list.size() > 0);
        assertTrue(list.size() <= limit);

        for (Map<String, Object> row : list) {
            assertTrue(row.containsKey("layer"));
            assertNotNull(row.get("layer"));

            assertTrue(row.containsKey("connections"));
            assertNotNull(row.get("connections"));
            assertTrue(((Integer) row.get("connections")) > 0);
        }

    }
}
