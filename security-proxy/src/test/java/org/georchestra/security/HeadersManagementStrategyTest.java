package org.georchestra.security;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 4/24/2014.
 */
public class HeadersManagementStrategyTest {
    @Test
    public void testConfigureRequestHeaders_RemoveSecHeaders() throws Exception {
        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
        List<HeaderFilter> filters = createTrustLocalHostFilter();
        headerManagement.setFilters(filters);

        HttpRequestBase proxyRequest = new HttpGet("http://localhost/geonetwork");
        MockHttpServletRequest originalRequest = createTestRequest();
        headerManagement.configureRequestHeaders(originalRequest, proxyRequest);

        assertFalse(hasHeader("sec-username", proxyRequest));
        assertFalse(hasHeader("sec-roles", proxyRequest));
        assertTrue(hasHeader("other_header", proxyRequest));
    }

    /**
     * show that they are not removed when the request server is trustworthy
     */
    @Test
    public void testConfigureRequestHeaders_KeepSecHeaders_WhenRequestServerIsTrusted() throws Exception {
        HeadersManagementStrategy headerManagement = new HeadersManagementStrategy();
        List<HeaderFilter> filters = createTrustLocalHostFilter();
        headerManagement.setFilters(filters);

        HttpRequestBase proxyRequest = new HttpGet("http://localhost/geonetwork");
        MockHttpServletRequest originalRequest = createTestRequest();

        proxyRequest.setHeaders(new Header[0]);
        originalRequest.setRemoteHost("localhost");
        headerManagement.configureRequestHeaders(originalRequest, proxyRequest);

        assertTrue(hasHeader("sec-username", proxyRequest));
        assertTrue(hasHeader("sec-roles", proxyRequest));
        assertTrue(hasHeader("other_header", proxyRequest));

    }

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
        assertTrue(hasHeader("other_header", proxyRequest));
    }


    private MockHttpServletRequest createTestRequest() {
        MockHttpServletRequest originalRequest = new MockHttpServletRequest("get", "http://georchestra.org/geonetwork");
        originalRequest.setRemoteHost("someserver.com");
        originalRequest.addHeader("sec-username", "jeichar");
        originalRequest.addHeader("sec-roles", "ROLE_SV_ADMIN");
        originalRequest.addHeader("other_header", "value");
        return originalRequest;
    }

    private List<HeaderFilter> createTrustLocalHostFilter() {
        List<HeaderFilter> filters = new ArrayList<HeaderFilter>();
        List<String> trustedHosts = new ArrayList<String>();
        trustedHosts.add("localhost");
        final SecurityRequestHeaderFilter filter = new SecurityRequestHeaderFilter();
        filter.setTrustedHosts(trustedHosts);
        filters.add(filter);
        return filters;
    }

    private boolean hasHeader(String headerName, HttpRequestBase proxyRequest) {
        return proxyRequest.getHeaders(headerName).length > 0;
    }
}
