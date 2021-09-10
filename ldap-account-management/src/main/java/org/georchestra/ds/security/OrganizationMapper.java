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

import org.georchestra.ds.orgs.Org;
import org.georchestra.security.model.GeorchestraUserHasher;
import org.georchestra.security.model.Organization;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = UUIDMapper.class, unmappedTargetPolicy = ERROR)
public interface OrganizationMapper {

    @Mapping(target = "id", source = "uniqueIdentifier")
    @Mapping(target = "shortName", source = "id")
    @Mapping(target = "notes", source = "note")
    @Mapping(target = "linkage", source = "url")
    @Mapping(target = "postalAddress", source = "orgAddress")
    @Mapping(target = "category", source = "orgType")
    @Mapping(target = "lastUpdated", ignore = true)
    Organization map(Org org);

    @AfterMapping
    default void addLastUpdated(Org source, @MappingTarget Organization target) {
        String hash = GeorchestraUserHasher.createLastUpdatedOrgHash(target);
        target.setLastUpdated(hash);
    }
}
