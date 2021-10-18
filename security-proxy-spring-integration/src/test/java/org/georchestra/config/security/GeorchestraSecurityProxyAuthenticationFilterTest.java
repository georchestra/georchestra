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
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORGID;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORGNAME;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_ADDRESS;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_CATEGORY;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_DESCRIPTION;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_LASTUPDATED;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_LINKAGE;
import static org.georchestra.config.security.GeorchestraUserDetails.SEC_ORG_NOTES;
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

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeorchestraSecurityProxyAuthenticationFilterTest {

    private GeorchestraSecurityProxyAuthenticationFilter filter;
    private MockHttpServletRequest request;

    private GeorchestraUser user;
    private Organization org;

    public @Before void before() {
        request = new MockHttpServletRequest();
        filter = new GeorchestraSecurityProxyAuthenticationFilter();

        user = new GeorchestraUser();
        user.setId(UUID.randomUUID().toString());
        user.setLastUpdated("anystringwoulddo");
        user.setUsername("testadmin");
        user.setEmail("testadmin@georchestra.org");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRoles(Arrays.asList("ADMINISTRATOR", "GN_ADMIN"));
        user.setTelephoneNumber("341444111");
        user.setTitle("developer");
        user.setNotes("user notes");
        user.setPostalAddress("123 java street");
        user.setOrganization("PSC");

        org = new Organization();
        org.setShortName("PSC");
        org.setId(UUID.randomUUID().toString());
        org.setLastUpdated(UUID.randomUUID().toString());
        org.setName("Org Name");
        org.setCategory("category");
        org.setDescription("description");
        org.setLinkage("http://test.com/PSC");
        org.setNotes("org notes");
        org.setPostalAddress("org postal address");
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
    public void getPreAuthenticatedCredentials_SingleHeader() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(user);

        addEncodedHeader(SecurityHeaders.SEC_PROXY, "true");
        addEncodedHeader(SecurityHeaders.SEC_USER, json);

        GeorchestraUserDetails preauth = filter.getPreAuthenticatedPrincipal(request);
        assertNotNull(preauth);
        assertNotNull(preauth.getUser());
        assertEquals(user, preauth.getUser());
        assertFalse(preauth.getOrganization().isPresent());
    }

    @Test
    public void getPreAuthenticatedCredentials_SingleHeader_Include_Organization() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String userJson = mapper.writeValueAsString(user);
        String orgJson = mapper.writeValueAsString(org);

        addEncodedHeader(SecurityHeaders.SEC_PROXY, "true");
        addEncodedHeader(SecurityHeaders.SEC_USER, userJson);
        addEncodedHeader(SecurityHeaders.SEC_ORGANIZATION, orgJson);

        GeorchestraUserDetails preauth = filter.getPreAuthenticatedPrincipal(request);
        assertEquals(user, preauth.getUser());
        assertTrue(preauth.getOrganization().isPresent());
        assertEquals(org, preauth.getOrganization().get());
    }

    @Test
    public void getPreAuthenticatedCredentials_IndividualHeaders() {
        addEncodedHeader(SecurityHeaders.SEC_PROXY, "true");
        addUserHeaders();

        GeorchestraUserDetails userDetails = filter.getPreAuthenticatedPrincipal(request);
        assertNotNull(userDetails);
        assertFalse(userDetails.isAnonymous());

        GeorchestraUser user = userDetails.getUser();
        assertEquals(this.user, user);
        assertFalse(userDetails.getOrganization().isPresent());
    }

    private void addUserHeaders() {
        final String rolesHeader = this.user.getRoles().stream().collect(Collectors.joining(";"));
        addEncodedHeader(SEC_USERID, user.getId());
        addEncodedHeader(SEC_LASTUPDATED, user.getLastUpdated());
        addEncodedHeader(SEC_USERNAME, user.getUsername());
        addEncodedHeader(SEC_FIRSTNAME, user.getFirstName());
        addEncodedHeader(SEC_LASTNAME, user.getLastName());
        addEncodedHeader(SEC_ORG, user.getOrganization());
        addEncodedHeader(SEC_ROLES, rolesHeader);
        addEncodedHeader(SEC_EMAIL, user.getEmail());
        addEncodedHeader(SEC_TEL, user.getTelephoneNumber());
        addEncodedHeader(SEC_ADDRESS, user.getPostalAddress());
        addEncodedHeader(SEC_TITLE, user.getTitle());
        addEncodedHeader(SEC_NOTES, user.getNotes());
    }

    @Test
    public void getPreAuthenticatedCredentials_IndividualHeaders_including_Organization() {
        addEncodedHeader(SecurityHeaders.SEC_PROXY, "true");
        addUserHeaders();
        addOrgHeaders();

        GeorchestraUserDetails userDetails = filter.getPreAuthenticatedPrincipal(request);
        assertNotNull(userDetails);
        assertFalse(userDetails.isAnonymous());

        GeorchestraUser user = userDetails.getUser();
        assertEquals(this.user, user);
        assertTrue(userDetails.getOrganization().isPresent());
        assertEquals(this.org, userDetails.getOrganization().get());
    }

    private void addOrgHeaders() {
        request.removeHeader(SEC_ORG);
        addEncodedHeader(SEC_ORG, org.getShortName());

        addEncodedHeader(SEC_ORGID, org.getId());
        addEncodedHeader(SEC_ORGNAME, org.getName());
        addEncodedHeader(SEC_ORG_LASTUPDATED, org.getLastUpdated());
        addEncodedHeader(SEC_ORG_CATEGORY, org.getCategory());
        addEncodedHeader(SEC_ORG_DESCRIPTION, org.getDescription());
        addEncodedHeader(SEC_ORG_LINKAGE, org.getLinkage());
        addEncodedHeader(SEC_ORG_NOTES, org.getNotes());
        addEncodedHeader(SEC_ORG_ADDRESS, org.getPostalAddress());
    }

    private void addEncodedHeader(String name, String value) {
        String encoded = SecurityHeaders.encodeBase64((String) value);
        addHeader(name, encoded);
    }

    private void addHeader(String name, String value) {
        request.addHeader(name, value);
    }
}
