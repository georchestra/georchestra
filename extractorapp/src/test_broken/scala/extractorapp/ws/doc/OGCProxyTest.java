package extractorapp.ws.doc;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import extractorapp.ws.doc.OGCProxy;

/**
 * Test OGCProxy
 * @author yoann buch - yoann.buch@gmail.com
 *
 */

public class OGCProxyTest {
    
    private OGCProxy _proxy = new OGCProxy();
    
    // set up mocks to simulate request
    private MockHttpServletRequest _request = new MockHttpServletRequest();
    private MockHttpServletResponse _response = new MockHttpServletResponse();
    
    @Before
    public void setUp() {
        
        // fake Firefox browser 
        _request.addHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.11) Gecko/2009060310 Ubuntu/8.10 (intrepid) Firefox/3.0.11 GTB5");
    }    
    
    /**
     * Test a known distant web service
     * @throws Exception
     */
    @Test(timeout=10000)
    public void testGetFeatureInfo() throws Exception {
        
        // remote host to test
        String url = "http://demo.opengeo.org/geoserver/wms?service=WMS&version=1.1.0&request=getfeatureinfo&layers=topp%3Atasmania_water_bodies&query_layers=topp%3Atasmania_water_bodies&styles=&bbox=140.5315%2C-44.423%2C151.7815%2C-38.798&srs=EPSG%3A4326&feature_count=10&x=281&y=141&height=256&width=512&info_format=application/vnd.ogc.gml";

        assertEquals(true, _proxy.isHostAllowed("demo.opengeo.org")); // host should be allowed

        // set method
        _request.setMethod("GET"); 
        
        // launch request 
        _proxy.handleGETRequest(_request, _response, url);
        
        // tests

        assertEquals(_response.getErrorMessage(), 200, _response.getStatus()); // status code 200 expected : OK

        String contentType = (String) _response.getHeaders("Content-Type").get(0);
        assertEquals(_response.getErrorMessage(), "application/vnd.ogc.gml", contentType.split(";")[0]); // content type valid
        
        //content from this server is too volatile
        //String expectedContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><wfs:FeatureCollection xmlns=\"http://www.opengis.net/wfs\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:topp=\"http://www.openplans.org/topp\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openplans.org/topp http://demo.opengeo.org/geoserver/wfs?service=WFS&amp;version=1.0.0&amp;request=DescribeFeatureType&amp;typeName=topp:tasmania_water_bodies http://www.opengis.net/wfs http://demo.opengeo.org/geoserver/schemas/wfs/1.0.0/WFS-basic.xsd\"><gml:boundedBy><gml:Box srsName=\"http://www.opengis.net/gml/srs/epsg.xml#4326\"><gml:coordinates xmlns:gml=\"http://www.opengis.net/gml\" decimal=\".\" cs=\",\" ts=\" \">146.673309,-41.988892 146.825531,-41.775558</gml:coordinates></gml:Box></gml:boundedBy><gml:featureMember><topp:tasmania_water_bodies fid=\"tasmania_water_bodies.4\"><topp:the_geom><gml:MultiPolygon srsName=\"http://www.opengis.net/gml/srs/epsg.xml#4326\"><gml:polygonMember><gml:Polygon><gml:outerBoundaryIs><gml:LinearRing><gml:coordinates xmlns:gml=\"http://www.opengis.net/gml\" decimal=\".\" cs=\",\" ts=\" \">146.697205,-41.988892 146.688873,-41.988052 146.682465,-41.985832 146.67746,-41.976105 146.673859,-41.973328 146.674133,-41.966393 146.673309,-41.960281 146.674408,-41.95417 146.680817,-41.937218 146.696625,-41.907219 146.69693,-41.900551 146.694122,-41.895554 146.693573,-41.889442 146.695526,-41.883888 146.702179,-41.875275 146.703583,-41.869164 146.700256,-41.858055 146.697754,-41.853058 146.684418,-41.834999 146.680817,-41.83139 146.675812,-41.821671 146.674988,-41.815552 146.680267,-41.797783 146.683319,-41.792503 146.684418,-41.786949 146.691071,-41.778336 146.69693,-41.775558 146.704132,-41.776947 146.708588,-41.781387 146.714691,-41.789726 146.722748,-41.797226 146.728027,-41.800835 146.733582,-41.803055 146.75,-41.804718 146.761658,-41.816666 146.766663,-41.826393 146.772217,-41.828613 146.780548,-41.828613 146.808319,-41.821671 146.815796,-41.820557 146.823029,-41.822777 146.825531,-41.833061 146.824677,-41.853615 146.822754,-41.858894 146.816925,-41.868607 146.80304,-41.871666 146.786377,-41.872772 146.777191,-41.872498 146.764984,-41.876389 146.761108,-41.880554 146.759979,-41.886665 146.762207,-41.898338 146.767487,-41.908607 146.774414,-41.917221 146.779694,-41.927498 146.777771,-41.93222 146.765259,-41.943611 146.754425,-41.963333 146.749695,-41.96666 146.732727,-41.974716 146.728027,-41.97805 146.703857,-41.987778 146.697205,-41.988892</gml:coordinates></gml:LinearRing></gml:outerBoundaryIs></gml:Polygon></gml:polygonMember></gml:MultiPolygon></topp:the_geom><topp:AREA>1066494066</topp:AREA><topp:PERIMETER>1071999090</topp:PERIMETER><topp:WATER_TYPE>Lake</topp:WATER_TYPE><topp:CNTRY_NAME>Australia</topp:CNTRY_NAME><topp:CONTINENT>Australia</topp:CONTINENT></topp:tasmania_water_bodies></gml:featureMember></wfs:FeatureCollection>";
        //assertEquals(expectedContent, _response.getContentAsString().trim()); // content 
    }
    
    /**
     * Test a known web service. Does not test the content considering the size
     * @throws Exception
     */
    @Test(timeout=10000)
    public void testGetCapabilities() throws Exception {
        
        // remote host to test
        String url = "http://demo.opengeo.org/geoserver/wms?service=WMS&version=1.1.0&request=getcapabilities";
        
        assertEquals(true, _proxy.isHostAllowed("demo.opengeo.org")); // host should be allowed

        // set method
        _request.setMethod("GET"); 

        // launch request
        _proxy.handleGETRequest(_request, _response, url);
            
        assertEquals(_response.getErrorMessage(), 200, _response.getStatus()); // status code 200 expected : OK
        
        String contentType = (String) _response.getHeaders("Content-Type").get(0);
        assertEquals(_response.getErrorMessage(), "application/vnd.ogc.wms_xml",  contentType.split(";")[0]); // content type valid
    }

    @Test(timeout=10000)
    public void testGetFeatureWithPOST() throws Exception {

        String url = "http://demo.opengeo.org/geoserver/wfs";

        String content = "<wfs:GetFeature xmlns:wfs=\"http://www.opengis.net/wfs\" service=\"WFS\" version=\"1.1.0\" maxFeatures=\"1\" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/WFS-transaction.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:topp=\"http://www.openplans.org/topp\"><wfs:Query typeName=\"topp:states\" xmlns:topp=\"http://www.openplans.org/topp\"/></wfs:GetFeature>";

        // set method
        _request.setMethod("POST");

        // set Content-Type
        // /!\ for some reason setContentType does not the job!
        //_request.setContentType("application-xml");
        _request.addHeader("Content-Type", "application-xml");

        // set content
        _request.setContent(content.getBytes());

        // launch request
        _proxy.handlePOSTRequest(_request, _response, url);

        assertEquals(_response.getErrorMessage(), 200, _response.getStatus()); // status code 200 espected : OK
        assertTrue(_response.getContentAsString().contains("<wfs:FeatureCollection numberOfFeatures=\"1\""));
    }


    @Test(timeout=10000)
    public void testHeaders() throws Exception {
       
        // remote host to test
        String url = "http://drebretagne-geobretagne.int.lsn.camptocamp.com/geoserver/wms?request=GetCapabilities&service=WMS";        
        _request.addHeader("Accept-Encoding", "gzip,deflate"); // some browser let servers return compressed data

        // set method
        _request.setMethod("GET"); 

        // launch request
        _proxy.handleGETRequest(_request, _response, url);

        assertEquals(_response.getErrorMessage(), 200, _response.getStatus()); // status code 200 expected : OK  
        
        // test that headers from server were copied in response
        // check known headers
        //assertEquals(true, _response.containsHeader("Content-Encoding")); // Content-Encoding exists
        //assertEquals("gzip", _response.getHeader("Content-Encoding").toString()); // the service tested returns gzip
                                                                                  // therefore headers must contain the gzip one
                                                                                  // to warn browser
    }
    
    /**
     * Test reject from proxy when it filters on host.
     * @throws Exception
     */
    @Test
    public void testForbiddenHost() throws Exception {
        
        // This test will work only if Proxy filters on final host
        if(_proxy.isFilteringOnFinalHost()) {

            // set method
            _request.setMethod("GET"); 
            
            // set host forbidden by Proxy    
            _proxy.handleGETRequest(_request, _response, "http://www.example.com/");
            
            assertEquals(_response.getErrorMessage(), 400, _response.getStatus()); // expect reject from proxy : status 400, bad request
        }
    }
    
    /**
     * Test reject from proxy when protocol is omitted
     * @throws Exception
     */
    @Test
    public void testMalformedUrl() throws Exception {
        
        // This test will work only if Proxy does not filter host
        if(!_proxy.isFilteringOnFinalHost()) {

            // set method
            _request.setMethod("GET"); 

            _proxy.handleGETRequest(_request, _response, "www.example.com"); // omit protocol in url
            
            assertEquals(_response.getErrorMessage(), 400, _response.getStatus()); // expect reject from proxy : status 400, bad request
        }
    }
    
    /**
     * Test reject from proxy when protocol is not http
     * @throws Exception
     */
    @Test
    public void testWrongProtocol() throws Exception {
        
        // set allowed host 
        // but wrong protocol
        String url = "ftp://" + provideValidFinalHost();   

        // set method
        _request.setMethod("GET"); 

        _proxy.handleGETRequest(_request, _response, url);
        
        assertEquals(_response.getErrorMessage(), 400, _response.getStatus()); // expect reject from proxy : status 400, bad request
    }
    
    /**
     * Test the method defining if one host is part of the allowed ones
     * @return String: Valid final host according to the proxy's filtration policy
     */
    private String provideValidFinalHost() {
        
        if(_proxy.isFilteringOnFinalHost()) {
            // Proxy filters, get the first one
            return OGCProxy._allowedHosts[0];
        }
        else {
            // No filtering
            return "www.example.com";
        }
    }
    
    /**
     * Test the method determining if a content type is valid
     * @throws Exception
     */
    @Test
    public void testIsContentTypeValid() throws Exception {
        
        // test if every single content type listed in the proxy will be validated
        assertEquals(true, areContentTypeAllValid());
    }
    
    /**
     * Helps testIsContentTypeValid
     * @return
     */
    private boolean areContentTypeAllValid() {
        
        for(String contentType : OGCProxy._validContentTypes)
        {
            if(!_proxy.isContentTypeValid(contentType + ";text-encoding")) { // add text encoding (somtimes appended to the content type)
                return false;
            }
        }
        return true;
    }
    
    /**
     * Test method copying headers (except Host) sent by client to distant host request
     * @throws Exception
     */
    @Test
    public void testCopyHeadersToConnection() throws Exception {
        
        // add headers to request
        _request.addHeader("test", "myvalue");
        _request.addHeader("test2", "myvalue2");
        
        //add header host that should not be copied
        _request.addHeader("Host", "myhost"); // must ignore case
        _request.addHeader("host", "myhost"); // must ignore case

        // set method
        _request.setMethod("GET"); 
        
        // create fake connection
        URL  url = new URL("http://www.example.com");
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        
        // copy headers from request to connection
        _proxy.copyHeadersToConnection(_request, uc);
        
        assertEquals(3, uc.getRequestProperties().size()); // connection should contain headers
                                                           // but host header
    }

    
}
