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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.ds.roles.Role;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.users.Account;
import org.georchestra.security.model.Organization;

import lombok.NonNull;

class MappingContext {

    private Map<String, Organization> orgs = new HashMap<>();

    public List<String> getRoles(@NonNull Account account, @NonNull RoleDao roleDao) {
        try {
            return roleDao.findAllForUser(account).stream().map(Role::getName).collect(Collectors.toList());
        } catch (DataServiceException e) {
            throw new RuntimeException("Error getting roles for account " + account.getUid(), e);
        }
    }

    public Organization getOrg(@NonNull Account source, @NonNull OrgsDao orgsDao,
            @NonNull OrganizationMapper orgMapper) {
        return orgs.computeIfAbsent(source.getOrg(), dn -> buildOrg(dn, orgsDao, orgMapper));
    }

    private Organization buildOrg(String orgDn, OrgsDao orgsDao, OrganizationMapper orgMapper) {
        Org org = orgsDao.findByCommonNameWithExt(orgDn);
        return orgMapper.map(org);
    }
}
