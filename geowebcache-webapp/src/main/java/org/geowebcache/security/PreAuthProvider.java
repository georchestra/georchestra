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

package org.geowebcache.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * A provider that accepts {@link org.geowebcache.security.PreAuthToken} authentication objects.
 *
 * @author Jesse on 4/24/2014.
 */
public class PreAuthProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        if (authentication instanceof PreAuthToken) {
            PreAuthToken authToken = (PreAuthToken) authentication;
            return authToken;
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthToken.class.isAssignableFrom(authentication);
    }
}
