/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.ImmutableMap;

/**
 * Generates {@code sec-username} and {@code sec-roles} headers based on the
 * {@link Authentication authenticated user}
 */
public class SecurityRequestHeaderProvider extends HeaderProvider {

    protected static final Log logger = LogFactory
            .getLog(LdapUserDetailsRequestHeaderProvider.class.getPackage().getName());

    @PostConstruct
    public void init() {
        logger.info(String.format("Will contribute standard header %s", SEC_USERNAME));
        logger.info(String.format("Will contribute standard header %s", SEC_ROLES));
    }

    @Override
    public Map<String, String> getCustomRequestHeaders(HttpServletRequest originalRequest, String targetServiceName) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String authName = authentication.getName();
        if (authName.equals("anonymousUser"))
            return Collections.emptyMap();

        String roles = buildRolesList(authentication);
        return ImmutableMap.of(//
                SEC_USERNAME, authName, //
                SEC_ROLES, roles);
    }

    private String buildRolesList(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(";"));
        return roles;
    }
}
