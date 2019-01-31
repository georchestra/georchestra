package org.georchestra.mapfishapp.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.georchestra.mapfishapp.ws.classif.MockWFSDataStoreFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.ReflectionUtils;


/**
 * Test <br />
 * - DocController <br />
 * - A_DocService <br />
 * - WMCDocService <br />
 * - CSVDocService <br />
 * - SLDDocService <br />
 * @author yoann buch - yoann.buch@gmail.com
 *
 */

public class DocControllerTest {

    private static final String DOMAIN_NAME = "mapfishapp/";
    private DocController _controller = new DocController();

    // sets up mocks to simulate request
    private MockHttpServletRequest _requestPost = new MockHttpServletRequest();
    private MockHttpServletResponse _responsePost = new MockHttpServletResponse();
    private MockHttpServletRequest _requestGet = new MockHttpServletRequest();
    private MockHttpServletResponse _responseGet = new MockHttpServletResponse();

    private MockConnectionPool mockConnectionHelper = new MockConnectionPool();
    private DataSource mockDataSource = mockConnectionHelper.create();

    @Before
    public void setUp() {
        _requestPost.setMethod("POST");
        _requestGet.setMethod("GET");

        File workDir = new File(".");
        File testTempDir = new File(workDir.getAbsolutePath() + File.separatorChar + "test-temporary");
        testTempDir.deleteOnExit();

        _controller.setDocTempDir(testTempDir.getAbsolutePath());
        _controller.setConnectionPool(mockDataSource);

    }

    /**
     * Test reject from server when the DocController is accessed with GET and does not receive the filename in argument
     */
    @Test
    public void testDocControllerGETNoFilename() throws Exception {

        // take the WMC url but do not provide the filename in argument
        _requestGet.setRequestURI("mapfishapp/" + DocController.WMC_URL);
        _controller.getCSVFile(_requestGet, _responseGet);
        assertEquals(400, _responseGet.getStatus()); // HTTP 400 Bad Request

        // take the CSV url but do not provide the filename in argument
        _requestGet.setRequestURI("mapfishapp/" + DocController.CSV_URL);
        _controller.getCSVFile(_requestGet, _responseGet);
        assertEquals(400, _responseGet.getStatus()); // HTTP 400 Bad Request

    }

    /**
     * Test a complete scenario. User sends .wmc file via POST and gets back the generated file path.
     * Then user asks via GET to get back the file stored on the server
     * @throws Exception
     */
    @Test
    public void testWMCService() throws Exception {

        // valid wmc file content
        String fileContent = "<ViewContext xmlns=\"http://www.opengis.net/context\" version=\"1.1.0\" id=\"OpenLayers_Context_133\" xsi:schemaLocation=\"http://www.opengis.net/context http://schemas.opengis.net/context/1.1.0/context.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><General><Window width=\"1233\" height=\"342\"/><BoundingBox minx=\"-201405.7589\" miny=\"2245252.767\" maxx=\"598866.8058\" maxy=\"2467226.179\" SRS=\"EPSG:2154\"/><Title/><Extension><ol:maxExtent xmlns:ol=\"http://openlayers.org/context\" minx=\"47680.03567\" miny=\"2267644.975\" maxx=\"349781.0112\" maxy=\"2444833.970\"/></Extension></General><LayerList><Layer queryable=\"0\" hidden=\"0\"><Server service=\"OGC:WMS\" version=\"1.1.1\"><OnlineResource xlink:type=\"simple\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"http://drebretagne-geobretagne.int.lsn.camptocamp.com/geoserver/wms\"/></Server><Name>topp:communes_geofla</Name><Title>communes_geofla</Title><FormatList><Format current=\"1\">image/jpeg</Format></FormatList><StyleList><Style current=\"1\"><Name/><Title>Default</Title></Style></StyleList><Extension><ol:maxExtent xmlns:ol=\"http://openlayers.org/context\" minx=\"47680.03567\" miny=\"2267644.975\" maxx=\"349781.0112\" maxy=\"2444833.970\"/><ol:numZoomLevels xmlns:ol=\"http://openlayers.org/context\">16</ol:numZoomLevels><ol:units xmlns:ol=\"http://openlayers.org/context\">m</ol:units><ol:isBaseLayer xmlns:ol=\"http://openlayers.org/context\">true</ol:isBaseLayer><ol:displayInLayerSwitcher xmlns:ol=\"http://openlayers.org/context\">true</ol:displayInLayerSwitcher><ol:singleTile xmlns:ol=\"http://openlayers.org/context\">false</ol:singleTile></Extension></Layer></LayerList></ViewContext>"; // wmc file to be send
        mockConnectionHelper.setExpectedDocument(fileContent);

        _requestPost.setRequestURI(DOMAIN_NAME + DocController.WMC_URL); // fake URI, Rest style
        _requestPost.setContent(fileContent.getBytes()); // fake body containing wmc file

        _controller.storeWMCFile(_requestPost, _responsePost);
        assertEquals(201, _responsePost.getStatus()); // 201 Created

        String sResp = _responsePost.getContentAsString();

        // the response is actually a JSON object
        JSONObject oResp = new JSONObject(sResp);
        String filePath = (String) oResp.get(DocController.FILEPATH_VARNAME);

        // file name contains absolute url + CSV service path
        assertEquals(true, filePath.contains(DocController.WMC_URL));

        _requestGet.setRequestURI(DOMAIN_NAME + filePath); // fake URI, Rest style

        _controller.getWMCFile(_requestGet, _responseGet);
        assertEquals(200, _responseGet.getStatus()); // 200 OK
        assertEquals(fileContent, _responseGet.getContentAsString().trim()); // content sent is
                                                                               // back
    }

    /**
     * Test a complete scenario. User send JSON data via POST and gets back the generated csv file name.
     * Then user asks via GET to get back the csv file stored on the server
     * @throws Exception
     */
    @Test
    public void testCSVService() throws Exception {

        // json file to be send
        JSONObject JSONContent = new JSONObject().put("columns", new JSONArray().put("col1").put("col2"))
                .put("data",
                        new JSONArray().put(new JSONArray().put("1").put("2"))
                        .put(new JSONArray().put(3).put(4)));

        // csv content that client expect to receive
        String expectedCSVContent = "col1;col2\r\n1;2\r\n3;4";
        mockConnectionHelper.setExpectedDocument(expectedCSVContent);

        _requestPost.setRequestURI(DOMAIN_NAME + DocController.CSV_URL); // fake URI, Rest style
        _requestPost.setContent(JSONContent.toString().getBytes()); // fake body containing json file
        _requestPost.setContentType("application/json");

        _controller.storeCSVFile(_requestPost, _responsePost);
        assertEquals(201, _responsePost.getStatus()); // 201 Created

        String sResp = _responsePost.getContentAsString();

        // the response is actually a JSON object
        JSONObject oResp = new JSONObject(sResp);
        String filePath = (String) oResp.get(DocController.FILEPATH_VARNAME);

        // file name contains absolute url + CSV service path
        assertEquals(true, filePath.contains(DocController.CSV_URL));

        _requestGet.setRequestURI(DOMAIN_NAME + filePath); // fake URI, Rest style

        _controller.getCSVFile(_requestGet, _responseGet);
        assertEquals(200, _responseGet.getStatus()); // 200 OK
        assertEquals(expectedCSVContent, _responseGet.getContentAsString().trim()); // content sent is
                                                                               // back
    }

    /**
     * Test a complete scenario. User sends sld file content via POST and gets back the generated file path.
     * Then user asks via GET to get back the sld file stored on the server
     * @throws Exception
     */
    @Test
    public void testSLDService() throws Exception {

        // valid sld file
        String sldContent = "<StyledLayerDescriptor version=\"1.1.0\" xsi:schemaLocation=\"http://schemas.opengis.net/sld/1.1.0/StyledLayerDescriptor.xsd\" xmlns=\"http://www.opengis.net/sld\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:se=\"http://www.opengis.net/se\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> <NamedLayer><se:Name>OCEANSEA_1M:Foundation</se:Name><UserStyle><se:Name>GEOSYM</se:Name><IsDefault>1</IsDefault><se:FeatureTypeStyle><se:FeatureTypeName>Foundation</se:FeatureTypeName><se:Rule><se:Name>main</se:Name><se:PolygonSymbolizer uom=\"http://www.opengis.net/sld/units/pixel\"><se:Name>MySymbol</se:Name><se:Description><se:Title>Example Symbol</se:Title><se:Abstract>This is just a simple example.</se:Abstract></se:Description><se:Geometry><ogc:PropertyName>GEOMETRY</ogc:PropertyName></se:Geometry><se:Fill><se:SvgParameter name=\"fill\">#96C3F5</se:SvgParameter></se:Fill></se:PolygonSymbolizer></se:Rule></se:FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>";
        mockConnectionHelper.setExpectedDocument(sldContent);

        _requestPost.setRequestURI(DOMAIN_NAME + DocController.SLD_URL); // fake URI, Rest style
        _requestPost.setContent(sldContent.getBytes()); // fake body containing sld file
        _requestPost.setContentType("application/vnd.ogc.sld+xml");

        _controller.doSLDPost(_requestPost, _responsePost);
        assertEquals(201, _responsePost.getStatus()); // 201 Created

        String sResp = _responsePost.getContentAsString();

        // the response is actually a JSON object
        JSONObject oResp = new JSONObject(sResp);
        String filePath = (String) oResp.get(DocController.FILEPATH_VARNAME);

        // file name contains absolute url + CSV service path
        assertEquals(true, filePath.contains(DocController.SLD_URL));

        _requestGet.setRequestURI(DOMAIN_NAME + filePath); // fake URI, Rest style

        _controller.getSLDFile(_requestGet, _responseGet);
        assertEquals(200, _responseGet.getStatus()); // 200 OK
        assertEquals(sldContent, _responseGet.getContentAsString().trim()); // content sent is
                                                                               // back
    }

    @Test
    @Ignore("Moving to wfs-ng, tests are known to be broken")
    public void testClassifier() throws Exception {

        int minSize = 4;
        int maxSize = 20;
        int classCount = 3;

        JSONObject jsReq = new JSONObject()
            .put("type", "PROP_SYMBOLS")
            .put("wfs_url", "https://sdi.georchestra.org/geoserver/wfs?service=WFS&request=GetCapabilities&version=1.0.0")
            .put("layer_name", "geor:sdi")
            .put("attribute_name", "csw_db")
            .put("class_count", classCount)
            .put("min_size", minSize)
            .put("symbol_type", "point")
            .put("max_size", maxSize);

        _requestPost.setRequestURI(DOMAIN_NAME + DocController.SLD_URL); // fake URI, Rest style
        _requestPost.setContent(jsReq.toString().getBytes()); // fake body containing json request
        _requestPost.setContentType("application/json");

        // Using a fake WFSDataStoreFactory to avoid remote connection to WFS
        _controller.setWFSDataStoreFactory(new MockWFSDataStoreFactory());

        _controller.doSLDPost(_requestPost, _responsePost);

        assertEquals(200, _responseGet.getStatus()); // 200 OK
    }

    @Test
    public void testIndentDataXee() throws Exception {
        assumeTrue("file does not exist, which is unlikely if you are running the testsuite under linux. Skipping test",
                new File("/etc/passwd").exists());

        final String xeeVuln = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
 + "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]><foo>&xxe;</foo>";

        A_DocService ads = new A_DocService("xml", "application/xml", "/tmp", null) {};

        Method id = ReflectionUtils.findMethod(ads.getClass(), "indentData", String.class);
        id.setAccessible(true);

        String ret = (String) ReflectionUtils.invokeMethod(id, ads, xeeVuln);

        // Length expected of the string (without external entity resolution < 150 chars)
        assertTrue("XML generated seems too long, vulnerable to XEE attacks ?",
                ret.length() < 150);
    }
}
