package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.Link;
import org.fao.geonet.openapi.model.ResponseEntity;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface LinksApi extends ApiClient.Api {

    /**
     * Analyze records links
     * 
     * @param uuids       Record UUIDs. If null current selection is used.
     *                    (optional)
     * @param bucket      Selection bucket name (optional)
     * @param removeFirst removeFirst (optional, default to true)
     * @param analyze     analyze (optional, default to false)
     * @return ResponseEntity
     */
    @RequestLine("POST /srv/api/0.1/records/links?uuids={uuids}&bucket={bucket}&removeFirst={removeFirst}&analyze={analyze}")
    @Headers({ "Accept: application/json", })
    ResponseEntity analyzeRecordLinks(@Param("uuids") List<String> uuids, @Param("bucket") String bucket,
            @Param("removeFirst") Boolean removeFirst, @Param("analyze") Boolean analyze);

    /**
     * Analyze records links
     * 
     * Note, this is equivalent to the other <code>analyzeRecordLinks</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link AnalyzeRecordLinksQueryParams} class that allows for building
     * up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>uuids - Record UUIDs. If null current selection is
     *                    used. (optional)</li>
     *                    <li>bucket - Selection bucket name (optional)</li>
     *                    <li>removeFirst - removeFirst (optional, default to
     *                    true)</li>
     *                    <li>analyze - analyze (optional, default to false)</li>
     *                    </ul>
     * @return ResponseEntity
     */
    @RequestLine("POST /srv/api/0.1/records/links?uuids={uuids}&bucket={bucket}&removeFirst={removeFirst}&analyze={analyze}")
    @Headers({ "Accept: application/json", })
    ResponseEntity analyzeRecordLinks(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>analyzeRecordLinks</code> method in a fluent style.
     */
    public static class AnalyzeRecordLinksQueryParams extends HashMap<String, Object> {
        public AnalyzeRecordLinksQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public AnalyzeRecordLinksQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public AnalyzeRecordLinksQueryParams removeFirst(final Boolean value) {
            put("removeFirst", EncodingUtils.encode(value));
            return this;
        }

        public AnalyzeRecordLinksQueryParams analyze(final Boolean value) {
            put("analyze", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get record links
     * 
     * @param from From page (optional, default to 0)
     * @param size Number of records to return (optional, default to 200)
     * @return List&lt;Link&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/links?from={from}&size={size}")
    @Headers({ "Accept: application/json", })
    List<Link> getRecordLinks(@Param("from") Integer from, @Param("size") Integer size);

    /**
     * Get record links
     * 
     * Note, this is equivalent to the other <code>getRecordLinks</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link GetRecordLinksQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>from - From page (optional, default to 0)</li>
     *                    <li>size - Number of records to return (optional, default
     *                    to 200)</li>
     *                    </ul>
     * @return List&lt;Link&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/links?from={from}&size={size}")
    @Headers({ "Accept: application/json", })
    List<Link> getRecordLinks(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getRecordLinks</code> method in a fluent style.
     */
    public static class GetRecordLinksQueryParams extends HashMap<String, Object> {
        public GetRecordLinksQueryParams from(final Integer value) {
            put("from", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordLinksQueryParams size(final Integer value) {
            put("size", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Remove all links and status history
     * 
     * @return ResponseEntity
     */
    @RequestLine("DELETE /srv/api/0.1/records/links")
    @Headers({ "Accept: application/json", })
    ResponseEntity purgeAll();
}
