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

import static org.georchestra.commons.security.SecurityHeaders.*;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ORGNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

import com.google.common.collect.ImmutableMap;

public class LdapUserDetailsRequestHeaderProviderTest {

    private LdapUserDetailsRequestHeaderProvider ldapProvider;

    private FilterBasedLdapUserSearch userSearch;
    private LdapTemplate ldapTemplate;

    private final String orgSearchBaseDN = "ou=orgs";

    private String username;

    private HttpSession session;
    private final HttpServletRequest request = new MockHttpServletRequest();

    public @Before void before() {
        username = "testUser";
        userSearch = mock(FilterBasedLdapUserSearch.class);
        ldapTemplate = mock(LdapTemplate.class);
        ldapProvider = new LdapUserDetailsRequestHeaderProvider(userSearch, orgSearchBaseDN);
        ldapProvider.ldapTemplate = ldapTemplate;
        session = new MockHttpSession();

        Authentication auth = new TestingAuthenticationToken(username, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public @After void after() {

    }

    @Test
    public void testLoadConfig_DefaultMappings() {
        Properties props = new Properties();
        props.setProperty(SEC_EMAIL, "mail");
        props.setProperty(SEC_FIRSTNAME, "givenName");
        ldapProvider.loadConfig(props);
        assertEquals(ImmutableMap.of(SEC_EMAIL, "mail", SEC_FIRSTNAME, "givenName"), ldapProvider.getDefaultMappings());
        assertTrue(ldapProvider.perServiceMappings.isEmpty());
    }

    @Test
    public void testLoadConfig_ServiceMappings() {
        Properties props = new Properties();
        props.setProperty(SEC_EMAIL, "mail");
        props.setProperty(SEC_FIRSTNAME, "givenName");

        props.setProperty("analytics.sec-lastname", "sn");
        props.setProperty("analytics.sec-tel", "telephoneNumber");

        props.setProperty("console.sec-email", "mail-override");
        props.setProperty("console.sec-firstname", "givenName-override");
        props.setProperty("console.sec-lastname", "sn");

        ldapProvider.loadConfig(props);
        assertEquals(ImmutableMap.of(SEC_EMAIL, "mail", SEC_FIRSTNAME, "givenName"), ldapProvider.getDefaultMappings());
        assertEquals(2, ldapProvider.perServiceMappings.size());

        assertEquals(ldapProvider.getDefaultMappings(), ldapProvider.getServiceMappings("mapfishapp"));

        Map<String, String> expected;

        Map<String, String> analytics = ldapProvider.getServiceMappings("analytics");
        expected = new HashMap<>(ldapProvider.getDefaultMappings());
        expected.put(SEC_LASTNAME, "sn");
        expected.put(SEC_TEL, "telephoneNumber");
        assertEquals(expected, analytics);

        Map<String, String> console = ldapProvider.getServiceMappings("console");
        expected = new HashMap<>(ldapProvider.getDefaultMappings());
        expected.put(SEC_EMAIL, "mail-override");
        expected.put(SEC_FIRSTNAME, "givenName-override");
        expected.put(SEC_LASTNAME, "sn");
        assertEquals(expected, console);
    }

    @Test
    public void testCachedHeaders_NoServiceName() {
        String service = null;
        Optional<Collection<Header>> cached = this.ldapProvider.getCachedHeaders(session, service);
        assertFalse(cached.isPresent());
        Collection<Header> headers = Collections.singletonList(new BasicHeader("test", "value"));
        this.ldapProvider.setCachedHeaders(session, headers, service);
        cached = this.ldapProvider.getCachedHeaders(session, service);
        assertTrue(cached.isPresent());
        assertNotSame(headers, cached.get());
        assertEquals(headers, cached.get());
    }

    @Test
    public void testCachedHeaders_ServiceName() {

        final String service1 = "analytics";
        final String service2 = "console";
        Collection<Header> headers1 = Collections.singletonList(new BasicHeader("test", "analytics-value"));
        Collection<Header> headers2 = Collections.singletonList(new BasicHeader("test", "console-value"));

        Optional<Collection<Header>> cached1 = this.ldapProvider.getCachedHeaders(session, service1);
        Optional<Collection<Header>> cached2 = this.ldapProvider.getCachedHeaders(session, service2);

        assertFalse(cached1.isPresent());
        assertFalse(cached2.isPresent());

        this.ldapProvider.setCachedHeaders(session, headers1, service1);
        this.ldapProvider.setCachedHeaders(session, headers2, service2);

        cached1 = this.ldapProvider.getCachedHeaders(session, service1);
        cached2 = this.ldapProvider.getCachedHeaders(session, service2);

        assertTrue(cached1.isPresent());
        assertTrue(cached2.isPresent());

        assertNotSame(headers1, cached1.get());
        assertNotSame(headers2, cached2.get());

        assertEquals(headers1, cached1.get());
        assertEquals(headers2, cached2.get());
    }

    @Test
    public void testGetCustomRequestHeaders_SetsOrgAndOrgNameStandardHeaders() throws Exception {
        setupMockLdapContext("test-org", "Test Org Name", null);

        String targetServiceName = null;
        Collection<Header> headers = ldapProvider.getCustomRequestHeaders(session, request, targetServiceName);
        headers.forEach(
                h -> assertEquals("header has more than one value: " + h.toString(), 1, h.getElements().length));

        Map<String, String> actual = headers.stream().collect(Collectors.toMap(Header::getName, Header::getValue));
        assertEquals(ImmutableMap.of(SEC_ORG, "test-org", SEC_ORGNAME, "Test Org Name"), actual);
    }

    @Test
    public void testGetCustomRequestHeaders_defaultHeaders() throws Exception {
        Map<String, String> ldapContext = ImmutableMap.of("mail", "test@mock.com", "givenName", "Mockfirsname",
                "mail-override", "test@mock.override.com", "sn", "Mocklastname", "telephoneNumber", "123456");

        setupMockLdapContext("test-org", "Test Org Name", ldapContext);

        Properties props = new Properties();
        props.setProperty(SEC_EMAIL, "mail");
        props.setProperty(SEC_FIRSTNAME, "givenName");

        props.setProperty("analytics.sec-email", "mail-override");
        props.setProperty("analytics.sec-lastname", "sn");
        props.setProperty("analytics.sec-tel", "telephoneNumber");
        this.ldapProvider.loadConfig(props);

        String targetServiceName = null;
        Collection<Header> headers = ldapProvider.getCustomRequestHeaders(session, request, targetServiceName);
        headers.forEach(
                h -> assertEquals("header shall have just one value: " + h.toString(), 1, h.getElements().length));

        Map<String, String> actual = headers.stream().collect(Collectors.toMap(Header::getName, Header::getValue));

        Map<String, String> expected = new HashMap<>();
        expected.put(SEC_ORG, "test-org");
        expected.put(SEC_ORGNAME, "Test Org Name");
        expected.put(SEC_EMAIL, ldapContext.get("mail"));
        expected.put(SEC_FIRSTNAME, ldapContext.get("givenName"));

        assertEquals(expected.keySet(), actual.keySet());
        assertEquals("expected default headers on non service-name match", expected, actual);
    }

    @Test
    public void testGetCustomRequestHeaders_ServiceSpecificHeaders() throws Exception {
        Map<String, String> ldapContext = ImmutableMap.of("mail", "test@mock.com", "givenName", "Mockfirsname",
                "mail-override", "test@mock.override.com", "sn", "Mocklastname", "telephoneNumber", "123456");

        setupMockLdapContext("test-org", "Test Org Name", ldapContext);

        Properties props = new Properties();
        props.setProperty(SEC_EMAIL, "mail");
        props.setProperty(SEC_FIRSTNAME, "givenName");

        props.setProperty("analytics.sec-email", "mail-override");
        props.setProperty("analytics.sec-lastname", "sn");
        props.setProperty("analytics.sec-tel", "telephoneNumber");
        this.ldapProvider.loadConfig(props);

        String targetServiceName = "analytics";
        Collection<Header> headers = ldapProvider.getCustomRequestHeaders(session, request, targetServiceName);
        headers.forEach(
                h -> assertEquals("header shall have just one value: " + h.toString(), 1, h.getElements().length));

        Map<String, String> actual = headers.stream().collect(Collectors.toMap(Header::getName, Header::getValue));

        Map<String, String> expected = new HashMap<>();
        expected.put(SEC_ORG, "test-org");
        expected.put(SEC_ORGNAME, "Test Org Name");
        expected.put(SEC_FIRSTNAME, ldapContext.get("givenName"));
        // overridden per-service header
        expected.put(SEC_EMAIL, ldapContext.get("mail-override"));
        // per-service headers not present in default headers
        expected.put(SEC_LASTNAME, ldapContext.get("sn"));
        expected.put(SEC_TEL, ldapContext.get("telephoneNumber"));

        assertEquals(expected.keySet(), actual.keySet());
        assertEquals(expected, actual);
    }

    /**
     * Headers mappings can be defined with an encoding scheme, like {@code base64},
     * for example: {@code sec-firstname=base64:givenName}, the resulting value will
     * be the encoding-prefixed encoded value, for example: {@code "{base64}<encoded
     * string>"}
     * <p>
     */
    @Test
    public void testGetCustomRequestHeaders_HeaderValueEncoding_Base64() throws Exception {
        final String givenName = "ガブリエル";
        final String lastName = "ロルダン";

        final String encodedGivenName = Base64.getEncoder().encodeToString(givenName.getBytes(StandardCharsets.UTF_8));
        final String encodedLastName = Base64.getEncoder().encodeToString(lastName.getBytes(StandardCharsets.UTF_8));
        final String expectedGivenName = "{base64}" + encodedGivenName;
        final String expectedLastName = "{base64}" + encodedLastName;

        String decodedGivenName = new String(Base64.getDecoder().decode(encodedGivenName), StandardCharsets.UTF_8);
        assertEquals(givenName, decodedGivenName);

        Map<String, String> ldapContext = ImmutableMap.of(//
                "givenName", givenName, //
                "sn", lastName, //
                "mail", "test@mock.com", "mail-override", "test@mock.override.com", "telephoneNumber", "123456");

        setupMockLdapContext("test-org", "Test Org Name", ldapContext);

        Properties props = new Properties();
        props.setProperty(SEC_FIRSTNAME, "base64:givenName");
        props.setProperty(SEC_LASTNAME, "base64:sn");
        props.setProperty(SEC_EMAIL, "mail");

        this.ldapProvider.loadConfig(props);

        String targetServiceName = "analytics";
        Collection<Header> headers = ldapProvider.getCustomRequestHeaders(session, request, targetServiceName);
        headers.forEach(
                h -> assertEquals("header shall have just one value: " + h.toString(), 1, h.getElements().length));

        Map<String, String> actual = headers.stream().collect(Collectors.toMap(Header::getName, Header::getValue));
        assertEquals(expectedGivenName, actual.get(SEC_FIRSTNAME));
        assertEquals(expectedLastName, actual.get(SEC_LASTNAME));
        assertEquals("test@mock.com", actual.get(SEC_EMAIL));
    }

    private void setupMockLdapContext(String org, String orgName, Map<String, String> ldapContext) throws Exception {

        Attribute memberOf = mock(Attribute.class);
        NamingEnumeration<Object> orgs = namingEnumeration("cn=test-org,ou=orgs");// ([^=,]+)=([^=,]+),ou=orgs.*
        doReturn(orgs).when(memberOf).getAll();
        DirContextOperations ctx = mock(DirContextOperations.class);
        when(ctx.getStringAttribute(eq("o"))).thenReturn(orgName);
        when(ldapTemplate.lookupContext(eq("cn=" + org + ",ou=orgs"))).thenReturn(ctx);

        Attributes userAttributes = mock(Attributes.class);
        when(userAttributes.get(eq("memberOf"))).thenReturn(memberOf);

        DirContextOperations userProperties = mock(DirContextOperations.class);
        when(userProperties.getAttributes()).thenReturn(userAttributes);

        when(userSearch.searchForUser(eq(username))).thenReturn(userProperties);

        if (ldapContext != null) {
            for (Map.Entry<String, String> e : ldapContext.entrySet()) {
                String name = e.getKey();
                String value = e.getValue();

                NamingEnumeration<?> propEnum = namingEnumeration(value);
                Attribute propAttribute = mock(Attribute.class);
                doReturn(propEnum).when(propAttribute).getAll();
                when(userAttributes.get(eq(name))).thenReturn(propAttribute);
            }
        }
    }

    private NamingEnumeration<Object> namingEnumeration(Object... values) {
        return new NamingEnumeration<Object>() {
            final Iterator<Object> it = Arrays.asList(values).iterator();

            public @Override boolean hasMoreElements() {
                return it.hasNext();
            }

            public @Override Object nextElement() {
                return it.next();
            }

            public @Override Object next() throws NamingException {
                return it.next();
            }

            public @Override boolean hasMore() throws NamingException {
                return hasMoreElements();
            }

            public @Override void close() throws NamingException {
            }
        };
    }
}
