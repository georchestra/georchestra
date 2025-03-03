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

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.georchestra.security.LdapHeaderMappings.HeaderMapping;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

class LdapHeaderMappingsTestSupport {

    ListMultimap<String, ?> userContextMap;
    ListMultimap<String, ?> managerContextMap;
    ListMultimap<String, ?> orgContextMap;
    ListMultimap<String, ?> orgExtContextMap;

    LdapTemplate ldapTemplate;
    final String orgSearchBaseDN = "ou=orgs";

    DirContextOperations userContext;
    DirContextOperations managerContext;
    DirContextOperations orgContext;
    DirContextOperations orgExtContext;

    final String username = "testadmin";
    final String org = "PSC";
    final String managerName = "testeditor";

    FilterBasedLdapUserSearch userSearch;

    public void initMockLdapContext() {
        userContextMap = createUserContext();
        managerContextMap = createManagerContext();
        orgContextMap = createOrgContext();
        orgExtContextMap = createOrgExtContext();

        userContext = createMockContext(userContextMap);
        managerContext = createMockContext(managerContextMap);
        orgContext = createMockContext(orgContextMap);
        orgExtContext = createMockContext(orgExtContextMap);

        ldapTemplate = mock(LdapTemplate.class);
        when(ldapTemplate.lookupContext(eq("o=PSC,ou=orgs"))).thenReturn(orgExtContext);

        final String groupDn = format("cn=%s,%s", "PSC", this.orgSearchBaseDN);
        when(ldapTemplate.lookupContext(eq(groupDn))).thenReturn(orgContext);

        userSearch = mock(FilterBasedLdapUserSearch.class);
        when(userSearch.searchForUser(eq(username))).thenReturn(userContext);
        when(userSearch.searchForUser(eq(managerName))).thenReturn(managerContext);
    }

    private DirContextOperations createMockContext(ListMultimap<String, ?> map) {
        DirContextAdapter context = new DirContextAdapter();
        map.forEach((attribute, value) -> {
            context.addAttributeValue(attribute, value);
        });

        // validate mock context
        map.forEach((attribute, value) -> {
            String cvalue = context.getStringAttribute(attribute);
            assertEquals(value, cvalue);
        });
        return context;
    }

    private ListMultimap<String, String> createUserContext() {
        Map<String, String> map = asMap(//
                "uid", "testadmin", //
                "givenName", "Gabriel", //
                "sn", "Raúl Roldán", //
                "cn", "Gabriel Raúl Roldán", //
                "telephoneNumber", "0054-555-7654321", //
                "mail", "testadmin@test.com", //
                "postalAddress", "Avenue of Testing 123 10º B", //
                "description", "Admin user", //
                "title", "Amo del universo", //
                "objectClass", "georchestraUser", //
                "knowledgeInformation", "Internal CRM notes on testadmin", //
                "manager", "uid=testeditor,ou=users,dc=georchestra,dc=org", //
                "georchestraObjectIdentifier", "0c6bb556-4ee8-46f2-892d-6116e262b489"//
        );

        map.put("memberOf", "cn=ADMINISTRATOR,ou=roles,dc=georchestra,dc=org");
        map.put("memberOf", "cn=SUPERUSER,ou=roles,dc=georchestra,dc=org");
        map.put("memberOf", "cn=EXTRACTORAPP,ou=roles,dc=georchestra,dc=org");
        map.put("memberOf", "cn=PSC,ou=orgs,dc=georchestra,dc=org");

        return asMultimap(map);
    }

    private ListMultimap<String, String> createManagerContext() {
        Map<String, String> map = asMap(//
                "uid", "testeditor", //
                "mail", "psc+testeditor@georchestra.org", //
                "givenName", "Test", //
                "description", "editor", //
                "sn", "EDITOR", //
                "cn", "testeditor", //
                "objectClass", "georchestraUser"//
        );
        return asMultimap(map);
    }

    private ListMultimap<String, String> createOrgContext() {
        Map<String, String> map = asMap(//
                "cn", "PSC", //
                "ou", "PSC", //
                "o", "Project Steering Committee", //
                "member", "uid=testadmin,ou=users,dc=georchestra,dc=org", //
                "description", "2A004,2B033", //
                "objectClass", "groupOfMembers", //
                "seeAlso", "o=PSC,ou=orgs,dc=georchestra,dc=org"//
        );
        return asMultimap(map);
    }

    private ListMultimap<String, String> createOrgExtContext() {
        Map<String, String> map = asMap(//
                "o", "PSC", //
                "labeledURI", "https://www.georchestra.org/", //
                "businessCategory", "Association", //
                "postalAddress", "127 rue georchestra, 73590 Chamblille", //
                "description", "Association PSC geOrchestra", //
                "knowledgeInformation", "Internal CRM notes on PSC", //
                "objectClass", "organization", //
                "georchestraObjectIdentifier", "bddf474d-125d-4b18-92bd-bd8ebb6699a9"//
        );
        return asMultimap(map);
    }

    private <K, V> ListMultimap<K, V> asMultimap(Map<K, V> map) {
        ListMultimap<K, V> context = ArrayListMultimap.create();
        map.forEach(context::put);
        return context;
    }

    public Map<String, String> selectService(String service, Map<String, String> config) {
        Map<String, String> global = config.entrySet().stream().filter(LdapHeaderMappings::isGlobalHeader)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final String prefix = service + ".";
        Map<String, String> serviceHeaders = config.entrySet().stream()
                .filter(LdapHeaderMappings::isServiceSpecificHeader)//
                .filter(e -> e.getKey().startsWith(prefix))//
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().substring(prefix.length()), e.getValue()))//
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, String> result = new HashMap<>(global);
        result.putAll(serviceHeaders);
        return result;
    }

    public Map<String, String> withEmbedded(String... kvps) {
        Map<String, String> map = new HashMap<>(LdapHeaderMappings.EMBEDDED_MAPPINGS);
        map.putAll(asMap(kvps));
        return map;
    }

    public Map<String, String> withEmbedded(Map<String, String> config) {
        Map<String, String> map = new HashMap<>(config);
        // if config already has an embedded mapping, it's an override, so don't
        // re-override
        LdapHeaderMappings.EMBEDDED_MAPPINGS.forEach(map::putIfAbsent);
        return map;
    }

    public Map<String, String> asMap(String... kvps) {
        assertTrue(kvps.length % 2 == 0, "Required an even number of values to create a map");
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < kvps.length - 1; i += 2) {
            map.put(kvps[i], kvps[i + 1]);
        }
        return map;
    }

    public Map<String, String> buildHeaders(String... kvps) {
        Map<String, String> mappings = withEmbedded(asMap(kvps));
        Map<String, String> headers = new HashMap<>();
        mappings.forEach((name, att) -> headers.put(name, resolve(name, att)));
        return headers;
    }

    public String resolve(String header, String att) {
        HeaderMapping mapping = HeaderMapping.valueOf(att, att);
        String value = resolve(mapping);
        return value;
    }

    private String resolve(HeaderMapping mapping) {
        final String ldapAttribute = mapping.getLdapAttribute();
        ListMultimap<String, ?> contextMap = resolveContextMap(mapping.getFullPropertyName());

        List<?> list = contextMap.get(ldapAttribute);
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Property not found: " + ldapAttribute);
        }
        String encoded = mapping.encode(list.toArray(new String[list.size()]));
        return encoded;
    }

    @SuppressWarnings("unchecked")
    private ListMultimap<String, Object> resolveContextMap(String propertyName) {
        propertyName = HeaderMapping.stripEncoding(propertyName);

        ListMultimap<String, Object> contextMap;
        if (propertyName.startsWith("org.seeAlso.")) {
            contextMap = (ListMultimap<String, Object>) orgExtContextMap;
        } else if (propertyName.startsWith("org.")) {
            contextMap = (ListMultimap<String, Object>) orgContextMap;
        } else if (propertyName.startsWith("manager")) {
            contextMap = (ListMultimap<String, Object>) managerContextMap;
        } else if (propertyName.indexOf('.') == -1) {
            contextMap = (ListMultimap<String, Object>) userContextMap;
        } else if (propertyName.indexOf('.') == -1) {
            contextMap = (ListMultimap<String, Object>) userContextMap;
        } else {
            throw new IllegalArgumentException(propertyName);
        }
        return contextMap;
    }

    private DirContextOperations resolveContext(String propertyName) {
        propertyName = HeaderMapping.stripEncoding(propertyName);

        DirContextOperations context;
        if (propertyName.startsWith("org.seeAlso.")) {
            context = orgExtContext;
        } else if (propertyName.startsWith("org.")) {
            context = orgContext;
        } else if (propertyName.startsWith("manager")) {
            context = managerContext;
        } else if (propertyName.indexOf('.') == -1) {
            context = userContext;
        } else {
            throw new IllegalArgumentException(propertyName);
        }
        return context;
    }

    /**
     * replaces the property with a new value, this removes all previous values for
     * the given ldap property
     */
    public void setProperty(String name, String value) {
        ListMultimap<String, Object> contextMap = resolveContextMap(name);
        DirContextOperations context = resolveContext(name);

        contextMap.removeAll(name);
        contextMap.put(name, value);
        context.setAttributeValue(name, value);
    }
}
