package org.fao.geonet.openapi;

import java.util.List;

import org.fao.geonet.ApiClient;
import org.fao.geonet.openapi.model.Operation;

import feign.Headers;
import feign.RequestLine;

public interface OperationsApi extends ApiClient.Api {

    /**
     * Get operations Return the list of operations. Operations are used to define
     * authorization per group. Extending the list of default operations (ie. view,
     * dynamic, download, editing, notify, featured) might be feasible but is
     * experimental.&lt;br/&gt; Featured is not really used anymore (was used in
     * past version for home page highlights).
     * 
     * @return List&lt;Operation&gt;
     */
    @RequestLine("GET /srv/api/0.1/operations")
    @Headers({ "Accept: application/json", })
    List<Operation> getOperations();
}
