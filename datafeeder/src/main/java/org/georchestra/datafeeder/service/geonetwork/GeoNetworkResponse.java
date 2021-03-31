package org.georchestra.datafeeder.service.geonetwork;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import lombok.Data;

public @Data class GeoNetworkResponse {

    HttpStatus status;
    String statusText;
    HttpHeaders headers;
    String errorResponseBody;
}
