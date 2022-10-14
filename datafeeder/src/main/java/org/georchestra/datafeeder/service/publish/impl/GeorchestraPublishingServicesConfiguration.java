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

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.Auth;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.Auth.AuthType;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.BasicAuth;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.ExternalApiConfiguration;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.GeonetworkPublishingConfiguration;
import org.georchestra.datafeeder.service.geonetwork.DefaultGeoNetworkClient;
import org.georchestra.datafeeder.service.geonetwork.GeoNetworkClient;
import org.georchestra.datafeeder.service.geonetwork.GeoNetworkRemoteService;
import org.georchestra.datafeeder.service.geoserver.GeoServerRemoteService;
import org.georchestra.datafeeder.service.publish.DataBackendService;
import org.georchestra.datafeeder.service.publish.MetadataPublicationService;
import org.georchestra.datafeeder.service.publish.OWSPublicationService;
import org.geoserver.restconfig.client.GeoServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration providing strategy beans to publish uploaded datasets to
 * Georchetra's GeoServer and Geonetwork service instances, using a PostGIS
 * database as data back-end.
 *
 * @see DataFeederConfigurationProperties
 */
@Configuration
@Profile("!mock")
@Slf4j(topic = "org.georchestra.datafeeder.service.publish")
public class GeorchestraPublishingServicesConfiguration {

    private @Autowired DataFeederConfigurationProperties config;

    public @Bean DataBackendService dataBackendService() {
        return new GeorchestraDataBackendService();
    }

    public @Bean OWSPublicationService owsPublicationService() {
        return new GeorchestraOwsPublicationService();
    }

    public @Bean MetadataPublicationService metadataPublicationService() {
        return new GeorchestraMetadataPublicationService(geoNetworkRemoteService(), templateMapper(),
                config.getPublishing());
    }

    public @Bean GeoNetworkRemoteService geoNetworkRemoteService() {
        ExternalApiConfiguration gnConfig = config.getPublishing().getGeonetwork();
        return new GeoNetworkRemoteService(gnConfig, geoNetworkClient());
    }

    public @Bean GeoNetworkClient geoNetworkClient() {
        GeonetworkPublishingConfiguration config = this.config.getPublishing().getGeonetwork();
        URL apiUrl = config.getApiUrl();

        DefaultGeoNetworkClient client = new DefaultGeoNetworkClient();
        client.setApiUrl(apiUrl);
        client.setDebugRequests(config.isLogRequests());
        log.info("Configuring authentication for GeoNetwork REST API at {}", apiUrl);
        setAuth("GeoNetwork", //
                config.getAuth(), //
                client::setHeadersAuth, //
                client::setBasicAuth);
        return client;
    }

    public @Bean TemplateMapper templateMapper() {
        return new GeorchestraTemplateMapper();
    }

    public @Bean GeoServerClient geoServerApiClient(DataFeederConfigurationProperties props) {
        final ExternalApiConfiguration config = props.getPublishing().getGeoserver();
        final String restApiEntryPoint = config.getApiUrl().toExternalForm();

        GeoServerClient client = new GeoServerClient(restApiEntryPoint);
        client.setDebugRequests(config.isLogRequests());
        log.info("Configuring authentication for GeoServer REST API at {}", restApiEntryPoint);
        setAuth("GeoServer", //
                config.getAuth(), //
                headers -> client.setRequestHeaderAuth("georchestra", headers), //
                client::setBasicAuth);

        return client;
    }

    private void setAuth(String serviceName, Auth auth, Consumer<Map<String, String>> headersSetter,
            BiConsumer<String, String> basicSetter) {
        AuthType type = auth.getType();
        if (type == AuthType.headers) {
            // Allow passing restricted headers to the downstream service
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

            Map<String, String> headers = auth.getHeaders() == null ? Collections.emptyMap() : auth.getHeaders();
            headersSetter.accept(headers);
            if (headers.isEmpty()) {
                log.warn("{} REST API uses HTTP headers authentication but no headers were provided", serviceName);
            } else {
                log.info("{} REST API uses HTTP headers based authentication. Header names: {}", serviceName,
                        headers.keySet());
            }
        } else if (type == AuthType.basic) {
            BasicAuth basic = auth.getBasic();
            Objects.requireNonNull(basic == null ? null : basic.getUsername(), () -> String.format(
                    "%s REST API client configured to use HTTP Basic authentication but no credentials provided",
                    serviceName));

            String username = basic.getUsername();
            String password = basic.getPassword();
            basicSetter.accept(username, password);
            if (StringUtils.hasText(password)) {
                log.info("{} REST API client uses HTTP-Basic authentication. User name: {}", serviceName, username);
            } else {
                log.warn("{} REST API client uses HTTP-Basic authentication but no password is provided. User name: {}",
                        serviceName, username);
            }
        } else if (type == AuthType.none || type == null) {
            log.info("No authentication configuration provided to use {}'s REST API", serviceName);
        } else {
            throw new IllegalArgumentException("Uknown AuthType: " + type);
        }
    }

    public @Bean GeoServerRemoteService geoServerRemoteService() {
        return new GeoServerRemoteService();
    }

}
