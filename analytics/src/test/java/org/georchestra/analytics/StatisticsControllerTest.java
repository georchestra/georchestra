package org.georchestra.analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;

import org.georchestra.analytics.StatisticsController.GRANULARITY;
import org.georchestra.analytics.dao.StatsRepo;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ReflectionUtils;

public class StatisticsControllerTest {

	@Before
	public void setUp() throws Exception {}

	@After
	public void tearDown() throws Exception {}

	@Test
	public final void testCombinedRequestsNoData() throws Exception {
		StatisticsController ctrl = new StatisticsController("UTC");
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		// default empty post should return a 400 Bad request status
		mockMvc.perform(post("/combinedRequests")).andExpect(status().isBadRequest());
	}

	@Test
	public final void testCombinedRequestsBothUserAndGroupSet() throws Exception {
		StatisticsController ctrl = new StatisticsController("UTC");
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		JSONObject posted = new JSONObject("{\"user\": \"testadmin\", \"startDate\": \"2015-01-01\", "
				+ "\"group\": \"ADMINISTRATOR\", \"endDate\": \"2015-12-01\" }");
		// -> 400
		mockMvc.perform(post("/combinedRequests").content(posted.toString()))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public final void testCombinedRequestsNoDateOrBadDate() throws Exception {
		StatisticsController ctrl = new StatisticsController("UTC");
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		JSONObject posted = new JSONObject("{\"user\": \"testadmin\"}");
		// -> 400
		mockMvc.perform(post("/combinedRequests").content(posted.toString()))
			.andExpect(status().isBadRequest());
		
		mockMvc.perform(post("/combinedRequests").content(posted.put("startDate", "invalid")
				.put("endDate", "invalid").toString()))
		.andExpect(status().isBadRequest());
	
	}	

	
	@Test
	public final void testCombinedRequestsLegitUser() throws Exception {
		StatisticsController ctrl = new StatisticsController("UTC");
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		JSONObject posted = new JSONObject("{\"user\": \"testadmin\", \"startDate\": \"2015-01-01\" }");
		StatsRepo statsRepo = Mockito.mock(StatsRepo.class);
		Mockito.when(statsRepo.getRequestCountForUserBetweenStartDateAndEndDateByHour(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountForUserBetweenStartDateAndEndDateByDay(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountForUserBetweenStartDateAndEndDateByWeek(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList());
		
		Object[] sampleData = {4, "2015-01"};
		ArrayList<Object[]> sample = new ArrayList<Object[]>();
		sample.add(sampleData);
		Mockito.when(statsRepo.getRequestCountForUserBetweenStartDateAndEndDateByMonth(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(sample);

		ctrl.setStatsRepository(statsRepo);
		mockMvc.perform(post("/combinedRequests").content(posted.put("endDate", "2015-01-01").toString()))
		.andExpect(content().string(containsString("granularity\": \"HOUR\"")))
		.andExpect(status().isOk());
		mockMvc.perform(post("/combinedRequests").content(posted.put("endDate", "2015-01-08").toString()))
		.andExpect(content().string(containsString("granularity\": \"DAY\"")))
		.andExpect(status().isOk());
		mockMvc.perform(post("/combinedRequests").content(posted.put("endDate", "2015-12-01").toString()))
		.andExpect(content().string(containsString("granularity\": \"WEEK\"")))
		.andExpect(status().isOk());
		mockMvc.perform(post("/combinedRequests").content(posted.put("endDate", "2016-12-01").toString()))
			.andExpect(content().string(containsString("granularity\": \"MONTH\"")))
			.andExpect(status().isOk());
		
	}

	@Test
	public final void testCombinedRequestsLegitGroup() throws Exception {
		StatisticsController ctrl = new StatisticsController("UTC");
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		StatsRepo statsRepo = Mockito.mock(StatsRepo.class);
		Mockito.when(statsRepo.getRequestCountForGroupBetweenStartDateAndEndDateByHour(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountForGroupBetweenStartDateAndEndDateByDay(Mockito.anyString(),
				Mockito.anyString(),Mockito.anyString())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountForGroupBetweenStartDateAndEndDateByWeek(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountForGroupBetweenStartDateAndEndDateByMonth(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList());
		ctrl.setStatsRepository(statsRepo);
		JSONObject posted = new JSONObject("{\"group\": \"ADMINISTRATOR\", \"startDate\": \"2015-01-01\" }");

		mockMvc.perform(post("/combinedRequests").content(posted.put("endDate", "2015-01-01").toString()))
			.andExpect(content().string(containsString("granularity\": \"HOUR\"")))
			.andExpect(status().isOk());
		mockMvc.perform(post("/combinedRequests").content(posted.put("endDate", "2015-01-08").toString()))
		.andExpect(content().string(containsString("granularity\": \"DAY\"")))
		.andExpect(status().isOk());
		mockMvc.perform(post("/combinedRequests").content(posted.put("endDate", "2015-05-01").toString()))
		.andExpect(content().string(containsString("granularity\": \"WEEK\"")))
		.andExpect(status().isOk());
		mockMvc.perform(post("/combinedRequests").content(posted.put("endDate", "2016-02-01").toString()))
		.andExpect(content().string(containsString("granularity\": \"MONTH\"")))
		.andExpect(status().isOk());
	}
	
	@Test
	public final void testCombinedRequestsNoUserNorGroup() throws Exception {
		StatisticsController ctrl = new StatisticsController("UTC");
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		StatsRepo statsRepo = Mockito.mock(StatsRepo.class);
		Mockito.when(statsRepo.getRequestCountBetweenStartDateAndEndDateByHour(Mockito.anyString(),
				Mockito.anyString())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountBetweenStartDateAndEndDateByDay(Mockito.anyString(),
				Mockito.anyString())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountBetweenStartDateAndEndDateByWeek(Mockito.anyString(),
				Mockito.anyString())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountBetweenStartDateAndEndDateByMonth(Mockito.anyString(),
				Mockito.anyString())).thenReturn(new ArrayList());
		ctrl.setStatsRepository(statsRepo);
		JSONObject posted = new JSONObject("{\"startDate\": \"2015-01-01\" }");

		mockMvc.perform(post("/combinedRequests").content(posted.put("endDate", "2015-01-01").toString()))
			.andExpect(content().string(containsString("granularity\": \"HOUR\"")))
			.andExpect(status().isOk());
		mockMvc.perform(post("/combinedRequests").content(posted.put("endDate", "2015-01-08").toString()))
		.andExpect(content().string(containsString("granularity\": \"DAY\"")))
		.andExpect(status().isOk());
		mockMvc.perform(post("/combinedRequests").content(posted.put("endDate", "2015-05-01").toString()))
		.andExpect(content().string(containsString("granularity\": \"WEEK\"")))
		.andExpect(status().isOk());
		mockMvc.perform(post("/combinedRequests").content(posted.put("endDate", "2016-02-01").toString()))
		.andExpect(content().string(containsString("granularity\": \"MONTH\"")))
		.andExpect(status().isOk());
	}
	
	@Test
	public final void testLayersUsage() throws Exception {
		StatisticsController ctrl = new StatisticsController("UTC");
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		StatsRepo statsRepo = Mockito.mock(StatsRepo.class);
		Mockito.when(statsRepo.getLayersStatisticsForUserLimit(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyInt())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getLayersStatisticsForUser(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getLayersStatisticsForGroupLimit(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyInt())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getLayersStatisticsForGroup(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(new ArrayList());
		Mockito.when(
				statsRepo.getLayersStatisticsLimit(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
				.thenReturn(new ArrayList());

		ArrayList<Object[]> sample = new ArrayList<Object[]>();
		Object[] sampleData = {"layerName", 2};
		sample.add(sampleData);

		Mockito.when(statsRepo.getLayersStatistics(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(sample);

		ctrl.setStatsRepository(statsRepo);

		mockMvc.perform(post("/layersUsage.json")
				.content(new JSONObject().put("startDate", "2015-01-01")
						.put("endDate", "2015-01-01")
						.put("user", "testadmin")
						.put("limit", 10).toString()))
				.andExpect(content().string(containsString("results"))).andExpect(status().isOk());

		mockMvc.perform(
				post("/layersUsage.json").content(new JSONObject()
						.put("startDate", "2015-01-01")
						.put("endDate", "2015-01-01")
						.put("user", "testadmin").toString()))
						.andExpect(content().string(containsString("results"))).andExpect(status().isOk());

		mockMvc.perform(post("/layersUsage.json").content(
				new JSONObject()
						.put("startDate", "2015-01-01")
						.put("endDate", "2015-01-01")
						.put("group", "ADMINISTRATOR")
						.put("limit", 10).toString()))
				.andExpect(content().string(containsString("results"))).andExpect(status().isOk());

		mockMvc.perform(post("/layersUsage.json").content(
				new JSONObject()
						.put("startDate", "2015-01-01")
						.put("endDate", "2015-01-01")
						.put("group", "ADMINISTRATOR").toString()))
				.andExpect(content().string(containsString("results"))).andExpect(status().isOk());

		mockMvc.perform(post("/layersUsage.json").content(
				new JSONObject()
						.put("startDate", "2015-01-01")
						.put("endDate", "2015-01-01")
						.put("limit", 10).toString()))
				.andExpect(content().string(containsString("results"))).andExpect(status().isOk());

		mockMvc.perform(post("/layersUsage.json").content(
				new JSONObject()
						.put("startDate", "2015-01-01")
						.put("endDate", "2015-01-01").toString()))
				.andExpect(content().string(containsString("results"))).andExpect(status().isOk());
	}
	
	@Test
	public final void testLayersUsageNoDate() throws Exception {
		StatisticsController ctrl = new StatisticsController("UTC");
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		JSONObject posted = new JSONObject("{\"user\": \"testadmin\"}");
		// -> 400
		mockMvc.perform(post("/layersUsage.json").content(posted.toString()))
			.andExpect(status().isBadRequest());

		mockMvc.perform(post("/layersUsage.json").content(new JSONObject().put("startDate", "2015-01-01").toString()))
		.andExpect(status().isBadRequest());
		mockMvc.perform(post("/layersUsage.json").content(new JSONObject().put("endDate", "2015-01-01").toString()))
		.andExpect(status().isBadRequest());
	
		
		mockMvc.perform(post("/layersUsage.json").content(posted.put("startDate", "invalid")
				.put("endDate", "invalid").toString()))
		.andExpect(status().isBadRequest());
	}
	
	@Test
	public final void testLayersUsageUnparseableInput() throws Exception {
		StatisticsController ctrl = new StatisticsController("UTC");
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		mockMvc.perform(post("/layersUsage.json").content("{]{[[|[")).andExpect(status().isBadRequest());
	}
	
	@Test
	public final void testDistinctUsers() throws Exception {
		StatisticsController ctrl = new StatisticsController("UTC");
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		StatsRepo statsRepo = Mockito.mock(StatsRepo.class);
		ArrayList someUsers = new ArrayList();
		someUsers.add(new Object[] { "testadmin", "georchestra", "643" });
		someUsers.add(new Object[] { "testuser", "camptocamp", "29" });
		
		Mockito.when(statsRepo.getDistinctUsersByGroup(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(someUsers);
		Mockito.when(statsRepo.getDistinctUsers(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(new ArrayList());
		ctrl.setStatsRepository(statsRepo);
		JSONObject posted = new JSONObject("{\"startDate\": \"2015-01-01\" }");

		mockMvc.perform(post("/distinctUsers").content(posted.put("endDate", "2015-01-01").toString()))
			.andExpect(content().string(containsString("results\": ")))
			.andExpect(status().isOk());

		mockMvc.perform(post("/distinctUsers").content(posted.put("endDate", "2015-01-08").put("group", "ADMINISTRATOR").toString()))
		.andExpect(content().string(containsString("results\": ")))
		.andExpect(content().string(containsString("testadmin")))
		.andExpect(content().string(containsString("georchestra")))
		.andExpect(content().string(containsString("643")))
		.andExpect(content().string(containsString("testuser")))
		.andExpect(content().string(containsString("camptocamp")))
		.andExpect(content().string(containsString("29")))
		.andExpect(status().isOk());
	}
	
	@Test
	public final void testDistinctUsersNoDateOrParseError() throws Exception {
		StatisticsController ctrl = new StatisticsController("UTC");
		MockMvc mockMvc = standaloneSetup(ctrl).build();

		JSONObject posted = new JSONObject("{\"startDate\": \"2015-01-01\" }");

		mockMvc.perform(post("/distinctUsers").content(posted.toString()))
			.andExpect(status().isBadRequest());
		mockMvc.perform(post("/distinctUsers").content(new JSONObject().put("endDate", "2015-01-08").toString()))
			.andExpect(status().isBadRequest());
		mockMvc.perform(post("/distinctUsers").content(new JSONObject().put("group", "ADMINISTRATOR").toString()))
		.andExpect(status().isBadRequest());
		mockMvc.perform(post("/distinctUsers").content(new JSONObject().put("endDate", "zefcvsd").toString()))
		.andExpect(status().isBadRequest());
	}

	@Test
	public final void testDistinctUsersUnparseableInput() throws Exception {
		StatisticsController ctrl = new StatisticsController("UTC");
		MockMvc mockMvc = standaloneSetup(ctrl).build();

		mockMvc.perform(post("/distinctUsers").content(" [{ }{ ]"))
			.andExpect(status().isBadRequest());
	}

	@Test
	public final void testGuessGranularity() throws ParseException {
		StatisticsController ctrl = new StatisticsController("UTC");
		Method m = ReflectionUtils.findMethod(ctrl.getClass(), "guessGranularity", String.class, String.class);
		m.setAccessible(true);
		String startDate ="2015-12-03 10:00:55";

		// < 2 day => by hour
		GRANULARITY gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2015-12-04 12:33:00");
		assertTrue(gran == GRANULARITY.HOUR);

		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2015-12-05 15:12:54");
		assertTrue(gran == GRANULARITY.DAY);

		// < 1 week => by day
		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2015-12-06 16:18:21");
		assertTrue(gran == GRANULARITY.DAY);

		// < 1 month => by day
		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2016-01-02 09:14:15");
		assertTrue(gran == GRANULARITY.DAY);

		// < 3 months => by day
		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2016-02-05 12:45:11");
		assertTrue(gran == GRANULARITY.DAY);

		// > 3 months < 1year => by week
		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2016-03-02 16:17:15");
		assertTrue(gran == GRANULARITY.WEEK);

		// other => by year
		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2016-12-03 22:24:54");
		assertTrue(gran == GRANULARITY.MONTH);
		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, "2017-12-03 23:46:17");
		assertTrue(gran == GRANULARITY.MONTH);
	}

	@Test
	public final void testConvertLocalDateToUTC(){

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
	public final void testConvertUTCDateToLocal(){
		// Test with Paris timezone (positive offset with DST)
		StatisticsController ctrl = new StatisticsController("Europe/Paris");
		Method m = ReflectionUtils.findMethod(ctrl.getClass(), "convertUTCDateToLocal", String.class, GRANULARITY.class);
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
