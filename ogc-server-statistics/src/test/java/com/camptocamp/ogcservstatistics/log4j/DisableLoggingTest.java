/**
 * 
 */
package com.camptocamp.ogcservstatistics.log4j;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.camptocamp.ogcservstatistics.calculations.OGCServiceStatistics;

/**
 * Test case for activated key. It is configured in false to avoid the insertion of log messages
 * 
 * @author Mauricio Pazos
 *
 */
public class DisableLoggingTest {

	
	private static final Logger LOGGER = Logger.getLogger(OGCServicesAppenderTest.class);


	static{
		// activated=false in the porperties files
		String file = "src/test/resources/com/camptocamp/ogcservstatistics/log4j-disable.properties";
		PropertyConfigurator.configure(file);
	}

	/**
	 * the following log message is not inserted
	 * @throws Exception
	 */
	@Test
	public void testOGCOperationLogging() throws Exception {
		
		List<Map<String, Object>> logList = OGCServiceStatistics.list();
		final int logSizeBefore = logList.size();

		final Date time = Calendar.getInstance().getTime();

		final String  request = "http://www.someserver.com/geoserver/wfs/WfsDispatcher?REQUEST=DescribeFeatureType&TYPENAME=ign%3Acommune&SERVICE=WFS&VERSION=1.0.0";

		String ogcServiceMessage = OGCServiceMessageFormatter.format("userNoInsert!", time, request);

		LOGGER.info(ogcServiceMessage);

		logList = OGCServiceStatistics.list();
		assertEquals(logList.size(), logSizeBefore );
	}
	
}
