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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

public class UserDetailsJSONRequestHeaderProviderTest {

    private UsersApi lookupService;
    private UserDetailsJSONRequestHeaderProvider provider;
    private GeorchestraUser user;
    private Properties headersMapping;
    private MockHttpServletRequest request = new MockHttpServletRequest();

    public @BeforeEach void before() {
        this.user = newGeorchestraUser("testadmin");

        this.lookupService = mock(UsersApi.class);
        when(lookupService.findByUsername(anyString())).thenReturn(Optional.empty());
        when(lookupService.findByUsername(eq(user.getUsername()))).thenReturn(Optional.of(user));

        this.provider = new UserDetailsJSONRequestHeaderProvider();
        this.provider.setUsers(lookupService);

        this.headersMapping = new Properties();
        this.request = new MockHttpServletRequest();

        Authentication auth = new TestingAuthenticationToken(user.getUsername(), null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private GeorchestraUser newGeorchestraUser(String username) {
        GeorchestraUser user = new GeorchestraUser();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setEmail(username + "@georchestra.org");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRoles(Arrays.asList("ADMINISTRATOR", "GN_ADMIN"));
        user.setOrganization("PSC");
        return user;
    }

    private void enableGlobally() {
        headersMapping.setProperty(UserDetailsJSONRequestHeaderProvider.CONFIG_PROPERTY, "true");
        init();
    }

    private void enableService(String service, boolean enabled) {
        String key = String.format("%s.%s", service, UserDetailsJSONRequestHeaderProvider.CONFIG_PROPERTY);
        headersMapping.setProperty(key, String.valueOf(enabled));
        init();
    }

    private void init() {
        provider.init(headersMapping);
    }

    public @Test void testAnonymous() {
        enableGlobally();
        Authentication auth = new AnonymousAuthenticationToken("anonymous", "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Map<String, String> headers = provider.getCustomRequestHeaders(request, null);
        assertEquals(Collections.emptyMap(), headers);
    }

    public @Test void testEnabledGloballyAndDisabledForService() {
        final String service = "geonetwork";
        enableGlobally();
        enableService(service, false);

        Map<String, String> headers = provider.getCustomRequestHeaders(request, service);
        assertEquals(Collections.emptyMap(), headers);

        headers = provider.getCustomRequestHeaders(request, null);
        assertTrue(headers.containsKey("sec-user"));

        headers = provider.getCustomRequestHeaders(request, "atlas");
        assertTrue(headers.containsKey("sec-user"));
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
        Map<String, String> headers = provider.getCustomRequestHeaders(request, service);
        assertTrue(headers.containsKey("sec-user"));

        final String base64Json = headers.get("sec-user");
        assertNotNull(base64Json);

        final String json = SecurityHeaders.decode(base64Json);
        assertEquals(SecurityHeaders.encodeBase64(json), base64Json);

        GeorchestraUser decodedUser = new ObjectMapper().readValue(json, GeorchestraUser.class);
        assertEquals(this.user, decodedUser);
    }

    public @Test void testCacheTTL() throws InterruptedException {
        // set cache TTL to 500ms
        PropertyResolver env = mock(PropertyResolver.class);
        when(env.getProperty("security-proxy.ldap.cache.ttl")).thenReturn("500");
        this.provider.setPropertyResolver(env);
        this.enableGlobally();

        Map<String, String> pre1 = provider.getCustomRequestHeaders(request, null);
        Map<String, String> pre2 = provider.getCustomRequestHeaders(request, null);
        Thread.sleep(1000);
        Map<String, String> post = provider.getCustomRequestHeaders(request, null);

        assertTrue(pre1.containsKey("sec-user"));
        assertSame(pre1.get("sec-user"), pre2.get("sec-user"));

        assertTrue(post.containsKey("sec-user"));
        assertNotSame(pre1.get("sec-user"), post.get("sec-user"));
    }

    public @Test void testCacheDisabledByConfig() throws InterruptedException {
        // set cache TTL to 0ms
        PropertyResolver env = mock(PropertyResolver.class);
        when(env.getProperty("security-proxy.ldap.cache.ttl")).thenReturn("0");
        this.provider.setPropertyResolver(env);
        this.enableGlobally();

        Map<String, String> r1 = provider.getCustomRequestHeaders(request, null);
        Map<String, String> r2 = provider.getCustomRequestHeaders(request, null);

        assertTrue(r1.containsKey("sec-user"));
        assertNotSame(r1.get("sec-user"), r2.get("sec-user"));
    }
}
