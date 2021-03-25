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

import org.fao.geonet.client.ApiClient;
import org.fao.geonet.client.ApiException;
import org.fao.geonet.client.RecordsApi;
import org.fao.geonet.client.model.InfoReport;
import org.fao.geonet.client.model.SimpleMetadataProcessingReport;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
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
            @NonNull String xmlRecord) {

        ApiClient client = newApiClient(baseUrl, additionalRequestHeaders);
        client.setDebugging(debugRequests());
        // RecordsApi api = client.buildClient(RecordsApi.class);
        RecordsApi api = new RecordsApi(client);

        String metadataType = "METADATA";
        String xml = xmlRecord;
        List<String> url = null;
        String serverFolder = null;
        Boolean recursiveSearch = false;
        Boolean assignToCatalog = false;
        String uuidProcessing = "NOTHING";
        String group = null;
        List<String> category = null;
        Boolean rejectIfInvalid = false;
        String transformWith = null;
        String schema = null;
        String extra = null;

        SimpleMetadataProcessingReport report;
        try {
            report = api.insert(metadataType, xml, url, serverFolder, recursiveSearch, assignToCatalog, uuidProcessing,
                    group, category, rejectIfInvalid, transformWith, schema, extra);
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
