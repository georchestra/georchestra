/*
 * Copyright (C) 2020, 2021 by the geOrchestra PSC
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
package org.georchestra.datafeeder.service.geonetwork;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.ws.rs.client.Client;

import org.fao.geonet.client.ApiClient;
import org.fao.geonet.client.ApiException;
import org.fao.geonet.client.GroupsApi;
import org.fao.geonet.client.MeApi;
import org.fao.geonet.client.RecordsApi;
import org.fao.geonet.client.UsersApi;
import org.fao.geonet.client.model.Group;
import org.fao.geonet.client.model.InfoReport;
import org.fao.geonet.client.model.MeResponse;
import org.fao.geonet.client.model.SimpleMetadataProcessingReport;
import org.fao.geonet.client.model.User;
import org.georchestra.datafeeder.model.UserInfo;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultGeoNetworkClient implements GeoNetworkClient {

    private @Setter URL apiUrl;
    private String username;
    private String password;
    private Map<String, String> authHeaders;
    private @Setter boolean debugRequests;

    public DefaultGeoNetworkClient() {
    }

    public DefaultGeoNetworkClient(@NonNull URL apiUrl) {
        this.apiUrl = apiUrl;
    }

    public @Override void setBasicAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public @Override void setHeadersAuth(Map<String, String> authHeaders) {
        this.authHeaders = authHeaders;
    }

    @Override
    public void checkServiceAvailable() throws IOException {
        ApiClient client = newApiClient();
        MeApi meApi = new MeApi(client);
        MeResponse me;
        try {
            me = meApi.getMe();
            if (me == null) {
                throw new IOException(
                        "Unable to get calling user information from geonetwork at " + client.getBasePath());
            }
            String id = me.getId();
            String username = me.getUsername();
            String organisation = me.getOrganisation();
            log.info("GeoNetwork availability checked at {}, received user id:{}, username:{}, org:{}",
                    client.getBasePath(), id, username, organisation);
        } catch (ApiException e) {
            log.warn("Error checking geonetwork availability", e);
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Inserts a metadata into GeoNetwork.
     *
     * @param metadataId   the metadata UUID to insert
     * @param xmlRecord    the raw metadata, a String representing the XML.
     * @param groupName    the name of the group the metadata should be attached to.
     * @param user         the UserInfo object describing the user having uploaded
     *                     the dataset.
     * @param publishToAll whether the metadata should be published right after
     *                     having been added to the catalogue or not.
     * @return the GeoNetworkResponse resulting object
     */
    @Override
    public GeoNetworkResponse putXmlRecord(@NonNull String metadataId, @NonNull String xmlRecord, String groupName,
            UserInfo user, Boolean publishToAll) {

        ApiClient client = newApiClient();
        // RecordsApi api = client.buildClient(RecordsApi.class);
        RecordsApi api = new RecordsApi(client);

        final String metadataType = "METADATA";
        final String xml = xmlRecord;
        final List<String> url = null;
        final String serverFolder = null;
        final Boolean recursiveSearch = false;
        final Boolean assignToCatalog = false;
        final String uuidProcessing = "NOTHING";
        final List<String> category = null;
        final Boolean rejectIfInvalid = false;
        final String transformWith = null;
        final String schema = null;
        final String extra = null;

        SimpleMetadataProcessingReport report;

        Optional<Integer> groupId = findGroupId(client, groupName);
        final String group = groupId.map(i -> i.toString()).orElse(null);
        try {
            log.info("Inserting record {} to GeoNetwork, under group {}({})", metadataId, groupName, group);
            report = api.insert(metadataType, xml, url, serverFolder, recursiveSearch, assignToCatalog, uuidProcessing,
                    group, category, rejectIfInvalid, transformWith, schema, extra, publishToAll);

            log.info("Publishing record {} to GeoNetwork", metadataId);
            {
                // Workaround IllegalStateException: Entity must not be null for http method PUT
                Client httpClient = api.getApiClient().getHttpClient();
                httpClient.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
            }
            // need to call publish, since publishToAll doesn't work/exist in GN 3.8.x?
            if (publishToAll) {
                api.publish(metadataId);
            }
            log.info("Published record {} to GeoNetwork", metadataId);

        } catch (ApiException e) {
            log.error("Error inserting metadata record", e);
            GeoNetworkResponse r = new GeoNetworkResponse();
            r.setStatus(HttpStatus.valueOf(e.getCode()));
            r.setStatusText(e.getMessage());
            r.setErrorResponseBody(e.getResponseBody());

            Map<String, List<String>> responseHeaders = e.getResponseHeaders();
            HttpHeaders headers = new HttpHeaders();
            responseHeaders.forEach(headers::addAll);
            r.setHeaders(headers);
            return r;
        }

        try {
            UsersApi usersApi = new UsersApi(client);
            // TODO Isn't there a more efficient way to look up a user using the API ?
            Optional<User> impersonatedUser = usersApi.getUsers().stream()
                    .filter(usr -> usr.getUsername().equals(user.getUsername())).findFirst();
            if ((!impersonatedUser.isPresent()) || (!groupId.isPresent())) {
                log.warn("Unable to find user {} and/or group {} in GeoNetwork, skipping record impersonation",
                        user.getUsername(), groupName);
            } else {
                api.setRecordOwnership(metadataId, groupId.get(), impersonatedUser.get().getId(), true);
            }
        } catch (ApiException e) {
            log.error("Unable to give ownership on record {} to user {}", metadataId, user, e);
        }

        GeoNetworkResponse r = new GeoNetworkResponse();
        r.setStatus(HttpStatus.CREATED);
        Map<String, List<InfoReport>> metadataInfos = report.getMetadataInfos();
        log.info("Created metadata record {}", metadataInfos);
        return r;
    }

    private Optional<Integer> findGroupId(ApiClient client, String groupName) {
        if (groupName == null) {
            return Optional.empty();
        }
        GroupsApi groupsApi = new GroupsApi(client);
        Boolean withReservedGroup = null;
        String profile = null;
        List<Group> groups = groupsApi.getGroups(withReservedGroup, profile);
        return groups.stream().filter(g -> groupName.equals(g.getName())).map(Group::getId).findFirst();
    }

    @Override
    public String getXmlRecord(@NonNull String recordId) {

        ApiClient client = newApiClient();

        RecordsApi api = new RecordsApi(client);
        String record = api.getRecord(recordId, "application/xml");
        return record;
    }

    ApiClient newApiClient() {
        Objects.requireNonNull(this.apiUrl, () -> getClass().getSimpleName() + ": API URL is not set");
        final String baseUrl = this.apiUrl.toExternalForm();
        ApiClient client = new ApiClient();
        client.setBasePath(baseUrl);
        client.setDebugging(this.debugRequests);

        if (this.username != null) {
            client.setUsername(this.username);
            client.setPassword(this.password);
        } else if (this.authHeaders != null) {
            this.authHeaders.forEach(client::addDefaultHeader);
        }
        return client;
    }
}
