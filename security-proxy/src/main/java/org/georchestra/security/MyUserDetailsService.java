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

package org.georchestra.security;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class MyUserDetailsService implements UserDetailsService {

    private static final Log logger = LogFactory.getLog(MyUserDetailsService.class.getPackage().getName());

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        logger.debug("Log user : " + username);

        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        authorities.add(this.createGrantedAuthority("ROLE_USER"));
        authorities.add(this.createGrantedAuthority("ROLE_ADMINISTRATOR"));

        UserDetails res = new User(username, "N/A", authorities);
        return res;
    }

    private GrantedAuthority createGrantedAuthority(final String role) {
        return new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return role;
            }
        };
    }
}
