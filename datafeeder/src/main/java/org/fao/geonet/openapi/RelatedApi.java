package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.RelatedResponse;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface RelatedApi extends ApiClient.Api {

    /**
     * Get record related resources for all requested metadatas Retrieve related
     * services, datasets, onlines, thumbnails, sources, ... to all requested
     * records.&lt;br/&gt;&lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/index.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param type Type of related resource. If none, all resources are returned.
     *             (optional)
     * @param uuid Uuids of the metadatas you request the relations from. (optional)
     * @return Map&lt;String, RelatedResponse&gt;
     */
    @RequestLine("GET /srv/api/0.1/related?type={type}&uuid={uuid}")
    @Headers({ "Accept: application/json", })
    Map<String, RelatedResponse> getAssociated1(@Param("type") List<String> type, @Param("uuid") List<String> uuid);

    /**
     * Get record related resources for all requested metadatas Retrieve related
     * services, datasets, onlines, thumbnails, sources, ... to all requested
     * records.&lt;br/&gt;&lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/index.html&#39;&gt;More
     * info&lt;/a&gt; Note, this is equivalent to the other
     * <code>getAssociated1</code> method, but with the query parameters collected
     * into a single Map parameter. This is convenient for services with optional
     * query parameters, especially when used with the
     * {@link GetAssociated1QueryParams} class that allows for building up this map
     * in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>type - Type of related resource. If none, all
     *                    resources are returned. (optional)</li>
     *                    <li>uuid - Uuids of the metadatas you request the
     *                    relations from. (optional)</li>
     *                    </ul>
     * @return Map&lt;String, RelatedResponse&gt;
     */
    @RequestLine("GET /srv/api/0.1/related?type={type}&uuid={uuid}")
    @Headers({ "Accept: application/json", })
    Map<String, RelatedResponse> getAssociated1(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getAssociated1</code> method in a fluent style.
     */
    public static class GetAssociated1QueryParams extends HashMap<String, Object> {
        public GetAssociated1QueryParams type(final List<String> value) {
            put("type", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public GetAssociated1QueryParams uuid(final List<String> value) {
            put("uuid", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }
}
