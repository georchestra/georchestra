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

import org.georchestra.security.api.RolesApi;
import org.georchestra.security.model.Role;

import lombok.NonNull;

/**
 * {@link RolesApi} implementation as client for geOrchestra's console
 * application
 * {@code org.georchestra.console.ws.security.api.SecurityApiController}
 */
public class GeorchestraConsoleRolesApiImpl implements RolesApi {

    private RestClient client;

    public GeorchestraConsoleRolesApiImpl(@NonNull RestClient consoleClient) {
        this.client = consoleClient;
    }

    @Override
    public List<Role> findAll() {
        return client.getAll("/console/internal/roles", Role[].class);
    }

    @Override
    public Optional<Role> findByName(String name) {
        Objects.requireNonNull(name);
        return client.get("/console/internal/roles/name/{name}", Role.class, name);
    }

}