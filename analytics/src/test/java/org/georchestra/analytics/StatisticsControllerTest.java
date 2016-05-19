package org.georchestra.analytics;

import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.georchestra.analytics.StatisticsController.GRANULARITY;
import org.georchestra.analytics.dao.StatsRepo;
import org.json.JSONException;
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
		StatisticsController ctrl = new StatisticsController();
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		// default empty post should return a 400 Bad request status
		mockMvc.perform(post("/combinedRequests")).andExpect(status().isBadRequest());
	}

	@Test
	public final void testCombinedRequestsBothUserAndGroupSet() throws Exception {
		StatisticsController ctrl = new StatisticsController();
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		JSONObject posted = new JSONObject("{\"user\": \"testadmin\", \"startDate\": \"2015-01-01\", "
				+ "\"group\": \"ADMINISTRATOR\", \"endDate\": \"2015-12-01\" }");
		// -> 400
		mockMvc.perform(post("/combinedRequests").content(posted.toString()))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public final void testCombinedRequestsNoDateOrBadDate() throws Exception {
		StatisticsController ctrl = new StatisticsController();
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
		StatisticsController ctrl = new StatisticsController();
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		JSONObject posted = new JSONObject("{\"user\": \"testadmin\", \"startDate\": \"2015-01-01\" }");
		StatsRepo statsRepo = Mockito.mock(StatsRepo.class);
		Mockito.when(statsRepo.getRequestCountForUserBetweenStartDateAndEndDateByHour(Mockito.anyString(),
				Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountForUserBetweenStartDateAndEndDateByDay(Mockito.anyString(),
				Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountForUserBetweenStartDateAndEndDateByWeek(Mockito.anyString(),
				Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(new ArrayList());
		
		Object[] sampleData = {4, "2015-01"};
		ArrayList<Object[]> sample = new ArrayList<Object[]>();
		sample.add(sampleData);
		Mockito.when(statsRepo.getRequestCountForUserBetweenStartDateAndEndDateByMonth(Mockito.anyString(),
				Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(sample);

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
		StatisticsController ctrl = new StatisticsController();
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		StatsRepo statsRepo = Mockito.mock(StatsRepo.class);
		Mockito.when(statsRepo.getRequestCountForGroupBetweenStartDateAndEndDateByHour(Mockito.anyString(),
				Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountForGroupBetweenStartDateAndEndDateByDay(Mockito.anyString(),
				Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountForGroupBetweenStartDateAndEndDateByWeek(Mockito.anyString(),
				Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountForGroupBetweenStartDateAndEndDateByMonth(Mockito.anyString(),
				Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(new ArrayList());
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
		StatisticsController ctrl = new StatisticsController();
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		StatsRepo statsRepo = Mockito.mock(StatsRepo.class);
		Mockito.when(statsRepo.getRequestCountBetweenStartDateAndEndDateByHour(Mockito.any(Date.class),
				Mockito.any(Date.class))).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountBetweenStartDateAndEndDateByDay(Mockito.any(Date.class),
				Mockito.any(Date.class))).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountBetweenStartDateAndEndDateByWeek(Mockito.any(Date.class),
				Mockito.any(Date.class))).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getRequestCountBetweenStartDateAndEndDateByMonth(Mockito.any(Date.class),
				Mockito.any(Date.class))).thenReturn(new ArrayList());
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
		StatisticsController ctrl = new StatisticsController();
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		StatsRepo statsRepo = Mockito.mock(StatsRepo.class);
		Mockito.when(statsRepo.getLayersStatisticsForUserLimit(Mockito.anyString(), Mockito.any(Date.class),
				Mockito.any(Date.class), Mockito.anyInt())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getLayersStatisticsForUser(Mockito.anyString(), Mockito.any(Date.class),
				Mockito.any(Date.class))).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getLayersStatisticsForGroupLimit(Mockito.anyString(), Mockito.any(Date.class),
				Mockito.any(Date.class), Mockito.anyInt())).thenReturn(new ArrayList());
		Mockito.when(statsRepo.getLayersStatisticsForGroup(Mockito.anyString(), Mockito.any(Date.class),
				Mockito.any(Date.class))).thenReturn(new ArrayList());
		Mockito.when(
				statsRepo.getLayersStatisticsLimit(Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt()))
				.thenReturn(new ArrayList());

		ArrayList<Object[]> sample = new ArrayList<Object[]>();
		Object[] sampleData = {"layerName", 2};
		sample.add(sampleData);

		Mockito.when(statsRepo.getLayersStatistics(Mockito.any(Date.class), Mockito.any(Date.class)))
				.thenReturn(sample);

		ctrl.setStatsRepository(statsRepo);

		mockMvc.perform(post("/layersUsage")
				.content(new JSONObject().put("startDate", "2015-01-01")
						.put("endDate", "2015-01-01")
						.put("user", "testadmin")
						.put("limit", 10).toString()))
				.andExpect(content().string(containsString("results"))).andExpect(status().isOk());

		mockMvc.perform(
				post("/layersUsage").content(new JSONObject()
						.put("startDate", "2015-01-01")
						.put("endDate", "2015-01-01")
						.put("user", "testadmin").toString()))
						.andExpect(content().string(containsString("results"))).andExpect(status().isOk());

		mockMvc.perform(post("/layersUsage").content(
				new JSONObject()
						.put("startDate", "2015-01-01")
						.put("endDate", "2015-01-01")
						.put("group", "ADMINISTRATOR")
						.put("limit", 10).toString()))
				.andExpect(content().string(containsString("results"))).andExpect(status().isOk());

		mockMvc.perform(post("/layersUsage").content(
				new JSONObject()
						.put("startDate", "2015-01-01")
						.put("endDate", "2015-01-01")
						.put("group", "ADMINISTRATOR").toString()))
				.andExpect(content().string(containsString("results"))).andExpect(status().isOk());

		mockMvc.perform(post("/layersUsage").content(
				new JSONObject()
						.put("startDate", "2015-01-01")
						.put("endDate", "2015-01-01")
						.put("limit", 10).toString()))
				.andExpect(content().string(containsString("results"))).andExpect(status().isOk());

		mockMvc.perform(post("/layersUsage").content(
				new JSONObject()
						.put("startDate", "2015-01-01")
						.put("endDate", "2015-01-01").toString()))
				.andExpect(content().string(containsString("results"))).andExpect(status().isOk());
	}
	
	@Test
	public final void testLayersUsageNoDate() throws Exception {
		StatisticsController ctrl = new StatisticsController();
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		JSONObject posted = new JSONObject("{\"user\": \"testadmin\"}");
		// -> 400
		mockMvc.perform(post("/layersUsage").content(posted.toString()))
			.andExpect(status().isBadRequest());

		mockMvc.perform(post("/layersUsage").content(new JSONObject().put("startDate", "2015-01-01").toString()))
		.andExpect(status().isBadRequest());
		mockMvc.perform(post("/layersUsage").content(new JSONObject().put("endDate", "2015-01-01").toString()))
		.andExpect(status().isBadRequest());
	
		
		mockMvc.perform(post("/layersUsage").content(posted.put("startDate", "invalid")
				.put("endDate", "invalid").toString()))
		.andExpect(status().isBadRequest());
	}
	
	@Test
	public final void testLayersUsageUnparseableInput() throws Exception {
		StatisticsController ctrl = new StatisticsController();
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		mockMvc.perform(post("/layersUsage").content("{]{[[|[")).andExpect(status().isBadRequest());
	}
	
	@Test
	public final void testDistinctUsers() throws Exception {
		StatisticsController ctrl = new StatisticsController();
		MockMvc mockMvc = standaloneSetup(ctrl).build();
		StatsRepo statsRepo = Mockito.mock(StatsRepo.class);
		ArrayList someUsers = new ArrayList();
		someUsers.add(new Object[] { "testadmin", "georchestra", "643" });
		someUsers.add(new Object[] { "testuser", "camptocamp", "29" });
		
		Mockito.when(statsRepo.getDistinctUsersByGroup(Mockito.anyString(), Mockito.any(Date.class),
				Mockito.any(Date.class))).thenReturn(someUsers);
		Mockito.when(statsRepo.getDistinctUsers(Mockito.any(Date.class), Mockito.any(Date.class)))
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
		StatisticsController ctrl = new StatisticsController();
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
		StatisticsController ctrl = new StatisticsController();
		MockMvc mockMvc = standaloneSetup(ctrl).build();

		mockMvc.perform(post("/distinctUsers").content(" [{ }{ ]"))
			.andExpect(status().isBadRequest());
	}

	@Test
	public final void testGuessGranularity() throws ParseException {
		StatisticsController ctrl = new StatisticsController();
		Method m = ReflectionUtils.findMethod(ctrl.getClass(), "guessGranularity", Date.class, Date.class);
		m.setAccessible(true);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = sdf.parse("2015-12-03");


		// < 2 day => by hour
		GRANULARITY gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, sdf.parse("2015-12-04"));
		assertTrue(gran == GRANULARITY.HOUR);

		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, sdf.parse("2015-12-05"));
		assertTrue(gran == GRANULARITY.DAY);

		// < 1 week => by day
		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, sdf.parse("2015-12-06"));
		assertTrue(gran == GRANULARITY.DAY);

		// < 1 month => by day
		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, sdf.parse("2016-01-02"));
		assertTrue(gran == GRANULARITY.DAY);

		// < 3 months => by day
		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, sdf.parse("2016-02-05"));
		assertTrue(gran == GRANULARITY.DAY);

		// > 3 months < 1year => by week
		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, sdf.parse("2016-03-02"));
		assertTrue(gran == GRANULARITY.WEEK);

		// other => by year
		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, sdf.parse("2016-12-03"));
		assertTrue(gran == GRANULARITY.MONTH);
		gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, sdf.parse("2017-12-03"));
		assertTrue(gran == GRANULARITY.MONTH);
	}

}
