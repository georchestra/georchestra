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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Supplier;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.NonNull;
import lombok.Setter;

public class GeoNetworkRemoteService {

    private URL apiBaseURL;
    private @NonNull URL publicURL;
    private @Setter GeoNetworkClient client = new DefaultGeoNetworkClient();

    public GeoNetworkRemoteService(@NonNull URL geonetworkApiURL, @NonNull URL geonetworkPublicURL) {
        this.publicURL = geonetworkPublicURL;
        this.apiBaseURL = geonetworkApiURL;

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
     * @param metadataId
     * 
     * @param xmlRecordAsString
     * @return
     */
    public GeoNetworkResponse publish(@NonNull Supplier<String> xmlRecordAsString) {
//
//        final String url = URI.create(apiURL.toExternalForm() + "/records").normalize().toString()
//                + "?metadataType=METADATA&recursiveSearch=false&assignToCatalog=false&uuidProcessing=NOTHING&rejectIfInvalid=false&transformWith=_none_";
        final String url = apiBaseURL.toString();
        final String xmlRecord = xmlRecordAsString.get();

//		  -H 'accept: application/json' \
//		  -H 'Content-Type: application/xml' \
//		  -H 'X-XSRF-TOKEN: c9f33266-e242-4198-a18c-b01290dce5f1' \
//		  -H 'Cookie: XSRF-TOKEN=c9f33266-e242-4198-a18c-b01290dce5f1' \
//		  -H 'sec-username: testadmin' \
//		  -H 'sec-roles: ROLE_USER;ROLE_ADMINISTRATOR' \
//		  -H 'sec-proxy: true' \
//		  -H 'sec-org: Datafeeder Test' \

//        > Host: localhost:28080
//        > User-Agent: curl/7.68.0
//        > Accept-Encoding: deflate, gzip, br
//        > accept: application/json
//        > X-XSRF-TOKEN: c9f33266-e242-4198-a18c-b01290dce5f1
//        > Cookie: XSRF-TOKEN=c9f33266-e242-4198-a18c-b01290dce5f1
//        > Content-Type: application/xml
//        > sec-username: testadmin
//        > sec-roles: ROLE_USER;ROLE_ADMINISTRATOR
//        > sec-proxy: true
//        > sec-org: Datafeeder Test
//        > Content-Length: 47097
//        > Expect: 100-continue

        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.set("Host", "localhost:28080");
        reqHeaders.set("Accept", "application/json");
        reqHeaders.set("Content-Type", "application/xml");
        reqHeaders.set("User-Agent", "curl/7.68.0");

        reqHeaders.set("sec-proxy", "true");
        reqHeaders.set("sec-username", "testadmin");
        reqHeaders.set("sec-roles", "ROLE_USER;ROLE_ADMINISTRATOR");
        reqHeaders.set("sec-org", "Datafeeder Test");
        // This is odd, apparently any UUID works as XSRF token, and these two need to
        // be set
        reqHeaders.set("X-XSRF-TOKEN", "c9f33266-e242-4198-a18c-b01290dce5f1");
        reqHeaders.set("Cookie", "XSRF-TOKEN=c9f33266-e242-4198-a18c-b01290dce5f1");

        GeoNetworkResponse response = client.putXmlRecord(url, reqHeaders, xmlRecord);

        HttpStatus statusCode = response.getStatus();
        String statusText = response.getStatusText();
//		HttpHeaders headers = response.getHeaders();

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

    public URI buildMetadataRecordURI(@NonNull String recordId) {
        UriComponentsBuilder builder;
        try {
            builder = UriComponentsBuilder.fromUri(publicURL.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        String queryString = "uuid=" + recordId;
        builder.query(queryString);
        return builder.build().toUri();
    }
}
