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

import javax.annotation.security.RolesAllowed;

import org.georchestra.config.security.GeorchestraSecurityProxyAuthenticationConfigurationTest;
import org.georchestra.config.security.GeorchestraUserDetails;
import org.georchestra.datafeeder.model.UserInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import springfox.documentation.annotations.ApiIgnore;

/**
 *
 * @see GeorchestraSecurityProxyAuthenticationConfigurationTest
 */
@ApiIgnore
@RequestMapping(path = "/test/security/georchestra")
public @Controller class GeorchestraSecurityTestController {

    @GetMapping("/anonymous")
    @RolesAllowed({ "ROLE_ANONYMOUS", "ROLE_ADMINISTRATOR", "ROLE_USER" })
    public ResponseEntity<UserInfo> testAnonymous() {
        UserInfo user = getPrincipal();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/admin")
    @RolesAllowed("ROLE_ADMINISTRATOR")
    public ResponseEntity<UserInfo> testAdmin() {
        UserInfo user = getPrincipal();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/user")
    @RolesAllowed({ "ROLE_USER", "ROLE_ADMINISTRATOR" })
    public ResponseEntity<UserInfo> testUser() {
        UserInfo user = getPrincipal();
        return ResponseEntity.ok(user);
    }

    private UserInfo getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        GeorchestraUserDetails principal = (GeorchestraUserDetails) authentication.getPrincipal();
        return AuthorizationService.userInfoMapper.map(principal);
    }
}
