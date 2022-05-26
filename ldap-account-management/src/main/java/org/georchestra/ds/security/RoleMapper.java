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

import static org.mapstruct.ReportingPolicy.ERROR;

import java.util.UUID;

import org.georchestra.security.model.GeorchestraUserHasher;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ERROR)
public interface RoleMapper {

    @Mapping(target = "id", source = "uniqueIdentifier")
    @Mapping(target = "members", source = "userList")
    @Mapping(target = "lastUpdated", ignore = true)
    org.georchestra.security.model.Role map(org.georchestra.ds.roles.Role role);

    @AfterMapping
    default void addLastUpdated(org.georchestra.ds.roles.Role source,
            @MappingTarget org.georchestra.security.model.Role target) {
        String hash = GeorchestraUserHasher.createHash(target);
        target.setLastUpdated(hash);
    }

    default String map(UUID value) {
        return value == null ? null : value.toString();
    }
}
