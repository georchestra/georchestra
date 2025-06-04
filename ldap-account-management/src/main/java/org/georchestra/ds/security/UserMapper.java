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
package org.georchestra.ds.security;

import static org.mapstruct.ReportingPolicy.ERROR;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.roles.Role;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.users.Account;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.GeorchestraUserHasher;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import lombok.Setter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ERROR)
public abstract class UserMapper {

    private @Autowired @Setter RoleDao roleDao;

    @Mapping(target = "id", source = "uniqueIdentifier")
    @Mapping(target = "username", source = "uid")
    @Mapping(target = "organization", source = "org")
    @Mapping(target = "firstName", source = "givenName")
    @Mapping(target = "lastName", source = "surname")
    @Mapping(target = "telephoneNumber", source = "phone")
    @Mapping(target = "notes", source = "note")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "ldapWarn", ignore = true)
    @Mapping(target = "ldapRemainingDays", ignore = true)
    @Mapping(target = "isExternalAuth", ignore = true)
    protected abstract GeorchestraUser map(Account account);

    String map(UUID value) {
        return value == null ? null : value.toString();
    }

    @AfterMapping
    protected void addRoles(Account source, @MappingTarget GeorchestraUser target) {
        List<String> roles = findRoles(source);
        target.setRoles(roles);

        String hash = GeorchestraUserHasher.createHash(target);
        target.setLastUpdated(hash);
    }

    @AfterMapping
    protected void setOrgToNullIfEmpty(Account source, @MappingTarget GeorchestraUser target) {
        if (!StringUtils.hasLength(source.getOrg())) {
            target.setOrganization(null);
        }
    }

    private List<String> findRoles(Account source) {
        List<String> roles;
        try {
            roles = roleDao.findAllForUser(source).stream().map(Role::getName).collect(Collectors.toList());
        } catch (DataServiceException e) {
            throw new RuntimeException("Error getting roles for account " + source.getUid(), e);
        }
        return roles;
    }
}
