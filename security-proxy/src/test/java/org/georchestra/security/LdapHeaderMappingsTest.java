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

import static org.georchestra.commons.security.SecurityHeaders.SEC_EMAIL;
import static org.georchestra.commons.security.SecurityHeaders.SEC_FIRSTNAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.georchestra.security.LdapHeaderMappings.HeaderMappings;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.common.collect.ImmutableSet;

public class LdapHeaderMappingsTest {

    private LdapHeaderMappings mappings;

    private LdapHeaderMappingsTestSupport support;

    public @BeforeEach void before() {
        mappings = new LdapHeaderMappings();
        support = new LdapHeaderMappingsTestSupport();
    }

    @Test
    public void loadConfig_DefaultMappings() {
        final Map<String, String> config = support.asMap(SEC_EMAIL, "mail", SEC_FIRSTNAME, "givenName");

        mappings.loadFrom(config);

        HeaderMappings configuredDefaults = mappings.getDefaultMappings();

        assertEquals(support.withEmbedded(config), configuredDefaults.toMap());
        assertTrue(mappings.serviceMappings.isEmpty());
    }

    @Test
    public void loadConfig_ValidateForbiddenProperty() {
        LdapHeaderMappings.FORBIDDEN_PROPERTIES.forEach(property -> {
            Map<String, String> config;
            config = support.withEmbedded("sec-should-fail", property);
            assertForbidden(config, property);
            config = support.withEmbedded("servicename.sec-should-fail", property);
            assertForbidden(config, property);

            config = support.withEmbedded("sec-should-fail", "base64:" + property);
            assertForbidden(config, property);
            config = support.withEmbedded("servicename.sec-should-fail", "base64:" + property);
            assertForbidden(config, property);
        });
    }

    private void assertForbidden(Map<String, String> config, String property) {
        try {
            mappings.loadFrom(config);
            fail("Expected IAE");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), CoreMatchers.containsString(property));
            assertThat(expected.getMessage(), CoreMatchers.containsString("is forbidden"));
        }
    }

    @Test
    public void loadConfig_ValidateInvalidAttributeName() {
        Map<String, String> config;
        config = support.withEmbedded("sec-should-fail", "org.notAnLdapAtt");
        assertInvalidLdapAttribute(config, "org.notAnLdapAtt");
        config = support.withEmbedded("servicename.sec-should-fail", "manager.org.notAnLdapAtt");
        assertInvalidLdapAttribute(config, "manager.org.notAnLdapAtt");
    }

    private void assertInvalidLdapAttribute(Map<String, String> config, String property) {
        try {
            mappings.loadFrom(config);
            fail("Expected IAE");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), CoreMatchers.containsString(property));
            assertThat(expected.getMessage(), CoreMatchers.containsString("does not exist"));
        }
    }

    @Test
    public void loadConfig_ValidateEmptyAttribute() {
        final String header = "sec-should-fail";
        assertEmptyAttribute(support.withEmbedded(header, null), header);
        assertEmptyAttribute(support.withEmbedded(header, ""), header);
        assertEmptyAttribute(support.withEmbedded(header, "base64:"), header);
    }

    private void assertEmptyAttribute(Map<String, String> config, String headerName) {
        try {
            mappings.loadFrom(config);
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), CoreMatchers.containsString("No target attribute is defined for header"));
            assertThat(expected.getMessage(), CoreMatchers.containsString(headerName));
        }
    }

    @Test
    public void loadConfig_ValidateHeaderNameFormat() {
        Map<String, String> config = support.withEmbedded("service.sec.should-fail", "manager.sn");
        try {
            mappings.loadFrom(config);
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), CoreMatchers.containsString(
                    "Invalid header name, expected '<header-name>' or '<service>.<header-name>', got 'service.sec.should-fail'"));
        }
    }

    @Test
    public void loadConfig_VdalidateInvalidEncoding() {
        assertInvalidEncoding(support.withEmbedded("sec-should-fail", "base54:org.o"), "base54:org.o");
        assertInvalidEncoding(support.withEmbedded("sec-should-fail", ":org.o"), ":org.o");
    }

    private void assertInvalidEncoding(Map<String, String> config, String ldapAtt) {
        try {
            mappings.loadFrom(config);
            fail("Expected IAE");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), CoreMatchers.containsString("Invalid encoding"));
            assertThat(expected.getMessage(), CoreMatchers.containsString(ldapAtt));
            assertThat(expected.getMessage(), CoreMatchers.containsString("expected one of: none,base64"));
        }
    }

    @Test
    public void loadConfig_ServiceMappings() {
        final Map<String, String> config = support.asMap(//
                SEC_EMAIL, "mail", //
                SEC_FIRSTNAME, "givenName", //
                "analytics.sec-lastname", "sn", //
                "analytics.sec-tel", "telephoneNumber", //
                "analytics.sec-org", "base64:org.cn", //
                "console.sec-email", "base64:mail", //
                "console.sec-firstname", "cn", //
                "console.sec-lastname", "sn", //
                "console.sec-orgname", "base64:org.o", //
                "console.sec-org-linkage", "base64:org.seeAlso.labeledURI"//
        );

        mappings.loadFrom(config);

        final Map<String, String> defaultMappings = mappings.getDefaultMappings().toMap();

        assertEquals(defaultMappings, mappings.getMappings("mapstore").toMap());

        assertEquals(ImmutableSet.of("analytics", "console"), mappings.serviceMappings.keySet());

        HeaderMappings analytics = mappings.getMappings("analytics");

        Map<String, String> expected = support.selectService("analytics", support.withEmbedded(config));
        assertEquals(expected.keySet(), analytics.toMap().keySet());
        assertEquals(expected, analytics.toMap());

        HeaderMappings console = mappings.getMappings("console");
        expected = support.selectService("console", support.withEmbedded(config));

        assertEquals(expected.keySet(), console.toMap().keySet());
        assertEquals(expected, console.toMap());
    }

    /**
     * Verify {@code headers-mapping.properties} config for
     * {@link UserDetailsJSONRequestHeaderProvider} are ignored
     */
    @Test
    public void loadConfig_IgnoresJSONProviderKeys() {
        final String propToIgnore = UserDetailsJSONRequestHeaderProvider.CONFIG_PROPERTY;
        final String servicePropToIgnore = "service." + propToIgnore;

        final Map<String, String> config = support.asMap(//
                SEC_EMAIL, "mail", //
                SEC_FIRSTNAME, "givenName", //
                "analytics.sec-lastname", "sn", //
                propToIgnore, "true", //
                servicePropToIgnore, "false"//
        );

        mappings.loadFrom(config);

        final Map<String, String> defaultMappings = mappings.getDefaultMappings().toMap();
        assertFalse(defaultMappings.keySet().contains(propToIgnore));
        assertFalse(defaultMappings.keySet().contains(servicePropToIgnore));
    }
}
