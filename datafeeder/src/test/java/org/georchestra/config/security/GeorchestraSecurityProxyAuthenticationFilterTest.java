/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.config.security;

import static org.georchestra.commons.security.SecurityHeaders.SEC_EMAIL;
import static org.georchestra.commons.security.SecurityHeaders.SEC_FIRSTNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_LASTNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ORG;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ORGNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_PROXY;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.georchestra.commons.security.SecurityHeaders;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class GeorchestraSecurityProxyAuthenticationFilterTest {

    private GeorchestraSecurityProxyAuthenticationFilter filter;
    private MockHttpServletRequest request;

    public @Before void before() {
        request = new MockHttpServletRequest();
        filter = new GeorchestraSecurityProxyAuthenticationFilter();
    }

    @Test
    public void getPreAuthenticatedCredentials_notPreauth() {
        assertNull(request.getHeader(SEC_PROXY));
        GeorchestraUserDetails preauth = filter.getPreAuthenticatedPrincipal(request);
        assertNull(preauth);
    }

    @Test
    public void getPreAuthenticatedCredentials_NoUserNameIsAnnonymous() {
        request.addHeader(SEC_PROXY, "true");
        GeorchestraUserDetails preauth = filter.getPreAuthenticatedPrincipal(request);
        assertNotNull(preauth);
        assertTrue(preauth.isAnonymous());
        assertEquals("anonymousUser", preauth.getUsername());
    }

    @Test
    public void getPreAuthenticatedCredentials() {
        addEncodedHeader(SEC_PROXY, "true");
        Map<String, String> headers = new HashMap<>();
        headers.put(SEC_USERNAME, "test?user");
        headers.put(SEC_FIRSTNAME, "Gábriel");
        headers.put(SEC_LASTNAME, "Roldán");
        headers.put(SEC_ORG, "test'org");
        headers.put(SEC_ORGNAME, "ジョルケストラコミュニティ");
        headers.put(SEC_EMAIL, "test@email.com");

        headers.forEach(this::addEncodedHeader);

        GeorchestraUserDetails auth = filter.getPreAuthenticatedPrincipal(request);
        assertNotNull(auth);
        assertFalse(auth.isAnonymous());

        assertEquals(headers.get(SEC_USERNAME), auth.getUsername());
        assertEquals(headers.get(SEC_FIRSTNAME), auth.getFirstName());
        assertEquals(headers.get(SEC_LASTNAME), auth.getLastName());
        assertEquals(headers.get(SEC_ORG), auth.getOrganization());
        assertEquals(headers.get(SEC_ORGNAME), auth.getOrganizationName());
        assertEquals(headers.get(SEC_EMAIL), auth.getEmail());
    }

    private void addEncodedHeader(String name, String value) {
        String encoded = SecurityHeaders.encodeBase64((String) value);
        addHeader(name, encoded);
    }

    private void addHeader(String name, String value) {
        request.addHeader(name, value);
    }
}
