/*
 * Copyright (C) 2020 by the geOrchestra PSC
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
package org.georchestra.datafeeder.service.publish.impl;

import java.util.HashMap;
import java.util.Map;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.ExternalApiConfiguration;
import org.georchestra.datafeeder.service.publish.DataBackendService;
import org.georchestra.datafeeder.service.publish.OWSPublicationService;
import org.geoserver.restconfig.client.GeoServerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration providing strategy beans to publish uploaded datasets to
 * Georchetra's GeoServer and Geonetwork service instances, using a PostGIS
 * database as data back-end.
 * 
 * @see DataFeederConfigurationProperties
 */
@Configuration
@Profile("!mock")
public class GeorchestraPublishingServicesConfiguration {

    public @Bean DataBackendService dataBackendService() {
        return new GeorchestraDataBackendService();
    }

    public @Bean OWSPublicationService owsPublicationService() {
        return new GeorchestraOwsPublicationService();
    }

    public @Bean GeoServerClient geoServerApiClient(DataFeederConfigurationProperties props) {
        ExternalApiConfiguration config = props.getPublishing().getGeoserver();
        String restApiEntryPoint = config.getApiUrl().toExternalForm();

        GeoServerClient client = new GeoServerClient(restApiEntryPoint);

        Map<String, String> authHeaders = new HashMap<>();
        // authHeaders.put("sec-proxy", "true");
        authHeaders.put("sec-username", "datafeeder-application");
        authHeaders.put("sec-roles", "ROLE_ADMINISTRATOR");

        client.setRequestHeaderAuth("georchestra", authHeaders);
        return client;
    }

    public @Bean GeoServerRemoteService geoServerRemoteService() {
        return new GeoServerRemoteService();
    }
}
