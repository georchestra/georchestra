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
package org.georchestra.datafeeder.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.BackendConfiguration;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.ExternalApiConfiguration;
import org.georchestra.datafeeder.service.geonetwork.GeoNetworkRemoteService;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.functions.ConstantFunction.False;

/**
 * {@link Service @Service} and JUnit {@link Rule @Rule} to aid in integration
 * testing, verifying the availability of the required external services
 *
 */
@Slf4j
public @Service class IntegrationTestSupport extends ExternalResource {

    private @Autowired @Getter DataFeederConfigurationProperties appConfiguration;
    private @Autowired @Getter TestRestTemplate template;
    private @Autowired(required = false) GeoNetworkRemoteService geoNetwork;

    @Override
    protected void before() throws Throwable {
        checkDatabaseAvailability();
        checkGeoServerAvailability();
        checkGeoNetworkAvailability();
    }

    private void checkDatabaseAvailability() throws SQLException {
        log.debug("Checking database availability");
        try (Connection conn = createLocalPostgisConnection()) {
            try (Statement st = conn.createStatement()) {
                st.execute("SELECT 1");
                log.debug("Local database config check success");
            }
        }
    }

    public Connection createLocalPostgisConnection() throws SQLException {
        BackendConfiguration backendConfiguration = appConfiguration.getPublishing().getBackend();
        Map<String, String> localConnectionParameters = backendConfiguration.getLocal();
        return getConnectionFromParams(localConnectionParameters);
    }

    @SuppressWarnings("rawtypes")
    private void checkGeoServerAvailability() throws URISyntaxException {
        log.debug("Checking GeoServer availability");
        ExternalApiConfiguration gsConfig = appConfiguration.getPublishing().getGeoserver();
        URL apiUrl = gsConfig.getApiUrl();
        assertNotNull(apiUrl);
        assertNotNull(gsConfig.getUsername());
        assertNotNull(gsConfig.getPassword());

        String path = Paths.get(apiUrl.getPath()).resolve("about/version.json").toString();
        String uri = apiUrl.toURI().resolve(path).toString();

        ResponseEntity<Map> entity;
        try {
            entity = doGet(uri, Map.class, "sec-username", "datafeeder", "sec-roles", "ROLE_ADMINISTRATOR");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to connect to GeoServer at " + uri + ". " + e.getMessage(), e);
        }
        assertEquals("Unexpected status code, check configured credentials for " + apiUrl, HttpStatus.OK,
                entity.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<Object, Object> body = entity.getBody();
        assertNotNull(body);
        assertNotNull(body.toString(), body.get("about"));
        assertNotNull(body.toString(), ((Map) body.get("about")).get("resource"));
    }

    private void checkGeoNetworkAvailability() throws IOException {
        log.debug("Checking GeoNetwork availability");
        if (geoNetwork != null)
            geoNetwork.checkServiceAvailable();
    }

    private Connection getConnectionFromParams(Map<String, String> gtParams) throws SQLException {
        String host = gtParams.get(PostgisNGDataStoreFactory.HOST.key);
        String port = gtParams.get(PostgisNGDataStoreFactory.PORT.key);
        String db = gtParams.get(PostgisNGDataStoreFactory.DATABASE.key);
        String user = gtParams.get(PostgisNGDataStoreFactory.USER.key);
        String pwd = gtParams.get(PostgisNGDataStoreFactory.PASSWD.key);

        String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, db);
        try {
            return DriverManager.getConnection(url, user, pwd);
        } catch (SQLException e) {
            log.error("Unable to connect to database with {} ({})", url, e.getMessage(), e);
            throw e;
        }
    }

    public <T> ResponseEntity<T> doGet(String uri, Class<T> type, String... requestHeadersKvps) {
        HttpHeaders headers = new HttpHeaders();
        if (null != requestHeadersKvps) {
            for (int i = 0; i < requestHeadersKvps.length; i += 2) {
                headers.add(requestHeadersKvps[i], requestHeadersKvps[i + 1]);
            }
        }
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        return template.exchange(uri, HttpMethod.GET, requestEntity, type);
    }

    public void deleteLocalDatabaseSchema(@NonNull String schema) throws SQLException {
        try (Connection c = createLocalPostgisConnection(); Statement st = c.createStatement()) {
            String sql = String.format("DROP SCHEMA IF EXISTS \"%s\" CASCADE", schema);
            st.executeUpdate(sql);
        }
    }

    public List<String> getDatabaseSchemas() throws SQLException {
        try (Connection c = createLocalPostgisConnection(); Statement st = c.createStatement()) {
            String sql = "select schema_name from information_schema.schemata";
            return new JdbcTemplate(new SingleConnectionDataSource(c, true)).queryForList(sql, String.class);
        }
    }

}
