package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.WFSHarvesterParameter;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface WorkersApi extends ApiClient.Api {

    /**
     * Delete a WFS feature type
     * 
     * @param serviceUrl serviceUrl (required)
     * @param typeName   typeName (required)
     * @return Map&lt;String, Object&gt;
     */
    @RequestLine("DELETE /srv/api/0.1/workers/data/wfs/actions?serviceUrl={serviceUrl}&typeName={typeName}")
    @Headers({ "Accept: */*", })
    Map<String, Object> deleteWfsFeatureType(@Param("serviceUrl") String serviceUrl,
            @Param("typeName") String typeName);

    /**
     * Delete a WFS feature type
     * 
     * Note, this is equivalent to the other <code>deleteWfsFeatureType</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link DeleteWfsFeatureTypeQueryParams} class that allows
     * for building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>serviceUrl - serviceUrl (required)</li>
     *                    <li>typeName - typeName (required)</li>
     *                    </ul>
     * @return Map&lt;String, Object&gt;
     */
    @RequestLine("DELETE /srv/api/0.1/workers/data/wfs/actions?serviceUrl={serviceUrl}&typeName={typeName}")
    @Headers({ "Accept: */*", })
    Map<String, Object> deleteWfsFeatureType(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deleteWfsFeatureType</code> method in a fluent style.
     */
    public static class DeleteWfsFeatureTypeQueryParams extends HashMap<String, Object> {
        public DeleteWfsFeatureTypeQueryParams serviceUrl(final String value) {
            put("serviceUrl", EncodingUtils.encode(value));
            return this;
        }

        public DeleteWfsFeatureTypeQueryParams typeName(final String value) {
            put("typeName", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Index a WFS feature type
     * 
     * @param config config (required)
     * @return Map&lt;String, Object&gt;
     */
    @RequestLine("PUT /srv/api/0.1/workers/data/wfs/actions/start")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    Map<String, Object> indexWfsFeatureType(WFSHarvesterParameter config);
}
