/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

package org.georchestra.geowebcache.security;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * An authentication that is obtained by reading the credentials from the
 * headers.
 *
 * @see org.georchestra.geowebcache.security.PreAuthFilter
 * @author Jesse on 4/24/2014.
 */
public class PreAuthToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = -5711092634457489286L;

    private final String principal;

    public PreAuthToken(String username, Set<String> roles) {
        super(createGrantedAuthorities(roles));
        this.principal = username;

        setAuthenticated(true);
        UserDetails details = new User(username, "", true, true, true, true, super.getAuthorities());
        setDetails(details);
    }

    private static List<GrantedAuthority> createGrantedAuthorities(Set<String> roles) {
        return roles == null ? Collections.emptyList()
                : roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
