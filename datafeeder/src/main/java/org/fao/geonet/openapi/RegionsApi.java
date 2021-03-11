package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.Category;
import org.fao.geonet.openapi.model.ListRegionsResponse;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface RegionsApi extends ApiClient.Api {

    /**
     * Get list of region types
     * 
     * @return List&lt;Category&gt;
     */
    @RequestLine("GET /srv/api/0.1/regions/types")
    @Headers({ "Accept: application/json", })
    List<Category> getRegionTypes();

    /**
     * Get list of regions
     * 
     * @param label      label (optional)
     * @param categoryId categoryId (optional)
     * @param maxRecords maxRecords (optional, default to -1)
     * @return ListRegionsResponse
     */
    @RequestLine("GET /srv/api/0.1/regions?label={label}&categoryId={categoryId}&maxRecords={maxRecords}")
    @Headers({ "Accept: application/json", })
    ListRegionsResponse getRegions(@Param("label") String label, @Param("categoryId") String categoryId,
            @Param("maxRecords") Integer maxRecords);

    /**
     * Get list of regions
     * 
     * Note, this is equivalent to the other <code>getRegions</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link GetRegionsQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>label - label (optional)</li>
     *                    <li>categoryId - categoryId (optional)</li>
     *                    <li>maxRecords - maxRecords (optional, default to -1)</li>
     *                    </ul>
     * @return ListRegionsResponse
     */
    @RequestLine("GET /srv/api/0.1/regions?label={label}&categoryId={categoryId}&maxRecords={maxRecords}")
    @Headers({ "Accept: application/json", })
    ListRegionsResponse getRegions(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getRegions</code> method in a fluent style.
     */
    public static class GetRegionsQueryParams extends HashMap<String, Object> {
        public GetRegionsQueryParams label(final String value) {
            put("label", EncodingUtils.encode(value));
            return this;
        }

        public GetRegionsQueryParams categoryId(final String value) {
            put("categoryId", EncodingUtils.encode(value));
            return this;
        }

        public GetRegionsQueryParams maxRecords(final Integer value) {
            put("maxRecords", EncodingUtils.encode(value));
            return this;
        }
    }
}
