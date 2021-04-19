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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Generates {@code sec-username} and {@code sec-roles} headers based on the
 * {@link Authentication authenticated user}
 */
public class SecurityRequestHeaderProvider extends HeaderProvider {

    @Override
    public Collection<Header> getCustomRequestHeaders(HttpSession session, HttpServletRequest originalRequest,
            String targetServiceName) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String authName = authentication.getName();
        if (authName.equals("anonymousUser"))
            return Collections.emptyList();

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(SEC_USERNAME, authName));
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(";"));
        headers.add(new BasicHeader(SEC_ROLES, roles.toString()));

        return headers;
    }
}
