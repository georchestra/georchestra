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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.georchestra.console.mailservice.EmailFactory;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.security.api.OrganizationsApi;
import org.georchestra.security.api.RolesApi;
import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.georchestra.security.model.Role;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/internal", produces = MediaType.APPLICATION_JSON_VALUE)
public class SecurityApiController {

    private @Autowired RolesApi roles;
    private @Autowired OrganizationsApi orgs;
    private @Autowired UsersApi users;

    private @Autowired @Setter LogUtils logUtils;

    private @Autowired @Setter RoleDao roleDao;

    private @Autowired @Setter AccountDao accountDao;

    private @Autowired @Setter EmailFactory emailFactory;

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
     * Fetch users by a list of IDs with optional field projection.
     * <p>
     * Request body example:
     *
     * <pre>
     * {
     *   "usernames": ["testadmin", "testuser"],
     *   "fields": ["firstName", "lastName", "email"]
     * }
     * </pre>
     *
     * If {@code fields} is null or empty, all user fields are returned.
     *
     * @param request the batch request containing IDs and optional fields
     * @return a list of users (as maps if fields are specified, or full objects
     *         otherwise)
     */
    @PostMapping(value = "/users/fetch-by-ids")
    @ResponseBody
    public List<?> findUsersByUsernames(@RequestBody UserBatchRequest request) {
        Set<String> ids = request.getUsernames() != null ? new HashSet<>(request.getUsernames()) : Set.of();
        if (ids.isEmpty()) {
            return List.of();
        }
        List<GeorchestraUser> userList = users.findAll().stream().filter(user -> ids.contains(user.getUsername()))
                .collect(Collectors.toList());

        List<String> fields = request.getFields();
        if (fields != null && !fields.isEmpty()) {
            return UserFieldsProjector.projectAll(userList, fields);
        }
        return userList;
    }

    /**
     * Return a user by {@link GeorchestraUser#getId() id}
     * <p>
     * This is the server-side counterpart of {@link UsersApi#findById}
     */
    @GetMapping(value = "/users/id/{id}")
    public ResponseEntity<GeorchestraUser> findUserById(@PathVariable String id) {
        return toEntityOrNotFound(users.findById(id));
    }

    /**
     * Return a user by {@link GeorchestraUser#getUsername() username}.
     * <p>
     * This is the server-side counterpart of {@link UsersApi#findByUsername}
     */
    @GetMapping(value = "/users/username/{name:.+}")
    public ResponseEntity<GeorchestraUser> findUserByUsername(@PathVariable String name) {
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
    public ResponseEntity<Organization> findOrganizationById(@PathVariable String id) {
        return toEntityOrNotFound(this.orgs.findById(id));
    }

    /**
     * Return an organization by {@link Organization#getShortName shortName}
     * <p>
     * This is the server-side counterpart of
     * {@link OrganizationsApi#findByShortName}
     */
    @GetMapping(value = "/organizations/shortname/{name}")
    public ResponseEntity<Organization> findOrganizationByShortName(@PathVariable String name) {
        return toEntityOrNotFound(this.orgs.findByShortName(name));
    }

    @GetMapping(value = "/organizations/id/{id}/logo", produces = "application/octet-stream")
    public ResponseEntity<byte[]> getOrganizationLogo(@PathVariable String id) {
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
    public ResponseEntity<Role> findRoleByName(@PathVariable String name) {
        return toEntityOrNotFound(roles.findByName(name));
    }

    @PostMapping(value = "/events/accountcreated", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeorchestraUser> createUser(HttpServletRequest request, @RequestBody String rawRequest) {
        JSONObject jsonObj = new JSONObject(rawRequest);
        try {
            String fullName = jsonObj.getString("fullName");
            String localUid = jsonObj.getString("localUid");
            String email = jsonObj.getString("email");
            String providerName = jsonObj.getString("providerName");
            String providerUid = jsonObj.getString("providerUid");
            String organization = null;
            if (jsonObj.has("organization")) {
                organization = jsonObj.getString("organization");
            }
            List<String> superUserAdmins = this.roleDao.findByCommonName("SUPERUSER").getUserList().stream()
                    .map(user -> {
                        try {
                            return this.accountDao.findByUID(user).getEmail();
                        } catch (DataServiceException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());

            final ServletContext servletContext = request.getSession().getServletContext();
            this.emailFactory.sendNewOAuth2AccountNotificationEmail(servletContext, superUserAdmins, fullName, localUid,
                    email, providerName, providerUid, organization, true);

            logUtils.createOAuth2Log(localUid, AdminLogType.OAUTH2_USER_CREATED, null);
        } catch (Exception e) {
            log.error("Error while processing rabbitMq message, message will be discarded for future processing.", e);
        }
        return ResponseEntity.ok().build();
    }

    private <T> ResponseEntity<T> toEntityOrNotFound(Optional<T> found) {
        if (found.isPresent()) {
            return ResponseEntity.ok(found.get());
        }
        return ResponseEntity.ok().build();
    }
}
