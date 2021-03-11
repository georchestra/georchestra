package org.fao.geonet.openapi;

import java.util.List;

import org.fao.geonet.ApiClient;
import org.fao.geonet.openapi.model.StatusValue;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface StatusApi extends ApiClient.Api {

    /**
     * Get status
     * 
     * @return List&lt;StatusValue&gt;
     */
    @RequestLine("GET /srv/api/0.1/status")
    @Headers({ "Accept: application/json", })
    List<StatusValue> getStatus1();

    /**
     * Get status by type
     * 
     * @param type Type (required)
     * @return List&lt;StatusValue&gt;
     */
    @RequestLine("GET /srv/api/0.1/status/{type}")
    @Headers({ "Accept: application/json", })
    List<StatusValue> getStatusByType(@Param("type") String type);
}
