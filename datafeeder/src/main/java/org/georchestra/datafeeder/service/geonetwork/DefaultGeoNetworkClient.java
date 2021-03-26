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

import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;

import org.fao.geonet.client.ApiClient;
import org.fao.geonet.client.ApiException;
import org.fao.geonet.client.RecordsApi;
import org.fao.geonet.client.model.InfoReport;
import org.fao.geonet.client.model.SimpleMetadataProcessingReport;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultGeoNetworkClient implements GeoNetworkClient {

    private @Autowired(required = false) DataFeederConfigurationProperties config;

    /**
     * @param baseUrl e.g. {@code http://localhost:8080/geonetwork}
     */
    @Override
    public GeoNetworkResponse putXmlRecord(@NonNull String baseUrl, @NonNull HttpHeaders additionalRequestHeaders,
            @NonNull String metadataId, @NonNull String xmlRecord) {

        ApiClient client = newApiClient(baseUrl, additionalRequestHeaders);
        client.setDebugging(debugRequests());
        // RecordsApi api = client.buildClient(RecordsApi.class);
        RecordsApi api = new RecordsApi(client);

        final String metadataType = "METADATA";
        final String xml = xmlRecord;
        final List<String> url = null;
        final String serverFolder = null;
        final Boolean recursiveSearch = false;
        final Boolean assignToCatalog = false;
        final String uuidProcessing = "NOTHING";
        final String group = null;
        final List<String> category = null;
        final Boolean rejectIfInvalid = false;
        final String transformWith = null;
        final String schema = null;
        final String extra = null;
        // This param makes the record public, but it seems it doesn't work in GN 3.8.x
        final Boolean publishToAll = true;

        SimpleMetadataProcessingReport report;
        try {
            log.info("Inserting record {} to GeoNetwork", metadataId);
            report = api.insert(metadataType, xml, url, serverFolder, recursiveSearch, assignToCatalog, uuidProcessing,
                    group, category, rejectIfInvalid, transformWith, schema, extra, publishToAll);

            log.info("Publishing record {} to GeoNetwork", metadataId);
            {
                // Workaround IllegalStateException: Entity must not be null for http method PUT
                Client httpClient = api.getApiClient().getHttpClient();
                httpClient.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
            }
            // need to call publish, since publishToAll doesn't work/exist in GN 3.8.x?
            api.publish(metadataId);

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

        GeoNetworkResponse r = new GeoNetworkResponse();
        r.setStatus(HttpStatus.CREATED);
        Map<String, List<InfoReport>> metadataInfos = report.getMetadataInfos();
        log.info("Created metadata record {}", metadataInfos);
        return r;
    }

    @Override
    public String getXmlRecord(@NonNull String baseUrl, @NonNull HttpHeaders additionalRequestHeaders,
            @NonNull String recordId) {

        ApiClient client = newApiClient(baseUrl, additionalRequestHeaders);
        client.setDebugging(debugRequests());

        RecordsApi api = new RecordsApi(client);
        String record = api.getRecord(recordId, "application/xml");
        return record;
    }

    private boolean debugRequests() {
        return this.config != null && this.config.getPublishing().getGeonetwork().isLogRequests();
    }

    private ApiClient newApiClient(@NonNull String baseUrl, @NonNull HttpHeaders authHeaders) {
        ApiClient client = new ApiClient();
//        Builder feignBuilder = client.getFeignBuilder();
//        // use okhttp client, the default one doesn't send request headers correctly
//        feignBuilder.client(new OkHttpClient());
//
//        // replace the Encoder, which would encode the xml literal as a JSON string
//        Encoder encoder = new Encoder() {
//            public @Override void encode(Object object, Type bodyType, RequestTemplate template)
//                    throws EncodeException {
//                if (String.class.equals(bodyType)) {
//                    byte[] body = ((String) object).getBytes(StandardCharsets.UTF_8);
//                    template.body(body, StandardCharsets.UTF_8);
//                    return;
//                }
//                throw new UnsupportedOperationException();
//            }
//        };
//        feignBuilder.encoder(encoder);

        client.setBasePath(baseUrl);
//        client.setRequestHeaderAuth("georchestra", authHeaders);
        for (String name : authHeaders.keySet()) {
            String value = authHeaders.getFirst(name);
            client.addDefaultHeader(name, value);
        }
        return client;
    }
}
