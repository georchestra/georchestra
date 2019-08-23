/**
 * 
 */
package org.georchestra.ogcservstatistics.log4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.georchestra.ogcservstatistics.util.Utility;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.georchestra.ogcservstatistics.OGCServStatisticsException;
import org.georchestra.ogcservstatistics.calculations.OGCServiceStatistics;
import org.georchestra.ogcservstatistics.dataservices.DataServicesConfiguration;
import org.georchestra.ogcservstatistics.dataservices.DeleteAllCommand;
import org.georchestra.ogcservstatistics.log4j.OGCServiceMessageFormatter;
import org.georchestra.ogcservstatistics.log4j.OGCServicesAppender;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for {@link OGCServicesAppender}
 * <p>
 * This test case is based in the document ogc_requests.txt
 * </p>
 * <p>
 * WARNING this test will initialize the log table.
 * </p>
 * 
 * @author Mauricio Pazos
 *
 */
public class OGCServicesAppenderTest {

	private static final Logger LOGGER = Logger.getLogger(OGCServicesAppenderTest.class.getName());

	static {
		// WARNING: because this test will delete all log before execute the test method
		// you should configure a test table in the log4j.properties file

		String file = "src/test/resources/org/georchestra/ogcservstatistics/log4j.properties";
		PropertyConfigurator.configure(file);
		try {
			DeleteAllCommand cmd = new DeleteAllCommand();
			Connection connection = DataServicesConfiguration.getInstance().getConnection();
			cmd.setConnection(connection);
			cmd.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * tests that exists the {@link OGCServicesAppender}
	 */
	@Ignore
	public void testExistAppender() {

		assertNotNull(getAppender());
	}

	/**
	 * Test for {@link org.georchestra.ogcservstatistics.log4j.OGCServicesAppender}
	 * configuration
	 * 
	 * Test that all properties were loaded from log4j.properties
	 */
	@Ignore
	public void testConfiguration() {

		OGCServicesAppender appender = getAppender();
		assertNotNull(appender);

		assertEquals("jdbc:postgresql://localhost:5432/testdb", appender.getJdbcURL());
		assertEquals("georchestra", appender.getDatabasePassword());
		assertEquals("georchestra", appender.getDatabaseUser());

		assertTrue(appender.isActivated());
		assertEquals(1, appender.getBufferSize());
	}

	private OGCServicesAppender getAppender() {

		Logger root = Logger.getRootLogger();

		Enumeration appenderList = root.getAllAppenders();
		while (appenderList.hasMoreElements()) {

			Object appender = appenderList.nextElement();
			System.out.println(appender);
			if (appender.getClass().equals(OGCServicesAppender.class)) {
				return (OGCServicesAppender) appender;
			}
		}
		return null;
	}

	/**
	 * Test based on ogc_requests.txt example 1
	 * <p>
	 * http://ns383241.ovh.net:80/geoserver/wfs/WfsDispatcher?REQUEST=DescribeFeatureType&TYPENAME=ign%3Acommune&
	 * SERVICE=WFS&VERSION=1.0.0
	 * </p>
	 * 
	 * <pre>
	 * SERVICE=wfs 
	 * REQUEST=DescribeFeatureType
	 * </pre>
	 * 
	 * @throws OGCServStatisticsException
	 */
	String[] roles = { "PENDING", "ADMIN", "USERS", "TEST" };

	@Test
	public void testWfsDescribeFeatureType() throws Exception {

		final String request = "http://www.someserver.com/geoserver/wfs/WfsDispatcher?REQUEST=DescribeFeatureType&TYPENAME=ign%3Acommune&SERVICE=WFS&VERSION=1.0.0";
		testOGCOperationLogging("user1", request, 1, roles);
	}

	/**
	 * Test based on ogc_requests.txt example 2
	 * <p>
	 * </p>
	 * 
	 * <pre>
	 * SERVICE=wfs 
	 * REQUEST=GetCapabilities
	 * </pre>
	 * 
	 * @throws OGCServStatisticsException
	 */
	@Test
	public void testWfsGetCapabilities() throws Exception {

		final String request = "http://www.someserver.com/geoserver/wfs?REQUEST=GetCapabilities&SERVICE=WFS&VERSION=1.0.0&namespace=pigma_loc";
		testOGCOperationLogging("user2", request, 1, roles);
	}

	/**
	 * Test based on ogc_requests.txt example 3
	 * <p>
	 * </p>
	 * 
	 * <pre>
	 * Test for
	 * SERVICE=WMS 
	 * REQUEST=GetFeatureInfo
	 * </pre>
	 * 
	 * @throws OGCServStatisticsException
	 */
	@Test
	public void testWmsGetFeatureInfo() throws Exception {

		final String request = "http://www.someserver.com/geoserver/ign/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetFeatureInfo&LAYERS=ign%3Acommune&QUERY_LAYERS=ign%3Acommune&STYLES=&BBOX=358085.648684%2C6401524.07185%2C494305.575125%2C6441144.050455&FEATURE_COUNT=1500&HEIGHT=283&WIDTH=973&FORMAT=image%2Fpng&INFO_FORMAT=application%2Fvnd.ogc.gml&SRS=EPSG%3A2154&X=508&Y=147";
		testOGCOperationLogging("user3", request, 1, roles);
	}

	/**
	 * Test based on ogc_requests.txt example 4
	 * <p>
	 * </p>
	 * 
	 * <pre>
	 * test for 
	 * SERVICE=WMS
	 * REQUEST=GetMap
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWmsGetMap() throws Exception {

		final String request = "http://www.someserver.com/geoserver/wms?SERVICE=WMS&LAYERS=fond_gip&TRANSPARENT=true&VERSION=1.1.1&FORMAT=image%2Fpng&REQUEST=GetMap&STYLES=&SRS=EPSG%3A2154&BBOX=358976.61292821,6395407.8064641,430656.57422103,6467087.7677569&WIDTH=512&HEIGHT=512";

		testOGCOperationLogging("user4", request, 1, roles);
	}

	/**
	 * Test based on ogc_requests.txt example 5
	 * <p>
	 * </p>
	 * 
	 * <pre>
	 * test for 
	 * SERVICE=WMS
	 * REQUEST=GetCapabilities
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWmsGetCapabilities() throws Exception {

		final String request = "http://www.someserver.com/geoserver/pigma/wms?REQUEST=GetCapabilities&SERVICE=WMS&VERSION=1.1.1&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&FORMAT=image%2Fpng";
		testOGCOperationLogging("user5", request, 1, roles);
	}

	/**
	 * Test based on ogc_requests.txt example 6
	 * <p>
	 * </p>
	 * 
	 * <pre>
	 * test for 
	 * SERVICE=WMS
	 * REQUEST=DescribeLayer
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWmsDescribeLayer() throws Exception {

		final String request = "http://www.someserver.com/geoserver/pigma/wms?REQUEST=DescribeLayer&LAYERS=pigma%3Acantons&WIDTH=1&HEIGHT=1&SERVICE=WMS&VERSION=1.1.1&EXCEPTIONS=application%2Fvnd.ogc.se_inimage&FORMAT=image%2Fpng";
		testOGCOperationLogging("user6", request, 1, roles);
	}

	/**
	 * Test based on ogc_requests.txt example 7
	 * <p>
	 * </p>
	 * 
	 * <pre>
	 * test for 
	 * SERVICE=WMS
	 * REQUEST=GetLegendGraphic
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWmsGetLegendGraphic() throws Exception {

		final String request = "http://www.someserver.com/geoserver/fdp33/wms?request=GetLegendGraphic&format=image%2Fpng&width=20&height=20&layer=paln_eau_federaux&SCALE=4000000.0000000005";
		testOGCOperationLogging("user7", request, 1, roles);
	}

	/**
	 * Test based on ogc_requests.txt example 8
	 * <p>
	 * </p>
	 * 
	 * <pre>
	 * test for 
	 * SERVICE=wcs
	 * REQUEST=GetCapabilities
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWcsGetCapabilities() throws Exception {

		final String request = "http://www.someserver.com/geoserver/ows?service=wcs&version=1.0.0&request=GetCapabilities";

		testOGCOperationLogging("user8", request, 1, roles);
	}

	/**
	 * Test based on ogc_requests.txt example 10
	 * <p>
	 * 
	 * </p>
	 * 
	 * <pre>
	 * test for 
	 * SERVICE=WFS
	 * REQUEST=GetFeature
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPostWfsGetFeature() throws Exception {

		final String request = Utility.loadRequest("postWfsGetFeature.txt");
		testOGCOperationLogging("user10", request, 1, roles);
	}

	/**
	 * Test based on ogc_requests.txt example 11
	 * <p>
	 * 
	 * </p>
	 * 
	 * <pre>
	 * test for 
	 * SERVICE=WFS
	 * REQUEST=Update
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWfsUpdate() throws Exception {

		final String request = Utility.loadRequest("postWfsUpdate.txt");

		testOGCOperationLogging("user11", request, 1, roles);
	}

	/**
	 * <pre>
	 * http://www.someserver.com/wfs.cgi&
	 *		SERVICE=WFS&
	 *		VERSION=1.1.0&
	 *		REQUEST=GetFeature&
	 *		TYPENAME=InWaterA_1M&
	 *		FILTER=<Filter><Within><PropertyName>InWaterA_1M/wkbGeom<PropertyName>
	 *		<gml:Envelope><gml:lowerCorner>10 10<gml:lowerCorner>
	 *		<gml:upperCorner>20 20</gml:upperCorner></gml:Envelope>
	 *		</Within></Filter>
	 * </pre>
	 */
	@Test
	public void testWfsGetFeature() throws Exception {

		final String request = "http://www.someserver.com/wfs.cgi&SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeature&TYPENAME=InWaterA_1M&FILTER=<Filter><Within><PropertyName>InWaterA_1M/wkbGeom<PropertyName><gml:Envelope><gml:lowerCorner>10 10<gml:lowerCorner><gml:upperCorner>20 20</gml:upperCorner></gml:Envelope></Within></Filter>";
		testOGCOperationLogging("user20", request, 1, roles);
	}

	/**
	 * <p>
	 * This test case have got two layers in the TYPENAME key
	 * </p>
	 * 
	 * <pre>
	 * http://www.someserver.com/wfs.cgi&
	 *	SERVICE=WFS&
	 *	VERSION=1.1.0&
	 *	REQUEST=GetFeature&
	 *	PROPERTY=(InWaterA_1M/wkbGeom,InWaterA_1M/tileId)(BuiltUpA_1M/*)&
	 *	TYPENAME=InWaterA_1M,BuiltUpA_1M
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWfsGetFeatureTwoTypeName() throws Exception {

		final String request = "http://www.someserver.com/wfs.cgi&SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeature&PROPERTY=(InWaterA_1M/wkbGeom,InWaterA_1M/tileId)(BuiltUpA_1M/*)&TYPENAME=InWaterA_1M,BuiltUpA_1M";
		testOGCOperationLogging("user21", request, 2, roles);
	}

	/**
	 * 
	 * http://www.someserver.com/wfs.cgi& SERVICE=WFS& VERSION=1.1.0&
	 * REQUEST=GetFeature& NAMESPACE=xmlns(myns=http://www.someserver.com),
	 * xmlns(yourns=http://www.someotherserver.com)
	 * TYPENAME=myns:InWaterA_1M,your:BuiltUpA_1M
	 */
	@Test
	public void testWfsGetFeatureLayerWithNamespace() throws Exception {

		final String request = "http://www.someserver.com/wfs.cgi&SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeature&NAMESPACE=xmlns(myns=http://www.someserver.com),	xmlns(yourns=http://www.someotherserver.com)TYPENAME=myns:InWaterA_1M,your:BuiltUpA_1M";
		testOGCOperationLogging("user22", request, 2, roles);
	}

	/**
	 * Test for SERVICE:WCS Request: DescribeCoverage
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWcsDescribeCoverage() throws Exception {

		final String request = Utility.loadRequest("postWcsDescribeCoverage.txt");
		testOGCOperationLogging("user23", request, 1, roles);
	}

	/**
	 * <pre>
	 * SERVICE: WCS
	 * REQUEST: GetCoverage
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWcsGetCoverage() throws Exception {

		final String request = Utility.loadRequest("postWcsGetCoverage.txt");
		testOGCOperationLogging("user24", request, 1, roles);
	}

	/**
	 * <p>
	 * 
	 * </p>
	 * 
	 * <pre>
	 * test for 
	 * SERVICE=WFS
	 * REQUEST=Delete
	 * </pre>
	 */
	@Test
	public void testWfsDelete() throws Exception {

		final String request = Utility.loadRequest("postWfsDelete.txt");

		testOGCOperationLogging("user25", request, 1, roles);
	}

	/**
	 * <pre>
	 * test for 
	 * SERVICE=WFS
	 * REQUEST=Insert
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWfsInsert() throws Exception {

		final String request = Utility.loadRequest("postWfsInsert.txt");

		testOGCOperationLogging("user26", request, 1, roles);
	}

	/**
	 * <pre>
	 * test for 
	 * SERVICE=WMTS
	 * REQUEST=GetCapabilities
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWmtsGetCapabilities() throws Exception {

		final String request = "http://www.someserver.com/maps.cgi?service=WMTS&version=1.0.0&request=GetCapabilities";
		testOGCOperationLogging("user27", request, 1, roles);
	}

	/**
	 * <pre>
	 * test for 
	 * METHOD=GET
	 * SERVICE=WMTS
	 * REQUEST=GetTile
	 * </pre>
	 */
	@Test
	public void testGetWmtsTile() throws Exception {

		final String request = "http://www.someserver.com/maps.cgi?service=WMTS&request=GetTile&version=1.0.0&layer=etopo2&style=default&format=image/png&TileMatrixSet=WholeWorld_CRS_84&TileMatrix=10m&TileRow=1&TileCol=3";
		testOGCOperationLogging("user29", request, 1, roles);
	}

	/**
	 * <pre>
	 * test for 
	 * METHOD=GET
	 * SERVICE=WMTS
	 * REQUEST=GetFeatureInfo
	 * </pre>
	 */
	@Test
	public void testWmtsGetGetFeatureInfo() throws Exception {

		final String request = "http://www.someserver.com/maps.cgi?service=WMTS&request=GetFeatureInfo&version=1.0.0&layer=coastlines&style=default&format=image/png&TileMatrixSet=WholeWorld_CRS_84&TileMatrix=10m&TileRow=1&TileCol=3&J=86&I=132&InfoFormat=application/gml+xml; version=3.1";
		testOGCOperationLogging("user30", request, 1, roles);
	}

	/**
	 * 
	 * <pre>
	 * test for WMS-C. The WMS url is equal to  WMS-C url, thus the service key is  WMS-C.
	 * 
	 * METHOD=GET
	 * SERVICE=WMS 
	 * REQUEST=GetMap
	 * </pre>
	 */
	@Test
	public void testWmscGetMap() throws Exception {

		final String request = "http://www.someserver.com/wms-c/tilecache.py?LAYERS=basic&FORMAT=image/png&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&EXCEPTIONS=application/vnd.ogc.se_inimage&SRS=EPSG:4326&BBOX=-90,0,0,90&WIDTH=256&HEIGHT=256";
		testOGCOperationLogging("user31", request, 1, roles);
	}

	/**
	 * <pre>
	 * test for TMS. The WMS url is equal to WMS url, thus the service key is WMS.
	 * 
	 * METHOD=GET
	 * SERVICE=WMS 
	 * REQUEST=getcapabilities
	 * </pre>
	 */
	@Test
	public void testTmsGetCapabilities() throws Exception {

		final String request = "http://www.someserver.com/geoserver/gwc/service/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=getcapabilities&TILED=true";
		testOGCOperationLogging("user32", request, 1, roles);
	}

	/**
	 * Appends a log and tests if it was inserted in the log table.
	 * 
	 * @param user
	 * @param request
	 * @param expectedLogs expected logs
	 * 
	 * @throws Exception
	 */
	private void testOGCOperationLogging(final String user, final String request, final int expectedLogs,
			final String[] roles) throws Exception {

		List<Map<String, Object>> logList = OGCServiceStatistics.list();
		final int logSizeBefore = logList.size();

		final Date time = Calendar.getInstance().getTime();

		String ogcServiceMessage = OGCServiceMessageFormatter.format(user, time, request, "c2c", roles);

		LOGGER.info(ogcServiceMessage);

		logList = OGCServiceStatistics.list();
		assertEquals(logSizeBefore + expectedLogs, logList.size());
	}

}
