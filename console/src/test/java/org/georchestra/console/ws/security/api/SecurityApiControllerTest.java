/*
 * Copyright (C) 2009-2026 by the geOrchestra PSC
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

import org.georchestra.security.api.OrganizationsApi;
import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityApiControllerTest {

    private @Mock OrganizationsApi orgs;
    private @Mock UsersApi users;

    private @InjectMocks SecurityApiController controller;

    @Test
    void findOrganizationsByShortNamesReturnsFoundOrgsInRequestOrder() {
        Organization c2c = org("c2c");
        Organization psc = org("psc");
        when(orgs.findByShortName("psc")).thenReturn(Optional.of(psc));
        when(orgs.findByShortName("unknown")).thenReturn(Optional.empty());
        when(orgs.findByShortName("c2c")).thenReturn(Optional.of(c2c));

        List<Organization> result = controller.findOrganizationsByShortNames(List.of("psc", "unknown", "c2c", "psc"));

        assertEquals(List.of(psc, c2c), result);
    }

    @Test
    void findOrganizationsByShortNamesDoesNotReturnMembers() {
        Organization c2c = org("c2c");
        c2c.setMembers(List.of("testadmin", "testuser"));
        when(orgs.findByShortName("c2c")).thenReturn(Optional.of(c2c));

        List<Organization> result = controller.findOrganizationsByShortNames(List.of("c2c"));

        assertEquals(1, result.size());
        assertEquals("c2c", result.get(0).getShortName());
        assertTrue(result.get(0).getMembers().isEmpty());
    }

    @Test
    void findOrganizationsByShortNamesWithNoShortNamesReturnsEmptyList() {
        assertTrue(controller.findOrganizationsByShortNames(null).isEmpty());
        assertTrue(controller.findOrganizationsByShortNames(List.of()).isEmpty());
    }

    @Test
    void findUsersByUsernamesReturnsFoundUsersInRequestOrder() {
        GeorchestraUser admin = user("testadmin");
        GeorchestraUser user = user("testuser");
        when(users.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(users.findByUsername("unknown")).thenReturn(Optional.empty());
        when(users.findByUsername("testadmin")).thenReturn(Optional.of(admin));

        List<GeorchestraUser> result = controller
                .findUsersByUsernames(List.of("testuser", "unknown", "testadmin", "testuser"));

        assertEquals(List.of(user, admin), result);
    }

    @Test
    void findUsersByUsernamesWithNoUsernamesReturnsEmptyList() {
        assertTrue(controller.findUsersByUsernames(null).isEmpty());
        assertTrue(controller.findUsersByUsernames(List.of()).isEmpty());
    }

    private static GeorchestraUser user(String username) {
        GeorchestraUser user = new GeorchestraUser();
        user.setId(username + "-uuid");
        user.setUsername(username);
        return user;
    }

    private static Organization org(String shortName) {
        Organization org = new Organization();
        org.setId(shortName + "-uuid");
        org.setShortName(shortName);
        return org;
    }
}
