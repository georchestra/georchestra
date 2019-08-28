package org.georchestra.analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.beans.PropertyVetoException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import org.georchestra.analytics.StatisticsController.GRANULARITY;

import org.georchestra.analytics.util.DBConnection;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ReflectionUtils;

public class StatisticsControllerTest {

    private DBConnection db;
    private ResultSet res;
    private StatisticsController ctrl;
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {

        this.db = Mockito.mock(DBConnection.class);
        this.res = Mockito.mock(ResultSet.class);
        Mockito.when(this.res.next()).thenReturn(false);
        Mockito.when(this.db.generateQuery(Mockito.anyString(), Mockito.anyMap())).thenReturn(new String());
        Mockito.when(this.db.execute(Mockito.anyString())).thenReturn(this.res);

        this.ctrl = new StatisticsController("UTC");
        this.mockMvc = standaloneSetup(ctrl).build();
        ctrl.setDb(db);

        GeorchestraConfiguration georConfig = Mockito.mock(GeorchestraConfiguration.class);
        ctrl.setGeorConfig(georConfig);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testCombinedRequestsNoData() throws Exception {
        // default empty post should return a 400 Bad request status
        mockMvc.perform(post("/combinedRequests.json")).andExpect(status().isBadRequest());
    }

    @Test
    public final void testCombinedRequestsBothUserAndGroupSet() throws Exception {
        JSONObject posted = new JSONObject("{\"user\": \"testadmin\", \"startDate\": \"2015-01-01\", "
                + "\"role\": \"ADMINISTRATOR\", \"endDate\": \"2015-12-01\" }");
        // -> 400
        mockMvc.perform(post("/combinedRequests.json").content(posted.toString())).andExpect(status().isBadRequest());
    }

    @Test
    public final void testCombinedRequestsNoDateOrBadDate() throws Exception {
        JSONObject posted = new JSONObject("{\"user\": \"testadmin\"}");
        // -> 400
        mockMvc.perform(post("/combinedRequests.json").content(posted.toString())).andExpect(status().isBadRequest());

        mockMvc.perform(post("/combinedRequests.json")
                .content(posted.put("startDate", "invalid").put("endDate", "invalid").toString()))
                .andExpect(status().isBadRequest());

    }

    @Test
    public final void testCombinedRequestsLegitUser() throws Exception {

        JSONObject posted = new JSONObject("{\"user\": \"testadmin\", \"startDate\": \"2015-01-01\" }");

        Object[] sampleData = { 4, "2015-01" };
        ArrayList<Object[]> sample = new ArrayList<Object[]>();
        sample.add(sampleData);

        mockMvc.perform(post("/combinedRequests.json").content(posted.put("endDate", "2015-01-01").toString()))
                .andExpect(content().string(containsString("granularity\": \"HOUR\""))).andExpect(status().isOk());
        mockMvc.perform(post("/combinedRequests.json").content(posted.put("endDate", "2015-01-08").toString()))
                .andExpect(content().string(containsString("granularity\": \"DAY\""))).andExpect(status().isOk());
        mockMvc.perform(post("/combinedRequests.json").content(posted.put("endDate", "2015-12-01").toString()))
                .andExpect(content().string(containsString("granularity\": \"WEEK\""))).andExpect(status().isOk());
        mockMvc.perform(post("/combinedRequests.json").content(posted.put("endDate", "2016-12-01").toString()))
                .andExpect(content().string(containsString("granularity\": \"MONTH\""))).andExpect(status().isOk());

    }

    @Test
    public final void testCombinedRequestsLegitGroup() throws Exception {
        JSONObject posted = new JSONObject("{\"group\": \"ADMINISTRATOR\", \"startDate\": \"2015-01-01\" }");

        mockMvc.perform(post("/combinedRequests.json").content(posted.put("endDate", "2015-01-01").toString()))
                .andExpect(content().string(containsString("granularity\": \"HOUR\""))).andExpect(status().isOk());
        mockMvc.perform(post("/combinedRequests.json").content(posted.put("endDate", "2015-01-08").toString()))
                .andExpect(content().string(containsString("granularity\": \"DAY\""))).andExpect(status().isOk());
        mockMvc.perform(post("/combinedRequests.json").content(posted.put("endDate", "2015-05-01").toString()))
                .andExpect(content().string(containsString("granularity\": \"WEEK\""))).andExpect(status().isOk());
        mockMvc.perform(post("/combinedRequests.json").content(posted.put("endDate", "2016-02-01").toString()))
                .andExpect(content().string(containsString("granularity\": \"MONTH\""))).andExpect(status().isOk());
    }

    @Test
    public final void testCombinedRequestsNoUserNorGroup() throws Exception {
        JSONObject posted = new JSONObject("{\"startDate\": \"2015-01-01\" }");

        mockMvc.perform(post("/combinedRequests.json").content(posted.put("endDate", "2015-01-01").toString()))
                .andExpect(content().string(containsString("granularity\": \"HOUR\""))).andExpect(status().isOk());
        mockMvc.perform(post("/combinedRequests.json").content(posted.put("endDate", "2015-01-08").toString()))
                .andExpect(content().string(containsString("granularity\": \"DAY\""))).andExpect(status().isOk());
        mockMvc.perform(post("/combinedRequests.json").content(posted.put("endDate", "2015-05-01").toString()))
                .andExpect(content().string(containsString("granularity\": \"WEEK\""))).andExpect(status().isOk());
        mockMvc.perform(post("/combinedRequests.json").content(posted.put("endDate", "2016-02-01").toString()))
                .andExpect(content().string(containsString("granularity\": \"MONTH\""))).andExpect(status().isOk());
    }

    @Test
    public final void testLayersUsage() throws Exception {

        mockMvc.perform(post("/layersUsage.json").content(new JSONObject().put("startDate", "2015-01-01")
                .put("endDate", "2015-01-01").put("user", "testadmin").put("limit", 10).toString()))
                .andExpect(content().string(containsString("results"))).andExpect(status().isOk());

        mockMvc.perform(post("/layersUsage.json").content(new JSONObject().put("startDate", "2015-01-01")
                .put("endDate", "2015-01-01").put("user", "testadmin").toString()))
                .andExpect(content().string(containsString("results"))).andExpect(status().isOk());

        mockMvc.perform(post("/layersUsage.json").content(new JSONObject().put("startDate", "2015-01-01")
                .put("endDate", "2015-01-01").put("group", "ADMINISTRATOR").put("limit", 10).toString()))
                .andExpect(content().string(containsString("results"))).andExpect(status().isOk());

        mockMvc.perform(post("/layersUsage.json").content(new JSONObject().put("startDate", "2015-01-01")
                .put("endDate", "2015-01-01").put("group", "ADMINISTRATOR").toString()))
                .andExpect(content().string(containsString("results"))).andExpect(status().isOk());

        mockMvc.perform(post("/layersUsage.json").content(new JSONObject().put("startDate", "2015-01-01")
                .put("endDate", "2015-01-01").put("limit", 10).toString()))
                .andExpect(content().string(containsString("results"))).andExpect(status().isOk());

        mockMvc.perform(post("/layersUsage.json")
                .content(new JSONObject().put("startDate", "2015-01-01").put("endDate", "2015-01-01").toString()))
                .andExpect(content().string(containsString("results"))).andExpect(status().isOk());
    }

    @Test
    public final void testLayersUsageNoDate() throws Exception {
        JSONObject posted = new JSONObject("{\"user\": \"testadmin\"}");
        // -> 400
        mockMvc.perform(post("/layersUsage.json").content(posted.toString())).andExpect(status().isBadRequest());

        mockMvc.perform(post("/layersUsage.json").content(new JSONObject().put("startDate", "2015-01-01").toString()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/layersUsage.json").content(new JSONObject().put("endDate", "2015-01-01").toString()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/layersUsage.json")
                .content(posted.put("startDate", "invalid").put("endDate", "invalid").toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public final void testLayersUsageUnparseableInput() throws Exception {
        mockMvc.perform(post("/layersUsage.json").content("{]{[[|[")).andExpect(status().isBadRequest());
    }

    @Test
    public final void testDistinctUsers() throws Exception {
        JSONObject posted = new JSONObject("{\"startDate\": \"2015-01-01\" }");

        mockMvc.perform(post("/distinctUsers").content(posted.put("endDate", "2015-01-01").toString()))
                .andExpect(content().string(containsString("results\": "))).andExpect(status().isOk());

        mockMvc.perform(post("/distinctUsers")
                .content(posted.put("endDate", "2015-01-08").put("group", "ADMINISTRATOR").toString()))
                .andExpect(content().string(containsString("results\": "))).andExpect(status().isOk());
    }

    @Test
    public final void testDistinctUsersNoDateOrParseError() throws Exception {
        JSONObject posted = new JSONObject("{\"startDate\": \"2015-01-01\" }");

        mockMvc.perform(post("/distinctUsers").content(posted.toString())).andExpect(status().isBadRequest());
        mockMvc.perform(post("/distinctUsers").content(new JSONObject().put("endDate", "2015-01-08").toString()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/distinctUsers").content(new JSONObject().put("group", "ADMINISTRATOR").toString()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/distinctUsers").content(new JSONObject().put("endDate", "zefcvsd").toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public final void testDistinctUsersUnparseableInput() throws Exception {
        mockMvc.perform(post("/distinctUsers").content(" [{ }{ ]")).andExpect(status().isBadRequest());
    }

    @Test
    public final void testGuessGranularity() throws ParseException, PropertyVetoException, SQLException {
        Method m = ReflectionUtils.findMethod(ctrl.getClass(), "guessGranularity", String.class, String.class);
        m.setAccessible(true);
        String startDate = "2015-12-03 10:00:55";

        // < 2 day => by hour
        GRANULARITY gran = (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2015-12-04 12:33:00");
        assertTrue(gran == GRANULARITY.HOUR);

        gran = (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2015-12-05 15:12:54");
        assertTrue(gran == GRANULARITY.DAY);

        // < 1 week => by day
        gran = (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2015-12-06 16:18:21");
        assertTrue(gran == GRANULARITY.DAY);

        // < 1 month => by day
        gran = (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2016-01-02 09:14:15");
        assertTrue(gran == GRANULARITY.DAY);

        // < 3 months => by day
        gran = (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2016-02-05 12:45:11");
        assertTrue(gran == GRANULARITY.DAY);

        // > 3 months < 1year => by week
        gran = (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2016-03-02 16:17:15");
        assertTrue(gran == GRANULARITY.WEEK);

        // other => by year
        gran = (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2016-12-03 22:24:54");
        assertTrue(gran == GRANULARITY.MONTH);
        gran = (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2017-12-03 23:46:17");
        assertTrue(gran == GRANULARITY.MONTH);
    }

    @Test
    public final void testConvertLocalDateToUTC() throws PropertyVetoException, SQLException {

        // Test with Paris timezone (positive offset with DST)
        StatisticsController ctrl = new StatisticsController("Europe/Paris");
        Method m = ReflectionUtils.findMethod(ctrl.getClass(), "convertLocalDateToUTC", String.class);
        m.setAccessible(true);
        assertEquals("2016-11-14 23:00:00", ReflectionUtils.invokeMethod(m, ctrl, "2016-11-15"));
        assertEquals("2016-06-14 22:00:00", ReflectionUtils.invokeMethod(m, ctrl, "2016-06-15")); // DST
        assertEquals("2015-12-31 23:00:00", ReflectionUtils.invokeMethod(m, ctrl, "2016-01-01"));

        // Test with UTC timezone
        ctrl = new StatisticsController("UTC");
        assertEquals("2016-11-15 00:00:00", ReflectionUtils.invokeMethod(m, ctrl, "2016-11-15"));
        assertEquals("2016-06-15 00:00:00", ReflectionUtils.invokeMethod(m, ctrl, "2016-06-15"));
        assertEquals("2016-01-01 00:00:00", ReflectionUtils.invokeMethod(m, ctrl, "2016-01-01"));

        // Test with Martinique timezone (negative offset with no DST)
        ctrl = new StatisticsController("America/Martinique");
        assertEquals("2016-11-15 04:00:00", ReflectionUtils.invokeMethod(m, ctrl, "2016-11-15"));
        assertEquals("2016-06-15 04:00:00", ReflectionUtils.invokeMethod(m, ctrl, "2016-06-15"));
        assertEquals("2016-01-01 04:00:00", ReflectionUtils.invokeMethod(m, ctrl, "2016-01-01"));

    }

    @Test
    public final void testConvertUTCDateToLocal() throws PropertyVetoException, SQLException {
        // Test with Paris timezone (positive offset with DST)
        StatisticsController ctrl = new StatisticsController("Europe/Paris");
        Method m = ReflectionUtils.findMethod(ctrl.getClass(), "convertUTCDateToLocal", String.class,
                GRANULARITY.class);
        m.setAccessible(true);

        assertEquals("2016-11-16 00", ReflectionUtils.invokeMethod(m, ctrl, "2016-11-15 23", GRANULARITY.HOUR));
        assertEquals("2016-11-16 01", ReflectionUtils.invokeMethod(m, ctrl, "2016-11-16 00", GRANULARITY.HOUR));
        assertEquals("2016-01-01 01", ReflectionUtils.invokeMethod(m, ctrl, "2016-1-1 0", GRANULARITY.HOUR));
        assertEquals("2016-11-15", ReflectionUtils.invokeMethod(m, ctrl, "2016-11-15", GRANULARITY.DAY));
        assertEquals("2016-11", ReflectionUtils.invokeMethod(m, ctrl, "2016-11", GRANULARITY.MONTH));
        assertEquals("2016-47", ReflectionUtils.invokeMethod(m, ctrl, "2016-47", GRANULARITY.WEEK));

        // Test with UTC timezone
        ctrl = new StatisticsController("UTC");
        assertEquals("2016-11-15 23", ReflectionUtils.invokeMethod(m, ctrl, "2016-11-15 23", GRANULARITY.HOUR));
        assertEquals("2016-11-16 00", ReflectionUtils.invokeMethod(m, ctrl, "2016-11-16 00", GRANULARITY.HOUR));
        assertEquals("2016-01-01 00", ReflectionUtils.invokeMethod(m, ctrl, "2016-1-1 0", GRANULARITY.HOUR));
        assertEquals("2016-11-15", ReflectionUtils.invokeMethod(m, ctrl, "2016-11-15", GRANULARITY.DAY));
        assertEquals("2016-11", ReflectionUtils.invokeMethod(m, ctrl, "2016-11", GRANULARITY.MONTH));
        assertEquals("2016-47", ReflectionUtils.invokeMethod(m, ctrl, "2016-47", GRANULARITY.WEEK));

        // Test with Martinique timezone (negative offset with no DST)
        ctrl = new StatisticsController("America/Martinique");
        assertEquals("2016-11-15 19", ReflectionUtils.invokeMethod(m, ctrl, "2016-11-15 23", GRANULARITY.HOUR));
        assertEquals("2016-11-15 20", ReflectionUtils.invokeMethod(m, ctrl, "2016-11-16 00", GRANULARITY.HOUR));
        assertEquals("2015-12-31 20", ReflectionUtils.invokeMethod(m, ctrl, "2016-1-1 0", GRANULARITY.HOUR));
        assertEquals("2016-11-14", ReflectionUtils.invokeMethod(m, ctrl, "2016-11-15", GRANULARITY.DAY));
        assertEquals("2016-10", ReflectionUtils.invokeMethod(m, ctrl, "2016-11", GRANULARITY.MONTH));
        assertEquals("2016-46", ReflectionUtils.invokeMethod(m, ctrl, "2016-47", GRANULARITY.WEEK));
    }

}
