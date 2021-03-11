package org.fao.geonet.openapi;

import org.fao.geonet.ApiClient;
import org.fao.geonet.openapi.model.MeResponse;

import feign.Headers;
import feign.RequestLine;

public interface MeApi extends ApiClient.Api {

    /**
     * Get information about me If not authenticated, return status 204
     * (NO_CONTENT), else return basic user information. This operation is usually
     * used to know if current user is authenticated or not.It returns also info
     * about groups and profiles.
     * 
     * @return MeResponse
     */
    @RequestLine("GET /srv/api/0.1/me")
    @Headers({ "Accept: application/json", })
    MeResponse getMe();
}
