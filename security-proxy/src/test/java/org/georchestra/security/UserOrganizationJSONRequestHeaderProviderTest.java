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
package org.georchestra.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.security.api.OrganizationsApi;
import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.PropertyResolver;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserOrganizationJSONRequestHeaderProviderTest {

    private UsersApi userLookupService;
    private OrganizationsApi orgsLookupService;
    private UserOrganizationJSONRequestHeaderProvider orgHeaderProvider;

    private Organization org;

    private Properties headersMapping;
    private MockHttpServletRequest request = new MockHttpServletRequest();

    public @Before void before() {
        GeorchestraUser user = newGeorchestraUser("testadmin");
        this.org = newOrganization(user.getOrganization(), user.getUsername());

        this.userLookupService = mock(UsersApi.class);
        when(userLookupService.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userLookupService.findByUsername(eq(user.getUsername()))).thenReturn(Optional.of(user));

        this.orgsLookupService = mock(OrganizationsApi.class);
        when(orgsLookupService.findByShortName(anyString())).thenReturn(Optional.empty());
        when(orgsLookupService.findByShortName(eq(user.getOrganization()))).thenReturn(Optional.of(org));

        this.orgHeaderProvider = new UserOrganizationJSONRequestHeaderProvider();
        this.orgHeaderProvider.setUsers(userLookupService);
        this.orgHeaderProvider.setOrgs(orgsLookupService);

        this.headersMapping = new Properties();
        this.request = new MockHttpServletRequest();

        Authentication auth = new TestingAuthenticationToken(user.getUsername(), null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private GeorchestraUser newGeorchestraUser(String username) {
        GeorchestraUser user = new GeorchestraUser();
        user.setId(UUID.randomUUID().toString());
        user.setLastUpdated(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setEmail(username + "@georchestra.org");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRoles(Arrays.asList("ADMINISTRATOR", "GN_ADMIN"));
        user.setOrganization("PSC");
        return user;
    }

    private Organization newOrganization(String orgShortName, String userName) {
        Organization org = new Organization();

        org.setShortName(orgShortName);
        org.setId(UUID.randomUUID().toString());
        org.setLastUpdated(UUID.randomUUID().toString());

        org.setName(orgShortName + " Org Name");
        org.setCategory(orgShortName + " category");
        org.setDescription(orgShortName + " description");
        org.setLinkage("http://test.com/" + orgShortName);
        org.setNotes(orgShortName + " notes");
        org.setPostalAddress(orgShortName + " postal address");
        org.setMembers(Arrays.asList("user1", "user2", "user3", userName));
        return org;
    }

    private void enableGlobally() {
        headersMapping.setProperty(UserOrganizationJSONRequestHeaderProvider.CONFIG_PROPERTY, "true");
        init();
    }

    private void enableService(String service, boolean enabled) {
        String key = String.format("%s.%s", service, UserOrganizationJSONRequestHeaderProvider.CONFIG_PROPERTY);
        headersMapping.setProperty(key, String.valueOf(enabled));
        init();
    }

    private void init() {
        orgHeaderProvider.init(headersMapping);
    }

    public @Test void testAnonymous() {
        enableGlobally();
        Authentication auth = new AnonymousAuthenticationToken("anonymous", "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Map<String, String> headers = orgHeaderProvider.getCustomRequestHeaders(request, null);
        assertEquals(Collections.emptyMap(), headers);
    }

    public @Test void testEnabledGloballyAndDisabledForService() {
        final String service = "geonetwork";
        enableGlobally();
        enableService(service, false);

        Map<String, String> headers = orgHeaderProvider.getCustomRequestHeaders(request, service);
        assertEquals(Collections.emptyMap(), headers);

        headers = orgHeaderProvider.getCustomRequestHeaders(request, null);
        assertTrue(headers.containsKey("sec-organization"));

        headers = orgHeaderProvider.getCustomRequestHeaders(request, "atlas");
        assertTrue(headers.containsKey("sec-organization"));
    }

    public @Test void testDisabledGloballyAndEnabledForService()
            throws JsonParseException, JsonMappingException, IOException {
        final String service = "geonetwork";
        enableService(service, true);
        testHeader(service);
    }

    public @Test void testEnabledGloballyNullService() throws JsonParseException, JsonMappingException, IOException {
        enableGlobally();
        testHeader(null);
    }

    private void testHeader(final String service) throws IOException, JsonParseException, JsonMappingException {
        Map<String, String> headers = orgHeaderProvider.getCustomRequestHeaders(request, service);
        assertTrue(headers.containsKey("sec-organization"));

        final String base64Json = headers.get("sec-organization");
        assertNotNull(base64Json);

        final String json = SecurityHeaders.decode(base64Json);
        assertEquals(SecurityHeaders.encodeBase64(json), base64Json);

        Organization decodedOrg = new ObjectMapper().readValue(json, Organization.class);
        Organization expected = this.org.withMembers(Collections.emptyList());
        assertEquals(expected, decodedOrg);
    }

    public @Test void testCacheTTL() throws InterruptedException {
        // set cache TTL to 500ms
        PropertyResolver env = mock(PropertyResolver.class);
        when(env.getProperty("security-proxy.ldap.cache.ttl")).thenReturn("500");
        this.orgHeaderProvider.setPropertyResolver(env);
        this.enableGlobally();

        Map<String, String> pre1 = orgHeaderProvider.getCustomRequestHeaders(request, null);
        Map<String, String> pre2 = orgHeaderProvider.getCustomRequestHeaders(request, null);
        Thread.sleep(1000);
        Map<String, String> post = orgHeaderProvider.getCustomRequestHeaders(request, null);

        assertTrue(pre1.containsKey("sec-organization"));
        assertSame(pre1.get("sec-organization"), pre2.get("sec-organization"));

        assertTrue(post.containsKey("sec-organization"));
        assertNotSame(pre1.get("sec-organization"), post.get("sec-organization"));
    }

    public @Test void testCacheDisabledByConfig() throws InterruptedException {
        // set cache TTL to 0ms
        PropertyResolver env = mock(PropertyResolver.class);
        when(env.getProperty("security-proxy.ldap.cache.ttl")).thenReturn("0");
        this.orgHeaderProvider.setPropertyResolver(env);
        this.enableGlobally();

        Map<String, String> r1 = orgHeaderProvider.getCustomRequestHeaders(request, null);
        Map<String, String> r2 = orgHeaderProvider.getCustomRequestHeaders(request, null);

        assertTrue(r1.containsKey("sec-organization"));
        assertNotSame(r1.get("sec-organization"), r2.get("sec-organization"));
    }
}
