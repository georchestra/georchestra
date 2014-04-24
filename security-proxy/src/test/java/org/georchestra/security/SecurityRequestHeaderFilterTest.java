package org.georchestra.security;

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
public class SecurityRequestHeaderFilterTest {
    @Test
    public void testFilterLocalhost() throws Exception {
        List<String> trustedHosts = new ArrayList<String>();
        trustedHosts.add("localhost");
        final SecurityRequestHeaderFilter filter = new SecurityRequestHeaderFilter();
        filter.setTrustedHosts(trustedHosts);
        HttpRequestBase proxyRequest = new HttpGet("http://localhost/geonetwork");

        MockHttpServletRequest untrustWorthyServer = new MockHttpServletRequest("get", "http://destServer/geonetwork");
        untrustWorthyServer.setRemoteHost("someServer.com");
        assertUntrustworthyServerFiltering(filter, untrustWorthyServer, proxyRequest);

        MockHttpServletRequest ipV4UntrustWorthyServer = new MockHttpServletRequest("get", "http://destServer/geonetwork");
        ipV4UntrustWorthyServer.setRemoteHost("192.8.8.2");
        assertUntrustworthyServerFiltering(filter, ipV4UntrustWorthyServer, proxyRequest);

        MockHttpServletRequest ipV6UntrustWorthyServer = new MockHttpServletRequest("get", "http://destServer/geonetwork");
        ipV6UntrustWorthyServer.setRemoteHost("2607:f0d0:1002:51::4");
        assertUntrustworthyServerFiltering(filter, ipV6UntrustWorthyServer, proxyRequest);

        MockHttpServletRequest ipV6UntrustWorthy2Server = new MockHttpServletRequest("get", "http://destServer/geonetwork");
        ipV6UntrustWorthy2Server.setRemoteHost("2607:f0d0:1002:0051:0000:0000:0000:0004");
        assertUntrustworthyServerFiltering(filter, ipV6UntrustWorthy2Server, proxyRequest);

        MockHttpServletRequest trustWorthyServer = new MockHttpServletRequest("get", "http://destServer/geonetwork");
        assertTrustworthyServerFiltering(filter, proxyRequest, trustWorthyServer);

        MockHttpServletRequest ipv4TrustWorthyServer = new MockHttpServletRequest("get", "http://destServer/geonetwork");
        ipv4TrustWorthyServer.setRemoteAddr("127.0.0.1");
        assertTrustworthyServerFiltering(filter, proxyRequest, ipv4TrustWorthyServer);

        MockHttpServletRequest ipv6TrustWorthyServer = new MockHttpServletRequest("get", "http://destServer/geonetwork");
        ipv6TrustWorthyServer.setRemoteAddr("::1");
        assertTrustworthyServerFiltering(filter, proxyRequest, ipv6TrustWorthyServer);
    }

    private void assertUntrustworthyServerFiltering(SecurityRequestHeaderFilter filter, MockHttpServletRequest untrustWorthyServer, HttpRequestBase proxyRequest) {
        assertFalse(filter.filter("some-random-header", untrustWorthyServer, proxyRequest));
        assertTrue(filter.filter("sec-username", untrustWorthyServer, proxyRequest));
        assertTrue(filter.filter("sec-roles", untrustWorthyServer, proxyRequest));
    }

    private void assertTrustworthyServerFiltering(SecurityRequestHeaderFilter filter, HttpRequestBase proxyRequest, MockHttpServletRequest ipv4TrustWorthyServer) {
        assertFalse(filter.filter("some-random-header", ipv4TrustWorthyServer, proxyRequest));
        assertFalse(filter.filter("sec-username", ipv4TrustWorthyServer, proxyRequest));
        assertFalse(filter.filter("sec-roles", ipv4TrustWorthyServer, proxyRequest));
    }
}
