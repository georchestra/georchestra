package org.georchestra.security;

import static org.georchestra.commons.security.SecurityHeaders.IMP_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.IMP_USERNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

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

    private void assertUntrustworthyServerFiltering(SecurityRequestHeaderFilter filter,
            MockHttpServletRequest untrustWorthyServer, HttpRequestBase proxyRequest) {
        assertFalse(filter.filter("some-random-header", untrustWorthyServer, proxyRequest));
        assertTrue(filter.filter(SEC_USERNAME, untrustWorthyServer, proxyRequest));
        assertTrue(filter.filter(SEC_ROLES, untrustWorthyServer, proxyRequest));
        assertTrue(filter.filter(IMP_USERNAME, untrustWorthyServer, proxyRequest));
        assertTrue(filter.filter(IMP_ROLES, untrustWorthyServer, proxyRequest));
    }

}
