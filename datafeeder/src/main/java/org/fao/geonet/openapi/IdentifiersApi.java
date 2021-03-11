package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.MetadataIdentifierTemplate;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface IdentifiersApi extends ApiClient.Api {

    /**
     * Add an identifier template
     * 
     * @param metadataIdentifierTemplate Identifier template details (optional)
     * @return Integer
     */
    @RequestLine("PUT /srv/api/0.1/identifiers")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    Integer addIdentifier(MetadataIdentifierTemplate metadataIdentifierTemplate);

    /**
     * Remove an identifier template
     * 
     * @param identifier Identifier template identifier (required)
     */
    @RequestLine("DELETE /srv/api/0.1/identifiers/{identifier}")
    @Headers({ "Accept: application/json", })
    void deleteIdentifier(@Param("identifier") Integer identifier);

    /**
     * Get identifier templates Identifier templates are used to create record UUIDs
     * havind a particular structure. The template will be used when user creates a
     * new record. The template identifier to use is defined in the administration
     * &gt; settings.
     * 
     * @param userDefinedOnly Only user defined ones (optional, default to false)
     * @return List&lt;MetadataIdentifierTemplate&gt;
     */
    @RequestLine("GET /srv/api/0.1/identifiers?userDefinedOnly={userDefinedOnly}")
    @Headers({ "Accept: application/json", })
    List<MetadataIdentifierTemplate> getIdentifiers(@Param("userDefinedOnly") Boolean userDefinedOnly);

    /**
     * Get identifier templates Identifier templates are used to create record UUIDs
     * havind a particular structure. The template will be used when user creates a
     * new record. The template identifier to use is defined in the administration
     * &gt; settings. Note, this is equivalent to the other
     * <code>getIdentifiers</code> method, but with the query parameters collected
     * into a single Map parameter. This is convenient for services with optional
     * query parameters, especially when used with the
     * {@link GetIdentifiersQueryParams} class that allows for building up this map
     * in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>userDefinedOnly - Only user defined ones (optional,
     *                    default to false)</li>
     *                    </ul>
     * @return List&lt;MetadataIdentifierTemplate&gt;
     */
    @RequestLine("GET /srv/api/0.1/identifiers?userDefinedOnly={userDefinedOnly}")
    @Headers({ "Accept: application/json", })
    List<MetadataIdentifierTemplate> getIdentifiers(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getIdentifiers</code> method in a fluent style.
     */
    public static class GetIdentifiersQueryParams extends HashMap<String, Object> {
        public GetIdentifiersQueryParams userDefinedOnly(final Boolean value) {
            put("userDefinedOnly", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Update an identifier template
     * 
     * @param identifier                 Identifier template identifier (required)
     * @param metadataIdentifierTemplate Identifier template details (optional)
     */
    @RequestLine("PUT /srv/api/0.1/identifiers/{identifier}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    void updateIdentifier(@Param("identifier") Integer identifier,
            MetadataIdentifierTemplate metadataIdentifierTemplate);
}
