/*
 * Copyright (C) 2020 by the geOrchestra PSC
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
package org.georchestra.config.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeorchestraSecurityProxyAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    @Override
    protected GeorchestraUserDetails getPreAuthenticatedPrincipal(HttpServletRequest request) {
        final boolean preAuthenticated = Boolean.parseBoolean(request.getHeader("sec-proxy"));
        if (preAuthenticated) {
            String username = request.getHeader("sec-username");
            final boolean anonymous = username == null;
            if (anonymous) {
                username = "anonymousUser";
            }
            List<String> roles = extractRoles(request);
            String email = request.getHeader("sec-email");
            String firstName = request.getHeader("sec-firstname");
            String lastName = request.getHeader("sec-lastname");
            String organization = request.getHeader("sec-org");
            String organizationName = request.getHeader("sec-orgname");
            return new GeorchestraUserDetails(username, roles, email, firstName, lastName, organization,
                    organizationName, anonymous);
        }
        return null;
    }

    /**
     * @return {@code true} if the request comes from georchestra's security proxy
     */
    @Override
    protected Boolean getPreAuthenticatedCredentials(HttpServletRequest request) {
        return Boolean.parseBoolean(request.getHeader("sec-proxy"));
    }

    private List<String> extractRoles(HttpServletRequest request) {
        String rolesHeader = request.getHeader("sec-roles");
        if (StringUtils.isEmpty(rolesHeader)) {
            return Collections.emptyList();
        }

        String[] roles = rolesHeader.split(";");
        log.info("roles: {}", roles == null ? null : Arrays.toString(roles));
        return Arrays.stream(roles).filter(StringUtils::hasText).collect(Collectors.toList());
    }

}
