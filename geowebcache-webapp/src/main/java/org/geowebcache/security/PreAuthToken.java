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

package org.geowebcache.security;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;

import java.util.Set;

/**
 * An authentication that is obtained by reading the credentials from the headers.
 *
 * @see org.geowebcache.security.PreAuthFilter
 *
 * @author Jesse on 4/24/2014.
 */
public class PreAuthToken extends AbstractAuthenticationToken{

    private final String principal;

    public PreAuthToken(String username, Set<String> roles) {
        super(createGrantedAuthorities(roles));
        this.principal = username;

        setAuthenticated(true);
        UserDetails details = new User(username, "", true, true, true, true, super.getAuthorities());
        setDetails(details);

    }

    private static GrantedAuthority[] createGrantedAuthorities(Set<String> roles) {
        GrantedAuthority[] authorities = new GrantedAuthority[roles.size()];
        int i = 0;
        for (String role : roles) {
            authorities[i] = new GrantedAuthorityImpl(role);
            i++;
        }
        return authorities;
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
