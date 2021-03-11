package org.fao.geonet.openapi;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.BatchEditParameter;
import org.fao.geonet.openapi.model.Element;
import org.fao.geonet.openapi.model.FeatureResponse;
import org.fao.geonet.openapi.model.FileSystemResource;
import org.fao.geonet.openapi.model.IProcessingReport;
import org.fao.geonet.openapi.model.MetadataCategory;
import org.fao.geonet.openapi.model.MetadataProcessingReport;
import org.fao.geonet.openapi.model.MetadataResource;
import org.fao.geonet.openapi.model.MetadataStatusParameter;
import org.fao.geonet.openapi.model.MetadataStatusResponse;
import org.fao.geonet.openapi.model.MetadataWorkflowStatusResponse;
import org.fao.geonet.openapi.model.RelatedResponse;
import org.fao.geonet.openapi.model.Reports;
import org.fao.geonet.openapi.model.ResponseEntity;
import org.fao.geonet.openapi.model.SavedQuery;
import org.fao.geonet.openapi.model.SharingParameter;
import org.fao.geonet.openapi.model.SharingResponse;
import org.fao.geonet.openapi.model.SimpleMetadataProcessingReport;
import org.fao.geonet.openapi.model.SuggestionType;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface RecordsApi extends ApiClient.Api {

    /**
     * Add element
     * 
     * @param metadataUuid      Record UUID. (required)
     * @param ref               Reference of the insertion point. (required)
     * @param name              Name of the element to add (with prefix) (required)
     * @param child             Use geonet:attribute for attributes or child name.
     *                          (optional)
     * @param displayAttributes Should attributes be shown on the editor snippet?
     *                          (optional, default to false)
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/editor/elements?ref={ref}&name={name}&child={child}&displayAttributes={displayAttributes}")
    @Headers({ "Accept: application/xml", })
    void addElement(@Param("metadataUuid") String metadataUuid, @Param("ref") String ref, @Param("name") String name,
            @Param("child") String child, @Param("displayAttributes") Boolean displayAttributes);

    /**
     * Add element
     * 
     * Note, this is equivalent to the other <code>addElement</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link AddElementQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>ref - Reference of the insertion point.
     *                     (required)</li>
     *                     <li>name - Name of the element to add (with prefix)
     *                     (required)</li>
     *                     <li>child - Use geonet:attribute for attributes or child
     *                     name. (optional)</li>
     *                     <li>displayAttributes - Should attributes be shown on the
     *                     editor snippet? (optional, default to false)</li>
     *                     </ul>
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/editor/elements?ref={ref}&name={name}&child={child}&displayAttributes={displayAttributes}")
    @Headers({ "Accept: application/xml", })
    void addElement(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>addElement</code> method in a fluent style.
     */
    public static class AddElementQueryParams extends HashMap<String, Object> {
        public AddElementQueryParams ref(final String value) {
            put("ref", EncodingUtils.encode(value));
            return this;
        }

        public AddElementQueryParams name(final String value) {
            put("name", EncodingUtils.encode(value));
            return this;
        }

        public AddElementQueryParams child(final String value) {
            put("child", EncodingUtils.encode(value));
            return this;
        }

        public AddElementQueryParams displayAttributes(final Boolean value) {
            put("displayAttributes", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Add samples Add sample records for one or more schemas. Samples are defined
     * for each standard in the samples folder as MEF files.
     * 
     * @param schema Schema identifiers (required)
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/samples?schema={schema}")
    @Headers({ "Accept: */*", })
    SimpleMetadataProcessingReport addSamples(@Param("schema") List<String> schema);

    /**
     * Add samples Add sample records for one or more schemas. Samples are defined
     * for each standard in the samples folder as MEF files. Note, this is
     * equivalent to the other <code>addSamples</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link AddSamplesQueryParams} class that allows for building up this map in a
     * fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>schema - Schema identifiers (required)</li>
     *                    </ul>
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/samples?schema={schema}")
    @Headers({ "Accept: */*", })
    SimpleMetadataProcessingReport addSamples(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>addSamples</code> method in a fluent style.
     */
    public static class AddSamplesQueryParams extends HashMap<String, Object> {
        public AddSamplesQueryParams schema(final List<String> value) {
            put("schema", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Add tags to a record
     * 
     * @param metadataUuid Record UUID. (required)
     * @param id           Tag identifier (required)
     * @param clear        Clear all before adding new ones (optional, default to
     *                     false)
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/tags?id={id}&clear={clear}")
    @Headers({ "Accept: */*", })
    void addTagsToRecord(@Param("metadataUuid") String metadataUuid, @Param("id") List<Integer> id,
            @Param("clear") Boolean clear);

    /**
     * Add tags to a record
     * 
     * Note, this is equivalent to the other <code>addTagsToRecord</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link AddTagsToRecordQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>id - Tag identifier (required)</li>
     *                     <li>clear - Clear all before adding new ones (optional,
     *                     default to false)</li>
     *                     </ul>
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/tags?id={id}&clear={clear}")
    @Headers({ "Accept: */*", })
    void addTagsToRecord(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>addTagsToRecord</code> method in a fluent style.
     */
    public static class AddTagsToRecordQueryParams extends HashMap<String, Object> {
        public AddTagsToRecordQueryParams id(final List<Integer> value) {
            put("id", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public AddTagsToRecordQueryParams clear(final Boolean value) {
            put("clear", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Add tags to one or more records
     * 
     * @param id     Tag identifier (required)
     * @param uuids  Record UUIDs. If null current selection is used. (optional)
     * @param bucket Selection bucket name (optional)
     * @param clear  Clear all before adding new ones (optional, default to false)
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/tags?uuids={uuids}&bucket={bucket}&id={id}&clear={clear}")
    @Headers({ "Accept: application/json", })
    MetadataProcessingReport addTagsToRecords(@Param("id") List<Integer> id, @Param("uuids") List<String> uuids,
            @Param("bucket") String bucket, @Param("clear") Boolean clear);

    /**
     * Add tags to one or more records
     * 
     * Note, this is equivalent to the other <code>addTagsToRecords</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link AddTagsToRecordsQueryParams} class that allows for building
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
     *                    <li>id - Tag identifier (required)</li>
     *                    <li>clear - Clear all before adding new ones (optional,
     *                    default to false)</li>
     *                    </ul>
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/tags?uuids={uuids}&bucket={bucket}&id={id}&clear={clear}")
    @Headers({ "Accept: application/json", })
    MetadataProcessingReport addTagsToRecords(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>addTagsToRecords</code> method in a fluent style.
     */
    public static class AddTagsToRecordsQueryParams extends HashMap<String, Object> {
        public AddTagsToRecordsQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public AddTagsToRecordsQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public AddTagsToRecordsQueryParams id(final List<Integer> value) {
            put("id", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public AddTagsToRecordsQueryParams clear(final Boolean value) {
            put("clear", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Add templates Add template records for one or more schemas. Templates are
     * defined for each standard in the template folder as XML files. Template may
     * also contains subtemplates.
     * 
     * @param schema Schema identifiers (required)
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/templates?schema={schema}")
    @Headers({ "Accept: */*", })
    SimpleMetadataProcessingReport addTemplates(@Param("schema") List<String> schema);

    /**
     * Add templates Add template records for one or more schemas. Templates are
     * defined for each standard in the template folder as XML files. Template may
     * also contains subtemplates. Note, this is equivalent to the other
     * <code>addTemplates</code> method, but with the query parameters collected
     * into a single Map parameter. This is convenient for services with optional
     * query parameters, especially when used with the
     * {@link AddTemplatesQueryParams} class that allows for building up this map in
     * a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>schema - Schema identifiers (required)</li>
     *                    </ul>
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/templates?schema={schema}")
    @Headers({ "Accept: */*", })
    SimpleMetadataProcessingReport addTemplates(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>addTemplates</code> method in a fluent style.
     */
    public static class AddTemplatesQueryParams extends HashMap<String, Object> {
        public AddTemplatesQueryParams schema(final List<String> value) {
            put("schema", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Apply a saved query for this metadata All parameters will be substituted to
     * the XPath query. eg. {{protocol}} in the XPath expression will be replaced by
     * the protocol parameter provided in the request body.
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param savedQuery   The saved query to apply (required)
     * @param parameters   The query parameters (optional)
     * @return Map&lt;String, String&gt;
     */
    @RequestLine("POST /srv/api/0.1/records/{metadataUuid}/query/{savedQuery}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    Map<String, String> applyQuery(@Param("metadataUuid") String metadataUuid, @Param("savedQuery") String savedQuery,
            Object parameters);

    /**
     * Edit a set of records by XPath expressions. This operations applies the
     * update-fixed-info.xsl transformation for the metadata schema and updates the
     * change date if the parameter updateDateStamp is set to true.
     * 
     * @param edits           edits (required)
     * @param uuids           Record UUIDs. If null current selection is used.
     *                        (optional)
     * @param bucket          Selection bucket name (optional)
     * @param updateDateStamp If true updates the DateStamp (or equivalent in
     *                        standards different to ISO 19139) field in the
     *                        metadata with the current timestamp (optional, default
     *                        to false)
     * @return IProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/batchediting?uuids={uuids}&bucket={bucket}&updateDateStamp={updateDateStamp}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    IProcessingReport batchEdit(List<BatchEditParameter> edits, @Param("uuids") List<String> uuids,
            @Param("bucket") String bucket, @Param("updateDateStamp") Boolean updateDateStamp);

    /**
     * Edit a set of records by XPath expressions. This operations applies the
     * update-fixed-info.xsl transformation for the metadata schema and updates the
     * change date if the parameter updateDateStamp is set to true.
     * 
     * Note, this is equivalent to the other <code>batchEdit</code> method, but with
     * the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link BatchEditQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param edits       edits (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>uuids - Record UUIDs. If null current selection is
     *                    used. (optional)</li>
     *                    <li>bucket - Selection bucket name (optional)</li>
     *                    <li>updateDateStamp - If true updates the DateStamp (or
     *                    equivalent in standards different to ISO 19139) field in
     *                    the metadata with the current timestamp (optional, default
     *                    to false)</li>
     *                    </ul>
     * @return IProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/batchediting?uuids={uuids}&bucket={bucket}&updateDateStamp={updateDateStamp}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    IProcessingReport batchEdit(List<BatchEditParameter> edits,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>batchEdit</code> method in a fluent style.
     */
    public static class BatchEditQueryParams extends HashMap<String, Object> {
        public BatchEditQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public BatchEditQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public BatchEditQueryParams updateDateStamp(final Boolean value) {
            put("updateDateStamp", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Cancel edits Cancel current editing session.
     * 
     * @param metadataUuid Record UUID. (required)
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/editor")
    @Headers({ "Accept: application/xml", })
    void cancelEdits(@Param("metadataUuid") String metadataUuid);

    /**
     * Check that a record can be submitted to DataCite for DOI creation. DataCite
     * requires some fields to be populated.
     * 
     * @param metadataUuid Record UUID. (required)
     * @return Map&lt;String, Boolean&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/doi/checkPreConditions")
    @Headers({ "Accept: application/json", })
    Map<String, Boolean> checkDoiStatus(@Param("metadataUuid") String metadataUuid);

    /**
     * Check the status of validation with the INSPIRE service. User MUST be able to
     * edit the record to validate it. An INSPIRE endpoint must be configured in
     * Settings. If the process is complete an object with status is returned.
     * 
     * @param testId Test identifier (required)
     * @return Map&lt;String, String&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{testId}/validate/inspire")
    @Headers({ "Accept: application/json", })
    Map<String, String> checkValidateStatus(@Param("testId") String testId);

    /**
     * Close a record task
     * 
     * @param metadataUuid Record UUID. (required)
     * @param statusId     Status identifier (required)
     * @param userId       User identifier (required)
     * @param changeDate   Change date (required)
     * @param closeDate    Close date (required)
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/status/{statusId}{userId}{changeDate}/close?closeDate={closeDate}")
    @Headers({ "Accept: */*", })
    void closeTask(@Param("metadataUuid") String metadataUuid, @Param("statusId") Integer statusId,
            @Param("userId") Integer userId, @Param("changeDate") String changeDate,
            @Param("closeDate") String closeDate);

    /**
     * Close a record task
     * 
     * Note, this is equivalent to the other <code>closeTask</code> method, but with
     * the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link CloseTaskQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param statusId     Status identifier (required)
     * @param userId       User identifier (required)
     * @param changeDate   Change date (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>closeDate - Close date (required)</li>
     *                     </ul>
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/status/{statusId}{userId}{changeDate}/close?closeDate={closeDate}")
    @Headers({ "Accept: */*", })
    void closeTask(@Param("metadataUuid") String metadataUuid, @Param("statusId") Integer statusId,
            @Param("userId") Integer userId, @Param("changeDate") String changeDate,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>closeTask</code> method in a fluent style.
     */
    public static class CloseTaskQueryParams extends HashMap<String, Object> {
        public CloseTaskQueryParams closeDate(final String value) {
            put("closeDate", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Create a new record Create a record from a template or by copying an existing
     * record.Return the UUID of the newly created record. Existing links in the
     * source record are preserved, this means that the new record may contains link
     * to the source attachements. They need to be manually updated after creation.
     * 
     * @param sourceUuid                 UUID of the source record to copy.
     *                                   (required)
     * @param group                      The group the record is attached to.
     *                                   (required)
     * @param metadataType               The type of record. (optional, default to
     *                                   METADATA)
     * @param targetUuid                 Assign a custom UUID. If this UUID already
     *                                   exist an error is returned. This is enabled
     *                                   only if metadata create / generate UUID
     *                                   settings is activated. (optional)
     * @param isVisibleByAllGroupMembers Is published to all user group members? If
     *                                   not, only the author and administrator can
     *                                   edit the record. (optional, default to
     *                                   false)
     * @param category                   Tags to assign to the record. (optional)
     * @param hasCategoryOfSource        Copy categories from source? (optional,
     *                                   default to false)
     * @param isChildOfSource            Is child of the record to copy? (optional,
     *                                   default to false)
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/records/duplicate?metadataType={metadataType}&sourceUuid={sourceUuid}&targetUuid={targetUuid}&group={group}&isVisibleByAllGroupMembers={isVisibleByAllGroupMembers}&category={category}&hasCategoryOfSource={hasCategoryOfSource}&isChildOfSource={isChildOfSource}")
    @Headers({ "Accept: application/json", })
    String create(@Param("sourceUuid") String sourceUuid, @Param("group") String group,
            @Param("metadataType") String metadataType, @Param("targetUuid") String targetUuid,
            @Param("isVisibleByAllGroupMembers") Boolean isVisibleByAllGroupMembers,
            @Param("category") List<String> category, @Param("hasCategoryOfSource") Boolean hasCategoryOfSource,
            @Param("isChildOfSource") Boolean isChildOfSource);

    /**
     * Create a new record Create a record from a template or by copying an existing
     * record.Return the UUID of the newly created record. Existing links in the
     * source record are preserved, this means that the new record may contains link
     * to the source attachements. They need to be manually updated after creation.
     * Note, this is equivalent to the other <code>create</code> method, but with
     * the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link CreateQueryParams} class that allows for building up this map
     * in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>metadataType - The type of record. (optional, default
     *                    to METADATA)</li>
     *                    <li>sourceUuid - UUID of the source record to copy.
     *                    (required)</li>
     *                    <li>targetUuid - Assign a custom UUID. If this UUID
     *                    already exist an error is returned. This is enabled only
     *                    if metadata create / generate UUID settings is activated.
     *                    (optional)</li>
     *                    <li>group - The group the record is attached to.
     *                    (required)</li>
     *                    <li>isVisibleByAllGroupMembers - Is published to all user
     *                    group members? If not, only the author and administrator
     *                    can edit the record. (optional, default to false)</li>
     *                    <li>category - Tags to assign to the record.
     *                    (optional)</li>
     *                    <li>hasCategoryOfSource - Copy categories from source?
     *                    (optional, default to false)</li>
     *                    <li>isChildOfSource - Is child of the record to copy?
     *                    (optional, default to false)</li>
     *                    </ul>
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/records/duplicate?metadataType={metadataType}&sourceUuid={sourceUuid}&targetUuid={targetUuid}&group={group}&isVisibleByAllGroupMembers={isVisibleByAllGroupMembers}&category={category}&hasCategoryOfSource={hasCategoryOfSource}&isChildOfSource={isChildOfSource}")
    @Headers({ "Accept: application/json", })
    String create(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>create</code> method in a fluent style.
     */
    public static class CreateQueryParams extends HashMap<String, Object> {
        public CreateQueryParams metadataType(final String value) {
            put("metadataType", EncodingUtils.encode(value));
            return this;
        }

        public CreateQueryParams sourceUuid(final String value) {
            put("sourceUuid", EncodingUtils.encode(value));
            return this;
        }

        public CreateQueryParams targetUuid(final String value) {
            put("targetUuid", EncodingUtils.encode(value));
            return this;
        }

        public CreateQueryParams group(final String value) {
            put("group", EncodingUtils.encode(value));
            return this;
        }

        public CreateQueryParams isVisibleByAllGroupMembers(final Boolean value) {
            put("isVisibleByAllGroupMembers", EncodingUtils.encode(value));
            return this;
        }

        public CreateQueryParams category(final List<String> value) {
            put("category", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public CreateQueryParams hasCategoryOfSource(final Boolean value) {
            put("hasCategoryOfSource", EncodingUtils.encode(value));
            return this;
        }

        public CreateQueryParams isChildOfSource(final Boolean value) {
            put("isChildOfSource", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Submit a record to the Datacite metadata store in order to create a DOI.
     * 
     * @param metadataUuid Record UUID. (required)
     * @return Map&lt;String, String&gt;
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/doi")
    @Headers({ "Accept: application/json", })
    Map<String, String> createDoi(@Param("metadataUuid") String metadataUuid);

    /**
     * Create an overview using the map print module &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/linking-thumbnail.html#generating-a-thumbnail-using-wms-layers&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param metadataUuid  Record UUID. (required)
     * @param jsonConfig    The mapprint module JSON configuration (required)
     * @param rotationAngle The rotation angle of the map (optional, default to 0)
     * @return MetadataResource
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/attachments/print-thumbnail?jsonConfig={jsonConfig}&rotationAngle={rotationAngle}")
    @Headers({ "Accept: */*", })
    MetadataResource createMetadataOverview(@Param("metadataUuid") String metadataUuid,
            @Param("jsonConfig") String jsonConfig, @Param("rotationAngle") Integer rotationAngle);

    /**
     * Create an overview using the map print module &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/linking-thumbnail.html#generating-a-thumbnail-using-wms-layers&#39;&gt;More
     * info&lt;/a&gt; Note, this is equivalent to the other
     * <code>createMetadataOverview</code> method, but with the query parameters
     * collected into a single Map parameter. This is convenient for services with
     * optional query parameters, especially when used with the
     * {@link CreateMetadataOverviewQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>jsonConfig - The mapprint module JSON configuration
     *                     (required)</li>
     *                     <li>rotationAngle - The rotation angle of the map
     *                     (optional, default to 0)</li>
     *                     </ul>
     * @return MetadataResource
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/attachments/print-thumbnail?jsonConfig={jsonConfig}&rotationAngle={rotationAngle}")
    @Headers({ "Accept: */*", })
    MetadataResource createMetadataOverview(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>createMetadataOverview</code> method in a fluent style.
     */
    public static class CreateMetadataOverviewQueryParams extends HashMap<String, Object> {
        public CreateMetadataOverviewQueryParams jsonConfig(final String value) {
            put("jsonConfig", EncodingUtils.encode(value));
            return this;
        }

        public CreateMetadataOverviewQueryParams rotationAngle(final Integer value) {
            put("rotationAngle", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Delete all uploaded metadata resources
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param approved     Use approved version or not (optional, default to false)
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/attachments?approved={approved}")
    @Headers({ "Accept: application/json", })
    void deleteAllMetadataResources(@Param("metadataUuid") String metadataUuid, @Param("approved") Boolean approved);

    /**
     * Delete all uploaded metadata resources
     * 
     * Note, this is equivalent to the other <code>deleteAllMetadataResources</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link DeleteAllMetadataResourcesQueryParams} class that
     * allows for building up this map in a fluent style.
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>approved - Use approved version or not (optional,
     *                     default to false)</li>
     *                     </ul>
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/attachments?approved={approved}")
    @Headers({ "Accept: application/json", })
    void deleteAllMetadataResources(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deleteAllMetadataResources</code> method in a fluent style.
     */
    public static class DeleteAllMetadataResourcesQueryParams extends HashMap<String, Object> {
        public DeleteAllMetadataResourcesQueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Delete attribute
     * 
     * @param metadataUuid      Record UUID. (required)
     * @param ref               Reference of the attribute to remove. (required)
     * @param displayAttributes Should attributes be shown on the editor snippet?
     *                          (optional, default to false)
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/editor/attributes?ref={ref}&displayAttributes={displayAttributes}")
    @Headers({ "Accept: application/xml", })
    void deleteAttribute(@Param("metadataUuid") String metadataUuid, @Param("ref") String ref,
            @Param("displayAttributes") Boolean displayAttributes);

    /**
     * Delete attribute
     * 
     * Note, this is equivalent to the other <code>deleteAttribute</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link DeleteAttributeQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>ref - Reference of the attribute to remove.
     *                     (required)</li>
     *                     <li>displayAttributes - Should attributes be shown on the
     *                     editor snippet? (optional, default to false)</li>
     *                     </ul>
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/editor/attributes?ref={ref}&displayAttributes={displayAttributes}")
    @Headers({ "Accept: application/xml", })
    void deleteAttribute(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deleteAttribute</code> method in a fluent style.
     */
    public static class DeleteAttributeQueryParams extends HashMap<String, Object> {
        public DeleteAttributeQueryParams ref(final String value) {
            put("ref", EncodingUtils.encode(value));
            return this;
        }

        public DeleteAttributeQueryParams displayAttributes(final Boolean value) {
            put("displayAttributes", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Remove a DOI (this is not recommended, DOI are supposed to be persistent once
     * created. This is mainly here for testing).
     * 
     * @param metadataUuid Record UUID. (required)
     * @return ResponseEntity
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/doi")
    @Headers({ "Accept: application/json", })
    ResponseEntity deleteDoi(@Param("metadataUuid") String metadataUuid);

    /**
     * Delete element
     * 
     * @param metadataUuid      Record UUID. (required)
     * @param ref               Reference of the element to remove. (required)
     * @param parent            Name of the parent. (required)
     * @param displayAttributes Should attributes be shown on the editor snippet?
     *                          (optional, default to false)
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/editor/elements?ref={ref}&parent={parent}&displayAttributes={displayAttributes}")
    @Headers({ "Accept: application/xml", })
    void deleteElement(@Param("metadataUuid") String metadataUuid, @Param("ref") List<String> ref,
            @Param("parent") String parent, @Param("displayAttributes") Boolean displayAttributes);

    /**
     * Delete element
     * 
     * Note, this is equivalent to the other <code>deleteElement</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link DeleteElementQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>ref - Reference of the element to remove.
     *                     (required)</li>
     *                     <li>parent - Name of the parent. (required)</li>
     *                     <li>displayAttributes - Should attributes be shown on the
     *                     editor snippet? (optional, default to false)</li>
     *                     </ul>
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/editor/elements?ref={ref}&parent={parent}&displayAttributes={displayAttributes}")
    @Headers({ "Accept: application/xml", })
    void deleteElement(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deleteElement</code> method in a fluent style.
     */
    public static class DeleteElementQueryParams extends HashMap<String, Object> {
        public DeleteElementQueryParams ref(final List<String> value) {
            put("ref", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public DeleteElementQueryParams parent(final String value) {
            put("parent", EncodingUtils.encode(value));
            return this;
        }

        public DeleteElementQueryParams displayAttributes(final Boolean value) {
            put("displayAttributes", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Delete a metadata resource
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param resourceId   The resource identifier (ie. filename) (required)
     * @param approved     Use approved version or not (optional, default to false)
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/attachments/{resourceId}?approved={approved}")
    @Headers({ "Accept: */*", })
    void deleteMetadataResource(@Param("metadataUuid") String metadataUuid, @Param("resourceId") String resourceId,
            @Param("approved") Boolean approved);

    /**
     * Delete a metadata resource
     * 
     * Note, this is equivalent to the other <code>deleteMetadataResource</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link DeleteMetadataResourceQueryParams} class that
     * allows for building up this map in a fluent style.
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param resourceId   The resource identifier (ie. filename) (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>approved - Use approved version or not (optional,
     *                     default to false)</li>
     *                     </ul>
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/attachments/{resourceId}?approved={approved}")
    @Headers({ "Accept: */*", })
    void deleteMetadataResource(@Param("metadataUuid") String metadataUuid, @Param("resourceId") String resourceId,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deleteMetadataResource</code> method in a fluent style.
     */
    public static class DeleteMetadataResourceQueryParams extends HashMap<String, Object> {
        public DeleteMetadataResourceQueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Delete a record User MUST be able to edit the record to delete it. By
     * default, a backup is made in ZIP format. After that, the record attachments
     * are removed, the document removed from the index and then from the database.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param withBackup   Backup first the record as MEF in the metadata removed
     *                     folder. (optional, default to true)
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}?withBackup={withBackup}")
    @Headers({ "Accept: */*", })
    void deleteRecord(@Param("metadataUuid") String metadataUuid, @Param("withBackup") Boolean withBackup);

    /**
     * Delete a record User MUST be able to edit the record to delete it. By
     * default, a backup is made in ZIP format. After that, the record attachments
     * are removed, the document removed from the index and then from the database.
     * Note, this is equivalent to the other <code>deleteRecord</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link DeleteRecordQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>withBackup - Backup first the record as MEF in the
     *                     metadata removed folder. (optional, default to true)</li>
     *                     </ul>
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}?withBackup={withBackup}")
    @Headers({ "Accept: */*", })
    void deleteRecord(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deleteRecord</code> method in a fluent style.
     */
    public static class DeleteRecordQueryParams extends HashMap<String, Object> {
        public DeleteRecordQueryParams withBackup(final Boolean value) {
            put("withBackup", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Delete tags of a record
     * 
     * @param metadataUuid Record UUID. (required)
     * @param id           Tag identifier. If none, all tags are removed. (optional)
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/tags?id={id}")
    @Headers({ "Accept: */*", })
    void deleteRecordTags(@Param("metadataUuid") String metadataUuid, @Param("id") List<Integer> id);

    /**
     * Delete tags of a record
     * 
     * Note, this is equivalent to the other <code>deleteRecordTags</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link DeleteRecordTagsQueryParams} class that allows for building
     * up this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>id - Tag identifier. If none, all tags are removed.
     *                     (optional)</li>
     *                     </ul>
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/tags?id={id}")
    @Headers({ "Accept: */*", })
    void deleteRecordTags(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deleteRecordTags</code> method in a fluent style.
     */
    public static class DeleteRecordTagsQueryParams extends HashMap<String, Object> {
        public DeleteRecordTagsQueryParams id(final List<Integer> value) {
            put("id", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Delete one or more records User MUST be able to edit the record to delete it.
     * 
     * @param uuids      Record UUIDs. If null current selection is used. (optional)
     * @param bucket     Selection bucket name (optional)
     * @param withBackup Backup first the record as MEF in the metadata removed
     *                   folder. (optional, default to true)
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("DELETE /srv/api/0.1/records?uuids={uuids}&bucket={bucket}&withBackup={withBackup}")
    @Headers({ "Accept: application/json", })
    SimpleMetadataProcessingReport deleteRecords(@Param("uuids") List<String> uuids, @Param("bucket") String bucket,
            @Param("withBackup") Boolean withBackup);

    /**
     * Delete one or more records User MUST be able to edit the record to delete it.
     * Note, this is equivalent to the other <code>deleteRecords</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link DeleteRecordsQueryParams} class that allows for building up
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
     *                    <li>withBackup - Backup first the record as MEF in the
     *                    metadata removed folder. (optional, default to true)</li>
     *                    </ul>
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("DELETE /srv/api/0.1/records?uuids={uuids}&bucket={bucket}&withBackup={withBackup}")
    @Headers({ "Accept: application/json", })
    SimpleMetadataProcessingReport deleteRecords(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deleteRecords</code> method in a fluent style.
     */
    public static class DeleteRecordsQueryParams extends HashMap<String, Object> {
        public DeleteRecordsQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public DeleteRecordsQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public DeleteRecordsQueryParams withBackup(final Boolean value) {
            put("withBackup", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Delete tags to one or more records
     * 
     * @param uuids  Record UUIDs. If null current selection is used. (optional)
     * @param bucket Selection bucket name (optional)
     * @param id     Tag identifier (optional)
     * @return MetadataProcessingReport
     */
    @RequestLine("DELETE /srv/api/0.1/records/tags?uuids={uuids}&bucket={bucket}&id={id}")
    @Headers({ "Accept: application/json", })
    MetadataProcessingReport deleteRecordsTags(@Param("uuids") List<String> uuids, @Param("bucket") String bucket,
            @Param("id") List<Integer> id);

    /**
     * Delete tags to one or more records
     * 
     * Note, this is equivalent to the other <code>deleteRecordsTags</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link DeleteRecordsTagsQueryParams} class that allows for building
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
     *                    <li>id - Tag identifier (optional)</li>
     *                    </ul>
     * @return MetadataProcessingReport
     */
    @RequestLine("DELETE /srv/api/0.1/records/tags?uuids={uuids}&bucket={bucket}&id={id}")
    @Headers({ "Accept: application/json", })
    MetadataProcessingReport deleteRecordsTags(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deleteRecordsTags</code> method in a fluent style.
     */
    public static class DeleteRecordsTagsQueryParams extends HashMap<String, Object> {
        public DeleteRecordsTagsQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public DeleteRecordsTagsQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public DeleteRecordsTagsQueryParams id(final List<Integer> value) {
            put("id", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Delete a record status
     * 
     * @param metadataUuid Record UUID. (required)
     * @param statusId     Status identifier (required)
     * @param userId       User identifier (required)
     * @param changeDate   Change date (required)
     */
    @RequestLine("DELETE /srv/api/0.1/records/{metadataUuid}/status/{statusId}{userId}{changeDate}")
    @Headers({ "Accept: */*", })
    void deleteStatus(@Param("metadataUuid") String metadataUuid, @Param("statusId") Integer statusId,
            @Param("userId") Integer userId, @Param("changeDate") String changeDate);

    /**
     * Download MEF backup archive The backup contains all metadata not harvested
     * including templates.
     * 
     * @return FileSystemResource
     */
    @RequestLine("GET /srv/api/0.1/records/backups/latest")
    @Headers({ "Accept: application/zip", })
    FileSystemResource downloadBackup();

    /**
     * Edit a record Return HTML form for editing.
     * 
     * @param metadataUuid   Record UUID. (required)
     * @param currTab        Tab (optional, default to simple)
     * @param withAttributes withAttributes (optional, default to false)
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/editor?currTab={currTab}&withAttributes={withAttributes}")
    @Headers({ "Accept: application/xml", })
    void editor(@Param("metadataUuid") String metadataUuid, @Param("currTab") String currTab,
            @Param("withAttributes") Boolean withAttributes);

    /**
     * Edit a record Return HTML form for editing. Note, this is equivalent to the
     * other <code>editor</code> method, but with the query parameters collected
     * into a single Map parameter. This is convenient for services with optional
     * query parameters, especially when used with the {@link EditorQueryParams}
     * class that allows for building up this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>currTab - Tab (optional, default to simple)</li>
     *                     <li>withAttributes - withAttributes (optional, default to
     *                     false)</li>
     *                     </ul>
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/editor?currTab={currTab}&withAttributes={withAttributes}")
    @Headers({ "Accept: application/xml", })
    void editor(@Param("metadataUuid") String metadataUuid, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>editor</code> method in a fluent style.
     */
    public static class EditorQueryParams extends HashMap<String, Object> {
        public EditorQueryParams currTab(final String value) {
            put("currTab", EncodingUtils.encode(value));
            return this;
        }

        public EditorQueryParams withAttributes(final Boolean value) {
            put("withAttributes", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * (Experimental) Enable version control
     * 
     * @param metadataUuid Record UUID. (required)
     * @return ResponseEntity
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/versions")
    @Headers({ "Accept: */*", })
    ResponseEntity enableVersionControl(@Param("metadataUuid") String metadataUuid);

    /**
     * (Experimental) Enable version control for one or more records
     * 
     * @param uuids  Record UUIDs. If null current selection is used. (optional)
     * @param bucket Selection bucket name (optional)
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/versions?uuids={uuids}&bucket={bucket}")
    @Headers({ "Accept: application/json", })
    MetadataProcessingReport enableVersionControlForRecords(@Param("uuids") List<String> uuids,
            @Param("bucket") String bucket);

    /**
     * (Experimental) Enable version control for one or more records
     * 
     * Note, this is equivalent to the other
     * <code>enableVersionControlForRecords</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link EnableVersionControlForRecordsQueryParams} class that allows for
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
     *                    </ul>
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/versions?uuids={uuids}&bucket={bucket}")
    @Headers({ "Accept: application/json", })
    MetadataProcessingReport enableVersionControlForRecords(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>enableVersionControlForRecords</code> method in a fluent style.
     */
    public static class EnableVersionControlForRecordsQueryParams extends HashMap<String, Object> {
        public EnableVersionControlForRecordsQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public EnableVersionControlForRecordsQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get the list of users \&quot;active\&quot; during a time period.
     * 
     * @param dateFrom From date of users login date (required)
     * @param dateTo   To date of users login date (required)
     * @param groups   Group(s) for the users (optional)
     */
    @RequestLine("GET /srv/api/0.1/reports/users?dateFrom={dateFrom}&dateTo={dateTo}&groups={groups}")
    @Headers({ "Accept: text/x-csv;charset&#x3D;UTF-8", })
    void getActiveUsers(@Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo,
            @Param("groups") List<Integer> groups);

    /**
     * Get the list of users \&quot;active\&quot; during a time period.
     * 
     * Note, this is equivalent to the other <code>getActiveUsers</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link GetActiveUsersQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>dateFrom - From date of users login date
     *                    (required)</li>
     *                    <li>dateTo - To date of users login date (required)</li>
     *                    <li>groups - Group(s) for the users (optional)</li>
     *                    </ul>
     */
    @RequestLine("GET /srv/api/0.1/reports/users?dateFrom={dateFrom}&dateTo={dateTo}&groups={groups}")
    @Headers({ "Accept: text/x-csv;charset&#x3D;UTF-8", })
    void getActiveUsers(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getActiveUsers</code> method in a fluent style.
     */
    public static class GetActiveUsersQueryParams extends HashMap<String, Object> {
        public GetActiveUsersQueryParams dateFrom(final String value) {
            put("dateFrom", EncodingUtils.encode(value));
            return this;
        }

        public GetActiveUsersQueryParams dateTo(final String value) {
            put("dateTo", EncodingUtils.encode(value));
            return this;
        }

        public GetActiveUsersQueryParams groups(final List<Integer> value) {
            put("groups", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * List all metadata attachments &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/using-filestore.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param sort         Sort by (optional, default to name)
     * @param approved     Use approved version or not (optional, default to true)
     * @param filter       filter (optional, default to *.*)
     * @return List&lt;MetadataResource&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/attachments?sort={sort}&approved={approved}&filter={filter}")
    @Headers({ "Accept: application/json", })
    List<MetadataResource> getAllMetadataResources(@Param("metadataUuid") String metadataUuid,
            @Param("sort") String sort, @Param("approved") Boolean approved, @Param("filter") String filter);

    /**
     * List all metadata attachments &lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/using-filestore.html&#39;&gt;More
     * info&lt;/a&gt; Note, this is equivalent to the other
     * <code>getAllMetadataResources</code> method, but with the query parameters
     * collected into a single Map parameter. This is convenient for services with
     * optional query parameters, especially when used with the
     * {@link GetAllMetadataResourcesQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>sort - Sort by (optional, default to name)</li>
     *                     <li>approved - Use approved version or not (optional,
     *                     default to true)</li>
     *                     <li>filter - filter (optional, default to *.*)</li>
     *                     </ul>
     * @return List&lt;MetadataResource&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/attachments?sort={sort}&approved={approved}&filter={filter}")
    @Headers({ "Accept: application/json", })
    List<MetadataResource> getAllMetadataResources(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getAllMetadataResources</code> method in a fluent style.
     */
    public static class GetAllMetadataResourcesQueryParams extends HashMap<String, Object> {
        public GetAllMetadataResourcesQueryParams sort(final String value) {
            put("sort", EncodingUtils.encode(value));
            return this;
        }

        public GetAllMetadataResourcesQueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }

        public GetAllMetadataResourcesQueryParams filter(final String value) {
            put("filter", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get catalog content as RDF. This endpoint supports the same Lucene query
     * parameters as for the GUI search. .
     * 
     * @param from        Indicates the start position in a sorted list of matches
     *                    that the client wants to use as the beginning of a page
     *                    result. (optional, default to 1)
     * @param hitsPerPage Indicates the number of hits per page. (optional, default
     *                    to 10)
     * @param any         Search key (optional)
     * @param title       A search key for the title. (optional)
     * @param facetQ      A search facet in the Lucene index. Use the GeoNetwork GUI
     *                    search to generate the suitable filter values. Example:
     *                    standard/dcat-ap&amp;createDateYear/2018&amp;sourceCatalog/6d93613e-2b76-4e26-94af-4b4c420a1758
     *                    (filter by creation year and source catalog). (optional)
     * @param sortBy      Lucene sortBy criteria. Relevant values: relevance, title,
     *                    changeDate. (optional)
     * @param sortOrder   Sort order. Possible values: reverse. (optional)
     * @param similarity  Use the Lucene FuzzyQuery. Values range from 0.0 to 1.0
     *                    and defaults to 0.8. (optional, default to 0.8)
     */
    @RequestLine("GET /srv/api/0.1/records?from={from}&hitsPerPage={hitsPerPage}&any={any}&title={title}&facet.q={facetQ}&sortBy={sortBy}&sortOrder={sortOrder}&similarity={similarity}")
    @Headers({ "Accept: */*,application/rdf+xml", })
    void getAsRdf(@Param("from") Integer from, @Param("hitsPerPage") Integer hitsPerPage, @Param("any") String any,
            @Param("title") String title, @Param("facetQ") String facetQ, @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder, @Param("similarity") Float similarity);

    /**
     * Get catalog content as RDF. This endpoint supports the same Lucene query
     * parameters as for the GUI search. . Note, this is equivalent to the other
     * <code>getAsRdf</code> method, but with the query parameters collected into a
     * single Map parameter. This is convenient for services with optional query
     * parameters, especially when used with the {@link GetAsRdfQueryParams} class
     * that allows for building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>from - Indicates the start position in a sorted list
     *                    of matches that the client wants to use as the beginning
     *                    of a page result. (optional, default to 1)</li>
     *                    <li>hitsPerPage - Indicates the number of hits per page.
     *                    (optional, default to 10)</li>
     *                    <li>any - Search key (optional)</li>
     *                    <li>title - A search key for the title. (optional)</li>
     *                    <li>facetQ - A search facet in the Lucene index. Use the
     *                    GeoNetwork GUI search to generate the suitable filter
     *                    values. Example:
     *                    standard/dcat-ap&amp;createDateYear/2018&amp;sourceCatalog/6d93613e-2b76-4e26-94af-4b4c420a1758
     *                    (filter by creation year and source catalog).
     *                    (optional)</li>
     *                    <li>sortBy - Lucene sortBy criteria. Relevant values:
     *                    relevance, title, changeDate. (optional)</li>
     *                    <li>sortOrder - Sort order. Possible values: reverse.
     *                    (optional)</li>
     *                    <li>similarity - Use the Lucene FuzzyQuery. Values range
     *                    from 0.0 to 1.0 and defaults to 0.8. (optional, default to
     *                    0.8)</li>
     *                    </ul>
     */
    @RequestLine("GET /srv/api/0.1/records?from={from}&hitsPerPage={hitsPerPage}&any={any}&title={title}&facet.q={facetQ}&sortBy={sortBy}&sortOrder={sortOrder}&similarity={similarity}")
    @Headers({ "Accept: */*,application/rdf+xml", })
    void getAsRdf(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getAsRdf</code> method in a fluent style.
     */
    public static class GetAsRdfQueryParams extends HashMap<String, Object> {
        public GetAsRdfQueryParams from(final Integer value) {
            put("from", EncodingUtils.encode(value));
            return this;
        }

        public GetAsRdfQueryParams hitsPerPage(final Integer value) {
            put("hitsPerPage", EncodingUtils.encode(value));
            return this;
        }

        public GetAsRdfQueryParams any(final String value) {
            put("any", EncodingUtils.encode(value));
            return this;
        }

        public GetAsRdfQueryParams title(final String value) {
            put("title", EncodingUtils.encode(value));
            return this;
        }

        public GetAsRdfQueryParams facetQ(final String value) {
            put("facet.q", EncodingUtils.encode(value));
            return this;
        }

        public GetAsRdfQueryParams sortBy(final String value) {
            put("sortBy", EncodingUtils.encode(value));
            return this;
        }

        public GetAsRdfQueryParams sortOrder(final String value) {
            put("sortOrder", EncodingUtils.encode(value));
            return this;
        }

        public GetAsRdfQueryParams similarity(final Float value) {
            put("similarity", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get record related resources Retrieve related services, datasets, onlines,
     * thumbnails, sources, ... to this records.&lt;br/&gt;&lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/index.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param metadataUuid Record UUID. (required)
     * @param type         Type of related resource. If none, all resources are
     *                     returned. (optional)
     * @param start        Start offset for paging. Default 1. Only applies to
     *                     related metadata records (ie. not for thumbnails).
     *                     (optional, default to 1)
     * @param rows         Number of rows returned. Default 100. (optional, default
     *                     to 100)
     * @return RelatedResponse
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/related?type={type}&start={start}&rows={rows}")
    @Headers({ "Accept: application/json", })
    RelatedResponse getAssociated(@Param("metadataUuid") String metadataUuid, @Param("type") List<String> type,
            @Param("start") Integer start, @Param("rows") Integer rows);

    /**
     * Get record related resources Retrieve related services, datasets, onlines,
     * thumbnails, sources, ... to this records.&lt;br/&gt;&lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/index.html&#39;&gt;More
     * info&lt;/a&gt; Note, this is equivalent to the other
     * <code>getAssociated</code> method, but with the query parameters collected
     * into a single Map parameter. This is convenient for services with optional
     * query parameters, especially when used with the
     * {@link GetAssociatedQueryParams} class that allows for building up this map
     * in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>type - Type of related resource. If none, all
     *                     resources are returned. (optional)</li>
     *                     <li>start - Start offset for paging. Default 1. Only
     *                     applies to related metadata records (ie. not for
     *                     thumbnails). (optional, default to 1)</li>
     *                     <li>rows - Number of rows returned. Default 100.
     *                     (optional, default to 100)</li>
     *                     </ul>
     * @return RelatedResponse
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/related?type={type}&start={start}&rows={rows}")
    @Headers({ "Accept: application/json", })
    RelatedResponse getAssociated(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getAssociated</code> method in a fluent style.
     */
    public static class GetAssociatedQueryParams extends HashMap<String, Object> {
        public GetAssociatedQueryParams type(final List<String> value) {
            put("type", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public GetAssociatedQueryParams start(final Integer value) {
            put("start", EncodingUtils.encode(value));
            return this;
        }

        public GetAssociatedQueryParams rows(final Integer value) {
            put("rows", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Returns a map to decode attributes in a dataset (from the associated feature
     * catalog) Retrieve related services, datasets, onlines, thumbnails, sources,
     * ... to this records.&lt;br/&gt;&lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/associating-resources/index.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param metadataUuid Record UUID. (required)
     * @return FeatureResponse
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/featureCatalog")
    @Headers({ "Accept: application/json", })
    FeatureResponse getFeatureCatalog(@Param("metadataUuid") String metadataUuid);

    /**
     * List saved queries for this metadata
     * 
     * @param metadataUuid Record UUID. (required)
     * @return List&lt;SavedQuery&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/query")
    @Headers({ "Accept: application/json", })
    List<SavedQuery> getMetadataSavedQueries(@Param("metadataUuid") String metadataUuid);

    /**
     * Get a metadata record Depending on the accept header the appropriate
     * formatter is used. When requesting a ZIP, a MEF version 2 file is returned.
     * When requesting HTML, the default formatter is used.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param accept       Accept header should indicate which is the appropriate
     *                     format to return. It could be text/html, application/xml,
     *                     application/zip, ...If no appropriate Accept header
     *                     found, the XML format is returned. (required)
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}")
    @Headers({ "Accept: application/json", "Accept: {accept}" })
    String getRecord(@Param("metadataUuid") String metadataUuid, @Param("accept") String accept);

    /**
     * Get a metadata record as XML or JSON
     * 
     * @param metadataUuid       Record UUID. (required)
     * @param accept             Accept (required)
     * @param addSchemaLocation  Add XSD schema location based on standard
     *                           configuration (see schema-ident.xml). (optional,
     *                           default to true)
     * @param increasePopularity Increase record popularity (optional, default to
     *                           true)
     * @param withInfo           Add geonet:info details (optional, default to
     *                           false)
     * @param attachment         Download as a file (optional, default to false)
     * @param approved           Download the approved version (optional, default to
     *                           true)
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/formatters/json?addSchemaLocation={addSchemaLocation}&increasePopularity={increasePopularity}&withInfo={withInfo}&attachment={attachment}&approved={approved}")
    @Headers({ "Accept: application/json", "Accept: {accept}" })
    Object getRecordAsXmlOrJSON(@Param("metadataUuid") String metadataUuid, @Param("accept") String accept,
            @Param("addSchemaLocation") Boolean addSchemaLocation,
            @Param("increasePopularity") Boolean increasePopularity, @Param("withInfo") Boolean withInfo,
            @Param("attachment") Boolean attachment, @Param("approved") Boolean approved);

    /**
     * Get a metadata record as XML or JSON
     * 
     * Note, this is equivalent to the other <code>getRecordAsXmlOrJSON</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link GetRecordAsXmlOrJSONQueryParams} class that allows
     * for building up this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param accept       Accept (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>addSchemaLocation - Add XSD schema location based on
     *                     standard configuration (see schema-ident.xml). (optional,
     *                     default to true)</li>
     *                     <li>increasePopularity - Increase record popularity
     *                     (optional, default to true)</li>
     *                     <li>withInfo - Add geonet:info details (optional, default
     *                     to false)</li>
     *                     <li>attachment - Download as a file (optional, default to
     *                     false)</li>
     *                     <li>approved - Download the approved version (optional,
     *                     default to true)</li>
     *                     </ul>
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/formatters/json?addSchemaLocation={addSchemaLocation}&increasePopularity={increasePopularity}&withInfo={withInfo}&attachment={attachment}&approved={approved}")
    @Headers({ "Accept: application/json", "Accept: {accept}" })
    Object getRecordAsXmlOrJSON(@Param("metadataUuid") String metadataUuid, @Param("accept") String accept,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getRecordAsXmlOrJSON</code> method in a fluent style.
     */
    public static class GetRecordAsXmlOrJSONQueryParams extends HashMap<String, Object> {
        public GetRecordAsXmlOrJSONQueryParams addSchemaLocation(final Boolean value) {
            put("addSchemaLocation", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsXmlOrJSONQueryParams increasePopularity(final Boolean value) {
            put("increasePopularity", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsXmlOrJSONQueryParams withInfo(final Boolean value) {
            put("withInfo", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsXmlOrJSONQueryParams attachment(final Boolean value) {
            put("attachment", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsXmlOrJSONQueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get a metadata record as XML or JSON
     * 
     * @param metadataUuid       Record UUID. (required)
     * @param accept             Accept (required)
     * @param addSchemaLocation  Add XSD schema location based on standard
     *                           configuration (see schema-ident.xml). (optional,
     *                           default to true)
     * @param increasePopularity Increase record popularity (optional, default to
     *                           true)
     * @param withInfo           Add geonet:info details (optional, default to
     *                           false)
     * @param attachment         Download as a file (optional, default to false)
     * @param approved           Download the approved version (optional, default to
     *                           true)
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/formatters/xml?addSchemaLocation={addSchemaLocation}&increasePopularity={increasePopularity}&withInfo={withInfo}&attachment={attachment}&approved={approved}")
    @Headers({ "Accept: application/json", "Accept: {accept}" })
    Object getRecordAsXmlOrJSON1(@Param("metadataUuid") String metadataUuid, @Param("accept") String accept,
            @Param("addSchemaLocation") Boolean addSchemaLocation,
            @Param("increasePopularity") Boolean increasePopularity, @Param("withInfo") Boolean withInfo,
            @Param("attachment") Boolean attachment, @Param("approved") Boolean approved);

    /**
     * Get a metadata record as XML or JSON
     * 
     * Note, this is equivalent to the other <code>getRecordAsXmlOrJSON1</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link GetRecordAsXmlOrJSON1QueryParams} class that allows
     * for building up this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param accept       Accept (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>addSchemaLocation - Add XSD schema location based on
     *                     standard configuration (see schema-ident.xml). (optional,
     *                     default to true)</li>
     *                     <li>increasePopularity - Increase record popularity
     *                     (optional, default to true)</li>
     *                     <li>withInfo - Add geonet:info details (optional, default
     *                     to false)</li>
     *                     <li>attachment - Download as a file (optional, default to
     *                     false)</li>
     *                     <li>approved - Download the approved version (optional,
     *                     default to true)</li>
     *                     </ul>
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/formatters/xml?addSchemaLocation={addSchemaLocation}&increasePopularity={increasePopularity}&withInfo={withInfo}&attachment={attachment}&approved={approved}")
    @Headers({ "Accept: application/json", "Accept: {accept}" })
    Object getRecordAsXmlOrJSON1(@Param("metadataUuid") String metadataUuid, @Param("accept") String accept,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getRecordAsXmlOrJSON1</code> method in a fluent style.
     */
    public static class GetRecordAsXmlOrJSON1QueryParams extends HashMap<String, Object> {
        public GetRecordAsXmlOrJSON1QueryParams addSchemaLocation(final Boolean value) {
            put("addSchemaLocation", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsXmlOrJSON1QueryParams increasePopularity(final Boolean value) {
            put("increasePopularity", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsXmlOrJSON1QueryParams withInfo(final Boolean value) {
            put("withInfo", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsXmlOrJSON1QueryParams attachment(final Boolean value) {
            put("attachment", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsXmlOrJSON1QueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get a metadata record as ZIP Metadata Exchange Format (MEF) is returned. MEF
     * is a ZIP file containing the metadata as XML and some others files depending
     * on the version requested. See
     * http://geonetwork-opensource.org/manuals/trunk/eng/users/annexes/mef-format.html.
     * 
     * @param metadataUuid       Record UUID. (required)
     * @param accept             Accept (required)
     * @param format             MEF file format. (optional, default to FULL)
     * @param withRelated        With related records (parent and service).
     *                           (optional, default to true)
     * @param withXLinksResolved Resolve XLinks in the records. (optional, default
     *                           to true)
     * @param withXLinkAttribute Preserve XLink URLs in the records. (optional,
     *                           default to false)
     * @param addSchemaLocation  addSchemaLocation (optional, default to true)
     * @param approved           Download the approved version (optional, default to
     *                           true)
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/formatters/zip?format={format}&withRelated={withRelated}&withXLinksResolved={withXLinksResolved}&withXLinkAttribute={withXLinkAttribute}&addSchemaLocation={addSchemaLocation}&approved={approved}")
    @Headers({ "Accept: application/x-gn-mef-1-zip,application/zip,application/x-gn-mef-2-zip", "Accept: {accept}" })
    void getRecordAsZip(@Param("metadataUuid") String metadataUuid, @Param("accept") String accept,
            @Param("format") String format, @Param("withRelated") Boolean withRelated,
            @Param("withXLinksResolved") Boolean withXLinksResolved,
            @Param("withXLinkAttribute") Boolean withXLinkAttribute,
            @Param("addSchemaLocation") Boolean addSchemaLocation, @Param("approved") Boolean approved);

    /**
     * Get a metadata record as ZIP Metadata Exchange Format (MEF) is returned. MEF
     * is a ZIP file containing the metadata as XML and some others files depending
     * on the version requested. See
     * http://geonetwork-opensource.org/manuals/trunk/eng/users/annexes/mef-format.html.
     * Note, this is equivalent to the other <code>getRecordAsZip</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link GetRecordAsZipQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param accept       Accept (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>format - MEF file format. (optional, default to
     *                     FULL)</li>
     *                     <li>withRelated - With related records (parent and
     *                     service). (optional, default to true)</li>
     *                     <li>withXLinksResolved - Resolve XLinks in the records.
     *                     (optional, default to true)</li>
     *                     <li>withXLinkAttribute - Preserve XLink URLs in the
     *                     records. (optional, default to false)</li>
     *                     <li>addSchemaLocation - addSchemaLocation (optional,
     *                     default to true)</li>
     *                     <li>approved - Download the approved version (optional,
     *                     default to true)</li>
     *                     </ul>
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/formatters/zip?format={format}&withRelated={withRelated}&withXLinksResolved={withXLinksResolved}&withXLinkAttribute={withXLinkAttribute}&addSchemaLocation={addSchemaLocation}&approved={approved}")
    @Headers({ "Accept: application/x-gn-mef-1-zip,application/zip,application/x-gn-mef-2-zip", "Accept: {accept}" })
    void getRecordAsZip(@Param("metadataUuid") String metadataUuid, @Param("accept") String accept,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getRecordAsZip</code> method in a fluent style.
     */
    public static class GetRecordAsZipQueryParams extends HashMap<String, Object> {
        public GetRecordAsZipQueryParams format(final String value) {
            put("format", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsZipQueryParams withRelated(final Boolean value) {
            put("withRelated", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsZipQueryParams withXLinksResolved(final Boolean value) {
            put("withXLinksResolved", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsZipQueryParams withXLinkAttribute(final Boolean value) {
            put("withXLinkAttribute", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsZipQueryParams addSchemaLocation(final Boolean value) {
            put("addSchemaLocation", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordAsZipQueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get a formatted metadata record
     * 
     * @param formatterId  formatterId (required)
     * @param metadataUuid Record UUID. (required)
     * @param accept       Formatter type to use. (optional, default to text/html)
     * @param width        width (optional, default to _100)
     * @param mdpath       mdpath (optional)
     * @param language     Optional language ISO 3 letters code to override HTTP
     *                     Accept-language header. (optional)
     * @param output       output (optional)
     * @param approved     Download the approved version (optional, default to true)
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/formatters/{formatterId}?width={width}&mdpath={mdpath}&language={language}&output={output}&approved={approved}")
    @Headers({ "Accept: text/html,application/pdf,*/*,application/xhtml+xml", "Accept: {accept}" })
    void getRecordFormattedBy(@Param("formatterId") String formatterId, @Param("metadataUuid") String metadataUuid,
            @Param("accept") String accept, @Param("width") String width, @Param("mdpath") String mdpath,
            @Param("language") String language, @Param("output") String output, @Param("approved") Boolean approved);

    /**
     * Get a formatted metadata record
     * 
     * Note, this is equivalent to the other <code>getRecordFormattedBy</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link GetRecordFormattedByQueryParams} class that allows
     * for building up this map in a fluent style.
     * 
     * @param formatterId  formatterId (required)
     * @param metadataUuid Record UUID. (required)
     * @param accept       Formatter type to use. (optional, default to text/html)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>width - width (optional, default to _100)</li>
     *                     <li>mdpath - mdpath (optional)</li>
     *                     <li>language - Optional language ISO 3 letters code to
     *                     override HTTP Accept-language header. (optional)</li>
     *                     <li>output - output (optional)</li>
     *                     <li>approved - Download the approved version (optional,
     *                     default to true)</li>
     *                     </ul>
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/formatters/{formatterId}?width={width}&mdpath={mdpath}&language={language}&output={output}&approved={approved}")
    @Headers({ "Accept: text/html,application/pdf,*/*,application/xhtml+xml", "Accept: {accept}" })
    void getRecordFormattedBy(@Param("formatterId") String formatterId, @Param("metadataUuid") String metadataUuid,
            @Param("accept") String accept, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getRecordFormattedBy</code> method in a fluent style.
     */
    public static class GetRecordFormattedByQueryParams extends HashMap<String, Object> {
        public GetRecordFormattedByQueryParams width(final String value) {
            put("width", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordFormattedByQueryParams mdpath(final String value) {
            put("mdpath", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordFormattedByQueryParams language(final String value) {
            put("language", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordFormattedByQueryParams output(final String value) {
            put("output", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordFormattedByQueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get record sharing settings Return current sharing options for a record.
     * 
     * @param metadataUuid Record UUID. (required)
     * @return SharingResponse
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/sharing")
    @Headers({ "Accept: application/json", })
    SharingResponse getRecordSharingSettings(@Param("metadataUuid") String metadataUuid);

    /**
     * Get record status history
     * 
     * @param metadataUuid Record UUID. (required)
     * @param details      details (optional)
     * @param sortOrder    Sort direction (optional, default to DESC)
     * @return List&lt;MetadataStatusResponse&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/status?details={details}&sortOrder={sortOrder}")
    @Headers({ "Accept: application/json", })
    List<MetadataStatusResponse> getRecordStatusHistory(@Param("metadataUuid") String metadataUuid,
            @Param("details") Boolean details, @Param("sortOrder") String sortOrder);

    /**
     * Get record status history
     * 
     * Note, this is equivalent to the other <code>getRecordStatusHistory</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link GetRecordStatusHistoryQueryParams} class that
     * allows for building up this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>details - details (optional)</li>
     *                     <li>sortOrder - Sort direction (optional, default to
     *                     DESC)</li>
     *                     </ul>
     * @return List&lt;MetadataStatusResponse&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/status?details={details}&sortOrder={sortOrder}")
    @Headers({ "Accept: application/json", })
    List<MetadataStatusResponse> getRecordStatusHistory(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getRecordStatusHistory</code> method in a fluent style.
     */
    public static class GetRecordStatusHistoryQueryParams extends HashMap<String, Object> {
        public GetRecordStatusHistoryQueryParams details(final Boolean value) {
            put("details", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordStatusHistoryQueryParams sortOrder(final String value) {
            put("sortOrder", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get record status history by type
     * 
     * @param metadataUuid Record UUID. (required)
     * @param type         Type (required)
     * @param details      details (optional)
     * @param sortOrder    Sort direction (optional, default to DESC)
     * @return List&lt;MetadataStatusResponse&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/status/{type}?details={details}&sortOrder={sortOrder}")
    @Headers({ "Accept: application/json", })
    List<MetadataStatusResponse> getRecordStatusHistoryByType(@Param("metadataUuid") String metadataUuid,
            @Param("type") String type, @Param("details") Boolean details, @Param("sortOrder") String sortOrder);

    /**
     * Get record status history by type
     * 
     * Note, this is equivalent to the other
     * <code>getRecordStatusHistoryByType</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link GetRecordStatusHistoryByTypeQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param type         Type (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>details - details (optional)</li>
     *                     <li>sortOrder - Sort direction (optional, default to
     *                     DESC)</li>
     *                     </ul>
     * @return List&lt;MetadataStatusResponse&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/status/{type}?details={details}&sortOrder={sortOrder}")
    @Headers({ "Accept: application/json", })
    List<MetadataStatusResponse> getRecordStatusHistoryByType(@Param("metadataUuid") String metadataUuid,
            @Param("type") String type, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getRecordStatusHistoryByType</code> method in a fluent style.
     */
    public static class GetRecordStatusHistoryByTypeQueryParams extends HashMap<String, Object> {
        public GetRecordStatusHistoryByTypeQueryParams details(final Boolean value) {
            put("details", EncodingUtils.encode(value));
            return this;
        }

        public GetRecordStatusHistoryByTypeQueryParams sortOrder(final String value) {
            put("sortOrder", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get record tags Tags are used to classify information.&lt;br/&gt;&lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/tag-information/tagging-with-categories.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param metadataUuid Record UUID. (required)
     * @return List&lt;MetadataCategory&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/tags")
    @Headers({ "Accept: application/json", })
    List<MetadataCategory> getRecordTags(@Param("metadataUuid") String metadataUuid);

    /**
     * Get list of metadata file downloads
     * 
     * @param dateFrom From date of the metadata downloads (required)
     * @param dateTo   To date of the metadata downloads (required)
     * @param groups   Metadata group(s) (optional)
     */
    @RequestLine("GET /srv/api/0.1/reports/datadownloads?dateFrom={dateFrom}&dateTo={dateTo}&groups={groups}")
    @Headers({ "Accept: text/x-csv;charset&#x3D;UTF-8", })
    void getReportDataDownloads(@Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo,
            @Param("groups") List<Integer> groups);

    /**
     * Get list of metadata file downloads
     * 
     * Note, this is equivalent to the other <code>getReportDataDownloads</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link GetReportDataDownloadsQueryParams} class that
     * allows for building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>dateFrom - From date of the metadata downloads
     *                    (required)</li>
     *                    <li>dateTo - To date of the metadata downloads
     *                    (required)</li>
     *                    <li>groups - Metadata group(s) (optional)</li>
     *                    </ul>
     */
    @RequestLine("GET /srv/api/0.1/reports/datadownloads?dateFrom={dateFrom}&dateTo={dateTo}&groups={groups}")
    @Headers({ "Accept: text/x-csv;charset&#x3D;UTF-8", })
    void getReportDataDownloads(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getReportDataDownloads</code> method in a fluent style.
     */
    public static class GetReportDataDownloadsQueryParams extends HashMap<String, Object> {
        public GetReportDataDownloadsQueryParams dateFrom(final String value) {
            put("dateFrom", EncodingUtils.encode(value));
            return this;
        }

        public GetReportDataDownloadsQueryParams dateTo(final String value) {
            put("dateTo", EncodingUtils.encode(value));
            return this;
        }

        public GetReportDataDownloadsQueryParams groups(final List<Integer> value) {
            put("groups", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Get uploaded files to metadata records during a period.
     * 
     * @param dateFrom From date of the metadata uploads (required)
     * @param dateTo   To date of the metadata uploads (required)
     * @param groups   Metadata group(s) (optional)
     */
    @RequestLine("GET /srv/api/0.1/reports/datauploads?dateFrom={dateFrom}&dateTo={dateTo}&groups={groups}")
    @Headers({ "Accept: text/x-csv;charset&#x3D;UTF-8", })
    void getReportDataUploads(@Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo,
            @Param("groups") List<Integer> groups);

    /**
     * Get uploaded files to metadata records during a period.
     * 
     * Note, this is equivalent to the other <code>getReportDataUploads</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link GetReportDataUploadsQueryParams} class that allows
     * for building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>dateFrom - From date of the metadata uploads
     *                    (required)</li>
     *                    <li>dateTo - To date of the metadata uploads
     *                    (required)</li>
     *                    <li>groups - Metadata group(s) (optional)</li>
     *                    </ul>
     */
    @RequestLine("GET /srv/api/0.1/reports/datauploads?dateFrom={dateFrom}&dateTo={dateTo}&groups={groups}")
    @Headers({ "Accept: text/x-csv;charset&#x3D;UTF-8", })
    void getReportDataUploads(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getReportDataUploads</code> method in a fluent style.
     */
    public static class GetReportDataUploadsQueryParams extends HashMap<String, Object> {
        public GetReportDataUploadsQueryParams dateFrom(final String value) {
            put("dateFrom", EncodingUtils.encode(value));
            return this;
        }

        public GetReportDataUploadsQueryParams dateTo(final String value) {
            put("dateTo", EncodingUtils.encode(value));
            return this;
        }

        public GetReportDataUploadsQueryParams groups(final List<Integer> value) {
            put("groups", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Get the metadata not published during a period.
     * 
     * @param dateFrom From date of metadata change date (required)
     * @param dateTo   To date of metadata change date (required)
     * @param groups   Metadata group(s) (optional)
     */
    @RequestLine("GET /srv/api/0.1/reports/metadatainternal?dateFrom={dateFrom}&dateTo={dateTo}&groups={groups}")
    @Headers({ "Accept: text/x-csv;charset&#x3D;UTF-8", })
    void getReportDataUploads1(@Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo,
            @Param("groups") List<Integer> groups);

    /**
     * Get the metadata not published during a period.
     * 
     * Note, this is equivalent to the other <code>getReportDataUploads1</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link GetReportDataUploads1QueryParams} class that allows
     * for building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>dateFrom - From date of metadata change date
     *                    (required)</li>
     *                    <li>dateTo - To date of metadata change date
     *                    (required)</li>
     *                    <li>groups - Metadata group(s) (optional)</li>
     *                    </ul>
     */
    @RequestLine("GET /srv/api/0.1/reports/metadatainternal?dateFrom={dateFrom}&dateTo={dateTo}&groups={groups}")
    @Headers({ "Accept: text/x-csv;charset&#x3D;UTF-8", })
    void getReportDataUploads1(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getReportDataUploads1</code> method in a fluent style.
     */
    public static class GetReportDataUploads1QueryParams extends HashMap<String, Object> {
        public GetReportDataUploads1QueryParams dateFrom(final String value) {
            put("dateFrom", EncodingUtils.encode(value));
            return this;
        }

        public GetReportDataUploads1QueryParams dateTo(final String value) {
            put("dateTo", EncodingUtils.encode(value));
            return this;
        }

        public GetReportDataUploads1QueryParams groups(final List<Integer> value) {
            put("groups", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Get the updated metadata during a period.
     * 
     * @param dateFrom From date of metadata change date (required)
     * @param dateTo   To date of metadata change date (required)
     * @param groups   Metadata group(s) (optional)
     */
    @RequestLine("GET /srv/api/0.1/reports/metadataupdated?dateFrom={dateFrom}&dateTo={dateTo}&groups={groups}")
    @Headers({ "Accept: text/x-csv;charset&#x3D;UTF-8", })
    void getReportDataUploads2(@Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo,
            @Param("groups") List<Integer> groups);

    /**
     * Get the updated metadata during a period.
     * 
     * Note, this is equivalent to the other <code>getReportDataUploads2</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link GetReportDataUploads2QueryParams} class that allows
     * for building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>dateFrom - From date of metadata change date
     *                    (required)</li>
     *                    <li>dateTo - To date of metadata change date
     *                    (required)</li>
     *                    <li>groups - Metadata group(s) (optional)</li>
     *                    </ul>
     */
    @RequestLine("GET /srv/api/0.1/reports/metadataupdated?dateFrom={dateFrom}&dateTo={dateTo}&groups={groups}")
    @Headers({ "Accept: text/x-csv;charset&#x3D;UTF-8", })
    void getReportDataUploads2(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getReportDataUploads2</code> method in a fluent style.
     */
    public static class GetReportDataUploads2QueryParams extends HashMap<String, Object> {
        public GetReportDataUploads2QueryParams dateFrom(final String value) {
            put("dateFrom", EncodingUtils.encode(value));
            return this;
        }

        public GetReportDataUploads2QueryParams dateTo(final String value) {
            put("dateTo", EncodingUtils.encode(value));
            return this;
        }

        public GetReportDataUploads2QueryParams groups(final List<Integer> value) {
            put("groups", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Get a metadata resource
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param resourceId   The resource identifier (ie. filename) (required)
     * @param approved     Use approved version or not (optional, default to true)
     * @return byte[]
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/attachments/{resourceId}?approved={approved}")
    @Headers({ "Accept: */*", })
    byte[] getResource(@Param("metadataUuid") String metadataUuid, @Param("resourceId") String resourceId,
            @Param("approved") Boolean approved);

    /**
     * Get a metadata resource
     * 
     * Note, this is equivalent to the other <code>getResource</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link GetResourceQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param resourceId   The resource identifier (ie. filename) (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>approved - Use approved version or not (optional,
     *                     default to true)</li>
     *                     </ul>
     * @return byte[]
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/attachments/{resourceId}?approved={approved}")
    @Headers({ "Accept: */*", })
    byte[] getResource(@Param("metadataUuid") String metadataUuid, @Param("resourceId") String resourceId,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getResource</code> method in a fluent style.
     */
    public static class GetResourceQueryParams extends HashMap<String, Object> {
        public GetResourceQueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get record sharing settings
     * 
     * @return SharingResponse
     */
    @RequestLine("GET /srv/api/0.1/records/sharing")
    @Headers({ "Accept: application/json", })
    SharingResponse getSharingSettings();

    /**
     * Get last workflow status for a record
     * 
     * @param metadataUuid Record UUID. (required)
     * @return MetadataWorkflowStatusResponse
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/status/workflow/last")
    @Headers({ "Accept: application/json", })
    MetadataWorkflowStatusResponse getStatus(@Param("metadataUuid") String metadataUuid);

    /**
     * Get suggestions Analyze the record an suggest processes to improve the
     * quality of the record.&lt;br/&gt;&lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/workflow/batchupdate-xsl.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param metadataUuid Record UUID. (required)
     * @return List&lt;SuggestionType&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/processes")
    @Headers({ "Accept: application/json", })
    List<SuggestionType> getSuggestions(@Param("metadataUuid") String metadataUuid);

    /**
     * Get test suites available. TG13, TG2, ...
     * 
     * @param metadataUuid Record UUID. (required)
     * @return Map&lt;String, List&lt;String&gt;&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/validate/inspire/testsuites")
    @Headers({ "Accept: application/json", })
    Map<String, List<String>> getTestSuites(@Param("metadataUuid") String metadataUuid);

    /**
     * Increase record popularity Used when a view is based on the search results
     * content and does not really access the record. Record is then added to the
     * indexing queue and popularity will be updated soon.
     * 
     * @param metadataUuid Record UUID. (required)
     */
    @RequestLine("POST /srv/api/0.1/records/{metadataUuid}/popularity")
    @Headers({ "Accept: */*", })
    void increaseRecordPopularity(@Param("metadataUuid") String metadataUuid);

    /**
     * Index a set of records Index a set of records provided either by a bucket or
     * a list of uuids
     * 
     * @param uuids  Record UUIDs. If null current selection is used. (optional)
     * @param bucket Selection bucket name (optional)
     * @return Map&lt;String, Object&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/index?uuids={uuids}&bucket={bucket}")
    @Headers({ "Accept: application/json", })
    Map<String, Object> indexSelection(@Param("uuids") List<String> uuids, @Param("bucket") String bucket);

    /**
     * Index a set of records Index a set of records provided either by a bucket or
     * a list of uuids Note, this is equivalent to the other
     * <code>indexSelection</code> method, but with the query parameters collected
     * into a single Map parameter. This is convenient for services with optional
     * query parameters, especially when used with the
     * {@link IndexSelectionQueryParams} class that allows for building up this map
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
     *                    </ul>
     * @return Map&lt;String, Object&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/index?uuids={uuids}&bucket={bucket}")
    @Headers({ "Accept: application/json", })
    Map<String, Object> indexSelection(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>indexSelection</code> method in a fluent style.
     */
    public static class IndexSelectionQueryParams extends HashMap<String, Object> {
        public IndexSelectionQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public IndexSelectionQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Add a record Add one or more record from an XML fragment, URL or file in a
     * folder on the catalog server. When loadingfrom the catalog server folder, it
     * might be faster to use a local filesystem harvester.
     * 
     * @param metadataType    The type of record. (optional, default to METADATA)
     * @param xml             XML fragment. (optional)
     * @param url             URL of a file to download and insert. (optional)
     * @param serverFolder    Server folder where to look for files. (optional)
     * @param recursiveSearch (Server folder import only) Recursive search in
     *                        folder. (optional, default to false)
     * @param assignToCatalog (MEF file only) Assign to current catalog. (optional,
     *                        default to false)
     * @param uuidProcessing  Record identifier processing. (optional, default to
     *                        NOTHING)
     * @param group           The group the record is attached to. (optional)
     * @param category        Tags to assign to the record. (optional)
     * @param rejectIfInvalid Validate the record first and reject it if not valid.
     *                        (optional, default to false)
     * @param transformWith   XSL transformation to apply to the record. (optional,
     *                        default to _none_)
     * @param schema          Force the schema of the record. If not set, schema
     *                        autodetection is used (and is the preferred method).
     *                        (optional)
     * @param extra           (experimental) Add extra information to the record.
     *                        (optional)
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records?metadataType={metadataType}&url={url}&serverFolder={serverFolder}&recursiveSearch={recursiveSearch}&assignToCatalog={assignToCatalog}&uuidProcessing={uuidProcessing}&group={group}&category={category}&rejectIfInvalid={rejectIfInvalid}&transformWith={transformWith}&schema={schema}&extra={extra}")
    @Headers({ "Content-Type: application/xml; charset=UTF-8", "Accept: application/json", })
    SimpleMetadataProcessingReport insert(String xml, @Param("metadataType") String metadataType,
            @Param("url") List<String> url, @Param("serverFolder") String serverFolder,
            @Param("recursiveSearch") Boolean recursiveSearch, @Param("assignToCatalog") Boolean assignToCatalog,
            @Param("uuidProcessing") String uuidProcessing, @Param("group") String group,
            @Param("category") List<String> category, @Param("rejectIfInvalid") Boolean rejectIfInvalid,
            @Param("transformWith") String transformWith, @Param("schema") String schema, @Param("extra") String extra);

    /**
     * Add a record Add one or more record from an XML fragment, URL or file in a
     * folder on the catalog server. When loadingfrom the catalog server folder, it
     * might be faster to use a local filesystem harvester. Note, this is equivalent
     * to the other <code>insert</code> method, but with the query parameters
     * collected into a single Map parameter. This is convenient for services with
     * optional query parameters, especially when used with the
     * {@link InsertQueryParams} class that allows for building up this map in a
     * fluent style.
     * 
     * @param xml         XML fragment. (optional)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>metadataType - The type of record. (optional, default
     *                    to METADATA)</li>
     *                    <li>url - URL of a file to download and insert.
     *                    (optional)</li>
     *                    <li>serverFolder - Server folder where to look for files.
     *                    (optional)</li>
     *                    <li>recursiveSearch - (Server folder import only)
     *                    Recursive search in folder. (optional, default to
     *                    false)</li>
     *                    <li>assignToCatalog - (MEF file only) Assign to current
     *                    catalog. (optional, default to false)</li>
     *                    <li>uuidProcessing - Record identifier processing.
     *                    (optional, default to NOTHING)</li>
     *                    <li>group - The group the record is attached to.
     *                    (optional)</li>
     *                    <li>category - Tags to assign to the record.
     *                    (optional)</li>
     *                    <li>rejectIfInvalid - Validate the record first and reject
     *                    it if not valid. (optional, default to false)</li>
     *                    <li>transformWith - XSL transformation to apply to the
     *                    record. (optional, default to _none_)</li>
     *                    <li>schema - Force the schema of the record. If not set,
     *                    schema autodetection is used (and is the preferred
     *                    method). (optional)</li>
     *                    <li>extra - (experimental) Add extra information to the
     *                    record. (optional)</li>
     *                    </ul>
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records?metadataType={metadataType}&url={url}&serverFolder={serverFolder}&recursiveSearch={recursiveSearch}&assignToCatalog={assignToCatalog}&uuidProcessing={uuidProcessing}&group={group}&category={category}&rejectIfInvalid={rejectIfInvalid}&transformWith={transformWith}&schema={schema}&extra={extra}")
    @Headers({ "Content-Type: application/xml", "Accept: application/json", })
    SimpleMetadataProcessingReport insert(String xml, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>insert</code> method in a fluent style.
     */
    public static class InsertQueryParams extends HashMap<String, Object> {
        public InsertQueryParams metadataType(final String value) {
            put("metadataType", EncodingUtils.encode(value));
            return this;
        }

        public InsertQueryParams url(final List<String> value) {
            put("url", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public InsertQueryParams serverFolder(final String value) {
            put("serverFolder", EncodingUtils.encode(value));
            return this;
        }

        public InsertQueryParams recursiveSearch(final Boolean value) {
            put("recursiveSearch", EncodingUtils.encode(value));
            return this;
        }

        public InsertQueryParams assignToCatalog(final Boolean value) {
            put("assignToCatalog", EncodingUtils.encode(value));
            return this;
        }

        public InsertQueryParams uuidProcessing(final String value) {
            put("uuidProcessing", EncodingUtils.encode(value));
            return this;
        }

        public InsertQueryParams group(final String value) {
            put("group", EncodingUtils.encode(value));
            return this;
        }

        public InsertQueryParams category(final List<String> value) {
            put("category", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public InsertQueryParams rejectIfInvalid(final Boolean value) {
            put("rejectIfInvalid", EncodingUtils.encode(value));
            return this;
        }

        public InsertQueryParams transformWith(final String value) {
            put("transformWith", EncodingUtils.encode(value));
            return this;
        }

        public InsertQueryParams schema(final String value) {
            put("schema", EncodingUtils.encode(value));
            return this;
        }

        public InsertQueryParams extra(final String value) {
            put("extra", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Add a record from XML or MEF/ZIP file Add record in the catalog by uploading
     * files.
     * 
     * @param metadataType    The type of record. (optional, default to METADATA)
     * @param file            XML or MEF file to upload (optional)
     * @param uuidProcessing  Record identifier processing. (optional, default to
     *                        NOTHING)
     * @param group           The group the record is attached to. (optional)
     * @param category        Tags to assign to the record. (optional)
     * @param rejectIfInvalid Validate the record first and reject it if not valid.
     *                        (optional, default to false)
     * @param publishToAll    (XML file only) Publish record. (optional, default to
     *                        false)
     * @param assignToCatalog (MEF file only) Assign to current catalog. (optional,
     *                        default to false)
     * @param transformWith   XSL transformation to apply to the record. (optional,
     *                        default to _none_)
     * @param schema          Force the schema of the record. If not set, schema
     *                        autodetection is used (and is the preferred method).
     *                        (optional)
     * @param extra           (experimental) Add extra information to the record.
     *                        (optional)
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("POST /srv/api/0.1/records?metadataType={metadataType}&uuidProcessing={uuidProcessing}&group={group}&category={category}&rejectIfInvalid={rejectIfInvalid}&publishToAll={publishToAll}&assignToCatalog={assignToCatalog}&transformWith={transformWith}&schema={schema}&extra={extra}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    SimpleMetadataProcessingReport insertFile(@Param("metadataType") String metadataType,
            @Param("file") List<File> file, @Param("uuidProcessing") String uuidProcessing,
            @Param("group") String group, @Param("category") List<String> category,
            @Param("rejectIfInvalid") Boolean rejectIfInvalid, @Param("publishToAll") Boolean publishToAll,
            @Param("assignToCatalog") Boolean assignToCatalog, @Param("transformWith") String transformWith,
            @Param("schema") String schema, @Param("extra") String extra);

    /**
     * Add a record from XML or MEF/ZIP file Add record in the catalog by uploading
     * files. Note, this is equivalent to the other <code>insertFile</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link InsertFileQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param file        XML or MEF file to upload (optional)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>metadataType - The type of record. (optional, default
     *                    to METADATA)</li>
     *                    <li>uuidProcessing - Record identifier processing.
     *                    (optional, default to NOTHING)</li>
     *                    <li>group - The group the record is attached to.
     *                    (optional)</li>
     *                    <li>category - Tags to assign to the record.
     *                    (optional)</li>
     *                    <li>rejectIfInvalid - Validate the record first and reject
     *                    it if not valid. (optional, default to false)</li>
     *                    <li>publishToAll - (XML file only) Publish record.
     *                    (optional, default to false)</li>
     *                    <li>assignToCatalog - (MEF file only) Assign to current
     *                    catalog. (optional, default to false)</li>
     *                    <li>transformWith - XSL transformation to apply to the
     *                    record. (optional, default to _none_)</li>
     *                    <li>schema - Force the schema of the record. If not set,
     *                    schema autodetection is used (and is the preferred
     *                    method). (optional)</li>
     *                    <li>extra - (experimental) Add extra information to the
     *                    record. (optional)</li>
     *                    </ul>
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("POST /srv/api/0.1/records?metadataType={metadataType}&uuidProcessing={uuidProcessing}&group={group}&category={category}&rejectIfInvalid={rejectIfInvalid}&publishToAll={publishToAll}&assignToCatalog={assignToCatalog}&transformWith={transformWith}&schema={schema}&extra={extra}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    SimpleMetadataProcessingReport insertFile(@Param("file") List<File> file,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>insertFile</code> method in a fluent style.
     */
    public static class InsertFileQueryParams extends HashMap<String, Object> {
        public InsertFileQueryParams metadataType(final String value) {
            put("metadataType", EncodingUtils.encode(value));
            return this;
        }

        public InsertFileQueryParams uuidProcessing(final String value) {
            put("uuidProcessing", EncodingUtils.encode(value));
            return this;
        }

        public InsertFileQueryParams group(final String value) {
            put("group", EncodingUtils.encode(value));
            return this;
        }

        public InsertFileQueryParams category(final List<String> value) {
            put("category", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public InsertFileQueryParams rejectIfInvalid(final Boolean value) {
            put("rejectIfInvalid", EncodingUtils.encode(value));
            return this;
        }

        public InsertFileQueryParams publishToAll(final Boolean value) {
            put("publishToAll", EncodingUtils.encode(value));
            return this;
        }

        public InsertFileQueryParams assignToCatalog(final Boolean value) {
            put("assignToCatalog", EncodingUtils.encode(value));
            return this;
        }

        public InsertFileQueryParams transformWith(final String value) {
            put("transformWith", EncodingUtils.encode(value));
            return this;
        }

        public InsertFileQueryParams schema(final String value) {
            put("schema", EncodingUtils.encode(value));
            return this;
        }

        public InsertFileQueryParams extra(final String value) {
            put("extra", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Add a map metadata record from OGC OWS context Add record in the catalog by
     * uploading a map context.
     * 
     * @param title            A map title (required)
     * @param recordAbstract   A map abstract (optional)
     * @param xml              OGC OWS context as string (optional)
     * @param filename         OGC OWS context file name (optional)
     * @param url              OGC OWS context URL (optional)
     * @param viewerUrl        A map viewer URL to visualize the map (optional)
     * @param overview         Map overview as PNG (base64 encoded) (optional)
     * @param overviewFilename Map overview filename (optional)
     * @param topic            Topic category (optional)
     * @param publishToAll     Publish record. (optional, default to false)
     * @param uuidProcessing   Record identifier processing. (optional, default to
     *                         NOTHING)
     * @param group            The group the record is attached to. (optional)
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("POST /srv/api/0.1/records/importfrommap?title={title}&recordAbstract={recordAbstract}&xml={xml}&filename={filename}&url={url}&viewerUrl={viewerUrl}&overview={overview}&overviewFilename={overviewFilename}&topic={topic}&publishToAll={publishToAll}&uuidProcessing={uuidProcessing}&group={group}")
    @Headers({ "Accept: application/json", })
    SimpleMetadataProcessingReport insertOgcMapContextFile(@Param("title") String title,
            @Param("recordAbstract") String recordAbstract, @Param("xml") String xml,
            @Param("filename") String filename, @Param("url") String url, @Param("viewerUrl") String viewerUrl,
            @Param("overview") String overview, @Param("overviewFilename") String overviewFilename,
            @Param("topic") String topic, @Param("publishToAll") Boolean publishToAll,
            @Param("uuidProcessing") String uuidProcessing, @Param("group") String group);

    /**
     * Add a map metadata record from OGC OWS context Add record in the catalog by
     * uploading a map context. Note, this is equivalent to the other
     * <code>insertOgcMapContextFile</code> method, but with the query parameters
     * collected into a single Map parameter. This is convenient for services with
     * optional query parameters, especially when used with the
     * {@link InsertOgcMapContextFileQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>title - A map title (required)</li>
     *                    <li>recordAbstract - A map abstract (optional)</li>
     *                    <li>xml - OGC OWS context as string (optional)</li>
     *                    <li>filename - OGC OWS context file name (optional)</li>
     *                    <li>url - OGC OWS context URL (optional)</li>
     *                    <li>viewerUrl - A map viewer URL to visualize the map
     *                    (optional)</li>
     *                    <li>overview - Map overview as PNG (base64 encoded)
     *                    (optional)</li>
     *                    <li>overviewFilename - Map overview filename
     *                    (optional)</li>
     *                    <li>topic - Topic category (optional)</li>
     *                    <li>publishToAll - Publish record. (optional, default to
     *                    false)</li>
     *                    <li>uuidProcessing - Record identifier processing.
     *                    (optional, default to NOTHING)</li>
     *                    <li>group - The group the record is attached to.
     *                    (optional)</li>
     *                    </ul>
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("POST /srv/api/0.1/records/importfrommap?title={title}&recordAbstract={recordAbstract}&xml={xml}&filename={filename}&url={url}&viewerUrl={viewerUrl}&overview={overview}&overviewFilename={overviewFilename}&topic={topic}&publishToAll={publishToAll}&uuidProcessing={uuidProcessing}&group={group}")
    @Headers({ "Accept: application/json", })
    SimpleMetadataProcessingReport insertOgcMapContextFile(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>insertOgcMapContextFile</code> method in a fluent style.
     */
    public static class InsertOgcMapContextFileQueryParams extends HashMap<String, Object> {
        public InsertOgcMapContextFileQueryParams title(final String value) {
            put("title", EncodingUtils.encode(value));
            return this;
        }

        public InsertOgcMapContextFileQueryParams recordAbstract(final String value) {
            put("recordAbstract", EncodingUtils.encode(value));
            return this;
        }

        public InsertOgcMapContextFileQueryParams xml(final String value) {
            put("xml", EncodingUtils.encode(value));
            return this;
        }

        public InsertOgcMapContextFileQueryParams filename(final String value) {
            put("filename", EncodingUtils.encode(value));
            return this;
        }

        public InsertOgcMapContextFileQueryParams url(final String value) {
            put("url", EncodingUtils.encode(value));
            return this;
        }

        public InsertOgcMapContextFileQueryParams viewerUrl(final String value) {
            put("viewerUrl", EncodingUtils.encode(value));
            return this;
        }

        public InsertOgcMapContextFileQueryParams overview(final String value) {
            put("overview", EncodingUtils.encode(value));
            return this;
        }

        public InsertOgcMapContextFileQueryParams overviewFilename(final String value) {
            put("overviewFilename", EncodingUtils.encode(value));
            return this;
        }

        public InsertOgcMapContextFileQueryParams topic(final String value) {
            put("topic", EncodingUtils.encode(value));
            return this;
        }

        public InsertOgcMapContextFileQueryParams publishToAll(final Boolean value) {
            put("publishToAll", EncodingUtils.encode(value));
            return this;
        }

        public InsertOgcMapContextFileQueryParams uuidProcessing(final String value) {
            put("uuidProcessing", EncodingUtils.encode(value));
            return this;
        }

        public InsertOgcMapContextFileQueryParams group(final String value) {
            put("group", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Update the metadata resource visibility
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param resourceId   The resource identifier (ie. filename) (required)
     * @param visibility   The visibility (required)
     * @param approved     Use approved version or not (optional, default to false)
     * @return MetadataResource
     */
    @RequestLine("PATCH /srv/api/0.1/records/{metadataUuid}/attachments/{resourceId}?visibility={visibility}&approved={approved}")
    @Headers({ "Accept: application/json", })
    MetadataResource patchMetadataResourceVisibility(@Param("metadataUuid") String metadataUuid,
            @Param("resourceId") String resourceId, @Param("visibility") String visibility,
            @Param("approved") Boolean approved);

    /**
     * Update the metadata resource visibility
     * 
     * Note, this is equivalent to the other
     * <code>patchMetadataResourceVisibility</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link PatchMetadataResourceVisibilityQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param resourceId   The resource identifier (ie. filename) (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>visibility - The visibility (required)</li>
     *                     <li>approved - Use approved version or not (optional,
     *                     default to false)</li>
     *                     </ul>
     * @return MetadataResource
     */
    @RequestLine("PATCH /srv/api/0.1/records/{metadataUuid}/attachments/{resourceId}?visibility={visibility}&approved={approved}")
    @Headers({ "Accept: application/json", })
    MetadataResource patchMetadataResourceVisibility(@Param("metadataUuid") String metadataUuid,
            @Param("resourceId") String resourceId, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>patchMetadataResourceVisibility</code> method in a fluent style.
     */
    public static class PatchMetadataResourceVisibilityQueryParams extends HashMap<String, Object> {
        public PatchMetadataResourceVisibilityQueryParams visibility(final String value) {
            put("visibility", EncodingUtils.encode(value));
            return this;
        }

        public PatchMetadataResourceVisibilityQueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Apply a process Process a metadata with an XSL transformation declared in the
     * metadata schema (See the process folder). Parameters sent to the service are
     * forwarded to XSL process.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param process      Process identifier (required)
     * @return ResponseEntity
     */
    @RequestLine("POST /srv/api/0.1/records/{metadataUuid}/processes/{process}")
    @Headers({ "Accept: application/xml", })
    ResponseEntity processRecord(@Param("metadataUuid") String metadataUuid, @Param("process") String process);

    /**
     * Preview process result Process a metadata with an XSL transformation declared
     * in the metadata schema (See the process folder). Parameters sent to the
     * service are forwarded to XSL process.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param process      Process identifier (required)
     * @return Element
     */
    @RequestLine("GET /srv/api/0.1/records/{metadataUuid}/processes/{process}")
    @Headers({ "Accept: application/xml", })
    Element processRecordPreview(@Param("metadataUuid") String metadataUuid, @Param("process") String process);

    /**
     * Set privileges for ALL group to publish the metadata for all users.
     * 
     * @param metadataUuid Record UUID. (required)
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/publish")
    @Headers({ "Accept: */*", })
    void publish(@Param("metadataUuid") String metadataUuid);

    /**
     * Publish one or more records See record sharing for more details.
     * 
     * @param uuids  Record UUIDs. If null current selection is used. (optional)
     * @param bucket Selection bucket name (optional)
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/publish?uuids={uuids}&bucket={bucket}")
    @Headers({ "Accept: */*", })
    MetadataProcessingReport publishRecords(@Param("uuids") List<String> uuids, @Param("bucket") String bucket);

    /**
     * Publish one or more records See record sharing for more details. Note, this
     * is equivalent to the other <code>publishRecords</code> method, but with the
     * query parameters collected into a single Map parameter. This is convenient
     * for services with optional query parameters, especially when used with the
     * {@link PublishRecordsQueryParams} class that allows for building up this map
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
     *                    </ul>
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/publish?uuids={uuids}&bucket={bucket}")
    @Headers({ "Accept: */*", })
    MetadataProcessingReport publishRecords(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>publishRecords</code> method in a fluent style.
     */
    public static class PublishRecordsQueryParams extends HashMap<String, Object> {
        public PublishRecordsQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public PublishRecordsQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Un-publish one or more records See record sharing for more details.
     * 
     * @param uuids  Record UUIDs. If null current selection is used. (optional)
     * @param bucket Selection bucket name (optional)
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/unpublish?uuids={uuids}&bucket={bucket}")
    @Headers({ "Accept: */*", })
    MetadataProcessingReport publishRecords1(@Param("uuids") List<String> uuids, @Param("bucket") String bucket);

    /**
     * Un-publish one or more records See record sharing for more details. Note,
     * this is equivalent to the other <code>publishRecords1</code> method, but with
     * the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link PublishRecords1QueryParams} class that allows for building up
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
     *                    </ul>
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/unpublish?uuids={uuids}&bucket={bucket}")
    @Headers({ "Accept: */*", })
    MetadataProcessingReport publishRecords1(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>publishRecords1</code> method in a fluent style.
     */
    public static class PublishRecords1QueryParams extends HashMap<String, Object> {
        public PublishRecords1QueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public PublishRecords1QueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Create a new resource for a given metadata
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param visibility   The sharing policy (optional, default to public)
     * @param file         The file to upload (optional)
     * @param approved     Use approved version or not (optional, default to false)
     * @return MetadataResource
     */
    @RequestLine("POST /srv/api/0.1/records/{metadataUuid}/attachments?visibility={visibility}&approved={approved}")
    @Headers({ "Content-Type: multipart/form-data", "Accept: application/json", })
    MetadataResource putResourceFromFile(@Param("metadataUuid") String metadataUuid,
            @Param("visibility") String visibility, @Param("file") File file, @Param("approved") Boolean approved);

    /**
     * Create a new resource for a given metadata
     * 
     * Note, this is equivalent to the other <code>putResourceFromFile</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link PutResourceFromFileQueryParams} class that allows
     * for building up this map in a fluent style.
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param file         The file to upload (optional)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>visibility - The sharing policy (optional, default to
     *                     public)</li>
     *                     <li>approved - Use approved version or not (optional,
     *                     default to false)</li>
     *                     </ul>
     * @return MetadataResource
     */
    @RequestLine("POST /srv/api/0.1/records/{metadataUuid}/attachments?visibility={visibility}&approved={approved}")
    @Headers({ "Content-Type: multipart/form-data", "Accept: application/json", })
    MetadataResource putResourceFromFile(@Param("metadataUuid") String metadataUuid, @Param("file") File file,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>putResourceFromFile</code> method in a fluent style.
     */
    public static class PutResourceFromFileQueryParams extends HashMap<String, Object> {
        public PutResourceFromFileQueryParams visibility(final String value) {
            put("visibility", EncodingUtils.encode(value));
            return this;
        }

        public PutResourceFromFileQueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Create a new resource from a URL for a given metadata
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param visibility   The sharing policy (optional, default to public)
     * @param url          The URL to load in the store (optional)
     * @param approved     Use approved version or not (optional, default to false)
     * @return MetadataResource
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/attachments?visibility={visibility}&url={url}&approved={approved}")
    @Headers({ "Accept: application/json", })
    MetadataResource putResourcesFromURL(@Param("metadataUuid") String metadataUuid,
            @Param("visibility") String visibility, @Param("url") String url, @Param("approved") Boolean approved);

    /**
     * Create a new resource from a URL for a given metadata
     * 
     * Note, this is equivalent to the other <code>putResourcesFromURL</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link PutResourcesFromURLQueryParams} class that allows
     * for building up this map in a fluent style.
     * 
     * @param metadataUuid The metadata UUID (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>visibility - The sharing policy (optional, default to
     *                     public)</li>
     *                     <li>url - The URL to load in the store (optional)</li>
     *                     <li>approved - Use approved version or not (optional,
     *                     default to false)</li>
     *                     </ul>
     * @return MetadataResource
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/attachments?visibility={visibility}&url={url}&approved={approved}")
    @Headers({ "Accept: application/json", })
    MetadataResource putResourcesFromURL(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>putResourcesFromURL</code> method in a fluent style.
     */
    public static class PutResourcesFromURLQueryParams extends HashMap<String, Object> {
        public PutResourcesFromURLQueryParams visibility(final String value) {
            put("visibility", EncodingUtils.encode(value));
            return this;
        }

        public PutResourcesFromURLQueryParams url(final String value) {
            put("url", EncodingUtils.encode(value));
            return this;
        }

        public PutResourcesFromURLQueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Rate a record User rating of metadata. If the metadata was harvested using
     * the &#39;GeoNetwork&#39; protocol and the system setting localrating/enable
     * is false (the default), the user&#39;s rating is shared between GN nodes in
     * this harvesting network. If the metadata was not harvested or if
     * localrating/enable is true then &#39;local rating&#39; is applied, counting
     * only rating from users of this node.&lt;br/&gt;When a remote rating is
     * applied, the local rating is not updated. It will be updated on the next
     * harvest run (FIXME ?).
     * 
     * @param metadataUuid Record UUID. (required)
     * @param rating       Rating (required)
     * @return Integer
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/rate")
    @Headers({ "Content-Type: application/json", "Accept: */*", })
    Integer rate(@Param("metadataUuid") String metadataUuid, Integer rating);

    /**
     * Reorder element
     * 
     * @param metadataUuid      Record UUID. (required)
     * @param ref               Reference of the element to move. (required)
     * @param direction         Direction (required)
     * @param displayAttributes Should attributes be shown on the editor snippet?
     *                          (optional, default to false)
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/editor/elements/{direction}?ref={ref}&displayAttributes={displayAttributes}")
    @Headers({ "Accept: application/xml", })
    void reorderElement(@Param("metadataUuid") String metadataUuid, @Param("ref") String ref,
            @Param("direction") String direction, @Param("displayAttributes") Boolean displayAttributes);

    /**
     * Reorder element
     * 
     * Note, this is equivalent to the other <code>reorderElement</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link ReorderElementQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param direction    Direction (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>ref - Reference of the element to move.
     *                     (required)</li>
     *                     <li>displayAttributes - Should attributes be shown on the
     *                     editor snippet? (optional, default to false)</li>
     *                     </ul>
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/editor/elements/{direction}?ref={ref}&displayAttributes={displayAttributes}")
    @Headers({ "Accept: application/xml", })
    void reorderElement(@Param("metadataUuid") String metadataUuid, @Param("direction") String direction,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>reorderElement</code> method in a fluent style.
     */
    public static class ReorderElementQueryParams extends HashMap<String, Object> {
        public ReorderElementQueryParams ref(final String value) {
            put("ref", EncodingUtils.encode(value));
            return this;
        }

        public ReorderElementQueryParams displayAttributes(final Boolean value) {
            put("displayAttributes", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Save edits Save the HTML form content.
     * 
     * @param metadataUuid         Record UUID. (required)
     * @param tab                  Tab (optional, default to simple)
     * @param withAttributes       withAttributes (optional, default to false)
     * @param withValidationErrors withValidationErrors (optional, default to false)
     * @param minor                minor (optional, default to false)
     * @param status               Submit for review directly after save. (optional,
     *                             default to 1)
     * @param commit               Save current edits. (optional, default to false)
     * @param terminate            Save and terminate session. (optional, default to
     *                             false)
     * @param data                 Record as XML. TODO: rename xml (optional)
     */
    @RequestLine("POST /srv/api/0.1/records/{metadataUuid}/editor?tab={tab}&withAttributes={withAttributes}&withValidationErrors={withValidationErrors}&minor={minor}&status={status}&commit={commit}&terminate={terminate}&data={data}")
    @Headers({ "Accept: application/xml", })
    void saveEdits(@Param("metadataUuid") String metadataUuid, @Param("tab") String tab,
            @Param("withAttributes") Boolean withAttributes,
            @Param("withValidationErrors") Boolean withValidationErrors, @Param("minor") Boolean minor,
            @Param("status") String status, @Param("commit") Boolean commit, @Param("terminate") Boolean terminate,
            @Param("data") String data);

    /**
     * Save edits Save the HTML form content. Note, this is equivalent to the other
     * <code>saveEdits</code> method, but with the query parameters collected into a
     * single Map parameter. This is convenient for services with optional query
     * parameters, especially when used with the {@link SaveEditsQueryParams} class
     * that allows for building up this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>tab - Tab (optional, default to simple)</li>
     *                     <li>withAttributes - withAttributes (optional, default to
     *                     false)</li>
     *                     <li>withValidationErrors - withValidationErrors
     *                     (optional, default to false)</li>
     *                     <li>minor - minor (optional, default to false)</li>
     *                     <li>status - Submit for review directly after save.
     *                     (optional, default to 1)</li>
     *                     <li>commit - Save current edits. (optional, default to
     *                     false)</li>
     *                     <li>terminate - Save and terminate session. (optional,
     *                     default to false)</li>
     *                     <li>data - Record as XML. TODO: rename xml
     *                     (optional)</li>
     *                     </ul>
     */
    @RequestLine("POST /srv/api/0.1/records/{metadataUuid}/editor?tab={tab}&withAttributes={withAttributes}&withValidationErrors={withValidationErrors}&minor={minor}&status={status}&commit={commit}&terminate={terminate}&data={data}")
    @Headers({ "Accept: application/xml", })
    void saveEdits(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>saveEdits</code> method in a fluent style.
     */
    public static class SaveEditsQueryParams extends HashMap<String, Object> {
        public SaveEditsQueryParams tab(final String value) {
            put("tab", EncodingUtils.encode(value));
            return this;
        }

        public SaveEditsQueryParams withAttributes(final Boolean value) {
            put("withAttributes", EncodingUtils.encode(value));
            return this;
        }

        public SaveEditsQueryParams withValidationErrors(final Boolean value) {
            put("withValidationErrors", EncodingUtils.encode(value));
            return this;
        }

        public SaveEditsQueryParams minor(final Boolean value) {
            put("minor", EncodingUtils.encode(value));
            return this;
        }

        public SaveEditsQueryParams status(final String value) {
            put("status", EncodingUtils.encode(value));
            return this;
        }

        public SaveEditsQueryParams commit(final Boolean value) {
            put("commit", EncodingUtils.encode(value));
            return this;
        }

        public SaveEditsQueryParams terminate(final Boolean value) {
            put("terminate", EncodingUtils.encode(value));
            return this;
        }

        public SaveEditsQueryParams data(final String value) {
            put("data", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Search status
     * 
     * @param type     One or more types to retrieve (ie. worflow, event, task).
     *                 Default is all. (optional)
     * @param details  All event details including XML changes. Responses are
     *                 bigger. Default is false (optional)
     * @param author   One or more event author. Default is all. (optional)
     * @param owner    One or more event owners. Default is all. (optional)
     * @param record   One or more record identifier. Default is all. (optional)
     * @param dateFrom Start date (optional)
     * @param dateTo   End date (optional)
     * @param from     From page (optional, default to 0)
     * @param size     Number of records to return (optional, default to 100)
     * @return List&lt;MetadataStatusResponse&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/status/search?type={type}&details={details}&author={author}&owner={owner}&record={record}&dateFrom={dateFrom}&dateTo={dateTo}&from={from}&size={size}")
    @Headers({ "Accept: application/json", })
    List<MetadataStatusResponse> searchStatusByType(@Param("type") List<String> type, @Param("details") Boolean details,
            @Param("author") List<Integer> author, @Param("owner") List<Integer> owner,
            @Param("record") List<Integer> record, @Param("dateFrom") String dateFrom, @Param("dateTo") String dateTo,
            @Param("from") Integer from, @Param("size") Integer size);

    /**
     * Search status
     * 
     * Note, this is equivalent to the other <code>searchStatusByType</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link SearchStatusByTypeQueryParams} class that allows for building
     * up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>type - One or more types to retrieve (ie. worflow,
     *                    event, task). Default is all. (optional)</li>
     *                    <li>details - All event details including XML changes.
     *                    Responses are bigger. Default is false (optional)</li>
     *                    <li>author - One or more event author. Default is all.
     *                    (optional)</li>
     *                    <li>owner - One or more event owners. Default is all.
     *                    (optional)</li>
     *                    <li>record - One or more record identifier. Default is
     *                    all. (optional)</li>
     *                    <li>dateFrom - Start date (optional)</li>
     *                    <li>dateTo - End date (optional)</li>
     *                    <li>from - From page (optional, default to 0)</li>
     *                    <li>size - Number of records to return (optional, default
     *                    to 100)</li>
     *                    </ul>
     * @return List&lt;MetadataStatusResponse&gt;
     */
    @RequestLine("GET /srv/api/0.1/records/status/search?type={type}&details={details}&author={author}&owner={owner}&record={record}&dateFrom={dateFrom}&dateTo={dateTo}&from={from}&size={size}")
    @Headers({ "Accept: application/json", })
    List<MetadataStatusResponse> searchStatusByType(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>searchStatusByType</code> method in a fluent style.
     */
    public static class SearchStatusByTypeQueryParams extends HashMap<String, Object> {
        public SearchStatusByTypeQueryParams type(final List<String> value) {
            put("type", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public SearchStatusByTypeQueryParams details(final Boolean value) {
            put("details", EncodingUtils.encode(value));
            return this;
        }

        public SearchStatusByTypeQueryParams author(final List<Integer> value) {
            put("author", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public SearchStatusByTypeQueryParams owner(final List<Integer> value) {
            put("owner", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public SearchStatusByTypeQueryParams record(final List<Integer> value) {
            put("record", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public SearchStatusByTypeQueryParams dateFrom(final String value) {
            put("dateFrom", EncodingUtils.encode(value));
            return this;
        }

        public SearchStatusByTypeQueryParams dateTo(final String value) {
            put("dateTo", EncodingUtils.encode(value));
            return this;
        }

        public SearchStatusByTypeQueryParams from(final Integer value) {
            put("from", EncodingUtils.encode(value));
            return this;
        }

        public SearchStatusByTypeQueryParams size(final Integer value) {
            put("size", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Set group and owner for one or more records
     * 
     * @param groupIdentifier Group identifier (required)
     * @param userIdentifier  User identifier (required)
     * @param uuids           Record UUIDs. If null current selection is used.
     *                        (optional)
     * @param bucket          Selection bucket name (optional)
     * @param approved        Use approved version or not (optional, default to
     *                        false)
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/ownership?uuids={uuids}&groupIdentifier={groupIdentifier}&bucket={bucket}&userIdentifier={userIdentifier}&approved={approved}")
    @Headers({ "Accept: */*", })
    MetadataProcessingReport setGroupAndOwner(@Param("groupIdentifier") Integer groupIdentifier,
            @Param("userIdentifier") Integer userIdentifier, @Param("uuids") List<String> uuids,
            @Param("bucket") String bucket, @Param("approved") Boolean approved);

    /**
     * Set group and owner for one or more records
     * 
     * Note, this is equivalent to the other <code>setGroupAndOwner</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link SetGroupAndOwnerQueryParams} class that allows for building
     * up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>uuids - Record UUIDs. If null current selection is
     *                    used. (optional)</li>
     *                    <li>groupIdentifier - Group identifier (required)</li>
     *                    <li>bucket - Selection bucket name (optional)</li>
     *                    <li>userIdentifier - User identifier (required)</li>
     *                    <li>approved - Use approved version or not (optional,
     *                    default to false)</li>
     *                    </ul>
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/ownership?uuids={uuids}&groupIdentifier={groupIdentifier}&bucket={bucket}&userIdentifier={userIdentifier}&approved={approved}")
    @Headers({ "Accept: */*", })
    MetadataProcessingReport setGroupAndOwner(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>setGroupAndOwner</code> method in a fluent style.
     */
    public static class SetGroupAndOwnerQueryParams extends HashMap<String, Object> {
        public SetGroupAndOwnerQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public SetGroupAndOwnerQueryParams groupIdentifier(final Integer value) {
            put("groupIdentifier", EncodingUtils.encode(value));
            return this;
        }

        public SetGroupAndOwnerQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public SetGroupAndOwnerQueryParams userIdentifier(final Integer value) {
            put("userIdentifier", EncodingUtils.encode(value));
            return this;
        }

        public SetGroupAndOwnerQueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Set record group A record is related to one group.
     * 
     * @param metadataUuid    Record UUID. (required)
     * @param groupIdentifier Group identifier (required)
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/group")
    @Headers({ "Content-Type: application/json", "Accept: */*", })
    void setRecordGroup(@Param("metadataUuid") String metadataUuid, Integer groupIdentifier);

    /**
     * Set record group and owner
     * 
     * @param metadataUuid    Record UUID. (required)
     * @param groupIdentifier Group identifier (required)
     * @param userIdentifier  User identifier (required)
     * @param approved        Use approved version or not (optional, default to
     *                        true)
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/ownership?groupIdentifier={groupIdentifier}&userIdentifier={userIdentifier}&approved={approved}")
    @Headers({ "Accept: */*", })
    MetadataProcessingReport setRecordOwnership(@Param("metadataUuid") String metadataUuid,
            @Param("groupIdentifier") Integer groupIdentifier, @Param("userIdentifier") Integer userIdentifier,
            @Param("approved") Boolean approved);

    /**
     * Set record group and owner
     * 
     * Note, this is equivalent to the other <code>setRecordOwnership</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link SetRecordOwnershipQueryParams} class that allows for building
     * up this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>groupIdentifier - Group identifier (required)</li>
     *                     <li>userIdentifier - User identifier (required)</li>
     *                     <li>approved - Use approved version or not (optional,
     *                     default to true)</li>
     *                     </ul>
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/ownership?groupIdentifier={groupIdentifier}&userIdentifier={userIdentifier}&approved={approved}")
    @Headers({ "Accept: */*", })
    MetadataProcessingReport setRecordOwnership(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>setRecordOwnership</code> method in a fluent style.
     */
    public static class SetRecordOwnershipQueryParams extends HashMap<String, Object> {
        public SetRecordOwnershipQueryParams groupIdentifier(final Integer value) {
            put("groupIdentifier", EncodingUtils.encode(value));
            return this;
        }

        public SetRecordOwnershipQueryParams userIdentifier(final Integer value) {
            put("userIdentifier", EncodingUtils.encode(value));
            return this;
        }

        public SetRecordOwnershipQueryParams approved(final Boolean value) {
            put("approved", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Set the record status
     * 
     * @param metadataUuid Record UUID. (required)
     * @param status       Metadata status (required)
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/status")
    @Headers({ "Content-Type: application/json", "Accept: */*", })
    void setStatus(@Param("metadataUuid") String metadataUuid, MetadataStatusParameter status);

    /**
     * Set record sharing Privileges are assigned by group. User needs to be able to
     * edit a record to set sharing settings. For reserved group (ie. Internet,
     * Intranet &amp; Guest), user MUST be reviewer of one group. For other group,
     * if Only set privileges to user&#39;s groups is set in catalog configuration
     * user MUST be a member of the group.&lt;br/&gt;Clear first allows to unset all
     * operations first before setting the new ones.Clear option does not remove
     * reserved groups operation if user is not an administrator, a reviewer or the
     * owner of the record.&lt;br/&gt;&lt;a
     * href&#x3D;&#39;http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/publishing/managing-privileges.html&#39;&gt;More
     * info&lt;/a&gt;
     * 
     * @param metadataUuid Record UUID. (required)
     * @param sharing      Privileges (required)
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/sharing")
    @Headers({ "Content-Type: application/json", "Accept: */*", })
    void share(@Param("metadataUuid") String metadataUuid, SharingParameter sharing);

    /**
     * Set sharing settings for one or more records See record sharing for more
     * details.
     * 
     * @param sharing Privileges (required)
     * @param uuids   Record UUIDs. If null current selection is used. (optional)
     * @param bucket  Selection bucket name (optional)
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/sharing?uuids={uuids}&bucket={bucket}")
    @Headers({ "Content-Type: application/json", "Accept: */*", })
    MetadataProcessingReport shareRecords(SharingParameter sharing, @Param("uuids") List<String> uuids,
            @Param("bucket") String bucket);

    /**
     * Set sharing settings for one or more records See record sharing for more
     * details. Note, this is equivalent to the other <code>shareRecords</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link ShareRecordsQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param sharing     Privileges (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>uuids - Record UUIDs. If null current selection is
     *                    used. (optional)</li>
     *                    <li>bucket - Selection bucket name (optional)</li>
     *                    </ul>
     * @return MetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/sharing?uuids={uuids}&bucket={bucket}")
    @Headers({ "Content-Type: application/json", "Accept: */*", })
    MetadataProcessingReport shareRecords(SharingParameter sharing,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>shareRecords</code> method in a fluent style.
     */
    public static class ShareRecordsQueryParams extends HashMap<String, Object> {
        public ShareRecordsQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public ShareRecordsQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Submit a record to the INSPIRE service for validation. User MUST be able to
     * edit the record to validate it. An INSPIRE endpoint must be configured in
     * Settings. This activates an asyncronous process, this method does not return
     * any report. This method returns an id to be used to get the report.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param testsuite    Test suite to run (optional)
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/validate/inspire?testsuite={testsuite}")
    @Headers({ "Accept: text/plain", })
    String submitValidate(@Param("metadataUuid") String metadataUuid, @Param("testsuite") String testsuite);

    /**
     * Submit a record to the INSPIRE service for validation. User MUST be able to
     * edit the record to validate it. An INSPIRE endpoint must be configured in
     * Settings. This activates an asyncronous process, this method does not return
     * any report. This method returns an id to be used to get the report. Note,
     * this is equivalent to the other <code>submitValidate</code> method, but with
     * the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link SubmitValidateQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>testsuite - Test suite to run (optional)</li>
     *                     </ul>
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/validate/inspire?testsuite={testsuite}")
    @Headers({ "Accept: text/plain", })
    String submitValidate(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>submitValidate</code> method in a fluent style.
     */
    public static class SubmitValidateQueryParams extends HashMap<String, Object> {
        public SubmitValidateQueryParams testsuite(final String value) {
            put("testsuite", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Trigger MEF backup archive The backup contains all metadata not harvested
     * including templates.
     * 
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/records/backups")
    @Headers({ "Accept: application/json", })
    String triggerBackup();

    /**
     * Unsets privileges for ALL group to publish the metadata for all users.
     * 
     * @param metadataUuid Record UUID. (required)
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/unpublish")
    @Headers({ "Accept: */*", })
    void unpublish(@Param("metadataUuid") String metadataUuid);

    /**
     * Validate a record User MUST be able to edit the record to validate it. FIXME
     * : id MUST be the id of the current metadata record in session ?
     * 
     * @param metadataUuid Record UUID. (required)
     * @param isvalid      Validation status. Should be provided only in case of
     *                     SUBTEMPLATE validation. If provided for another type,
     *                     throw a BadParameter Exception (optional)
     * @return Reports
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/validate/internal?isvalid={isvalid}")
    @Headers({ "Accept: application/json", })
    Reports validate(@Param("metadataUuid") String metadataUuid, @Param("isvalid") Boolean isvalid);

    /**
     * Validate a record User MUST be able to edit the record to validate it. FIXME
     * : id MUST be the id of the current metadata record in session ? Note, this is
     * equivalent to the other <code>validate</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link ValidateQueryParams} class that allows for building up this map in a
     * fluent style.
     * 
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>isvalid - Validation status. Should be provided only
     *                     in case of SUBTEMPLATE validation. If provided for
     *                     another type, throw a BadParameter Exception
     *                     (optional)</li>
     *                     </ul>
     * @return Reports
     */
    @RequestLine("PUT /srv/api/0.1/records/{metadataUuid}/validate/internal?isvalid={isvalid}")
    @Headers({ "Accept: application/json", })
    Reports validate(@Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>validate</code> method in a fluent style.
     */
    public static class ValidateQueryParams extends HashMap<String, Object> {
        public ValidateQueryParams isvalid(final Boolean value) {
            put("isvalid", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Validate one or more records Update validation status for all records.
     * 
     * @param uuids  Record UUIDs. If null current selection is used. (optional)
     * @param bucket Selection bucket name (optional)
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/validate?uuids={uuids}&bucket={bucket}")
    @Headers({ "Accept: application/json", })
    SimpleMetadataProcessingReport validateRecords(@Param("uuids") List<String> uuids, @Param("bucket") String bucket);

    /**
     * Validate one or more records Update validation status for all records. Note,
     * this is equivalent to the other <code>validateRecords</code> method, but with
     * the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link ValidateRecordsQueryParams} class that allows for building up
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
     *                    </ul>
     * @return SimpleMetadataProcessingReport
     */
    @RequestLine("PUT /srv/api/0.1/records/validate?uuids={uuids}&bucket={bucket}")
    @Headers({ "Accept: application/json", })
    SimpleMetadataProcessingReport validateRecords(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>validateRecords</code> method in a fluent style.
     */
    public static class ValidateRecordsQueryParams extends HashMap<String, Object> {
        public ValidateRecordsQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public ValidateRecordsQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }
    }
}
