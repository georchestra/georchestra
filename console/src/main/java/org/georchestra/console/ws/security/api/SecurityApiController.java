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
package org.georchestra.console.ws.security.api;

import java.util.List;
import java.util.Optional;

import org.georchestra.security.api.OrganizationsApi;
import org.georchestra.security.api.RolesApi;
import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.georchestra.security.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/internal", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class SecurityApiController {

    private @Autowired RolesApi roles;
    private @Autowired OrganizationsApi orgs;
    private @Autowired UsersApi users;

    /**
     * Return a list of available users as a json array.
     * <p>
     * This is the server-side counterpart of {@link UsersApi#findAll}
     */
    @GetMapping(value = "/users")
    @ResponseBody
    public List<GeorchestraUser> findUsers() {
        return users.findAll();
    }

    /**
     * Return a user by {@link GeorchestraUser#getId() id}
     * <p>
     * This is the server-side counterpart of {@link UsersApi#findById}
     */
    @GetMapping(value = "/users/id/{id}")
    public ResponseEntity<GeorchestraUser> findUserById(@PathVariable("id") String id) {
        return toEntityOrNotFound(users.findById(id));
    }

    /**
     * Return a user by {@link GeorchestraUser#getUsername() username}.
     * <p>
     * This is the server-side counterpart of {@link UsersApi#findByUsername}
     */
    @GetMapping(value = "/users/username/{name:.+}")
    public ResponseEntity<GeorchestraUser> findUserByUsername(@PathVariable("name") String name) {
        return toEntityOrNotFound(users.findByUsername(name));
    }

    /**
     * Return a list of available users as a json array
     * <p>
     * This is the server-side counterpart of {@link OrganizationsApi#findAll}
     */
    @GetMapping(value = "/organizations")
    @ResponseBody
    public List<Organization> findOrganizations() {
        return orgs.findAll();
    }

    /**
     * Return an organization by {@link Organization#getId() id}
     * <p>
     * This is the server-side counterpart of {@link OrganizationsApi#findById}
     */
    @GetMapping(value = "/organizations/id/{id}")
    public ResponseEntity<Organization> findOrganizationById(@PathVariable("id") String id) {
        return toEntityOrNotFound(this.orgs.findById(id));
    }

    /**
     * Return an organization by {@link Organization#getShortName shortName}
     * <p>
     * This is the server-side counterpart of
     * {@link OrganizationsApi#findByShortName}
     */
    @GetMapping(value = "/organizations/shortname/{name}")
    public ResponseEntity<Organization> findOrganizationByShortName(@PathVariable("name") String name) {
        return toEntityOrNotFound(this.orgs.findByShortName(name));
    }

    @GetMapping(value = "/organizations/id/{id}/logo", produces = "application/octet-stream")
    public ResponseEntity<byte[]> getOrganizationLogo(@PathVariable("id") String id) {
        return toEntityOrNotFound(this.orgs.getLogo(id));
    }

    /**
     * Return a list of available roles as a json array
     * <p>
     * This is the server-side counterpart of {@link RolesApi#findAll}
     */
    @GetMapping(value = "/roles")
    @ResponseBody
    public List<Role> findRoles() {
        return roles.findAll();
    }

    /**
     * Return a Role by its {@link Role#getName() name}
     * <p>
     * This is the server-side counterpart of {@link RolesApi#findByName}
     */
    @GetMapping(value = "/roles/name/{name}")
    public ResponseEntity<Role> findRoleByName(@PathVariable("name") String name) {
        return toEntityOrNotFound(roles.findByName(name));
    }

    private <T> ResponseEntity<T> toEntityOrNotFound(Optional<T> found) {
        if (found.isPresent()) {
            return ResponseEntity.ok(found.get());
        }
        return ResponseEntity.notFound().build();

    }
}
