package org.georchestra.security;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 4/24/2014.
 */
public class HeadersManagementStrategyTest {

    /**
     * Show that by default the headers are removed
     */
    @Test
    public void testConfigureRequestHeaders_RemoveSecHeaders_DefaultBehaviour() throws Exception {
        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();

        HttpRequestBase proxyRequest = new HttpGet("http://localhost/geonetwork");
        MockHttpServletRequest originalRequest = createTestRequest();

        headerManagement.configureRequestHeaders(originalRequest, proxyRequest, false);

        assertFalse(hasHeader("sec-username", proxyRequest));
        assertFalse(hasHeader("sec-roles", proxyRequest));

        assertFalse(hasHeader("imp-username", proxyRequest));
        assertFalse(hasHeader("imp-roles", proxyRequest));

        assertFalse(hasHeader("sec-org", proxyRequest));
        assertFalse(hasHeader("sec-email", proxyRequest));
        assertFalse(hasHeader("sEc-capitalize", proxyRequest));
        assertFalse(hasHeader("sec-capitalize", proxyRequest));

        assertTrue(hasHeader("other_header", proxyRequest));
    }


    private MockHttpServletRequest createTestRequest() {
        MockHttpServletRequest originalRequest = new MockHttpServletRequest("get", "http://georchestra.org/geonetwork");
        originalRequest.setRemoteHost("someserver.com");
        originalRequest.addHeader("sec-username", "jeichar");
        originalRequest.addHeader("sec-roles", "ROLE_GN_ADMIN");
        originalRequest.addHeader("imp-username", "imp_user");
        originalRequest.addHeader("imp-roles", "ROLE_GN_IMP");
        originalRequest.addHeader("other_header", "value");
        originalRequest.addHeader("sec-org", "value");
        originalRequest.addHeader("sec-email", "value");
        originalRequest.addHeader("sEc-capitalize", "value");
        return originalRequest;
    }

    private boolean hasHeader(String headerName, HttpRequestBase proxyRequest) {
        return proxyRequest.getHeaders(headerName).length > 0;
    }

    @Test
    public void testHeaderForProxy() throws Exception {
        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
        headerManagement.setHeaderProviders(Collections.<HeaderProvider>singletonList(new SecurityRequestHeaderProvider()));

        HttpRequestBase proxyRequest = new HttpGet("http://sdi.georchestra.org/geonetwork");
        MockHttpServletRequest originalRequest = createTestRequest();
        Authentication auth = new UsernamePasswordAuthenticationToken("jeichar", "random");
        SecurityContextHolder.getContext().setAuthentication(auth);
        headerManagement.configureRequestHeaders(originalRequest, proxyRequest, true);
        assertTrue(hasHeader("sec-username", proxyRequest));
        assertEquals(proxyRequest.getHeaders("sec-username")[0].getValue(), "jeichar");


        proxyRequest = new HttpGet("http://sdi.georchestra.org/geonetwork");
        originalRequest = createTestRequest();
        headerManagement.configureRequestHeaders(originalRequest, proxyRequest, false);
        assertFalse(hasHeader("sec-username", proxyRequest));


        proxyRequest = new HttpGet("http://sdi.georchestra.org/geonetwork");
        originalRequest = createTestRequest();
        headerManagement.configureRequestHeaders(originalRequest, proxyRequest, true);
        assertTrue(hasHeader("sec-username", proxyRequest));
        assertEquals(proxyRequest.getHeaders("sec-username")[0].getValue(), "jeichar");
    }

}
