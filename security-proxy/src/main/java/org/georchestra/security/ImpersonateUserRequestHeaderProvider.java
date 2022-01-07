/*
 * Copyright (C) 2009-2022 by the geOrchestra PSC
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

package org.georchestra.security;

import static org.georchestra.commons.security.SecurityHeaders.IMP_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.IMP_USERNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.ImmutableMap;

/**
 * Allows certain white-listed users to impersonate other users.
 *
 * @author Jesse on 5/5/2014.
 */
public class ImpersonateUserRequestHeaderProvider extends HeaderProvider {
    private List<String> trustedUsers = new ArrayList<String>();

    @PostConstruct
    public void init() {
        logger.info(
                String.format("User impersonation enabled through request headers %s and %s", IMP_USERNAME, IMP_ROLES));
    }

    @Override
    public Map<String, String> getCustomRequestHeaders(HttpServletRequest originalRequest, String targetServiceName) {
        if (originalRequest.getHeader(IMP_USERNAME) != null) {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && trustedUsers != null && trustedUsers.contains(authentication.getName())) {
                return ImmutableMap.of(//
                        SEC_USERNAME, originalRequest.getHeader(IMP_USERNAME), //
                        SEC_ROLES, originalRequest.getHeader(IMP_ROLES));
            }
        }
        return Collections.emptyMap();

    }

    /**
     * Set the users who are allowed to impersonate other users.
     *
     * @param trustedUsers list of trusted users
     */
    public void setTrustedUsers(List<String> trustedUsers) {
        this.trustedUsers = trustedUsers;
    }
}
