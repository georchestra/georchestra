package org.fao.geonet.openapi;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.Crs;
import org.fao.geonet.openapi.model.KeywordBean;
import org.fao.geonet.openapi.model.SimpleMetadataProcessingReport;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface RegistriesApi extends ApiClient.Api {

    /**
     * Delete a thesaurus by name Delete a thesaurus.
     * 
     * @param thesaurus Thesaurus to delete. (required)
     */
    @RequestLine("DELETE /srv/api/0.1/registries/vocabularies/{thesaurus}")
    @Headers({ "Accept: */*", })
    void deleteThesaurus(@Param("thesaurus") String thesaurus);

    /**
     * Extracts directory entries from records Scan one or more records for element
     * matching the XPath provided and save them as directory entries (ie.
     * subtemplate).&lt;br/&gt;&lt;br/&gt;Only records that the current user can
     * edit are analyzed.
     * 
     * @param xpath           XPath of the elements to extract as entry. (required)
     * @param uuids           Record UUIDs. If null current selection is used.
     *                        (optional)
     * @param bucket          Selection bucket name (optional)
     * @param identifierXpath XPath of the element identifier. If not defined a
     *                        random UUID is generated and analysis will not check
     *                        for duplicates. (optional)
     * @return Object
     */
    @RequestLine("PUT /srv/api/0.1/registries/actions/entries/collect?uuids={uuids}&bucket={bucket}&xpath={xpath}&identifierXpath={identifierXpath}")
    @Headers({ "Accept: application/json", })
    Object extractEntries(@Param("xpath") String xpath, @Param("uuids") List<String> uuids,
            @Param("bucket") String bucket, @Param("identifierXpath") String identifierXpath);

    /**
     * Extracts directory entries from records Scan one or more records for element
     * matching the XPath provided and save them as directory entries (ie.
     * subtemplate).&lt;br/&gt;&lt;br/&gt;Only records that the current user can
     * edit are analyzed. Note, this is equivalent to the other
     * <code>extractEntries</code> method, but with the query parameters collected
     * into a single Map parameter. This is convenient for services with optional
     * query parameters, especially when used with the
     * {@link ExtractEntriesQueryParams} class that allows for building up this map
     * in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>uuids - Record UUIDs. If null current selection is
     *                    used. (optional)</li>
     *                    <li>bucket - Selection bucket name (optional)</li>
     *                    <li>xpath - XPath of the elements to extract as entry.
     *                    (required)</li>
     *                    <li>identifierXpath - XPath of the element identifier. If
     *                    not defined a random UUID is generated and analysis will
     *                    not check for duplicates. (optional)</li>
     *                    </ul>
     * @return Object
     */
    @RequestLine("PUT /srv/api/0.1/registries/actions/entries/collect?uuids={uuids}&bucket={bucket}&xpath={xpath}&identifierXpath={identifierXpath}")
    @Headers({ "Accept: application/json", })
    Object extractEntries(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>extractEntries</code> method in a fluent style.
     */
    public static class ExtractEntriesQueryParams extends HashMap<String, Object> {
        public ExtractEntriesQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public ExtractEntriesQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public ExtractEntriesQueryParams xpath(final String value) {
            put("xpath", EncodingUtils.encode(value));
            return this;
        }

        public ExtractEntriesQueryParams identifierXpath(final String value) {
            put("identifierXpath", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get CRS
     * 
     * @param id CRS identifier (required)
     * @return Crs
     */
    @RequestLine("GET /srv/api/0.1/registries/crs/{id}")
    @Headers({ "Accept: application/json", })
    Crs getCrsById(@Param("id") String id);

    /**
     * Get list of CRS type
     * 
     * @return List&lt;String&gt;
     */
    @RequestLine("GET /srv/api/0.1/registries/crs/types")
    @Headers({ "Accept: application/json", })
    List<String> getCrsType();

    /**
     * Get a directory entry Directory entry (AKA subtemplates) are XML fragments
     * that can be inserted in metadata records using XLinks. XLinks can be remote
     * or local.
     * 
     * @param uuid           Directory entry UUID. (required)
     * @param process        Process (optional)
     * @param transformation Transformation (optional)
     * @param lang           lang (optional)
     * @param schema         schema (optional, default to iso19139)
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/registries/entries/{uuid}?process={process}&transformation={transformation}&lang={lang}&schema={schema}")
    @Headers({ "Accept: application/xml", })
    Object getEntry(@Param("uuid") String uuid, @Param("process") List<String> process,
            @Param("transformation") String transformation, @Param("lang") List<String> lang,
            @Param("schema") String schema);

    /**
     * Get a directory entry Directory entry (AKA subtemplates) are XML fragments
     * that can be inserted in metadata records using XLinks. XLinks can be remote
     * or local. Note, this is equivalent to the other <code>getEntry</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link GetEntryQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param uuid        Directory entry UUID. (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>process - Process (optional)</li>
     *                    <li>transformation - Transformation (optional)</li>
     *                    <li>lang - lang (optional)</li>
     *                    <li>schema - schema (optional, default to iso19139)</li>
     *                    </ul>
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/registries/entries/{uuid}?process={process}&transformation={transformation}&lang={lang}&schema={schema}")
    @Headers({ "Accept: application/xml", })
    Object getEntry(@Param("uuid") String uuid, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getEntry</code> method in a fluent style.
     */
    public static class GetEntryQueryParams extends HashMap<String, Object> {
        public GetEntryQueryParams process(final List<String> value) {
            put("process", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public GetEntryQueryParams transformation(final String value) {
            put("transformation", EncodingUtils.encode(value));
            return this;
        }

        public GetEntryQueryParams lang(final List<String> value) {
            put("lang", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public GetEntryQueryParams schema(final String value) {
            put("schema", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get keyword by id Retrieve XML representation of keyword(s) from same
     * thesaurususing different transformations. &#39;to-iso19139-keyword&#39; is
     * the default and return an ISO19139
     * snippet.&#39;to-iso19139-keyword-as-xlink&#39; return an XLinked element.
     * Custom transformation can be create on a per schema basis.
     * 
     * @param id             Keyword identifier or list of keyword identifiers comma
     *                       separated. (required)
     * @param thesaurus      Thesaurus to look info for the keyword(s). (required)
     * @param lang           Languages. (optional)
     * @param keywordOnly    Only print the keyword, no thesaurus information.
     *                       (optional, default to false)
     * @param transformation XSL template to use (ISO19139 keyword by default, see
     *                       convert.xsl). (optional)
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/registries/vocabularies/keyword?id={id}&thesaurus={thesaurus}&lang={lang}&keywordOnly={keywordOnly}&transformation={transformation}")
    @Headers({ "Accept: application/xml", })
    Object getKeywordById(@Param("id") String id, @Param("thesaurus") String thesaurus,
            @Param("lang") List<String> lang, @Param("keywordOnly") Boolean keywordOnly,
            @Param("transformation") String transformation);

    /**
     * Get keyword by id Retrieve XML representation of keyword(s) from same
     * thesaurususing different transformations. &#39;to-iso19139-keyword&#39; is
     * the default and return an ISO19139
     * snippet.&#39;to-iso19139-keyword-as-xlink&#39; return an XLinked element.
     * Custom transformation can be create on a per schema basis. Note, this is
     * equivalent to the other <code>getKeywordById</code> method, but with the
     * query parameters collected into a single Map parameter. This is convenient
     * for services with optional query parameters, especially when used with the
     * {@link GetKeywordByIdQueryParams} class that allows for building up this map
     * in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>id - Keyword identifier or list of keyword identifiers
     *                    comma separated. (required)</li>
     *                    <li>thesaurus - Thesaurus to look info for the keyword(s).
     *                    (required)</li>
     *                    <li>lang - Languages. (optional)</li>
     *                    <li>keywordOnly - Only print the keyword, no thesaurus
     *                    information. (optional, default to false)</li>
     *                    <li>transformation - XSL template to use (ISO19139 keyword
     *                    by default, see convert.xsl). (optional)</li>
     *                    </ul>
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/registries/vocabularies/keyword?id={id}&thesaurus={thesaurus}&lang={lang}&keywordOnly={keywordOnly}&transformation={transformation}")
    @Headers({ "Accept: application/xml", })
    Object getKeywordById(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getKeywordById</code> method in a fluent style.
     */
    public static class GetKeywordByIdQueryParams extends HashMap<String, Object> {
        public GetKeywordByIdQueryParams id(final String value) {
            put("id", EncodingUtils.encode(value));
            return this;
        }

        public GetKeywordByIdQueryParams thesaurus(final String value) {
            put("thesaurus", EncodingUtils.encode(value));
            return this;
        }

        public GetKeywordByIdQueryParams lang(final List<String> value) {
            put("lang", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public GetKeywordByIdQueryParams keywordOnly(final Boolean value) {
            put("keywordOnly", EncodingUtils.encode(value));
            return this;
        }

        public GetKeywordByIdQueryParams transformation(final String value) {
            put("transformation", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Download a thesaurus by name Download the thesaurus in SKOS format.
     * 
     * @param thesaurus Thesaurus to download. (required)
     */
    @RequestLine("GET /srv/api/0.1/registries/vocabularies/{thesaurus}")
    @Headers({ "Accept: text/xml", })
    void getThesaurus(@Param("thesaurus") String thesaurus);

    /**
     * Import spatial directory entries Directory entry (AKA subtemplates) are XML
     * fragments that can be inserted in metadata records. Use this service to
     * import geographic extent entries from an ESRI Shapefile format.
     * 
     * @param file                 The ZIP file to upload containing the Shapefile.
     *                             (required)
     * @param uuidAttribute        Attribute to use for UUID. If none, random UUID
     *                             are generated. (optional)
     * @param uuidPattern          Pattern to build UUID from. Default is
     *                             &#39;{{uuid}}&#39;. (optional, default to
     *                             {{uuid}})
     * @param descriptionAttribute Attribute to use for extent description. If none,
     *                             no extent description defined. TODO: Add per
     *                             language desc ? (optional)
     * @param geomProjectionTo     geomProjectionTo (optional)
     * @param lenient              lenient (optional, default to false)
     * @param onlyBoundingBox      Create only bounding box for each spatial
     *                             objects. (optional, default to true)
     * @param process              Process (optional, default to
     *                             build-extent-subtemplate)
     * @param schema               Schema identifier (optional, default to iso19139)
     * @param uuidProcessing       Record identifier processing. (optional, default
     *                             to NOTHING)
     * @param group                The group the record is attached to. (optional)
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("POST /srv/api/0.1/registries/actions/entries/import/spatial?uuidAttribute={uuidAttribute}&uuidPattern={uuidPattern}&descriptionAttribute={descriptionAttribute}&geomProjectionTo={geomProjectionTo}&lenient={lenient}&onlyBoundingBox={onlyBoundingBox}&process={process}&schema={schema}&uuidProcessing={uuidProcessing}&group={group}")
    @Headers({ "Content-Type: multipart/form-data", "Accept: application/json", })
    SimpleMetadataProcessingReport importSpatialEntries(@Param("file") File file,
            @Param("uuidAttribute") String uuidAttribute, @Param("uuidPattern") String uuidPattern,
            @Param("descriptionAttribute") String descriptionAttribute,
            @Param("geomProjectionTo") String geomProjectionTo, @Param("lenient") Boolean lenient,
            @Param("onlyBoundingBox") Boolean onlyBoundingBox, @Param("process") String process,
            @Param("schema") String schema, @Param("uuidProcessing") String uuidProcessing,
            @Param("group") Integer group);

    /**
     * Import spatial directory entries Directory entry (AKA subtemplates) are XML
     * fragments that can be inserted in metadata records. Use this service to
     * import geographic extent entries from an ESRI Shapefile format. Note, this is
     * equivalent to the other <code>importSpatialEntries</code> method, but with
     * the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link ImportSpatialEntriesQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param file        The ZIP file to upload containing the Shapefile.
     *                    (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>uuidAttribute - Attribute to use for UUID. If none,
     *                    random UUID are generated. (optional)</li>
     *                    <li>uuidPattern - Pattern to build UUID from. Default is
     *                    &#39;{{uuid}}&#39;. (optional, default to {{uuid}})</li>
     *                    <li>descriptionAttribute - Attribute to use for extent
     *                    description. If none, no extent description defined. TODO:
     *                    Add per language desc ? (optional)</li>
     *                    <li>geomProjectionTo - geomProjectionTo (optional)</li>
     *                    <li>lenient - lenient (optional, default to false)</li>
     *                    <li>onlyBoundingBox - Create only bounding box for each
     *                    spatial objects. (optional, default to true)</li>
     *                    <li>process - Process (optional, default to
     *                    build-extent-subtemplate)</li>
     *                    <li>schema - Schema identifier (optional, default to
     *                    iso19139)</li>
     *                    <li>uuidProcessing - Record identifier processing.
     *                    (optional, default to NOTHING)</li>
     *                    <li>group - The group the record is attached to.
     *                    (optional)</li>
     *                    </ul>
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("POST /srv/api/0.1/registries/actions/entries/import/spatial?uuidAttribute={uuidAttribute}&uuidPattern={uuidPattern}&descriptionAttribute={descriptionAttribute}&geomProjectionTo={geomProjectionTo}&lenient={lenient}&onlyBoundingBox={onlyBoundingBox}&process={process}&schema={schema}&uuidProcessing={uuidProcessing}&group={group}")
    @Headers({ "Content-Type: multipart/form-data", "Accept: application/json", })
    SimpleMetadataProcessingReport importSpatialEntries(@Param("file") File file,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>importSpatialEntries</code> method in a fluent style.
     */
    public static class ImportSpatialEntriesQueryParams extends HashMap<String, Object> {
        public ImportSpatialEntriesQueryParams uuidAttribute(final String value) {
            put("uuidAttribute", EncodingUtils.encode(value));
            return this;
        }

        public ImportSpatialEntriesQueryParams uuidPattern(final String value) {
            put("uuidPattern", EncodingUtils.encode(value));
            return this;
        }

        public ImportSpatialEntriesQueryParams descriptionAttribute(final String value) {
            put("descriptionAttribute", EncodingUtils.encode(value));
            return this;
        }

        public ImportSpatialEntriesQueryParams geomProjectionTo(final String value) {
            put("geomProjectionTo", EncodingUtils.encode(value));
            return this;
        }

        public ImportSpatialEntriesQueryParams lenient(final Boolean value) {
            put("lenient", EncodingUtils.encode(value));
            return this;
        }

        public ImportSpatialEntriesQueryParams onlyBoundingBox(final Boolean value) {
            put("onlyBoundingBox", EncodingUtils.encode(value));
            return this;
        }

        public ImportSpatialEntriesQueryParams process(final String value) {
            put("process", EncodingUtils.encode(value));
            return this;
        }

        public ImportSpatialEntriesQueryParams schema(final String value) {
            put("schema", EncodingUtils.encode(value));
            return this;
        }

        public ImportSpatialEntriesQueryParams uuidProcessing(final String value) {
            put("uuidProcessing", EncodingUtils.encode(value));
            return this;
        }

        public ImportSpatialEntriesQueryParams group(final Integer value) {
            put("group", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Preview directory entries extracted from records Scan one or more records for
     * element matching the XPath provided and save them as directory entries (ie.
     * subtemplate).&lt;br/&gt;&lt;br/&gt;Only records that the current user can
     * edit are analyzed.
     * 
     * @param xpath           XPath of the elements to extract as entry. (required)
     * @param uuids           Record UUIDs. If null current selection is used.
     *                        (optional)
     * @param bucket          Selection bucket name (optional)
     * @param identifierXpath XPath of the element identifier. If not defined a
     *                        random UUID is generated and analysis will not check
     *                        for duplicates. (optional)
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/registries/actions/entries/collect?uuids={uuids}&bucket={bucket}&xpath={xpath}&identifierXpath={identifierXpath}")
    @Headers({ "Accept: application/xml", })
    Object previewExtractedEntries(@Param("xpath") String xpath, @Param("uuids") List<String> uuids,
            @Param("bucket") String bucket, @Param("identifierXpath") String identifierXpath);

    /**
     * Preview directory entries extracted from records Scan one or more records for
     * element matching the XPath provided and save them as directory entries (ie.
     * subtemplate).&lt;br/&gt;&lt;br/&gt;Only records that the current user can
     * edit are analyzed. Note, this is equivalent to the other
     * <code>previewExtractedEntries</code> method, but with the query parameters
     * collected into a single Map parameter. This is convenient for services with
     * optional query parameters, especially when used with the
     * {@link PreviewExtractedEntriesQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>uuids - Record UUIDs. If null current selection is
     *                    used. (optional)</li>
     *                    <li>bucket - Selection bucket name (optional)</li>
     *                    <li>xpath - XPath of the elements to extract as entry.
     *                    (required)</li>
     *                    <li>identifierXpath - XPath of the element identifier. If
     *                    not defined a random UUID is generated and analysis will
     *                    not check for duplicates. (optional)</li>
     *                    </ul>
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/registries/actions/entries/collect?uuids={uuids}&bucket={bucket}&xpath={xpath}&identifierXpath={identifierXpath}")
    @Headers({ "Accept: application/xml", })
    Object previewExtractedEntries(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>previewExtractedEntries</code> method in a fluent style.
     */
    public static class PreviewExtractedEntriesQueryParams extends HashMap<String, Object> {
        public PreviewExtractedEntriesQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public PreviewExtractedEntriesQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public PreviewExtractedEntriesQueryParams xpath(final String value) {
            put("xpath", EncodingUtils.encode(value));
            return this;
        }

        public PreviewExtractedEntriesQueryParams identifierXpath(final String value) {
            put("identifierXpath", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Preview updated matching entries in records Scan one or more records for
     * element matching the XPath provided and then check if this element is
     * available in the directory. If Found, the element from the directory update
     * the element in the record and optionally properties are
     * preserved.&lt;br/&gt;&lt;br/&gt;The identifier XPath is used to find a match.
     * An optional filtercan be added to restrict search to a subset of the
     * directory. If no identifier XPaths is provided, the UUID is based on the
     * content of the snippet (hash). It is recommended to use an identifier for
     * better matching (eg. ISO19139 contact with different roles will not match on
     * the automatic UUID mode).
     * 
     * @param xpath             XPath of the elements to extract as entry.
     *                          (required)
     * @param uuids             Record UUIDs. If null current selection is used.
     *                          (optional)
     * @param bucket            Selection bucket name (optional)
     * @param identifierXpath   XPath of the element identifier. If not defined a
     *                          random UUID is generated and analysis will not check
     *                          for duplicates. (optional)
     * @param propertiesToCopy  List of XPath of properties to copy from record to
     *                          matching entry. (optional)
     * @param substituteAsXLink Replace entry by XLink. (optional, default to false)
     * @param fq                Filter query for directory search. (optional)
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/registries/actions/entries/synchronize?uuids={uuids}&bucket={bucket}&xpath={xpath}&identifierXpath={identifierXpath}&propertiesToCopy={propertiesToCopy}&substituteAsXLink={substituteAsXLink}&fq={fq}")
    @Headers({ "Accept: application/xml", })
    Object previewUpdatedRecordEntries(@Param("xpath") String xpath, @Param("uuids") List<String> uuids,
            @Param("bucket") String bucket, @Param("identifierXpath") String identifierXpath,
            @Param("propertiesToCopy") List<String> propertiesToCopy,
            @Param("substituteAsXLink") Boolean substituteAsXLink, @Param("fq") String fq);

    /**
     * Preview updated matching entries in records Scan one or more records for
     * element matching the XPath provided and then check if this element is
     * available in the directory. If Found, the element from the directory update
     * the element in the record and optionally properties are
     * preserved.&lt;br/&gt;&lt;br/&gt;The identifier XPath is used to find a match.
     * An optional filtercan be added to restrict search to a subset of the
     * directory. If no identifier XPaths is provided, the UUID is based on the
     * content of the snippet (hash). It is recommended to use an identifier for
     * better matching (eg. ISO19139 contact with different roles will not match on
     * the automatic UUID mode). Note, this is equivalent to the other
     * <code>previewUpdatedRecordEntries</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link PreviewUpdatedRecordEntriesQueryParams} class that allows for building
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
     *                    <li>xpath - XPath of the elements to extract as entry.
     *                    (required)</li>
     *                    <li>identifierXpath - XPath of the element identifier. If
     *                    not defined a random UUID is generated and analysis will
     *                    not check for duplicates. (optional)</li>
     *                    <li>propertiesToCopy - List of XPath of properties to copy
     *                    from record to matching entry. (optional)</li>
     *                    <li>substituteAsXLink - Replace entry by XLink. (optional,
     *                    default to false)</li>
     *                    <li>fq - Filter query for directory search.
     *                    (optional)</li>
     *                    </ul>
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/registries/actions/entries/synchronize?uuids={uuids}&bucket={bucket}&xpath={xpath}&identifierXpath={identifierXpath}&propertiesToCopy={propertiesToCopy}&substituteAsXLink={substituteAsXLink}&fq={fq}")
    @Headers({ "Accept: application/xml", })
    Object previewUpdatedRecordEntries(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>previewUpdatedRecordEntries</code> method in a fluent style.
     */
    public static class PreviewUpdatedRecordEntriesQueryParams extends HashMap<String, Object> {
        public PreviewUpdatedRecordEntriesQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public PreviewUpdatedRecordEntriesQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public PreviewUpdatedRecordEntriesQueryParams xpath(final String value) {
            put("xpath", EncodingUtils.encode(value));
            return this;
        }

        public PreviewUpdatedRecordEntriesQueryParams identifierXpath(final String value) {
            put("identifierXpath", EncodingUtils.encode(value));
            return this;
        }

        public PreviewUpdatedRecordEntriesQueryParams propertiesToCopy(final List<String> value) {
            put("propertiesToCopy", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public PreviewUpdatedRecordEntriesQueryParams substituteAsXLink(final Boolean value) {
            put("substituteAsXLink", EncodingUtils.encode(value));
            return this;
        }

        public PreviewUpdatedRecordEntriesQueryParams fq(final String value) {
            put("fq", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Search coordinate reference system (CRS) Based on GeoTools EPSG database. If
     * phrase query, each words are searched separately.
     * 
     * @param q    Search value (optional)
     * @param type Type of CRS (optional)
     * @param rows Number of results. Default is: 100 (optional, default to 100)
     * @return List&lt;Crs&gt;
     */
    @RequestLine("GET /srv/api/0.1/registries/crs?q={q}&type={type}&rows={rows}")
    @Headers({ "Accept: application/json", })
    List<Crs> searchCrs(@Param("q") String q, @Param("type") String type, @Param("rows") Integer rows);

    /**
     * Search coordinate reference system (CRS) Based on GeoTools EPSG database. If
     * phrase query, each words are searched separately. Note, this is equivalent to
     * the other <code>searchCrs</code> method, but with the query parameters
     * collected into a single Map parameter. This is convenient for services with
     * optional query parameters, especially when used with the
     * {@link SearchCrsQueryParams} class that allows for building up this map in a
     * fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>q - Search value (optional)</li>
     *                    <li>type - Type of CRS (optional)</li>
     *                    <li>rows - Number of results. Default is: 100 (optional,
     *                    default to 100)</li>
     *                    </ul>
     * @return List&lt;Crs&gt;
     */
    @RequestLine("GET /srv/api/0.1/registries/crs?q={q}&type={type}&rows={rows}")
    @Headers({ "Accept: application/json", })
    List<Crs> searchCrs(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>searchCrs</code> method in a fluent style.
     */
    public static class SearchCrsQueryParams extends HashMap<String, Object> {
        public SearchCrsQueryParams q(final String value) {
            put("q", EncodingUtils.encode(value));
            return this;
        }

        public SearchCrsQueryParams type(final String value) {
            put("type", EncodingUtils.encode(value));
            return this;
        }

        public SearchCrsQueryParams rows(final Integer value) {
            put("rows", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Search keywords
     * 
     * @param q         Query (optional)
     * @param lang      Query in that language (optional, default to eng)
     * @param rows      Number of rows (optional, default to 1000)
     * @param start     Start from (optional, default to 0)
     * @param pLang     Return keyword information in one or more languages
     *                  (optional)
     * @param thesaurus Thesaurus identifier (optional)
     * @param type      Type of search (optional, default to CONTAINS)
     * @param uri       URI query (optional)
     * @param sort      Sort by (optional, default to DESC)
     * @return List&lt;KeywordBean&gt;
     */
    @RequestLine("GET /srv/api/0.1/registries/vocabularies/search?q={q}&lang={lang}&rows={rows}&start={start}&pLang={pLang}&thesaurus={thesaurus}&type={type}&uri={uri}&sort={sort}")
    @Headers({ "Accept: application/json", })
    List<KeywordBean> searchKeywords(@Param("q") String q, @Param("lang") String lang, @Param("rows") Integer rows,
            @Param("start") Integer start, @Param("pLang") List<String> pLang,
            @Param("thesaurus") List<String> thesaurus, @Param("type") String type, @Param("uri") String uri,
            @Param("sort") String sort);

    /**
     * Search keywords
     * 
     * Note, this is equivalent to the other <code>searchKeywords</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link SearchKeywordsQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>q - Query (optional)</li>
     *                    <li>lang - Query in that language (optional, default to
     *                    eng)</li>
     *                    <li>rows - Number of rows (optional, default to 1000)</li>
     *                    <li>start - Start from (optional, default to 0)</li>
     *                    <li>pLang - Return keyword information in one or more
     *                    languages (optional)</li>
     *                    <li>thesaurus - Thesaurus identifier (optional)</li>
     *                    <li>type - Type of search (optional, default to
     *                    CONTAINS)</li>
     *                    <li>uri - URI query (optional)</li>
     *                    <li>sort - Sort by (optional, default to DESC)</li>
     *                    </ul>
     * @return List&lt;KeywordBean&gt;
     */
    @RequestLine("GET /srv/api/0.1/registries/vocabularies/search?q={q}&lang={lang}&rows={rows}&start={start}&pLang={pLang}&thesaurus={thesaurus}&type={type}&uri={uri}&sort={sort}")
    @Headers({ "Accept: application/json", })
    List<KeywordBean> searchKeywords(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>searchKeywords</code> method in a fluent style.
     */
    public static class SearchKeywordsQueryParams extends HashMap<String, Object> {
        public SearchKeywordsQueryParams q(final String value) {
            put("q", EncodingUtils.encode(value));
            return this;
        }

        public SearchKeywordsQueryParams lang(final String value) {
            put("lang", EncodingUtils.encode(value));
            return this;
        }

        public SearchKeywordsQueryParams rows(final Integer value) {
            put("rows", EncodingUtils.encode(value));
            return this;
        }

        public SearchKeywordsQueryParams start(final Integer value) {
            put("start", EncodingUtils.encode(value));
            return this;
        }

        public SearchKeywordsQueryParams pLang(final List<String> value) {
            put("pLang", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public SearchKeywordsQueryParams thesaurus(final List<String> value) {
            put("thesaurus", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public SearchKeywordsQueryParams type(final String value) {
            put("type", EncodingUtils.encode(value));
            return this;
        }

        public SearchKeywordsQueryParams uri(final String value) {
            put("uri", EncodingUtils.encode(value));
            return this;
        }

        public SearchKeywordsQueryParams sort(final String value) {
            put("sort", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Update matching entries in records Scan one or more records for element
     * matching the XPath provided and then check if this element is available in
     * the directory. If Found, the element from the directory update the element in
     * the record and optionally properties are preserved.&lt;br/&gt;&lt;br/&gt;The
     * identifier XPath is used to find a match. An optional filtercan be added to
     * restrict search to a subset of the directory. If no identifier XPaths is
     * provided, the UUID is based on the content of the snippet (hash). It is
     * recommended to use an identifier for better matching (eg. ISO19139 contact
     * with different roles will not match on the automatic UUID mode).
     * 
     * @param xpath             XPath of the elements to extract as entry.
     *                          (required)
     * @param uuids             Record UUIDs. If null current selection is used.
     *                          (optional)
     * @param bucket            Selection bucket name (optional)
     * @param identifierXpath   XPath of the element identifier. If not defined a
     *                          random UUID is generated and analysis will not check
     *                          for duplicates. (optional)
     * @param propertiesToCopy  List of XPath of properties to copy from record to
     *                          matching entry. (optional)
     * @param substituteAsXLink Replace entry by XLink. (optional, default to false)
     * @param fq                Filter query for directory search. (optional)
     * @return Object
     */
    @RequestLine("PUT /srv/api/0.1/registries/actions/entries/synchronize?uuids={uuids}&bucket={bucket}&xpath={xpath}&identifierXpath={identifierXpath}&propertiesToCopy={propertiesToCopy}&substituteAsXLink={substituteAsXLink}&fq={fq}")
    @Headers({ "Accept: application/json", })
    Object updateRecordEntries(@Param("xpath") String xpath, @Param("uuids") List<String> uuids,
            @Param("bucket") String bucket, @Param("identifierXpath") String identifierXpath,
            @Param("propertiesToCopy") List<String> propertiesToCopy,
            @Param("substituteAsXLink") Boolean substituteAsXLink, @Param("fq") String fq);

    /**
     * Update matching entries in records Scan one or more records for element
     * matching the XPath provided and then check if this element is available in
     * the directory. If Found, the element from the directory update the element in
     * the record and optionally properties are preserved.&lt;br/&gt;&lt;br/&gt;The
     * identifier XPath is used to find a match. An optional filtercan be added to
     * restrict search to a subset of the directory. If no identifier XPaths is
     * provided, the UUID is based on the content of the snippet (hash). It is
     * recommended to use an identifier for better matching (eg. ISO19139 contact
     * with different roles will not match on the automatic UUID mode). Note, this
     * is equivalent to the other <code>updateRecordEntries</code> method, but with
     * the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link UpdateRecordEntriesQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>uuids - Record UUIDs. If null current selection is
     *                    used. (optional)</li>
     *                    <li>bucket - Selection bucket name (optional)</li>
     *                    <li>xpath - XPath of the elements to extract as entry.
     *                    (required)</li>
     *                    <li>identifierXpath - XPath of the element identifier. If
     *                    not defined a random UUID is generated and analysis will
     *                    not check for duplicates. (optional)</li>
     *                    <li>propertiesToCopy - List of XPath of properties to copy
     *                    from record to matching entry. (optional)</li>
     *                    <li>substituteAsXLink - Replace entry by XLink. (optional,
     *                    default to false)</li>
     *                    <li>fq - Filter query for directory search.
     *                    (optional)</li>
     *                    </ul>
     * @return Object
     */
    @RequestLine("PUT /srv/api/0.1/registries/actions/entries/synchronize?uuids={uuids}&bucket={bucket}&xpath={xpath}&identifierXpath={identifierXpath}&propertiesToCopy={propertiesToCopy}&substituteAsXLink={substituteAsXLink}&fq={fq}")
    @Headers({ "Accept: application/json", })
    Object updateRecordEntries(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>updateRecordEntries</code> method in a fluent style.
     */
    public static class UpdateRecordEntriesQueryParams extends HashMap<String, Object> {
        public UpdateRecordEntriesQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public UpdateRecordEntriesQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public UpdateRecordEntriesQueryParams xpath(final String value) {
            put("xpath", EncodingUtils.encode(value));
            return this;
        }

        public UpdateRecordEntriesQueryParams identifierXpath(final String value) {
            put("identifierXpath", EncodingUtils.encode(value));
            return this;
        }

        public UpdateRecordEntriesQueryParams propertiesToCopy(final List<String> value) {
            put("propertiesToCopy", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public UpdateRecordEntriesQueryParams substituteAsXLink(final Boolean value) {
            put("substituteAsXLink", EncodingUtils.encode(value));
            return this;
        }

        public UpdateRecordEntriesQueryParams fq(final String value) {
            put("fq", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Uploads a new thesaurus from a file Uploads a new thesaurus.
     * 
     * @param file       If set, do a file upload. (optional)
     * @param type       Local or external (default). (optional, default to
     *                   external)
     * @param dir        Type of thesaurus, usually one of the ISO thesaurus type
     *                   codelist value. Default is theme. (optional, default to
     *                   theme)
     * @param stylesheet XSL to be use to convert the thesaurus before load. Default
     *                   _none_. (optional, default to _none_)
     * @return String
     */
    @RequestLine("POST /srv/api/0.1/registries/vocabularies?type={type}&dir={dir}&stylesheet={stylesheet}")
    @Headers({ "Content-Type: multipart/form-data", "Accept: text/xml", })
    String uploadThesaurus(@Param("file") File file, @Param("type") String type, @Param("dir") String dir,
            @Param("stylesheet") String stylesheet);

    /**
     * Uploads a new thesaurus from a file Uploads a new thesaurus. Note, this is
     * equivalent to the other <code>uploadThesaurus</code> method, but with the
     * query parameters collected into a single Map parameter. This is convenient
     * for services with optional query parameters, especially when used with the
     * {@link UploadThesaurusQueryParams} class that allows for building up this map
     * in a fluent style.
     * 
     * @param file        If set, do a file upload. (optional)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>type - Local or external (default). (optional, default
     *                    to external)</li>
     *                    <li>dir - Type of thesaurus, usually one of the ISO
     *                    thesaurus type codelist value. Default is theme.
     *                    (optional, default to theme)</li>
     *                    <li>stylesheet - XSL to be use to convert the thesaurus
     *                    before load. Default _none_. (optional, default to
     *                    _none_)</li>
     *                    </ul>
     * @return String
     */
    @RequestLine("POST /srv/api/0.1/registries/vocabularies?type={type}&dir={dir}&stylesheet={stylesheet}")
    @Headers({ "Content-Type: multipart/form-data", "Accept: text/xml", })
    String uploadThesaurus(@Param("file") File file, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>uploadThesaurus</code> method in a fluent style.
     */
    public static class UploadThesaurusQueryParams extends HashMap<String, Object> {
        public UploadThesaurusQueryParams type(final String value) {
            put("type", EncodingUtils.encode(value));
            return this;
        }

        public UploadThesaurusQueryParams dir(final String value) {
            put("dir", EncodingUtils.encode(value));
            return this;
        }

        public UploadThesaurusQueryParams stylesheet(final String value) {
            put("stylesheet", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Uploads a new thesaurus from URL or Registry Uploads a new thesaurus.
     * 
     * @param url              If set, try to download from the Internet. (optional)
     * @param registryUrl      If set, try to download from a registry. (optional)
     * @param registryLanguage Languages to download from a registry. (optional)
     * @param type             Local or external (default). (optional, default to
     *                         external)
     * @param dir              Type of thesaurus, usually one of the ISO thesaurus
     *                         type codelist value. Default is theme. (optional,
     *                         default to theme)
     * @param stylesheet       XSL to be use to convert the thesaurus before load.
     *                         Default _none_. (optional, default to _none_)
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/registries/vocabularies?url={url}&registryUrl={registryUrl}&registryLanguage={registryLanguage}&type={type}&dir={dir}&stylesheet={stylesheet}")
    @Headers({ "Accept: text/xml", })
    String uploadThesaurusFromUrl(@Param("url") String url, @Param("registryUrl") String registryUrl,
            @Param("registryLanguage") List<String> registryLanguage, @Param("type") String type,
            @Param("dir") String dir, @Param("stylesheet") String stylesheet);

    /**
     * Uploads a new thesaurus from URL or Registry Uploads a new thesaurus. Note,
     * this is equivalent to the other <code>uploadThesaurusFromUrl</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link UploadThesaurusFromUrlQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>url - If set, try to download from the Internet.
     *                    (optional)</li>
     *                    <li>registryUrl - If set, try to download from a registry.
     *                    (optional)</li>
     *                    <li>registryLanguage - Languages to download from a
     *                    registry. (optional)</li>
     *                    <li>type - Local or external (default). (optional, default
     *                    to external)</li>
     *                    <li>dir - Type of thesaurus, usually one of the ISO
     *                    thesaurus type codelist value. Default is theme.
     *                    (optional, default to theme)</li>
     *                    <li>stylesheet - XSL to be use to convert the thesaurus
     *                    before load. Default _none_. (optional, default to
     *                    _none_)</li>
     *                    </ul>
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/registries/vocabularies?url={url}&registryUrl={registryUrl}&registryLanguage={registryLanguage}&type={type}&dir={dir}&stylesheet={stylesheet}")
    @Headers({ "Accept: text/xml", })
    String uploadThesaurusFromUrl(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>uploadThesaurusFromUrl</code> method in a fluent style.
     */
    public static class UploadThesaurusFromUrlQueryParams extends HashMap<String, Object> {
        public UploadThesaurusFromUrlQueryParams url(final String value) {
            put("url", EncodingUtils.encode(value));
            return this;
        }

        public UploadThesaurusFromUrlQueryParams registryUrl(final String value) {
            put("registryUrl", EncodingUtils.encode(value));
            return this;
        }

        public UploadThesaurusFromUrlQueryParams registryLanguage(final List<String> value) {
            put("registryLanguage", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public UploadThesaurusFromUrlQueryParams type(final String value) {
            put("type", EncodingUtils.encode(value));
            return this;
        }

        public UploadThesaurusFromUrlQueryParams dir(final String value) {
            put("dir", EncodingUtils.encode(value));
            return this;
        }

        public UploadThesaurusFromUrlQueryParams stylesheet(final String value) {
            put("stylesheet", EncodingUtils.encode(value));
            return this;
        }
    }
}
