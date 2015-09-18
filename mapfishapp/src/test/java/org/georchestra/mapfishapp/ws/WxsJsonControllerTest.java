package org.georchestra.mapfishapp.ws;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class WxsJsonControllerTest {

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public final void testNoDatadirBadProto() throws Exception {
        WxsJsonController ctrl = new WxsJsonController();
        ServletContext ctx = Mockito.mock(ServletContext.class);
        Mockito.when(ctx.getRealPath("/")).thenReturn(this.getClass().getResource("/").toURI().toString());
        ctrl.setServletContext(ctx);
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        ctrl.wxsServerJson(null, req, res);

        assertTrue("Expected BAD_REQUEST", res.getStatus() == HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(String.format("Unexpected message: %s", res.getContentAsString()),
                res.getContentAsString().contains("Bad parameter value"));
    }

    @Test
    public final void testNoDatadirLegitProto() throws Exception {
        WxsJsonController ctrl = new WxsJsonController();
        ServletContext ctx = Mockito.mock(ServletContext.class);
        Mockito.when(ctx.getRealPath("/")).thenReturn(new File(this.getClass().getResource("/").toURI()).toString());
        ctrl.setServletContext(ctx);
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        ctrl.wxsServerJson("wms", req, res);

        assertTrue("Unexpected HTTP status code, expected 200 OK", res.getStatus() == HttpServletResponse.SC_OK);
        assertTrue("Unexpected content received", res.getContentAsString().contains("json to test WxsJsonController"));
    }
    @Test
    public final void testWrongProto() throws IOException {
        GeorchestraConfiguration gc = Mockito.mock(GeorchestraConfiguration.class);
        Mockito.when(gc.activated()).thenReturn(true);
        WxsJsonController ctrl = new WxsJsonController();
        ctrl.setGeorchestraConfiguration(gc);
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        ctrl.wxsServerJson(null, req, res);

        assertTrue("Expected BAD_REQUEST", res.getStatus() == HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(String.format("Unexpected message: %s", res.getContentAsString()),
                res.getContentAsString().contains("Bad parameter value"));

        req = new MockHttpServletRequest();
        res = new MockHttpServletResponse();

        ctrl.wxsServerJson("CSW", req, res);

        assertTrue("Expected BAD_REQUEST", res.getStatus() == HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(String.format("Unexpected message: %s", res.getContentAsString()),
                res.getContentAsString().contains("Bad parameter value"));
    }

    @Test
    public final void testProtoFileNotFound() throws IOException {
        GeorchestraConfiguration gc = Mockito.mock(GeorchestraConfiguration.class);
        Mockito.when(gc.activated()).thenReturn(true);
        WxsJsonController ctrl = new WxsJsonController();
        ctrl.setGeorchestraConfiguration(gc);
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        ctrl.wxsServerJson("wms", req, res);

        assertTrue("Expected NOT_FOUND", res.getStatus() == HttpServletResponse.SC_NOT_FOUND);
        assertTrue(String.format("Unexpected message: %s", res.getContentAsString()),
                res.getContentAsString().contains("file not found"));
    }

    @Test
    public final void testProtoWms() throws IOException, URISyntaxException {
        URL url = this.getClass().getResource("/");
        String testResPath = url.toURI().getPath();
        assumeTrue("resource test file wms.servers.json not found, skipping test",
                new File(testResPath, "/wms.servers.json").exists());

        GeorchestraConfiguration gc = Mockito.mock(GeorchestraConfiguration.class);
        Mockito.when(gc.activated()).thenReturn(true);
        Mockito.when(gc.getContextDataDir()).thenReturn(testResPath);
        WxsJsonController ctrl = new WxsJsonController();
        ctrl.setGeorchestraConfiguration(gc);
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        ctrl.wxsServerJson("wms", req, res);

        assertTrue("Expected SC_OK, received " + res.getStatus(),
                res.getStatus() == HttpServletResponse.SC_OK);
        assertTrue(String.format("Unexpected message: %s", res.getContentAsString()),
                res.getContentAsString().contains("json to test WxsJsonController"));
    }

}
