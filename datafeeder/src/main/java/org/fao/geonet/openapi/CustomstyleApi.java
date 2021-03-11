package org.fao.geonet.openapi;

import java.util.Map;

import org.fao.geonet.ApiClient;

import feign.Headers;
import feign.RequestLine;

public interface CustomstyleApi extends ApiClient.Api {

    /**
     * Get CssStyleSettings This returns a map with all Less variables.
     * 
     * @return Map&lt;String, String&gt;
     */
    @RequestLine("GET /srv/api/0.1/customstyle")
    @Headers({ "Accept: application/json", })
    Map<String, String> getCssStyle();

    /**
     * Saves custom style variables. Saves custom style variables.
     * 
     * @param gnCssStyle jsonVariables (optional)
     * @return String
     */
    @RequestLine("POST /srv/api/0.1/customstyle")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    String saveCustomStyle(String gnCssStyle);
}
