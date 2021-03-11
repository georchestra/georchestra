package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface HarvestersApi extends ApiClient.Api {

    /**
     * Assign harvester records to a new source
     * 
     * @param harvesterUuid The harvester UUID (required)
     * @param source        The target source UUID (optional)
     * @return String
     */
    @RequestLine("POST /srv/api/0.1/harvesters/{harvesterUuid}/assign?source={source}")
    @Headers({ "Accept: application/json", })
    String assignHarvestedRecordToSource(@Param("harvesterUuid") String harvesterUuid, @Param("source") String source);

    /**
     * Assign harvester records to a new source
     * 
     * Note, this is equivalent to the other
     * <code>assignHarvestedRecordToSource</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link AssignHarvestedRecordToSourceQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param harvesterUuid The harvester UUID (required)
     * @param queryParams   Map of query parameters as name-value pairs
     *                      <p>
     *                      The following elements may be specified in the query
     *                      map:
     *                      </p>
     *                      <ul>
     *                      <li>source - The target source UUID (optional)</li>
     *                      </ul>
     * @return String
     */
    @RequestLine("POST /srv/api/0.1/harvesters/{harvesterUuid}/assign?source={source}")
    @Headers({ "Accept: application/json", })
    String assignHarvestedRecordToSource(@Param("harvesterUuid") String harvesterUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>assignHarvestedRecordToSource</code> method in a fluent style.
     */
    public static class AssignHarvestedRecordToSourceQueryParams extends HashMap<String, Object> {
        public AssignHarvestedRecordToSourceQueryParams source(final String value) {
            put("source", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Check if a harvester name or host already exist
     * 
     * @param property The harvester property to check (required)
     * @param exist    The value to search (optional)
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/harvesters/properties/{property}?exist={exist}")
    @Headers({ "Accept: */*", })
    String checkHarvesterPropertyExist(@Param("property") String property, @Param("exist") String exist);

    /**
     * Check if a harvester name or host already exist
     * 
     * Note, this is equivalent to the other
     * <code>checkHarvesterPropertyExist</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link CheckHarvesterPropertyExistQueryParams} class that allows for building
     * up this map in a fluent style.
     * 
     * @param property    The harvester property to check (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>exist - The value to search (optional)</li>
     *                    </ul>
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/harvesters/properties/{property}?exist={exist}")
    @Headers({ "Accept: */*", })
    String checkHarvesterPropertyExist(@Param("property") String property,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>checkHarvesterPropertyExist</code> method in a fluent style.
     */
    public static class CheckHarvesterPropertyExistQueryParams extends HashMap<String, Object> {
        public CheckHarvesterPropertyExistQueryParams exist(final String value) {
            put("exist", EncodingUtils.encode(value));
            return this;
        }
    }
}
