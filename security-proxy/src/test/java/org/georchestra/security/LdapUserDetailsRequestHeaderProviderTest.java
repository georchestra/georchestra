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

import static org.georchestra.commons.security.SecurityHeaders.SEC_EMAIL;
import static org.georchestra.commons.security.SecurityHeaders.SEC_FIRSTNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_LASTNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ORG;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ORGNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_TEL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.georchestra.commons.security.SecurityHeaders;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.ImmutableMap;

public class LdapUserDetailsRequestHeaderProviderTest {

    private LdapHeaderMappingsTestSupport support;
    private LdapUserDetailsRequestHeaderProvider ldapProvider;

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    public @Before void before() {
        support = new LdapHeaderMappingsTestSupport();
        support.initMockLdapContext();
        ldapProvider = new LdapUserDetailsRequestHeaderProvider(() -> support.userSearch, support.orgSearchBaseDN);
        ldapProvider.ldapTemplate = support.ldapTemplate;

        Authentication auth = new TestingAuthenticationToken(support.username, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testCachedHeaders_NoServiceName() {
        String service = null;
        Map<String, String> cached = this.ldapProvider.getCachedHeaders(service);
        Map<String, String> headers = ImmutableMap.of("test", "value");
        this.ldapProvider.setCachedHeaders(headers, service);
        cached = this.ldapProvider.getCachedHeaders(service);
        assertNotSame(headers, cached);
        assertEquals(headers, cached);
    }

    @Test
    public void testCachedHeaders_ServiceName() {

        final String service1 = "analytics";
        final String service2 = "console";
        Map<String, String> headers1 = ImmutableMap.of("test", "analytics-value");
        Map<String, String> headers2 = ImmutableMap.of("test", "console-value");

        Map<String, String> cached1 = this.ldapProvider.getCachedHeaders(service1);
        Map<String, String> cached2 = this.ldapProvider.getCachedHeaders(service2);

        assertTrue(cached1.isEmpty());
        assertTrue(cached2.isEmpty());

        this.ldapProvider.setCachedHeaders(headers1, service1);
        this.ldapProvider.setCachedHeaders(headers2, service2);

        cached1 = this.ldapProvider.getCachedHeaders(service1);
        cached2 = this.ldapProvider.getCachedHeaders(service2);

        assertNotSame(headers1, cached1);
        assertNotSame(headers2, cached2);

        assertEquals(headers1, cached1);
        assertEquals(headers2, cached2);
    }

    @Test
    public void testGetCustomRequestHeaders_SetsOrgAndOrgNameStandardHeaders() throws Exception {
        String targetServiceName = null;
        Map<String, String> actual = ldapProvider.getCustomRequestHeaders(request, targetServiceName);
        String org = (String) support.orgContextMap.get("cn").get(0);
        String orgname = (String) support.orgContextMap.get("o").get(0);
        Map<String, String> expected = ImmutableMap.of(SEC_ORG, org, SEC_ORGNAME, orgname);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetCustomRequestHeaders_defaultHeaders() throws Exception {
        Map<String, String> props = support.asMap(//
                SEC_EMAIL, "mail", //
                SEC_FIRSTNAME, "givenName", //
                "analytics.sec-email", "base64:mail", //
                "analytics.sec-lastname", "sn", //
                "analytics.sec-tel", "telephoneNumber"//
        );
        this.ldapProvider.loadConfig(props);

        String targetServiceName = null;
        Map<String, String> actual = ldapProvider.getCustomRequestHeaders(request, targetServiceName);
        Map<String, String> expected = support.buildHeaders(//
                SEC_EMAIL, "mail", //
                SEC_FIRSTNAME, "givenName");

        assertEquals(expected.keySet(), actual.keySet());
        assertEquals("expected default headers on non service-name match", expected, actual);
    }

    @Test
    public void testGetCustomRequestHeaders_ServiceSpecificHeaders() throws Exception {
        Map<String, String> props = support.asMap(//
                SEC_EMAIL, "mail", //
                SEC_FIRSTNAME, "givenName", //
                "analytics.sec-email", "base64:mail", //
                "analytics.sec-lastname", "sn", //
                "analytics.sec-tel", "telephoneNumber"//
        );
        this.ldapProvider.loadConfig(props);

        String targetServiceName = "analytics";
        Map<String, String> actual = ldapProvider.getCustomRequestHeaders(request, targetServiceName);
        Map<String, String> expected = support.buildHeaders(//
                SEC_EMAIL, "base64:mail", //
                SEC_FIRSTNAME, "givenName", //
                SEC_LASTNAME, "sn", //
                SEC_TEL, "telephoneNumber"//
        );

        assertEquals(expected.keySet(), actual.keySet());
        expected.forEach((h, v) -> {
            String returned = actual.get(h);
            assertEquals(h, v, returned);
        });
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
        support.setProperty("givenName", givenName);
        support.setProperty("sn", lastName);
        support.setProperty("mail", "test@mock.com");

        final String encodedGivenName = Base64.getEncoder().encodeToString(givenName.getBytes(StandardCharsets.UTF_8));
        final String encodedLastName = Base64.getEncoder().encodeToString(lastName.getBytes(StandardCharsets.UTF_8));
        final String expectedGivenName = "{base64}" + encodedGivenName;
        final String expectedLastName = "{base64}" + encodedLastName;
        assertEquals(expectedGivenName, SecurityHeaders.encodeBase64(givenName));
        assertEquals(expectedLastName, SecurityHeaders.encodeBase64(lastName));

        Map<String, String> props = new HashMap<>();
        props.put(SEC_FIRSTNAME, "base64:givenName");
        props.put(SEC_LASTNAME, "base64:sn");
        props.put(SEC_EMAIL, "mail");

        this.ldapProvider.loadConfig(props);

        String targetServiceName = "analytics";
        Map<String, String> actual = ldapProvider.getCustomRequestHeaders(request, targetServiceName);
        assertEquals("test@mock.com", actual.get(SEC_EMAIL));
        assertEquals(expectedGivenName, actual.get(SEC_FIRSTNAME));
        assertEquals(expectedLastName, actual.get(SEC_LASTNAME));
    }

    @Test
    public void testAllValidHeaders() {
        List<String> allValidAttributes = new ArrayList<>(LdapHeaderMappings.ALL_VALID_ATTRIBUTES);
        Map<String, String> config = new HashMap<>();
        for (int i = 0; i < allValidAttributes.size(); i++) {
            String header = "sec-" + i;
            String ldapAtt = allValidAttributes.get(i);
            config.put(header, ldapAtt);
        }
        testConfig(config);
    }

    @Test
    public void testAllValidHeaders_encoded() {
        List<String> allValidAttributes = new ArrayList<>(LdapHeaderMappings.ALL_VALID_ATTRIBUTES);
        Map<String, String> config = new HashMap<>();
        for (int i = 0; i < allValidAttributes.size(); i++) {
            String header = "sec-" + i;
            String ldapAtt = "base64:" + allValidAttributes.get(i);
            config.put(header, ldapAtt);
        }
        testConfig(config);
    }

    private void testConfig(Map<String, String> config) {
        ldapProvider.loadConfig(config);
        Map<String, String> actual = ldapProvider.getCustomRequestHeaders(request, null);
        Map<String, String> expected = new HashMap<>();
        config.forEach((header, att) -> {
            String expectedValue = support.resolve(header, att);
            expected.put(header, expectedValue);
        });

        expected.forEach((header, value) -> {
            String mappingStr = String.format("%s=%s", header, config.get(header));
            assertEquals(mappingStr, value, actual.get(header));
            LdapUserDetailsRequestHeaderProvider.logger.info(mappingStr + " ok: " + value);
        });
    }
}
