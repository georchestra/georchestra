/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.security.api;

import org.georchestra.security.model.GeorchestraUser;

import java.util.List;
import java.util.Optional;

public interface UsersApi {

    /**
     * Find a user by {@link GeorchestraUser#getId() unique identifier}
     */
    Optional<GeorchestraUser> findById(String id);

    /**
     * Find a user by {@link GeorchestraUser#getUsername() login name}
     */
    Optional<GeorchestraUser> findByUsername(String username);

    default Optional<GeorchestraUser> findByOAuth2Uid(String oauth2Provider, String oauth2Uid) {
        return Optional.empty();
    };

    List<GeorchestraUser> findAll();
}
