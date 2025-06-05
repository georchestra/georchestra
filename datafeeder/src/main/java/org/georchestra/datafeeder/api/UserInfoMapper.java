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

package org.georchestra.datafeeder.api;

import java.util.Optional;

import org.georchestra.config.security.GeorchestraUserDetails;
import org.georchestra.datafeeder.model.Organization;
import org.georchestra.datafeeder.model.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserInfoMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "roles", source = "user.roles")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "postalAddress", source = "user.postalAddress")
    @Mapping(target = "telephoneNumber", source = "user.telephoneNumber")
    @Mapping(target = "title", source = "user.title")
    @Mapping(target = "notes", source = "user.notes")
    UserInfo map(GeorchestraUserDetails principal);

    default Organization map(Optional<org.georchestra.security.model.Organization> value) {
        return map(value.orElse(null));
    }

    Organization map(org.georchestra.security.model.Organization georOrg);
}
