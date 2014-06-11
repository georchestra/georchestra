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
public class SecurityRequestHeaderFilterTest {
    @Test
    public void testFilterLocalhost() throws Exception {
        final SecurityRequestHeaderFilter filter = new SecurityRequestHeaderFilter();
        HttpRequestBase proxyRequest = new HttpGet("http://localhost/geonetwork");

        MockHttpServletRequest untrustWorthyServer = new MockHttpServletRequest("get", "http://destServer/geonetwork");
        assertUntrustworthyServerFiltering(filter, untrustWorthyServer, proxyRequest);

    }

    private void assertUntrustworthyServerFiltering(SecurityRequestHeaderFilter filter, MockHttpServletRequest untrustWorthyServer, HttpRequestBase proxyRequest) {
        assertFalse(filter.filter("some-random-header", untrustWorthyServer, proxyRequest));
        assertTrue(filter.filter("sec-username", untrustWorthyServer, proxyRequest));
        assertTrue(filter.filter("sec-roles", untrustWorthyServer, proxyRequest));
        assertTrue(filter.filter("imp-username", untrustWorthyServer, proxyRequest));
        assertTrue(filter.filter("imp-roles", untrustWorthyServer, proxyRequest));
    }

}
