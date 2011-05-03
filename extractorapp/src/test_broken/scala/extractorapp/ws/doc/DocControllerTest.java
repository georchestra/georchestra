package extractorapp.ws.doc;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;



/**
 * Test <br />
 * - DocController <br />
 * - A_DocService <br />
 * - WMCDocService <br />
 * - CSVDocService <br />
 * @author yoann buch - yoann.buch@gmail.com
 *
 */

public class DocControllerTest {
    
    private static final String DOMAIN_NAME = "extractorapp/";
    private DocController _controller = new DocController();
    
    // set up mocks to simulate request
    private MockHttpServletRequest _requestPost = new MockHttpServletRequest();
    private MockHttpServletResponse _responsePost = new MockHttpServletResponse();
    private MockHttpServletRequest _requestGet = new MockHttpServletRequest();
    private MockHttpServletResponse _responseGet = new MockHttpServletResponse();
    
    @Before
    public void setUp() {
        _requestPost.setMethod("POST");
        _requestGet.setMethod("GET");
    }

    /**
     * Test reject from server when the DocController is accessed with GET and does not receive the filename in argument
     */
    @Test
    public void testDocControllerGETNoFilename() throws Exception {
        
        // take the WMC url but do not provide the filename in argument
        _requestGet.setRequestURI("extractorapp/" + DocController.WMC_URL); 
        _controller.getCSVFile(_requestGet, _responseGet);
        assertEquals(400, _responseGet.getStatus()); // HTTP 400 Bad Request
        
        // take the CSV url but do not provide the filename in argument
        _requestGet.setRequestURI("extractorapp/" + DocController.CSV_URL); 
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
        String JSONContent = "{" +
                             "\"columns\":[\"col1\", \"col2\"]," +
                             "\"data\":[" +
                             "[\"1\",\"2\"]," + 
                             "[3,4]" +
                             "]" +
        		             "}"; 
        
        // csv content that client expect to receive
        String expectedCSVContent = "col1;col2\r\n1;2\r\n3;4";
             
        _requestPost.setRequestURI(DOMAIN_NAME + DocController.CSV_URL); // fake URI, Rest style
        _requestPost.setContent(JSONContent.getBytes()); // fake body containing json file
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
    
}
