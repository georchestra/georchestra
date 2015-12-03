package org.georchestra.analytics;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.georchestra.analytics.StatisticsController.GRANULARITY;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

public class StatisticsControllerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGuessGranularity() throws ParseException {
		StatisticsController ctrl = new StatisticsController();
		Method m = ReflectionUtils.findMethod(ctrl.getClass(), "guessGranularity", Date.class, Date.class);
		m.setAccessible(true);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = sdf.parse("2015-12-03");

		// < 1 day => by hour
		GRANULARITY gran =  (GRANULARITY) ReflectionUtils.invokeMethod(m, ctrl, startDate, startDate);
		assertTrue(gran == GRANULARITY.HOUR);

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
