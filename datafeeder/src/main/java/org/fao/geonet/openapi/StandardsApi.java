package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.BatchEditing;
import org.fao.geonet.openapi.model.Codelist;
import org.fao.geonet.openapi.model.Element;
import org.fao.geonet.openapi.model.MetadataSchema;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface StandardsApi extends ApiClient.Api {

    /**
     * Get batch editor configuration for a standard
     * 
     * @param schema Schema identifier (required)
     * @return Map&lt;String, BatchEditing&gt;
     */
    @RequestLine("GET /srv/api/0.1/standards/{schema}/batchconfiguration")
    @Headers({ "Accept: application/json", })
    Map<String, BatchEditing> getBatchConfiguration(@Param("schema") String schema);

    /**
     * Get batch editor configuration for standards
     * 
     * @param schema Schema identifiers (optional)
     * @return Map&lt;String, BatchEditing&gt;
     */
    @RequestLine("GET /srv/api/0.1/standards/batchconfiguration?schema={schema}")
    @Headers({ "Accept: application/json", })
    Map<String, BatchEditing> getBatchConfigurations(@Param("schema") List<String> schema);

    /**
     * Get batch editor configuration for standards
     * 
     * Note, this is equivalent to the other <code>getBatchConfigurations</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link GetBatchConfigurationsQueryParams} class that
     * allows for building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>schema - Schema identifiers (optional)</li>
     *                    </ul>
     * @return Map&lt;String, BatchEditing&gt;
     */
    @RequestLine("GET /srv/api/0.1/standards/batchconfiguration?schema={schema}")
    @Headers({ "Accept: application/json", })
    Map<String, BatchEditing> getBatchConfigurations(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getBatchConfigurations</code> method in a fluent style.
     */
    public static class GetBatchConfigurationsQueryParams extends HashMap<String, Object> {
        public GetBatchConfigurationsQueryParams schema(final List<String> value) {
            put("schema", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Get editor associated resources panel configuration
     * 
     * @param schema Schema identifier (required)
     * @param name   Configuration identifier (required)
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/standards/{schema}/editor/associatedpanel/config/{name}.json")
    @Headers({ "Accept: application/json", })
    String getEditorAssociatedPanelConfiguration(@Param("schema") String schema, @Param("name") String name);

    /**
     * Get descriptor details
     * 
     * @param schema    Schema identifier (required)
     * @param element   Descriptor name (required)
     * @param parent    parent (optional)
     * @param displayIf displayIf (optional)
     * @param xpath     xpath (optional)
     * @param isoType   isoType (optional)
     * @return Element
     */
    @RequestLine("GET /srv/api/0.1/standards/{schema}/descriptors/{element}/details?parent={parent}&displayIf={displayIf}&xpath={xpath}&isoType={isoType}")
    @Headers({ "Accept: application/json", })
    Element getElementDetails(@Param("schema") String schema, @Param("element") String element,
            @Param("parent") String parent, @Param("displayIf") String displayIf, @Param("xpath") String xpath,
            @Param("isoType") String isoType);

    /**
     * Get descriptor details
     * 
     * Note, this is equivalent to the other <code>getElementDetails</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link GetElementDetailsQueryParams} class that allows for building
     * up this map in a fluent style.
     * 
     * @param schema      Schema identifier (required)
     * @param element     Descriptor name (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>parent - parent (optional)</li>
     *                    <li>displayIf - displayIf (optional)</li>
     *                    <li>xpath - xpath (optional)</li>
     *                    <li>isoType - isoType (optional)</li>
     *                    </ul>
     * @return Element
     */
    @RequestLine("GET /srv/api/0.1/standards/{schema}/descriptors/{element}/details?parent={parent}&displayIf={displayIf}&xpath={xpath}&isoType={isoType}")
    @Headers({ "Accept: application/json", })
    Element getElementDetails(@Param("schema") String schema, @Param("element") String element,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getElementDetails</code> method in a fluent style.
     */
    public static class GetElementDetailsQueryParams extends HashMap<String, Object> {
        public GetElementDetailsQueryParams parent(final String value) {
            put("parent", EncodingUtils.encode(value));
            return this;
        }

        public GetElementDetailsQueryParams displayIf(final String value) {
            put("displayIf", EncodingUtils.encode(value));
            return this;
        }

        public GetElementDetailsQueryParams xpath(final String value) {
            put("xpath", EncodingUtils.encode(value));
            return this;
        }

        public GetElementDetailsQueryParams isoType(final String value) {
            put("isoType", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get codelist details
     * 
     * @param schema    Schema identifier (required)
     * @param codelist  Codelist element name or alias (required)
     * @param parent    Parent name with namespace which may indicate a more precise
     *                  label as defined in context attribute. (optional)
     * @param displayIf Display if condition as defined in the codelist.xml file.
     *                  Allows to select a more precise codelist when more than one
     *                  is defined for same name. (optional)
     * @param xpath     XPath of the element to target which may indicate a more
     *                  precise label as defined in context attribute. (optional)
     * @param isoType   ISO type of the element to target which may indicate a more
     *                  precise label as defined in context attribute. (Same as
     *                  context. TODO: Deprecate ?) (optional)
     * @return Codelist
     */
    @RequestLine("GET /srv/api/0.1/standards/{schema}/codelists/{codelist}/details?parent={parent}&displayIf={displayIf}&xpath={xpath}&isoType={isoType}")
    @Headers({ "Accept: application/json", })
    Codelist getSchemaCodelistsWithDetails(@Param("schema") String schema, @Param("codelist") String codelist,
            @Param("parent") String parent, @Param("displayIf") String displayIf, @Param("xpath") String xpath,
            @Param("isoType") String isoType);

    /**
     * Get codelist details
     * 
     * Note, this is equivalent to the other
     * <code>getSchemaCodelistsWithDetails</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link GetSchemaCodelistsWithDetailsQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param schema      Schema identifier (required)
     * @param codelist    Codelist element name or alias (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>parent - Parent name with namespace which may indicate
     *                    a more precise label as defined in context attribute.
     *                    (optional)</li>
     *                    <li>displayIf - Display if condition as defined in the
     *                    codelist.xml file. Allows to select a more precise
     *                    codelist when more than one is defined for same name.
     *                    (optional)</li>
     *                    <li>xpath - XPath of the element to target which may
     *                    indicate a more precise label as defined in context
     *                    attribute. (optional)</li>
     *                    <li>isoType - ISO type of the element to target which may
     *                    indicate a more precise label as defined in context
     *                    attribute. (Same as context. TODO: Deprecate ?)
     *                    (optional)</li>
     *                    </ul>
     * @return Codelist
     */
    @RequestLine("GET /srv/api/0.1/standards/{schema}/codelists/{codelist}/details?parent={parent}&displayIf={displayIf}&xpath={xpath}&isoType={isoType}")
    @Headers({ "Accept: application/json", })
    Codelist getSchemaCodelistsWithDetails(@Param("schema") String schema, @Param("codelist") String codelist,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getSchemaCodelistsWithDetails</code> method in a fluent style.
     */
    public static class GetSchemaCodelistsWithDetailsQueryParams extends HashMap<String, Object> {
        public GetSchemaCodelistsWithDetailsQueryParams parent(final String value) {
            put("parent", EncodingUtils.encode(value));
            return this;
        }

        public GetSchemaCodelistsWithDetailsQueryParams displayIf(final String value) {
            put("displayIf", EncodingUtils.encode(value));
            return this;
        }

        public GetSchemaCodelistsWithDetailsQueryParams xpath(final String value) {
            put("xpath", EncodingUtils.encode(value));
            return this;
        }

        public GetSchemaCodelistsWithDetailsQueryParams isoType(final String value) {
            put("isoType", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get codelist translations
     * 
     * @param schema    Schema identifier (required)
     * @param codelist  Codelist element name or alias (required)
     * @param parent    parent (optional)
     * @param displayIf displayIf (optional)
     * @param xpath     xpath (optional)
     * @param isoType   isoType (optional)
     * @return Map&lt;String, String&gt;
     */
    @RequestLine("GET /srv/api/0.1/standards/{schema}/codelists/{codelist}?parent={parent}&displayIf={displayIf}&xpath={xpath}&isoType={isoType}")
    @Headers({ "Accept: application/json", })
    Map<String, String> getSchemaTranslations(@Param("schema") String schema, @Param("codelist") String codelist,
            @Param("parent") String parent, @Param("displayIf") String displayIf, @Param("xpath") String xpath,
            @Param("isoType") String isoType);

    /**
     * Get codelist translations
     * 
     * Note, this is equivalent to the other <code>getSchemaTranslations</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link GetSchemaTranslationsQueryParams} class that allows
     * for building up this map in a fluent style.
     * 
     * @param schema      Schema identifier (required)
     * @param codelist    Codelist element name or alias (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>parent - parent (optional)</li>
     *                    <li>displayIf - displayIf (optional)</li>
     *                    <li>xpath - xpath (optional)</li>
     *                    <li>isoType - isoType (optional)</li>
     *                    </ul>
     * @return Map&lt;String, String&gt;
     */
    @RequestLine("GET /srv/api/0.1/standards/{schema}/codelists/{codelist}?parent={parent}&displayIf={displayIf}&xpath={xpath}&isoType={isoType}")
    @Headers({ "Accept: application/json", })
    Map<String, String> getSchemaTranslations(@Param("schema") String schema, @Param("codelist") String codelist,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getSchemaTranslations</code> method in a fluent style.
     */
    public static class GetSchemaTranslationsQueryParams extends HashMap<String, Object> {
        public GetSchemaTranslationsQueryParams parent(final String value) {
            put("parent", EncodingUtils.encode(value));
            return this;
        }

        public GetSchemaTranslationsQueryParams displayIf(final String value) {
            put("displayIf", EncodingUtils.encode(value));
            return this;
        }

        public GetSchemaTranslationsQueryParams xpath(final String value) {
            put("xpath", EncodingUtils.encode(value));
            return this;
        }

        public GetSchemaTranslationsQueryParams isoType(final String value) {
            put("isoType", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get standards
     * 
     * @return List&lt;MetadataSchema&gt;
     */
    @RequestLine("GET /srv/api/0.1/standards")
    @Headers({ "Accept: application/json", })
    List<MetadataSchema> getStandards();

    /**
     * Reload standards
     * 
     */
    @RequestLine("GET /srv/api/0.1/standards/reload")
    @Headers({ "Accept: application/json", })
    void reloadStandards();
}
