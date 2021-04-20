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

import static org.georchestra.commons.security.SecurityHeaders.SEC_EMAIL;
import static org.georchestra.commons.security.SecurityHeaders.SEC_FIRSTNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_LASTNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ORG;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ORGNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_PROXY;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.georchestra.commons.security.SecurityHeaders;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;

import com.google.common.base.Splitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "org.georchestra.config.security")
public class GeorchestraSecurityProxyAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    @Override
    protected GeorchestraUserDetails getPreAuthenticatedPrincipal(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("security-proxy headers: {}",
                    Collections.list(request.getHeaderNames()).stream().filter(n -> n.startsWith("sec-"))
                            .map(n -> String.format("%s: '%s'", n, request.getHeader(n)))
                            .collect(Collectors.joining(",")));
        }
        final boolean preAuthenticated = getPreAuthenticatedCredentials(request);
        if (preAuthenticated) {
            String username = SecurityHeaders.decode(request.getHeader(SEC_USERNAME));
            final boolean anonymous = username == null;
            if (anonymous) {
                username = "anonymousUser";
            }
            List<String> roles = extractRoles(request);
            String email = SecurityHeaders.decode(request.getHeader(SEC_EMAIL));
            String firstName = SecurityHeaders.decode(request.getHeader(SEC_FIRSTNAME));
            String lastName = SecurityHeaders.decode(request.getHeader(SEC_LASTNAME));
            String organization = SecurityHeaders.decode(request.getHeader(SEC_ORG));
            String organizationName = SecurityHeaders.decode(request.getHeader(SEC_ORGNAME));
            GeorchestraUserDetails preAuthPrincipal = new GeorchestraUserDetails(username, roles, email, firstName,
                    lastName, organization, organizationName, anonymous);
            log.debug("principal: {}", preAuthPrincipal);
            return preAuthPrincipal;
        }
        return null;
    }

    /**
     * @return {@code true} if the request comes from georchestra's security proxy
     */
    @Override
    protected Boolean getPreAuthenticatedCredentials(HttpServletRequest request) {
        return Boolean.parseBoolean(SecurityHeaders.decode(request.getHeader(SEC_PROXY)));
    }

    private List<String> extractRoles(HttpServletRequest request) {
        String rolesHeader = SecurityHeaders.decode(request.getHeader(SEC_ROLES));
        if (StringUtils.isEmpty(rolesHeader)) {
            return Collections.emptyList();
        }
        return Splitter.on(';').omitEmptyStrings().trimResults().splitToList(rolesHeader);
    }

}
