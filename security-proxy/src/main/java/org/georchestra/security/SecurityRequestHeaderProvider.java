/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityRequestHeaderProvider extends HeaderProvider {

    @Override
    protected Collection<Header> getCustomRequestHeaders(HttpSession session, HttpServletRequest originalRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<Header> headers = new ArrayList<Header>();
        if (authentication.getName().equals("anonymousUser"))
             return headers;
        headers.add(new BasicHeader(HeaderNames.SEC_USERNAME, authentication.getName()));
        StringBuilder roles = new StringBuilder();
        for (GrantedAuthority grantedAuthority : authorities) {
            if (roles.length() != 0) roles.append(";");

            roles.append(grantedAuthority.getAuthority());
        }
        headers.add(new BasicHeader(HeaderNames.SEC_ROLES, roles.toString()));

        return headers;
    }
}
