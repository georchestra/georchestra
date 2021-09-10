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
package org.georchestra.datafeeder.api;

import java.util.UUID;

import org.georchestra.config.security.GeorchestraUserDetails;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.security.model.GeorchestraUser;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.NonNull;

/**
 * Utility service that checks existence of a DataUpload and verifies access
 * rights.
 *
 */
@Component
public class AuthorizationService {

    private @Autowired DataUploadService uploadService;
    public static final UserInfoMapper userInfoMapper = Mappers.getMapper(UserInfoMapper.class);

    public @NonNull String getUserName() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        String userName = auth.getName();
        return userName;
    }

    public @Nullable String getUserOrgName() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof GeorchestraUserDetails) {
            GeorchestraUserDetails userDetails = (GeorchestraUserDetails) principal;
            GeorchestraUser user = userDetails.getUser();
            return user.getOrganization().getId();
        }
        return null;
    }

    private DataUploadJob getOrNotFound(UUID uploadId) {
        DataUploadJob state = this.uploadService.findJob(uploadId)
                .orElseThrow(() -> ApiException.notFound("upload %s does not exist", uploadId));
        return state;
    }

    private @NonNull boolean isAdministrator() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMINISTRATOR"::equals);
    }

    public void checkAccessRights(UUID uploadId) {
        DataUploadJob state = getOrNotFound(uploadId);
        final String userName = getUserName();
        if (!userName.equals(state.getUsername()) && !isAdministrator()) {
            throw ApiException.forbidden("User %s has no access rights to this upload", userName);
        }
    }

    public @NonNull UserInfo getUserInfo() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        Object principal = auth.getPrincipal();
        UserInfo user = new UserInfo();
        if (principal instanceof GeorchestraUserDetails) {
            GeorchestraUserDetails georUser = (GeorchestraUserDetails) principal;
            return userInfoMapper.map(georUser);
        }
        return user;
    }
}
