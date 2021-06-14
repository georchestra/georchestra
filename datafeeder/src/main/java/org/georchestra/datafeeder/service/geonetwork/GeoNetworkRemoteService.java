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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Supplier;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.ExternalApiConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.NonNull;
import lombok.Setter;

public class GeoNetworkRemoteService {

    private @Setter GeoNetworkClient client = new DefaultGeoNetworkClient();
    private ExternalApiConfiguration config;

    public GeoNetworkRemoteService(@NonNull ExternalApiConfiguration gnConfig, @NonNull GeoNetworkClient client) {
        Objects.requireNonNull(gnConfig.getApiUrl());
        this.config = gnConfig;
        this.client = client;
    }

    // http://localhost:28080/geonetwork/srv/api/0.1/records?metadataType=METADATA&recursiveSearch=false&assignToCatalog=false&uuidProcessing=NOTHING&rejectIfInvalid=true&transformWith=_none_
    /**
     * <p>
     * Sample request:
     * 
     * <pre>
     * <code>
    curl -v 'http://localhost:28080/geonetwork/srv/api/0.1/records?metadataType=METADATA&recursiveSearch=false&assignToCatalog=false&uuidProcessing=NOTHING&rejectIfInvalid=false&transformWith=_none_' \
    -X 'PUT' \
    -H 'accept: application/json' \
    -H 'X-XSRF-TOKEN: 38ee29b5-70b1-4431-abf6-7c1aa3c50505' \
    -H 'Cookie: XSRF-TOKEN=38ee29b5-70b1-4431-abf6-7c1aa3c50505' \
    -H 'Content-Type: application/xml' \
    -H 'sec-username: testadmin' \
    -H 'sec-roles: ROLE_USER;ROLE_ADMINISTRATOR' \
    -H 'sec-proxy: true' \
    -H 'sec-org: Datafeeder Test' \
    --data-raw $'<?xml version="1.0"...'
     * </code>
     * </pre>
     * <p>
     * Sample response:
     * 
     * <pre>
     * <code>
     * {
     * "errors": [],
     * "infos": [],
     * "uuid": "d1a766e1-d3a5-4ef4-953a-a3375a488fd9",
     * "metadata": [],
     * "metadataErrors": {},
     * "metadataInfos": {
     * "121": [
     * {
     *     "message": "Metadata imported from XML with UUID '633f2882-2a90-4f98-9739-472a72d31b65'",
     *     "date": "2021-03-11T12:54:35"
     *   }
     * ]
     * },
     * "numberOfNullRecords": 0,
     * "numberOfRecordNotFound": 0,
     * "numberOfRecordsNotEditable": 0,
     * "numberOfRecordsProcessed": 1,
     * "numberOfRecordsWithErrors": 0,
     * "numberOfRecords": 0,
     * "running": false,
     * "startIsoDateTime": "2021-03-11T12:54:34",
     * "endIsoDateTime": "2021-03-11T12:54:35",
     * "ellapsedTimeInSeconds": 1,
     * "totalTimeInSeconds": 1,
     * "type": "SimpleMetadataProcessingReport"
     * }
     * </code>
     * </pre>
     * 
     * Response headers:
     * 
     * <pre>
     *  
     * <code>
     * content-security-policy: frame-ancestors 'none'
     * content-type: application/json
     * expires: Thu, 01 Jan 1970 00:00:00 GMT
     * transfer-encoding: chunked
     * x-frame-options: DENY
     * </code>
     * </pre>
     * 
     * @return
     */
    public GeoNetworkResponse publish(@NonNull String metadataId, @NonNull Supplier<String> xmlRecordAsString,
            String group) {
        final String xmlRecord = xmlRecordAsString.get();

        GeoNetworkResponse response = client.putXmlRecord(metadataId, xmlRecord, group);

        HttpStatus statusCode = response.getStatus();
        String statusText = response.getStatusText();

        if (statusCode.is2xxSuccessful()) {
            return response;
        }
        if (statusCode.is4xxClientError()) {
            throw new IllegalArgumentException(
                    "Error creating metadata record: " + statusText + " (" + response.getErrorResponseBody() + ")");
        }
        throw new RuntimeException(
                "Error creating metadata record: " + statusText + " (" + response.getErrorResponseBody() + ")");
    }

    public void checkServiceAvailable() throws IOException {
        client.checkServiceAvailable();
    }

    public String getRecordById(@NonNull String metadataId) {
        return client.getXmlRecord(metadataId);
    }

    public URI buildMetadataRecordIdentifier(@NonNull String recordId) {
        UriComponentsBuilder builder;
        try {
            URL publicURL = this.config.getPublicUrl();
            builder = UriComponentsBuilder.fromUri(publicURL.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        String queryString = "uuid=" + recordId;
        builder.query(queryString);
        return builder.build().toUri();
    }

    // e.g.
    // http://localhost:28080/geonetwork/srv/api/records/2bd68e79-7fb1-443b-8709-3570b19a3d6f/formatters/xml
    public URI buildMetadataRecordXmlURI(@NonNull String recordId) {
        URL publicURL = this.config.getPublicUrl();
        String xmlMdRecord = String.format("%s/srv/api/records/%s/formatters/xml", publicURL, recordId);
        URI uri = URI.create(xmlMdRecord).normalize();
        return uri;
    }

    // e.g.
    // https://localhost:28080/geonetwork/srv/fre/catalog.search#/metadata/FR-200052264-GGE_ERP_SCOLAIRES
    public URI buildMetadataRecordHtmlURI(@NonNull String recordId) {
        URL publicURL = this.config.getPublicUrl();
        String htmlURI = String.format("%s/srv/eng/catalog.search#/metadata/%s", publicURL, recordId);
        URI uri = URI.create(htmlURI).normalize();
        return uri;
    }
}
