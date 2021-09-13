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

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.georchestra.security.api.OrganizationsApi;
import org.georchestra.security.model.Organization;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link OrganizationsApi} implementation as client for geOrchestra's console
 * application
 * {@code org.georchestra.console.ws.security.api.SecurityApiController}
 */
@Slf4j(topic = "org.georchestra.security.client.console")
public class GeorchestraConsoleOrganizationsApiImpl implements OrganizationsApi {

    private RestClient client;

    public GeorchestraConsoleOrganizationsApiImpl(URI consoleURL) {
        log.info("Will retrieve georchestra organizations from " + consoleURL);
        this.client = new RestClient(consoleURL);
    }

    @Override
    public Stream<Organization> findAll() {
        Organization[] orgs = client.get("/internal/organizations", Organization[].class);
        return orgs == null ? Stream.empty() : Arrays.stream(orgs);
    }

    @Override
    public Optional<Organization> findById(String id) {
        Organization organization = client.get("/internal/organizations/id/{id}", Organization.class, id);
        return Optional.ofNullable(organization);
    }

    @Override
    public Optional<Organization> findByShortName(String shortName) {
        Organization organization = client.get("/internal/organizations/name/{name}", Organization.class, shortName);
        return Optional.ofNullable(organization);
    }

}
