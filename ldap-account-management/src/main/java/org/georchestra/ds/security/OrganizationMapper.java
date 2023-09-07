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

import org.georchestra.ds.orgs.Org;
import org.georchestra.security.model.GeorchestraUserHasher;
import org.georchestra.security.model.Organization;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ERROR)
public interface OrganizationMapper {

    @Mapping(target = "id", source = "uniqueIdentifier")
    @Mapping(target = "shortName", source = "id")
    @Mapping(target = "notes", source = "note")
    @Mapping(target = "linkage", source = "url")
    @Mapping(target = "postalAddress", source = "address")
    @Mapping(target = "category", source = "orgType")
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "withId", ignore = true)
    @Mapping(target = "withShortName", ignore = true)
    @Mapping(target = "withName", ignore = true)
    @Mapping(target = "withLinkage", ignore = true)
    @Mapping(target = "withPostalAddress", ignore = true)
    @Mapping(target = "withCategory", ignore = true)
    @Mapping(target = "withDescription", ignore = true)
    @Mapping(target = "withNotes", ignore = true)
    @Mapping(target = "withLastUpdated", ignore = true)
    @Mapping(target = "withMembers", ignore = true)
    @Mapping(target = "withMail", source = "mail", ignore = true)
    Organization map(Org org);

    @AfterMapping
    default void addLastUpdated(Org source, @MappingTarget Organization target) {
        String hash = GeorchestraUserHasher.createHash(target, source.getLogo().length());
        target.setLastUpdated(hash);
    }

    default String map(UUID value) {
        return value == null ? null : value.toString();
    }
}
