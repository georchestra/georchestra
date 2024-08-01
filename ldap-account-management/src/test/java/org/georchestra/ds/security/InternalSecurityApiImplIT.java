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
package org.georchestra.ds.security;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.georchestra.security.api.OrganizationsApi;
import org.georchestra.security.api.RolesApi;
import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.georchestra.security.model.Role;
import org.georchestra.testcontainers.ldap.GeorchestraLdapContainer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "classpath:testApplicationContext.xml" })
public class InternalSecurityApiImplIT {

    public static @ClassRule GeorchestraLdapContainer ldap = new GeorchestraLdapContainer();// .withLogToStdOut();

    private @Autowired UsersApi users;
    private @Autowired OrganizationsApi orgs;
    private @Autowired RolesApi roles;

    private static Map<String, GeorchestraUser> expectedUsers;
    private static Map<String, Organization> expectedOrganizations;
    private static Map<String, Role> expectedRoles;

    public static @BeforeClass void setup() throws Exception {
        Integer port = ldap.getFirstMappedPort();
        System.err.println("Running test against " + ldap.getContainerName() + " on port " + port);

        List<GeorchestraUser> defaultUsers = loadJson("/defaultUsers.json", GeorchestraUser.class);
        List<Organization> defaultOrganizations = loadJson("/defaultOrganizations.json", Organization.class);
        List<Role> defaultRoles = loadJson("/defaultRoles.json", Role.class);
        assertEquals(6, defaultUsers.size());
        assertEquals(2, defaultOrganizations.size());
        assertEquals(11, defaultRoles.size());

        expectedUsers = toMap(defaultUsers, GeorchestraUser::getId);
        expectedOrganizations = toMap(defaultOrganizations, Organization::getId);
        expectedRoles = toMap(defaultRoles, Role::getId);

        assertEquals(defaultUsers.size(), expectedUsers.size());
        assertEquals(defaultOrganizations.size(), expectedOrganizations.size());
        assertEquals(defaultRoles.size(), expectedRoles.size());
    }

    private static <T> List<T> loadJson(String resource, Class<T> type) throws Exception {
        URL url = InternalSecurityApiImplIT.class.getResource(resource);
        assertNotNull(url);

        ObjectMapper mapper = new ObjectMapper();
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, type);
        List<T> readValues = mapper.readValue(url, collectionType);
        return Collections.unmodifiableList(readValues);
    }

    public @Test void usersApiFindAll() {
        Map<String, GeorchestraUser> actual = toMap(users.findAll(), GeorchestraUser::getId);
        assertEquals(expectedUsers, actual);
    }

    public @Test void usersApiFindById() {
        expectedUsers.values().forEach(user -> {
            Optional<GeorchestraUser> found = users.findById(user.getId());
            assertTrue(found.isPresent());
            assertEquals(found.get(), user);
        });
    }

    public @Test void usersApiFindByUsername() {
        expectedUsers.values().forEach(expected -> {
            Optional<GeorchestraUser> found = users.findByUsername(expected.getUsername());
            assertTrue(found.isPresent());
            assertEquals(found.get(), expected);
        });
    }

    public @Test void organizationsApiFindAll() {
        Map<String, Organization> actual = toMap(orgs.findAll(), Organization::getId);
        assertEquals(expectedOrganizations, actual);
    }

    public @Test void organizationsApiFindById() {
        expectedOrganizations.values().forEach(expected -> {
            Optional<Organization> found = orgs.findById(expected.getId());
            assertTrue(found.isPresent());
            assertEquals(found.get(), expected);
        });
    }

    public @Test void organizationsApiFindByShortName() {
        expectedOrganizations.values().forEach(expected -> {
            Optional<Organization> found = orgs.findByShortName(expected.getShortName());
            assertTrue(found.isPresent());
            assertEquals(found.get(), expected);
        });
    }

    public @Test void organizationsApiGetLogo() throws NoSuchAlgorithmException {
        byte[] logo = orgs.getLogo("bddf474d-125d-4b18-92bd-bd8ebb6699a9").get();
        byte[] md5 = MessageDigest.getInstance("MD5").digest(logo);
        assertArrayEquals(new byte[] { -81, 25, 73, -126, -100, -125, 2, 34, 45, -47, 60, -40, -123, -105, 107, 61 },
                md5);
    }

    public @Test void organizationsApiGetLogoNoLogo() {
        assertFalse(orgs.getLogo("8c1ef87a-73fc-4d79-80cb-ba4ff7102cca").isPresent());
    }

    public @Test void rolesApiFindAll() {
        Map<String, Role> actual = toMap(roles.findAll(), Role::getId);
        assertEquals(expectedRoles, actual);
    }

    public @Test void rolesApiFindByName() {
        expectedRoles.values().forEach(role -> {
            Optional<Role> found = roles.findByName(role.getName());
            assertTrue(found.isPresent());
            assertEquals(found.get(), role);
        });
    }

    private static <T> Map<String, T> toMap(List<T> values, Function<T, String> idExtractor) {
        return values.stream().collect(Collectors.toMap(idExtractor, Function.identity()));
    }
}
