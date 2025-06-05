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
package org.georchestra.config.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import lombok.NonNull;

public class GeorchestraSecurityProxyAuthenticationManager implements AuthenticationManager {

    @Override
    public PreAuthenticatedAuthenticationToken authenticate(Authentication authentication)
            throws AuthenticationException {
        Object principal = authentication.getPrincipal();

        if (principal instanceof GeorchestraUserDetails) {
            GeorchestraUserDetails user = (GeorchestraUserDetails) principal;
            PreAuthenticatedAuthenticationToken auth;
            if (user.isAnonymous()) {
                auth = createAnonymousAuthenticationToken();
            } else {
                Object credentials = null;// i.e. password
                Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
                auth = new PreAuthenticatedAuthenticationToken(principal, credentials, authorities);
            }
            auth.setAuthenticated(true);
            return auth;
        }
        return null;
    }

    private PreAuthenticatedAuthenticationToken createAnonymousAuthenticationToken() {
        GeorchestraUser user = new GeorchestraUser();
        user.setUsername("anonymousUser");
        user.setRoles(Collections.singletonList("ANONYMOUS"));

        final boolean anonymous = true;
        Optional<Organization> org = Optional.empty();
        GeorchestraUserDetails principal = new GeorchestraUserDetails(user, org, anonymous);
        Object credentials = null;// i.e. password
        return new PreAuthenticatedAuthenticationToken(principal, credentials, principal.getAuthorities());
    }
}
