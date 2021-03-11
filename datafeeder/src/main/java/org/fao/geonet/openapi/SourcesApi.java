package org.fao.geonet.openapi;

import org.fao.geonet.ApiClient;
import org.fao.geonet.openapi.model.ResponseEntity;
import org.fao.geonet.openapi.model.Source;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface SourcesApi extends ApiClient.Api {

    /**
     * Add a source
     * 
     * @param source source (optional)
     * @return ResponseEntity
     */
    @RequestLine("PUT /srv/api/0.1/sources")
    @Headers({ "Content-Type: application/json", "Accept: text/plain", })
    ResponseEntity addSource(Source source);

    /**
     * Remove a source
     * 
     * @param sourceIdentifier Source identifier (required)
     * @return ResponseEntity
     */
    @RequestLine("DELETE /srv/api/0.1/sources/{sourceIdentifier}")
    @Headers({ "Accept: */*", })
    ResponseEntity deleteSource(@Param("sourceIdentifier") String sourceIdentifier);

    /**
     * Get portal list List all subportal available.
     */
    @RequestLine("GET /srv/api/0.1/sources")
    @Headers({ "Accept: text/html", })
    void getSubPortal();

    /**
     * Update a source
     * 
     * @param sourceIdentifier Source identifier (required)
     * @param source           source (optional)
     * @return ResponseEntity
     */
    @RequestLine("PUT /srv/api/0.1/sources/{sourceIdentifier}")
    @Headers({ "Content-Type: application/json", "Accept: */*", })
    ResponseEntity updateSource(@Param("sourceIdentifier") String sourceIdentifier, Source source);
}
