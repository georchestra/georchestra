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
package org.georchestra.datafeeder.service;

import java.util.UUID;

import org.georchestra.datafeeder.api.ApiException;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.NonNull;

/**
 * Utility service that checks existence of a DataUpload
 * and verifies access rights.
 *
 */
@Component
public class DataUploadValidityService {
    
    private @Autowired DataUploadService uploadService;
    
    public String getUserName() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        String userName = auth.getName();
        return userName;
    }
    
    public DataUploadJob getOrNotFound(UUID uploadId) {
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
    
    public DataUploadJob getAndCheckAccessRights(UUID uploadId) {
        DataUploadJob state = getOrNotFound(uploadId);
        final String userName = getUserName();
        if (!userName.equals(state.getUsername()) && !isAdministrator()) {
            throw ApiException.forbidden("User %s has no access rights to this upload", userName);
        }
        return state;
    }
}
