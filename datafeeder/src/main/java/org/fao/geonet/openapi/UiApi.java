package org.fao.geonet.openapi;

import java.util.List;

import org.fao.geonet.ApiClient;
import org.fao.geonet.openapi.model.ResponseEntity;
import org.fao.geonet.openapi.model.UiSetting;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface UiApi extends ApiClient.Api {

    /**
     * Remove a UI Configuration
     * 
     * @param uiIdentifier UI configuration identifier (required)
     * @return ResponseEntity
     */
    @RequestLine("DELETE /srv/api/0.1/ui/{uiIdentifier}")
    @Headers({ "Accept: */*", })
    ResponseEntity deleteUiConfiguration(@Param("uiIdentifier") String uiIdentifier);

    /**
     * Get a UI configuration
     * 
     * @param uiIdentifier UI identifier (required)
     * @return UiSetting
     */
    @RequestLine("GET /srv/api/0.1/ui/{uiIdentifier}")
    @Headers({ "Accept: application/json", })
    UiSetting getUiConfiguration(@Param("uiIdentifier") String uiIdentifier);

    /**
     * Get UI configuration
     * 
     * @return List&lt;UiSetting&gt;
     */
    @RequestLine("GET /srv/api/0.1/ui")
    @Headers({ "Accept: application/json", })
    List<UiSetting> getUiConfigurations();

    /**
     * Create a UI configuration
     * 
     * @param uiConfiguration uiConfiguration (optional)
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/ui")
    @Headers({ "Content-Type: application/json", "Accept: text/plain", })
    String putUiConfiguration(UiSetting uiConfiguration);

    /**
     * Update a UI configuration
     * 
     * @param uiIdentifier    UI configuration identifier (required)
     * @param uiConfiguration uiConfiguration (optional)
     * @return ResponseEntity
     */
    @RequestLine("PUT /srv/api/0.1/ui/{uiIdentifier}")
    @Headers({ "Content-Type: application/json", "Accept: */*", })
    ResponseEntity updateUiConfiguration(@Param("uiIdentifier") String uiIdentifier, UiSetting uiConfiguration);
}
