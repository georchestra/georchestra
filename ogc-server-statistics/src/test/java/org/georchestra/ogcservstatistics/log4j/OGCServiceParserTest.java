package org.georchestra.ogcservstatistics.log4j;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class OGCServiceParserTest {
	@Test
	public void testParseRequest() throws Exception {
		String[] REQUESTS_TO_BE_PARSED = {
				"anonymousUser|2013/12/18 12:18:00|http://localhost/geoserver?SERVICE=WmTS&REQUEST=GeTTILE",
				"anonymousUser|2013/12/18 12:21:00|http://localhost/mapserver?SeRVICE=WMS&REQUEsT=GETStyles",
				"anonymousUser|2013/12/18 12:25:00|http://localhost/mapserver?SeRVICE=wFs&REQUEsT=GetPropertyValue",
				"anonymousUser|2013/12/18 12:32:00|http://localhost/mapserver?SeRVICE=wFs&REQUEsT=GetPropertyValue",
				"anonymousUser|2013/12/18 12:33:00|http://localhost/mapserver?SeRVICE=wFs&REQUEsT=LOCKFeature",
				"anonymousUser|2013/12/18 12:37:00|http://localhost/mapserver?SeRVICE=wFs&REQUEsT=getfeatureWithlock",
				"anonymousUser|2013/12/18 12:43:00|http://localhost/mapserver?SeRVICE=wFs&REQUEsT=listSTOREDqueries",
				"anonymousUser|2013/12/18 12:45:00|http://localhost/mapserver?SeRVICE=wFs&REQUEsT=DESCRIBESTOREDQUERIES",
				"anonymousUser|2013/12/18 12:52:00|http://localhost/mapserver?SeRVICE=wFs&REQUEsT=CREATESTOREDquery",
				"anonymousUser|2013/12/18 12:53:00|http://localhost/mapserver?SeRVICE=wFs&REQUEsT=DROPSTOREDQUERY",
				"anonymousUser|2013/12/18 12:57:00|http://localhost/mapserver?SeRVICE=wFs&REQUEsT=DROPSTOREDQUERY",
		};

		for (int i = 0; i < REQUESTS_TO_BE_PARSED.length; i++) {
			Map<String, Object> lst = OGCServiceParser.parseLog(REQUESTS_TO_BE_PARSED[i]).get(0);
			assertTrue(((String) lst.get("service")).length() > 0);
			assertTrue(((String) lst.get("request")).length() > 0);
		}

		String NONEXISTANT_OPERATION = "anonymousUser|2013/12/18 12:12:23|http://localhost/mapserver?SeRVICE=wFs&REQUEsT=UNKOWNOPERATION";
		Map<String, Object> lstWhenUnknownOp = OGCServiceParser.parseLog(NONEXISTANT_OPERATION).get(0);
		assertTrue(((String) lstWhenUnknownOp.get("service")).length() > 0);
		assertTrue(((String) lstWhenUnknownOp.get("request")).length() == 0);

		String NONEXISTANT_SERVICE = "anonymousUser|2013/12/18 12:12:23|http://localhost/mapserver?SeRVICE=UNKNOWN&REQUEsT=DROPSTOREDQUERY";
		List<Map<String, Object>> lstWhenUnknownService = OGCServiceParser.parseLog(NONEXISTANT_SERVICE);
		assertTrue(lstWhenUnknownService.isEmpty());
	}
}
