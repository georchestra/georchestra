package org.georchestra.security;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

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

        headerManagement.configureRequestHeaders(originalRequest, proxyRequest);

        assertFalse(hasHeader("sec-username", proxyRequest));
        assertFalse(hasHeader("sec-roles", proxyRequest));

        assertFalse(hasHeader("imp-username", proxyRequest));
        assertFalse(hasHeader("imp-roles", proxyRequest));

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
        return originalRequest;
    }

    private boolean hasHeader(String headerName, HttpRequestBase proxyRequest) {
        return proxyRequest.getHeaders(headerName).length > 0;
    }
}
