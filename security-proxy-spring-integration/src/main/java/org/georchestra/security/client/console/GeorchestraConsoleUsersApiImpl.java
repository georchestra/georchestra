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
package org.georchestra.security.client.console;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;

import lombok.NonNull;

/**
 * {@link UsersApi} implementation as client for geOrchestra's console
 * application
 * {@code org.georchestra.console.ws.security.api.SecurityApiController}
 */
public class GeorchestraConsoleUsersApiImpl implements UsersApi {

    private RestClient client;

    public GeorchestraConsoleUsersApiImpl(@NonNull RestClient consoleClient) {
        this.client = consoleClient;
    }

    @Override
    public List<GeorchestraUser> findAll() {
        return client.getAll("/console/internal/users", GeorchestraUser.class);
    }

    @Override
    public Optional<GeorchestraUser> findById(String id) {
        Objects.requireNonNull(id);
        return client.get("/console/internal/users/id/{id}", GeorchestraUser.class, id);
    }

    @Override
    public Optional<GeorchestraUser> findByUsername(String username) {
        Objects.requireNonNull(username);
        return client.get("/console/internal/users/username/{name}", GeorchestraUser.class, username);
    }

    @Override
    public Optional<GeorchestraUser> findByOAuth2Uid(String oauth2Provider, String oauth2Uid) {
        return Optional.empty();
    }

}
