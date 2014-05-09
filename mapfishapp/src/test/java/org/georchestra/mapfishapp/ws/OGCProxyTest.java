package org.georchestra.mapfishapp.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test OGCProxy
 * @author yoann buch - yoann.buch@gmail.com
 *
 */

public class OGCProxyTest {
    
    private OGCProxy _proxy = new OGCProxy();
    
	private InputStream featureCollection;
	private InputStream getFeatureInfo;
	private InputStream getCapabilities;
	private InputStream getCapabilitiesGZipped;
	
	@Before
	public void setUp() throws Exception {
		featureCollection = OGCProxyTest.class.getResourceAsStream("/opengeo_featurecollection_post.xml");
		getFeatureInfo = OGCProxyTest.class.getResourceAsStream("/opengeo_getfeatureinfo_1.1.0.xml");
		getCapabilities = OGCProxyTest.class.getResourceAsStream("/opengeo_getcapabilities_1.1.0.xml");
		getCapabilitiesGZipped = OGCProxyTest.class.getResourceAsStream("/opengeo_getcapabilities_1.1.0.xml.gz");
	}

	@After
	public void tearDown() throws IOException {
    	mockedHttpUrlConnection.reset();
		try { featureCollection.close(); } catch (Throwable e) {};
		try { getFeatureInfo.close(); } catch (Throwable e) {};
		try { getCapabilities.close(); } catch (Throwable e) {};
	}

    private MockHttpURLConnection mockedHttpUrlConnection = new MockHttpURLConnection();
    
    
    public final String[] localHostList = {"localhost"};
    public final String[] emptyHostList = {};
 
    /**
     * Test a known distant web service
     * @throws Exception
     */
    @Test
    public void testGetFeatureInfo() throws Exception {
        MockHttpServletRequest _request = new MockHttpServletRequest();
        MockHttpServletResponse _response = new MockHttpServletResponse();
        
        mockedHttpUrlConnection.setContentType("application/vnd.ogc.gml");
        mockedHttpUrlConnection.setInputStream(getFeatureInfo);
        
        // remote host to test
        String url = "http://demo.opengeo.org/geoserver/wms?service=WMS&version=1.1.0&request=getfeatureinfo&layers=topp%3Atasmania_water_bodies&query_layers=topp%3Atasmania_water_bodies&styles=&bbox=140.5315%2C-44.423%2C151.7815%2C-38.798&srs=EPSG%3A4326&feature_count=10&x=281&y=141&height=256&width=512&info_format=application/vnd.ogc.gml";
        // host should be allowed
        assertTrue(_proxy.isHostAllowed("demo.opengeo.org"));

        // set method
        _request.setMethod("GET"); 
        
        // launch request 
        _proxy.handleGETRequest(_request, _response, url, mockedHttpUrlConnection);
        
        // tests

        assertEquals(_response.getErrorMessage(), 200, _response.getStatus()); // status code 200 expected : OK

        String contentType = (String) _response.getHeaders("Content-Type").get(0);
        assertEquals(_response.getErrorMessage(), "application/vnd.ogc.gml", contentType.split(";")[0]); // content type valid
    }
    
    /**
     * Test a known web service. Does not test the content considering the size
     * @throws Exception
     */
    @Test
    public void testGetCapabilities() throws Exception {
        MockHttpServletRequest _request = new MockHttpServletRequest();
        MockHttpServletResponse _response = new MockHttpServletResponse();
        
        mockedHttpUrlConnection.setContentType("application/vnd.ogc.wms_xml;charset=UTF-8");
        mockedHttpUrlConnection.setHeaderFields("Content-Type", "application/vnd.ogc.wms_xml;charset=UTF-8");
        
        mockedHttpUrlConnection.setInputStream(getCapabilities);
        
        // remote host to test
        String url = "http://demo.opengeo.org/geoserver/wms?service=WMS&version=1.1.0&request=getcapabilities";
        
        
        assertEquals(true, _proxy.isHostAllowed("demo.opengeo.org")); // host should be allowed

        // set method
        _request.setMethod("GET"); 

        // launch request
        _proxy.handleGETRequest(_request, _response, url, mockedHttpUrlConnection);
            
        assertEquals(_response.getErrorMessage(), 200, _response.getStatus()); // status code 200 expected : OK
        
        String contentType = (String) _response.getHeaders("Content-Type").get(0);
        assertEquals(_response.getErrorMessage(), "application/vnd.ogc.wms_xml",  contentType.split(";")[0]); // content type valid
    }

    @Test
    public void testGetFeatureWithPOST() throws Exception {

        MockHttpServletRequest _request = new MockHttpServletRequest();
        MockHttpServletResponse _response = new MockHttpServletResponse();

        mockedHttpUrlConnection.setContentType("application/xml");
        mockedHttpUrlConnection.setInputStream(featureCollection);
        
        String url = "http://demo.opengeo.org/geoserver/wfs";
        String content = "<wfs:GetFeature xmlns:wfs=\"http://www.opengis.net/wfs\" service=\"WFS\" "
        		+ "version=\"1.1.0\" maxFeatures=\"1\" xsi:schemaLocation=\"http://www.opengis.net/wfs "
        		+ "http://schemas.opengis.net/wfs/1.1.0/WFS-transaction.xsd\" "
        		+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:topp=\"http://www.openplans.org/topp\">"
        		+ "<wfs:Query typeName=\"topp:states\" xmlns:topp=\"http://www.openplans.org/topp\"/>"
        		+ "</wfs:GetFeature>";

        // set method
        _request.setMethod("POST");

        // set Content-Type
        _request.addHeader("Content-Type", "application/xml");

        // set content
        _request.setContent(content.getBytes());

        // launch request
        _proxy.handlePOSTRequest(_request, _response, url, mockedHttpUrlConnection);

        // status code 200 expected : OK
        assertEquals(_response.getErrorMessage(), 200, _response.getStatus());
        // should match the fake file
        assertTrue(_response.getContentAsString().contains("numberOfFeatures=\"49\""));
    }


    @Test
    public void testHeaders() throws Exception {
        MockHttpServletRequest _request = new MockHttpServletRequest();
        MockHttpServletResponse _response = new MockHttpServletResponse();        
        String url = "http://localhost/wms?request=GetCapabilities&service=WMS";        
        mockedHttpUrlConnection.setContentType("application/xml");
        mockedHttpUrlConnection.setInputStream(getCapabilitiesGZipped);

        mockedHttpUrlConnection.setHeaderFields("Content-Encoding", "gzip");
        
        _request.addHeader("Accept-Encoding", "gzip,deflate"); // some browser let servers return compressed data
        
        // set method
        _request.setMethod("GET"); 

        // launch request
        _proxy.handleGETRequest(_request, _response, url, mockedHttpUrlConnection);

        assertEquals(_response.getErrorMessage(), 200, _response.getStatus()); // status code 200 expected : OK  
        
        // test that headers from server were copied in response
        // check known headers
        assertTrue(_response.containsHeader("Content-Encoding")); // Content-Encoding exists
        assertEquals("gzip", _response.getHeader("Content-Encoding").toString()); // the service tested returns gzip
                                                                                  // therefore headers must contain the gzip one
                                                                                  // to warn browser
    }
    
    /**
     * Test reject from proxy when it filters on host.
     * @throws Exception
     */
    @Test
	public void testForbiddenHost() throws Exception {
        MockHttpServletRequest _request = new MockHttpServletRequest();
        MockHttpServletResponse _response = new MockHttpServletResponse();        

		String[] onlyLocalhost = { "localhost" };
		_proxy.setAllowedHosts(onlyLocalhost);
		assertTrue(_proxy.isFilteringOnFinalHost());
		// set method
		_request.setMethod("GET");

		// set host forbidden by Proxy
		_proxy.handleGETRequest(_request, _response, "http://www.example.com/", mockedHttpUrlConnection);
		// expect reject from proxy : status 400, bad request
		assertEquals(_response.getErrorMessage(), 400, _response.getStatus());

	}
    @Test
    public void testLegitHostWithBadReturnedContentType() throws Exception {
        MockHttpServletRequest _request = new MockHttpServletRequest();
        MockHttpServletResponse _response = new MockHttpServletResponse();        

		// same test on a more opened proxy
		_proxy.setAllowedHosts(null);
		assertFalse(_proxy.isFilteringOnFinalHost());
		// set method
		_request.setMethod("GET");
		mockedHttpUrlConnection.setContentType(null);
		// set host forbidden by Proxy		
		_proxy.handleGETRequest(_request, _response, "http://www.example.com/", mockedHttpUrlConnection);
		// content-type returned is null
		assertEquals(_response.getErrorMessage(), 403, _response.getStatus());
    }
    
    /**
     * Test reject from proxy when protocol is omitted
     * @throws Exception
     */
    @Test
    public void testMalformedUrl() throws Exception {
        MockHttpServletRequest _request = new MockHttpServletRequest();
        MockHttpServletResponse _response = new MockHttpServletResponse();        
       
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
        MockHttpServletRequest _request = new MockHttpServletRequest();
        MockHttpServletResponse _response = new MockHttpServletResponse();        
      
        // set allowed host 
        // but wrong protocol
        String url = "ftp://" + provideValidFinalHost();   

        // set method
        _request.setMethod("GET"); 

        _proxy.handleGETRequest(_request, _response, url, mockedHttpUrlConnection);
        
        assertEquals(_response.getErrorMessage(), 400, _response.getStatus()); // expect reject from proxy : status 400, bad request
    }
    
    /**
     * Test the method defining if one host is part of the allowed ones
     * @return String: Valid final host according to the proxy's filtration policy
     */
    private String provideValidFinalHost() {
        
        if(_proxy.isFilteringOnFinalHost()) {
            // Proxy filters, get the first one
            return _proxy.getAllowedHost(0);
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
        assertTrue(areContentTypeAllValid());
    }
    
    /**
     * Helps testIsContentTypeValid
     * @return
     */
    private boolean areContentTypeAllValid() {
        
        for(String contentType : _proxy.getValidContentTypes())
        {
            if(!_proxy.isContentTypeValid(contentType + ";text-encoding")) { // add text encoding (sometimes appended to the content type)
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

 
        MockHttpServletRequest _request = new MockHttpServletRequest();
        MockHttpServletResponse _response = new MockHttpServletResponse();        
        
        // add headers to request
        _request.addHeader("test", "myvalue");
        _request.addHeader("test2", "myvalue2");
        
        //add header host that should not be copied
        _request.addHeader("Host", "myhost"); // must ignore case
        _request.addHeader("host", "myhost"); // must ignore case

        // set method
        _request.setMethod("GET");
 
       
        // copy headers from request to connection
        _proxy.copyHeadersToConnection(_request, mockedHttpUrlConnection);
        assertEquals(2, mockedHttpUrlConnection.getRequestProperties().size()); // connection should contain headers
    }

    
}
