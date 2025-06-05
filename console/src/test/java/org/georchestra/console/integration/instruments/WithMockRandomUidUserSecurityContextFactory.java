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

package org.georchestra.console.integration.instruments;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockRandomUidUserSecurityContextFactory implements WithSecurityContextFactory<WithMockRandomUidUser> {

    WithMockRandomUidUserSecurityContextFactory() {
    }

    @Override
    public SecurityContext createSecurityContext(WithMockRandomUidUser customUser) {
        String userAdminName = ("IT_USER_" + RandomStringUtils.randomAlphabetic(8)).toLowerCase();

        Set<SimpleGrantedAuthority> grantedAuthorities = Collections
                .singleton(new SimpleGrantedAuthority("ROLE_SUPERUSER"));
        User principal = new User(userAdminName, "password", true, true, true, true, grantedAuthorities);

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(),
                principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
