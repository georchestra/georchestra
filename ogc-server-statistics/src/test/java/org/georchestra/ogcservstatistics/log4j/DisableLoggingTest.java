/**
 * 
 */
package org.georchestra.ogcservstatistics.log4j;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.georchestra.ogcservstatistics.calculations.OGCServiceStatistics;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for deactivated appender, set to false to avoid the insertion of
 * log messages
 * 
 * @author Mauricio Pazos
 *
 */
public class DisableLoggingTest {

    private Logger LOGGER;

    public static @BeforeClass void initialize() {
	// activated=false in the porperties files
	String file = "src/test/resources/org/georchestra/ogcservstatistics/log4j-disable.properties";
	OGCServiceStatistics.configure(file);
    }

    public @Before void before() {
	LOGGER = Logger.getLogger(OGCServicesAppenderTest.class.getName());
    }

    /**
     * the following log message is not inserted
     * 
     * @throws Exception
     */
    @Test
    public void testOGCOperationLogging() throws Exception {
	List<Map<String, Object>> logList = OGCServiceStatistics.list();
	final int logSizeBefore = logList.size();

	final String request = "http://www.someserver.com/geoserver/wfs/WfsDispatcher?REQUEST=DescribeFeatureType&TYPENAME=ign%3Acommune&SERVICE=WFS&VERSION=1.0.0";
	String[] roles = {};
	String ogcServiceMessage = OGCServiceMessageFormatter.format("userNoInsert!", request, "", roles);

	LOGGER.info(ogcServiceMessage);

	logList = OGCServiceStatistics.list();
	assertEquals(logSizeBefore, logList.size());
    }

}
