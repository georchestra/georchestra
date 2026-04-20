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
package org.georchestra.console.ws.security.api;

import org.georchestra.security.model.GeorchestraUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserFieldsProjectorTest {

    private GeorchestraUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new GeorchestraUser();
        testUser.setId("user-uuid-123");
        testUser.setUsername("jdoe");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setOrganization("TestOrg");
        testUser.setRoles(Arrays.asList("USER", "ADMIN"));
        testUser.setTitle("Developer");
        testUser.setTelephoneNumber("+33123456789");
    }

    @Test
    void project_withSpecificFields_returnsOnlyRequestedFields() {
        List<String> fields = Arrays.asList("firstName", "lastName", "email");

        Map<String, Object> result = UserFieldsProjector.project(testUser, fields);

        assertEquals(3, result.size());
        assertEquals("John", result.get("firstName"));
        assertEquals("Doe", result.get("lastName"));
        assertEquals("john.doe@example.com", result.get("email"));
        assertFalse(result.containsKey("id"));
        assertFalse(result.containsKey("username"));
    }

    @Test
    void project_withNullFields_returnsAllFields() {
        Map<String, Object> result = UserFieldsProjector.project(testUser, null);

        assertTrue(result.size() > 5);
        assertEquals("user-uuid-123", result.get("id"));
        assertEquals("jdoe", result.get("username"));
        assertEquals("John", result.get("firstName"));
        assertEquals("Doe", result.get("lastName"));
        assertEquals("john.doe@example.com", result.get("email"));
    }

    @Test
    void project_withEmptyFields_returnsAllFields() {
        Map<String, Object> result = UserFieldsProjector.project(testUser, List.of());

        assertTrue(result.size() > 5);
        assertEquals("user-uuid-123", result.get("id"));
    }

    @Test
    void project_withInvalidFields_ignoresInvalidFields() {
        List<String> fields = Arrays.asList("firstName", "invalidField", "lastName");

        Map<String, Object> result = UserFieldsProjector.project(testUser, fields);

        assertEquals(2, result.size());
        assertEquals("John", result.get("firstName"));
        assertEquals("Doe", result.get("lastName"));
        assertFalse(result.containsKey("invalidField"));
    }

    @Test
    void projectAll_withMultipleUsers_projectsAllUsers() {
        GeorchestraUser user2 = new GeorchestraUser();
        user2.setId("user-uuid-456");
        user2.setUsername("asmith");
        user2.setFirstName("Alice");
        user2.setLastName("Smith");
        user2.setEmail("alice.smith@example.com");

        List<GeorchestraUser> users = Arrays.asList(testUser, user2);
        List<String> fields = Arrays.asList("firstName", "lastName");

        List<Map<String, Object>> result = UserFieldsProjector.projectAll(users, fields);

        assertEquals(2, result.size());

        Map<String, Object> first = result.get(0);
        assertEquals("John", first.get("firstName"));
        assertEquals("Doe", first.get("lastName"));

        Map<String, Object> second = result.get(1);
        assertEquals("Alice", second.get("firstName"));
        assertEquals("Smith", second.get("lastName"));
    }

    @Test
    void project_withRolesField_returnsRolesList() {
        List<String> fields = List.of("roles");

        Map<String, Object> result = UserFieldsProjector.project(testUser, fields);

        assertEquals(1, result.size());
        assertTrue(result.get("roles") instanceof List);
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) result.get("roles");
        assertEquals(2, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
    }
}
