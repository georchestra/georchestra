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
import java.util.Optional;

import org.georchestra.security.api.OrganizationsApi;
import org.georchestra.security.model.Organization;

import lombok.NonNull;

/**
 * {@link OrganizationsApi} implementation as client for geOrchestra's console
 * application
 * {@code org.georchestra.console.ws.security.api.SecurityApiController}
 */
public class GeorchestraConsoleOrganizationsApiImpl implements OrganizationsApi {

    private RestClient client;

    public GeorchestraConsoleOrganizationsApiImpl(@NonNull RestClient consoleClient) {
        client = consoleClient;
    }

    @Override
    public List<Organization> findAll() {
        return client.getAll("/console/internal/organizations", Organization[].class);
    }

    @Override
    public Optional<Organization> findById(String id) {
        return client.get("/console/internal/organizations/id/{id}", Organization.class, id);
    }

    @Override
    public Optional<Organization> findByShortName(String shortName) {
        return client.get("/console/internal/organizations/shortname/{name}", Organization.class, shortName);
    }

    @Override
    public Optional<Organization> findByOrgUniqueId(String orgUniqueId) {
        return client.get("/console/internal/organizations/unique/{id}", Organization.class, orgUniqueId);
    }

    @Override
    public Optional<byte[]> getLogo(String id) {
        return client.get("/console/internal/organizations/id/{id}/logo", byte[].class, id);
    }
}
