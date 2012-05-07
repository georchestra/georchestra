/**
 * 
 */
package com.camptocamp.ogcservstatistics.calculations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.camptocamp.ogcservstatistics.OGCServStatisticsException;
import com.camptocamp.ogcservstatistics.dataservices.DataServicesConfiguration;
import com.camptocamp.ogcservstatistics.dataservices.DeleteAllCommand;
import com.camptocamp.ogcservstatistics.log4j.OGCServiceMessageFormatter;



/**
 * Test case for OGCServiceStatistics class
 * <p>
 * WARNING this test will initialize the log table. 
 * </p>
 * @author Mauricio Pazos
 *
 */
public class OGCServiceStatisticsTest {

	private final static Logger LOGGER = Logger.getLogger( OGCServiceStatisticsTest.class);
	private final static Date time = Calendar.getInstance().getTime();
	
	static{
		// WARNING: because this test will delete all log before execute the test method
		// you should configure a test table in the log4j.properties file
		final String file = "src/test/resources/com/camptocamp/ogcservstatistics/log4j.properties";
		OGCServiceStatistics.configure(file);

		try {
			// the log table Initialize
			
			DeleteAllCommand cmd = new DeleteAllCommand();
			cmd.setConnection(DataServicesConfiguration.getInstance().getConnection());
			cmd.execute();
			
			// add the initial logs

			// user101: one request layer1 (WFS) and two for layer2 (using WFS and WMS) 
			String request;
			
			request = "http://www.someserver.com/wfs.cgi&SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeature&TYPENAME=layer1&FILTER=<Filter><Within><PropertyName>InWaterA_1M/wkbGeom<PropertyName><gml:Envelope><gml:lowerCorner>10 10<gml:lowerCorner><gml:upperCorner>20 20</gml:upperCorner></gml:Envelope></Within></Filter>";
			LOGGER.info(OGCServiceMessageFormatter.format("user101", time, request,""));
			
			request = "http://www.someserver.com/wfs.cgi&SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeature&TYPENAME=layer2&FILTER=<Filter><Within><PropertyName>InWaterA_1M/wkbGeom<PropertyName><gml:Envelope><gml:lowerCorner>10 10<gml:lowerCorner><gml:upperCorner>20 20</gml:upperCorner></gml:Envelope></Within></Filter>";
			LOGGER.info(OGCServiceMessageFormatter.format("user101", time, request,""));
			
			request = "http://www.someserver.com/geoserver/wms?SERVICE=WMS&LAYERS=layer2&TRANSPARENT=true&VERSION=1.1.1&FORMAT=image%2Fpng&REQUEST=GetMap&STYLES=&SRS=EPSG%3A2154&BBOX=358976.61292821,6395407.8064641,430656.57422103,6467087.7677569&WIDTH=512&HEIGHT=512";
			LOGGER.info(OGCServiceMessageFormatter.format("user101", time, request,""));
			
			// user102: one request layer1 using WFS and other using WMS
			request = "http://www.someserver.com/wfs.cgi&SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeature&TYPENAME=layer1&FILTER=<Filter><Within><PropertyName>InWaterA_1M/wkbGeom<PropertyName><gml:Envelope><gml:lowerCorner>10 10<gml:lowerCorner><gml:upperCorner>20 20</gml:upperCorner></gml:Envelope></Within></Filter>";
			LOGGER.info(OGCServiceMessageFormatter.format("user102", time, request,""));
			
			request = "http://www.someserver.com/geoserver/wms?SERVICE=WMS&LAYERS=layer1&TRANSPARENT=true&VERSION=1.1.1&FORMAT=image%2Fpng&REQUEST=GetMap&STYLES=&SRS=EPSG%3A2154&BBOX=358976.61292821,6395407.8064641,430656.57422103,6467087.7677569&WIDTH=512&HEIGHT=512";
			LOGGER.info(OGCServiceMessageFormatter.format("user102", time, request,""));
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	
	
	}
	
	/**
	 * Populates the log table with the set of record required by the test methods
	 */
	public OGCServiceStatisticsTest() {
		
	}

	/**
	 * Test method for {@link com.camptocamp.ogcservstatistics.calculations.OGCServiceStatistics#list()}.
	 * @throws OGCServStatisticsException 
	 */
	@Test
	public void testList() throws Exception {
		List<Map<String,Object>> list = OGCServiceStatistics.list();
		assertTrue(list.size() > 0);
		
		for (Map<String,Object> row : list) {

			assertTrue(row.containsKey("service"));
			assertNotNull(row.get("service"));

			assertTrue(row.containsKey("layer"));
			assertNotNull(row.get("layer"));

			assertTrue(row.containsKey("user_name"));
			assertNotNull(row.get("user_name"));
			
			assertTrue(row.containsKey("date") );
			assertNotNull(row.get("date"));
		}
	}

	private int getYear(Date date){
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(this.time);
		return calendar.get(Calendar.YEAR);
	}
	
	private int getMonth(Date date){
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(this.time);
		return calendar.get(Calendar.MONTH) + 1;
	}
	
	/**
	 * Test method for {@link com.camptocamp.ogcservs)tatistics.calculations.OGCServiceStatistics#retrieveConnectionsForLayer()}.
	 * @throws OGCServStatisticsException 
	 */
	@Test
	public void testRetrieveConnectionsForLayer() throws Exception {
		
		List<Map<String,Object>> list = OGCServiceStatistics.retrieveConnectionsForLayer(getYear(this.time),getMonth(this.time));
		assertTrue(list.size() > 0);
		
		for (Map<String,Object> row : list) {

			assertTrue(row.containsKey("layer"));
			assertNotNull(row.get("layer"));

			assertTrue(row.containsKey("user_name"));
			assertNotNull(row.get("user_name"));
			
			assertTrue(row.containsKey("connections") );
			assertNotNull(row.get("connections"));
			assertTrue(((Integer) row.get("connections")) > 0);
		}
	}

	/**
	 * Test method for {@link com.camptocamp.ogcservs)tatistics.calculations.OGCServiceStatistics#retrieveConnectionsForLayer()}.
	 * @throws OGCServStatisticsException 
	 */
	@Test
	public void testRetrieveConnectionsForLayerForYear() throws Exception {
		
		List<Map<String,Object>> list = OGCServiceStatistics.retrieveConnectionsForLayer(getYear(this.time));
		assertTrue(list.size() > 0);
		
		for (Map<String,Object> row : list) {

			assertTrue(row.containsKey("layer"));
			assertNotNull(row.get("layer"));

			assertTrue(row.containsKey("user_name"));
			assertNotNull(row.get("user_name"));
			
			assertTrue(row.containsKey("connections") );
			assertNotNull(row.get("connections"));
			assertTrue(((Integer) row.get("connections")) > 0);
		}
	}
	
	/**
	 * Test wrong parameters exceptions
	 * 
	 * @throws OGCServStatisticsException
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testWrongParameters() throws IllegalArgumentException, OGCServStatisticsException {
		
		OGCServiceStatistics.retrieveConnectionsForLayer(getYear(this.time),13); // wrong month
		OGCServiceStatistics.retrieveConnectionsForLayer(0,13); // wrong year
		
		OGCServiceStatistics.retrieveMostActiveUsers(getYear(this.time), 12, 0); // wrong limit
		
		OGCServiceStatistics.retrieveMostConsultedLayers(getYear(this.time), 12, 0); // wrong limit
		
		OGCServiceStatistics.retrieveUserConnectionsForLayer(getYear(this.time), 13); // wrong month
		
	}
	
	/**
	 * Test method for {@link com.camptocamp.ogcservstatistics.calculations.OGCServiceStatistics#retrieveMostActiveUsers()}.
	 * @throws OGCServStatisticsException 
	 */
	@Test
	public void testRetrieveMostActiveUsers() throws Exception{
		
		final int limit = 5;
		List<Map<String,Object>> list = OGCServiceStatistics.retrieveMostActiveUsers(getYear(this.time), getMonth(this.time) , limit);
		assertTrue(list.size() > 0);
		assertTrue(list.size() <= limit);
		
		for (Map<String,Object> row : list) {

			assertTrue(row.containsKey("user_name"));
			assertNotNull(row.get("user_name"));
			
			assertTrue(row.containsKey("connections") );
			assertNotNull(row.get("connections"));
			assertTrue(((Integer) row.get("connections")) > 0);
		}
		
	}

	/**
	 * Test method for {@link com.camptocamp.ogcservstatistics.calculations.OGCServiceStatistics#retrieveUserConnectionsForLayer()}.
	 * @throws OGCServStatisticsException 
	 */
	@Test
	public void testRetrieveUserConnectionsForLayer() throws OGCServStatisticsException{
		
		List<Map<String,Object>> list = OGCServiceStatistics.retrieveUserConnectionsForLayer(getYear(this.time),getMonth(this.time));
		assertTrue(list.size() > 0);
		
		for (Map<String,Object> row : list) {
			assertTrue(row.containsKey("user_name"));
			assertNotNull(row.get("user_name"));

			assertTrue(row.containsKey("layer"));
			assertNotNull(row.get("layer"));
			
			assertTrue(row.containsKey("connections") );
			assertNotNull(row.get("connections"));
			assertTrue(((Integer) row.get("connections")) > 0);
		}
	}
	
	/**
	 * Test method for {@link com.camptocamp.ogcservstatistics.calculations.OGCServiceStatistics#retrieveUserConnectionsForLayer()}.
	 * @throws OGCServStatisticsException 
	 */
	@Test
	public void testRetrieveUserConnectionsForLayerForYear() throws OGCServStatisticsException{
		
		List<Map<String,Object>> list = OGCServiceStatistics.retrieveUserConnectionsForLayer(getYear(this.time));
		assertTrue(list.size() > 0);
		
		for (Map<String,Object> row : list) {
			assertTrue(row.containsKey("user_name"));
			assertNotNull(row.get("user_name"));

			assertTrue(row.containsKey("layer"));
			assertNotNull(row.get("layer"));
			
			assertTrue(row.containsKey("connections") );
			assertNotNull(row.get("connections"));
			assertTrue(((Integer) row.get("connections")) > 0);
		}
	}
	
	/**
	 * Test method for {@link com.camptocamp.ogcservstatistics.calculations.OGCServiceStatistics#retrieveMostConsultedLayers()}.
	 * @throws OGCServStatisticsException 
	 */
	@Test
	public void testRetrieveMostConsultedLayers() throws OGCServStatisticsException{
		
		final int limit = 3;
		List<Map<String,Object>> list = OGCServiceStatistics.retrieveMostConsultedLayers(getYear(this.time),getMonth(this.time),limit);
		assertTrue(list.size() > 0);
		assertTrue(list.size() <= limit);
		
		for (Map<String,Object> row : list) {
			assertTrue(row.containsKey("layer"));
			assertNotNull(row.get("layer"));
			
			assertTrue(row.containsKey("connections") );
			assertNotNull(row.get("connections"));
			assertTrue(((Integer) row.get("connections")) > 0);
		}
		
	}
	@Test
	public void testRetrieveMostConsultedLayersForYear() throws OGCServStatisticsException{
		
		final int limit = 3;
		List<Map<String,Object>> list = OGCServiceStatistics.retrieveMostConsultedLayers(getYear(this.time),limit);
		assertTrue(list.size() > 0);
		assertTrue(list.size() <= limit);
		
		for (Map<String,Object> row : list) {
			assertTrue(row.containsKey("layer"));
			assertNotNull(row.get("layer"));
			
			assertTrue(row.containsKey("connections") );
			assertNotNull(row.get("connections"));
			assertTrue(((Integer) row.get("connections")) > 0);
		}
		
	}
}
