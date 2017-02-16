package org.georchestra.dlform ;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;


public class AbstractApplicationTest {

    protected AbstractApplication ctrl;


    /**
     * Generates a legit request (i.e. containing all the parameters
     * to be a successful request.
     * @return
     * @throws Exception
     */
    protected HttpServletRequest generateLegitRequest() throws Exception {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getHeader("sec-firstname")).thenReturn("Scott");
        Mockito.when(req.getHeader("sec-lastname")).thenReturn("Tiger");
        Mockito.when(req.getHeader("sec-orgname")).thenReturn("geOrchestra");
        Mockito.when(req.getHeader("sec-email")).thenReturn("root@localhost");
        Mockito.when(req.getHeader("sec-tel")).thenReturn("+331234567890");
        Mockito.when(req.getParameter("datause")).thenReturn("1,2,3,4");
        Mockito.when(req.getParameter("comment")).thenReturn("OpenData For The Win.");
        Mockito.when(req.getParameter("ok")).thenReturn("on");
        Mockito.when(req.getHeader("sec-username")).thenReturn("testuser");
        Mockito.when(req.getParameter("sessionid")).thenReturn("JSSESSIONID-1234567");
        Mockito.when(req.getParameter("json_spec")).thenReturn(generateLegitJsonSpec());
        Mockito.when(req.getParameter("fname")).thenReturn("/dev/null");
        Mockito.when(req.getParameter("id")).thenReturn("42");
        return req;
    }


    protected String generateLegitJsonSpec() throws JSONException {
        JSONObject ret = new JSONObject();
        // Global properties
        JSONObject bbox = new JSONObject().put("value", new JSONArray().put(-180).put(-90).put(180).put(90))
                .put("srs", "EPSG:4326");
        ret.put("globalProperties", new JSONObject().put("projection", "EPSG:4326")
                                            .put("resolution", "600")
                                            .put("rasterFormat", "GeoTIFF")
                                            .put("vectorFormat", "KML")
                                            .put("bbox", bbox));
        // sample layers
        ret.put("layers", new JSONArray().put(new JSONObject()
                                .put("projection", "EPSG:900913")
                                .put("resolution", "1200")
                                .put("format", "null")
                                .put("owsType", "WFS")
                                .put("owsUrl", "http://georchestra.org/geoserver/ows")
                                .put("layerName", "sample:layer")
                                .put("bbox", "null"))
                            .put(new JSONObject()
                                .put("projection", "EPSG:900913")
                                .put("resolution", "1200")
                                .put("format", "Shapefile")
                                .put("owsType", "WFS")
                                .put("owsUrl", "http://georchestra.org/geoserver/ows")
                                .put("layerName", "sample:layer")
                                .put("bbox", bbox)));

        return ret.toString(4);
    }

    @Before
    public void setUp() throws Exception {
        DataSource ds = Mockito.mock(DataSource.class);
        // Since we cannot instantiate the abstractApplication class directly
        // we will use ExtractorApp for the current test suite
        ctrl = new ExtractorApp(ds, true);
    }

    @Test
    public void testInitializeVariables() throws Exception {
        HttpServletRequest req = generateLegitRequest();

        DownloadQuery q = ctrl.initializeVariables(req);
        assertFalse("Expected valid form", q.isInvalid());
    }

    @Test
    public void testBadInitializeVariables() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getHeader("sec-firstname")).thenReturn(null);

        DownloadQuery q = ctrl.initializeVariables(req);
        assertTrue("Expected invalid form", q.isInvalid());
    }

    /**
     * Tests when the controller is not activated.
     * @throws Exception
     */
    @Test
    public final void testDeactivatedController() throws Exception {
        ctrl.setActivated(false);
        MockHttpServletResponse mockedResponse = new MockHttpServletResponse();

        ctrl.handleRequest(null, mockedResponse);

        JSONObject ret = new JSONObject(mockedResponse.getContentAsString());

        assertTrue(ret.get("status").equals("unavailable"));
        assertTrue(ret.get("reason").equals("downloadform disabled"));
    }

}