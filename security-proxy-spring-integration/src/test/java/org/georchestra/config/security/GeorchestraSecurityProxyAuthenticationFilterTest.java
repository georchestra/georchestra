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

import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ADDRESS;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_EMAIL;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_FIRSTNAME;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_LASTNAME;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_LASTUPDATED;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_NOTES;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORGNAME;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_ADDRESS;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_CATEGORY;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_DESCRIPTION;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_LINKAGE;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ROLES;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_TEL;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_TITLE;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_USERID;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.security.model.GeorchestraUser;
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
        assertNull(request.getHeader(SecurityHeaders.SEC_PROXY));
        GeorchestraUserDetails preauth = filter.getPreAuthenticatedPrincipal(request);
        assertNull(preauth);
    }

    @Test
    public void getPreAuthenticatedCredentials_NoUserNameIsAnnonymous() {
        request.addHeader(SecurityHeaders.SEC_PROXY, "true");
        GeorchestraUserDetails preauth = filter.getPreAuthenticatedPrincipal(request);
        assertNotNull(preauth);
        assertTrue(preauth.isAnonymous());
        assertEquals("anonymousUser", preauth.getUsername());
    }

    @Test
    public void getPreAuthenticatedCredentials() {
        addEncodedHeader(SecurityHeaders.SEC_PROXY, "true");
        Map<String, String> headers = new HashMap<>();

        headers.put(SEC_USERID, UUID.randomUUID().toString());
        headers.put(SEC_LASTUPDATED, "abc123");
        headers.put(SEC_USERNAME, "test?user");
        headers.put(SEC_FIRSTNAME, "Gábriel");
        headers.put(SEC_LASTNAME, "Roldán");
        headers.put(SEC_ORG, "test'org");
        headers.put(SEC_ORGNAME, "ジョルケストラコミュニティ");
        headers.put(SEC_ROLES, "ROLE_USER;ROLE_ADMINISTRATOR");
        headers.put(SEC_EMAIL, "test@email.com");
        headers.put(SEC_TEL, "123456");
        headers.put(SEC_ADDRESS, "Test Postal Address");
        headers.put(SEC_TITLE, "Test Title");
        headers.put(SEC_NOTES, "Test User notes");
        headers.put(SEC_ORG_LINKAGE, "http://test.com");
        headers.put(SEC_ORG_ADDRESS, "Test organization address");
        headers.put(SEC_ORG_CATEGORY, "Testcategory");
        headers.put(SEC_ORG_DESCRIPTION, "Test org description");

        headers.forEach(this::addEncodedHeader);

        GeorchestraUserDetails userDetails = filter.getPreAuthenticatedPrincipal(request);
        assertNotNull(userDetails);
        assertFalse(userDetails.isAnonymous());

        GeorchestraUser user = userDetails.getUser();

        assertEquals(headers.get(SEC_USERID), user.getId());
        assertEquals(headers.get(SEC_LASTUPDATED), user.getLastUpdated());
        assertEquals(headers.get(SEC_USERNAME), user.getUsername());
        assertEquals(headers.get(SEC_FIRSTNAME), user.getFirstName());
        assertEquals(headers.get(SEC_LASTNAME), user.getLastName());
        assertEquals(headers.get(SEC_ORG), user.getOrganization().getId());
        assertEquals(headers.get(SEC_ORGNAME), user.getOrganization().getName());
        assertEquals(headers.get(SEC_EMAIL), user.getEmail());
        assertEquals(headers.get(SEC_TEL), user.getTelephoneNumber());
        assertEquals(headers.get(SEC_ADDRESS), user.getPostalAddress());
        assertEquals(headers.get(SEC_TITLE), user.getTitle());
        assertEquals(headers.get(SEC_NOTES), user.getNotes());
        assertEquals(headers.get(SEC_ORG_LINKAGE), user.getOrganization().getLinkage());
        assertEquals(headers.get(SEC_ORG_ADDRESS), user.getOrganization().getPostalAddress());
        assertEquals(headers.get(SEC_ORG_CATEGORY), user.getOrganization().getCategory());
        assertEquals(headers.get(SEC_ORG_DESCRIPTION), user.getOrganization().getDescription());
    }

    private void addEncodedHeader(String name, String value) {
        String encoded = SecurityHeaders.encodeBase64((String) value);
        addHeader(name, encoded);
    }

    private void addHeader(String name, String value) {
        request.addHeader(name, value);
    }
}
