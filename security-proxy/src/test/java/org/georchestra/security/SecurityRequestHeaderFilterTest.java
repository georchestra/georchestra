/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

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
