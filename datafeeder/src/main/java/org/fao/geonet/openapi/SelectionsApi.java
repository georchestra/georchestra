package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface SelectionsApi extends ApiClient.Api {

    /**
     * Select one or more items
     * 
     * @param bucket Bucket name (required)
     * @param uuid   One or more record UUIDs. If null, select all in current search
     *               if bucket name is &#39;metadata&#39; (TODO: remove this
     *               limitation?). (optional)
     * @return Integer
     */
    @RequestLine("PUT /srv/api/0.1/selections/{bucket}?uuid={uuid}")
    @Headers({ "Accept: application/json", })
    Integer addToSelection(@Param("bucket") String bucket, @Param("uuid") List<String> uuid);

    /**
     * Select one or more items
     * 
     * Note, this is equivalent to the other <code>addToSelection</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link AddToSelectionQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param bucket      Bucket name (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>uuid - One or more record UUIDs. If null, select all
     *                    in current search if bucket name is &#39;metadata&#39;
     *                    (TODO: remove this limitation?). (optional)</li>
     *                    </ul>
     * @return Integer
     */
    @RequestLine("PUT /srv/api/0.1/selections/{bucket}?uuid={uuid}")
    @Headers({ "Accept: application/json", })
    Integer addToSelection(@Param("bucket") String bucket, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>addToSelection</code> method in a fluent style.
     */
    public static class AddToSelectionQueryParams extends HashMap<String, Object> {
        public AddToSelectionQueryParams uuid(final List<String> value) {
            put("uuid", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Clear selection or remove items
     * 
     * @param bucket Selection bucket name (required)
     * @param uuid   One or more record UUIDs (optional)
     * @return Integer
     */
    @RequestLine("DELETE /srv/api/0.1/selections/{bucket}?uuid={uuid}")
    @Headers({ "Accept: application/json", })
    Integer clearSelection(@Param("bucket") String bucket, @Param("uuid") List<String> uuid);

    /**
     * Clear selection or remove items
     * 
     * Note, this is equivalent to the other <code>clearSelection</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link ClearSelectionQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param bucket      Selection bucket name (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>uuid - One or more record UUIDs (optional)</li>
     *                    </ul>
     * @return Integer
     */
    @RequestLine("DELETE /srv/api/0.1/selections/{bucket}?uuid={uuid}")
    @Headers({ "Accept: application/json", })
    Integer clearSelection(@Param("bucket") String bucket, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>clearSelection</code> method in a fluent style.
     */
    public static class ClearSelectionQueryParams extends HashMap<String, Object> {
        public ClearSelectionQueryParams uuid(final List<String> value) {
            put("uuid", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Get current selection
     * 
     * @param bucket Bucket name (required)
     * @return List&lt;String&gt;
     */
    @RequestLine("GET /srv/api/0.1/selections/{bucket}")
    @Headers({ "Accept: application/json", })
    List<String> getSelection(@Param("bucket") String bucket);
}
