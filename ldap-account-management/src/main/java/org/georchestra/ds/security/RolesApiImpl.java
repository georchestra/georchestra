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

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.security.api.RolesApi;
import org.georchestra.security.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicates;

import lombok.Setter;

@Service
public class RolesApiImpl implements RolesApi {
    private @Autowired @Setter RoleDao roleDao;
    private @Autowired @Setter RoleMapper roleMapper;

    @Override
    public Optional<Role> findByName(String name) {
        try {
            return Optional.of(this.roleDao.findByCommonName(name)).filter(this.notPending()).map(roleMapper::map);
        } catch (NameNotFoundException e) {
            return Optional.empty();
        } catch (DataServiceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Role> findAll() {
        try {
            return this.roleDao.findAll().stream().filter(this.notPending()).map(roleMapper::map)
                    .collect(Collectors.toList());
        } catch (DataServiceException e) {
            throw new RuntimeException(e);
        }
    }

    private Predicate<org.georchestra.ds.roles.Role> notPending() {
        return Predicates.not(org.georchestra.ds.roles.Role::isPending);
    }
}
