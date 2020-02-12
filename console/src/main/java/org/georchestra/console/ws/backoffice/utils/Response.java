package org.georchestra.console.ws.backoffice.utils;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

public @Data class Response {

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object response;
}
