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

import static com.google.common.base.Predicates.not;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.security.api.OrganizationsApi;
import org.georchestra.security.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Setter;

@Service
public class OrganizationsApiImpl implements OrganizationsApi {

    private @Autowired @Setter OrgsDao orgsDao;
    private @Autowired @Setter OrganizationMapper orgMapper;

    @Override
    public List<Organization> findAll() {
        return orgsDao.findAll()//
                .stream()//
                .filter(not(Org::isPending))//
                .map(orgMapper::map)//
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Organization> findById(String id) {
        final UUID uuid = UUID.fromString(id);
        return Optional.ofNullable(orgsDao.findById(uuid))//
                .filter(not(Org::isPending))//
                .map(orgMapper::map);
    }

    @Override
    public Optional<Organization> findByShortName(String shortName) {
        return Optional.ofNullable(orgsDao.findByCommonName(shortName))//
                .filter(not(Org::isPending))//
                .map(orgMapper::map);
    }

    @Override
    public Optional<Organization> findByOrgUniqueId(String orgUniqueId) {
        return Optional.ofNullable(orgsDao.findByOrgUniqueId(orgUniqueId))//
                .filter(not(Org::isPending))//
                .map(orgMapper::map);
    }

    @Override
    public Optional<byte[]> getLogo(String id) {
        UUID uuid = UUID.fromString(id);
        Org org = orgsDao.findById(uuid);
        if (org == null) {
            return Optional.empty();
        }
        byte[] base64encoded = org.getLogo().getBytes(StandardCharsets.UTF_8);
        if (base64encoded.length == 0) {
            return Optional.empty();
        }
        return Optional.of(Base64.getMimeDecoder().decode(base64encoded));
    }

}
