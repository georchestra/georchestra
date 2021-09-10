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

import static org.mapstruct.ReportingPolicy.ERROR;

import java.util.List;

import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.ds.RoleDao;
import org.georchestra.console.dto.Account;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.GeorchestraUserHasher;
import org.georchestra.security.model.Organization;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ERROR, uses = UUIDMapper.class)
abstract class UserMapper {

    private @Autowired OrganizationMapper orgMapper;
    private @Autowired BeanFactory appContext;

    private MappingContext context;

    public GeorchestraUser map(Account account) {
        if (context == null)
            context = new MappingContext();
        return map(account, context);
    }

    @Mapping(target = "id", source = "uniqueIdentifier")
    @Mapping(target = "username", source = "uid")
    @Mapping(target = "firstName", source = "givenName")
    @Mapping(target = "lastName", source = "surname")
    @Mapping(target = "telephoneNumber", source = "phone")
    @Mapping(target = "notes", source = "note")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    protected abstract GeorchestraUser map(Account account, @Context MappingContext context);

    @AfterMapping
    protected void addOrgAndRoles(Account source, @MappingTarget GeorchestraUser target,
            @Context MappingContext context) {

        final RoleDao roleDao = appContext.getBean(RoleDao.class);
        final OrgsDao orgsDao = appContext.getBean(OrgsDao.class);

        List<String> roles = context.getRoles(source, roleDao);
        target.setRoles(roles);

        Organization organization = context.getOrg(source, orgsDao, orgMapper);
        target.setOrganization(organization);

        String hash = GeorchestraUserHasher.createLastUpdatedUserHash(target);
        target.setLastUpdated(hash);
    }
}
