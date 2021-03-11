package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.MetadataReplacementProcessingReport;
import org.fao.geonet.openapi.model.ProcessingReport;
import org.fao.geonet.openapi.model.XsltMetadataProcessingReport;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface ProcessesApi extends ApiClient.Api {

    /**
     * Clear process reports list
     * 
     */
    @RequestLine("DELETE /srv/api/0.1/processes/reports")
    @Headers({ "Accept: application/json", })
    void deleteProcess();

    /**
     * Get current process reports When processing, the report is stored in memory
     * and allows to retrieve progress repport during processing. Usually, process
     * reports are returned by the synchronous processing operation.
     * 
     * @return List&lt;ProcessingReport&gt;
     */
    @RequestLine("GET /srv/api/0.1/processes/reports")
    @Headers({ "Accept: application/json", })
    List<ProcessingReport> getProcessReport();

    /**
     * Preview process result applied to one or more records Preview result of a
     * process applied to metadata records with an XSL transformation declared in
     * the metadata schema (See the process folder). Parameters sent to the service
     * are forwarded to XSL process. Append mode has 2 limitations. First, it only
     * support a set of records in the same schema. Secondly, it does not propagate
     * URL parameters. This mode is mainly used to create custom reports based on
     * metadata records content.If process name ends with &#39;.csv&#39;, the XSL
     * process output a text document which is returned. When errors occur during
     * processing, the processing report is returned in JSON format.
     * 
     * @param process         Process identifier (required)
     * @param uuids           Record UUIDs. If null current selection is used.
     *                        (optional)
     * @param bucket          Selection bucket name (optional)
     * @param updateDateStamp If true updates the DateStamp (or equivalent in
     *                        standards different to ISO 19139) field in the
     *                        metadata with the current timestamp (optional, default
     *                        to true)
     * @param appendFirst     Append documents before processing (optional, default
     *                        to false)
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/processes/{process}?uuids={uuids}&bucket={bucket}&updateDateStamp={updateDateStamp}&appendFirst={appendFirst}")
    @Headers({ "Accept: */*", })
    Object previewProcessRecordsUsingXslt(@Param("process") String process, @Param("uuids") List<String> uuids,
            @Param("bucket") String bucket, @Param("updateDateStamp") Boolean updateDateStamp,
            @Param("appendFirst") Boolean appendFirst);

    /**
     * Preview process result applied to one or more records Preview result of a
     * process applied to metadata records with an XSL transformation declared in
     * the metadata schema (See the process folder). Parameters sent to the service
     * are forwarded to XSL process. Append mode has 2 limitations. First, it only
     * support a set of records in the same schema. Secondly, it does not propagate
     * URL parameters. This mode is mainly used to create custom reports based on
     * metadata records content.If process name ends with &#39;.csv&#39;, the XSL
     * process output a text document which is returned. When errors occur during
     * processing, the processing report is returned in JSON format. Note, this is
     * equivalent to the other <code>previewProcessRecordsUsingXslt</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link PreviewProcessRecordsUsingXsltQueryParams} class that allows
     * for building up this map in a fluent style.
     * 
     * @param process     Process identifier (required)
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
     *                    to true)</li>
     *                    <li>appendFirst - Append documents before processing
     *                    (optional, default to false)</li>
     *                    </ul>
     * @return Object
     */
    @RequestLine("GET /srv/api/0.1/processes/{process}?uuids={uuids}&bucket={bucket}&updateDateStamp={updateDateStamp}&appendFirst={appendFirst}")
    @Headers({ "Accept: */*", })
    Object previewProcessRecordsUsingXslt(@Param("process") String process,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>previewProcessRecordsUsingXslt</code> method in a fluent style.
     */
    public static class PreviewProcessRecordsUsingXsltQueryParams extends HashMap<String, Object> {
        public PreviewProcessRecordsUsingXsltQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public PreviewProcessRecordsUsingXsltQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public PreviewProcessRecordsUsingXsltQueryParams updateDateStamp(final Boolean value) {
            put("updateDateStamp", EncodingUtils.encode(value));
            return this;
        }

        public PreviewProcessRecordsUsingXsltQueryParams appendFirst(final Boolean value) {
            put("appendFirst", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Apply a process to one or more records Process a metadata with an XSL
     * transformation declared in the metadata schema (See the process folder).
     * Parameters sent to the service are forwarded to XSL process.
     * 
     * @param process         Process identifier (required)
     * @param uuids           Record UUIDs. If null current selection is used.
     *                        (optional)
     * @param bucket          Selection bucket name (optional)
     * @param updateDateStamp If true updates the DateStamp (or equivalent in
     *                        standards different to ISO 19139) field in the
     *                        metadata with the current timestamp (optional, default
     *                        to true)
     * @param index           Index after processing (optional, default to true)
     * @return XsltMetadataProcessingReport
     */
    @RequestLine("POST /srv/api/0.1/processes/{process}?uuids={uuids}&bucket={bucket}&updateDateStamp={updateDateStamp}&index={index}")
    @Headers({ "Accept: application/json", })
    XsltMetadataProcessingReport processRecordsUsingXslt(@Param("process") String process,
            @Param("uuids") List<String> uuids, @Param("bucket") String bucket,
            @Param("updateDateStamp") Boolean updateDateStamp, @Param("index") Boolean index);

    /**
     * Apply a process to one or more records Process a metadata with an XSL
     * transformation declared in the metadata schema (See the process folder).
     * Parameters sent to the service are forwarded to XSL process. Note, this is
     * equivalent to the other <code>processRecordsUsingXslt</code> method, but with
     * the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link ProcessRecordsUsingXsltQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param process     Process identifier (required)
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
     *                    to true)</li>
     *                    <li>index - Index after processing (optional, default to
     *                    true)</li>
     *                    </ul>
     * @return XsltMetadataProcessingReport
     */
    @RequestLine("POST /srv/api/0.1/processes/{process}?uuids={uuids}&bucket={bucket}&updateDateStamp={updateDateStamp}&index={index}")
    @Headers({ "Accept: application/json", })
    XsltMetadataProcessingReport processRecordsUsingXslt(@Param("process") String process,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>processRecordsUsingXslt</code> method in a fluent style.
     */
    public static class ProcessRecordsUsingXsltQueryParams extends HashMap<String, Object> {
        public ProcessRecordsUsingXsltQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public ProcessRecordsUsingXsltQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public ProcessRecordsUsingXsltQueryParams updateDateStamp(final Boolean value) {
            put("updateDateStamp", EncodingUtils.encode(value));
            return this;
        }

        public ProcessRecordsUsingXsltQueryParams index(final Boolean value) {
            put("index", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Search and replace values in one or more records Service to apply
     * replacements to one or more records. To define a replacement, send the
     * following parameters: * mdsection-139815551372&#x3D;metadata *
     * mdfield-1398155513728&#x3D;id.contact.individualName *
     * replaceValue-1398155513728&#x3D;Juan * searchValue-1398155513728&#x3D;Jose
     * TODO: Would be good to provide a simple object to define list of changes
     * instead of group of parameters.&lt;br/&gt;Batch editing can also be used for
     * similar works.
     * 
     * @param process           process (optional, default to
     *                          massive-content-update)
     * @param uuids             Record UUIDs. If null current selection is used.
     *                          (optional)
     * @param bucket            Selection bucket name (optional)
     * @param isTesting         Test only (ie. metadata are not saved). Return the
     *                          report only. (optional, default to false)
     * @param isCaseInsensitive Case insensitive search. (optional, default to
     *                          false)
     * @param vacuumMode        &#39;record&#39; to apply vacuum.xsl,
     *                          &#39;element&#39; to remove empty elements. Empty to
     *                          not affect empty elements. (optional)
     * @return MetadataReplacementProcessingReport
     */
    @RequestLine("POST /srv/api/0.1/processes/search-and-replace?process={process}&uuids={uuids}&bucket={bucket}&isTesting={isTesting}&isCaseInsensitive={isCaseInsensitive}&vacuumMode={vacuumMode}")
    @Headers({ "Accept: application/json", })
    MetadataReplacementProcessingReport searchAndReplace(@Param("process") String process,
            @Param("uuids") List<String> uuids, @Param("bucket") String bucket, @Param("isTesting") Boolean isTesting,
            @Param("isCaseInsensitive") Boolean isCaseInsensitive, @Param("vacuumMode") String vacuumMode);

    /**
     * Search and replace values in one or more records Service to apply
     * replacements to one or more records. To define a replacement, send the
     * following parameters: * mdsection-139815551372&#x3D;metadata *
     * mdfield-1398155513728&#x3D;id.contact.individualName *
     * replaceValue-1398155513728&#x3D;Juan * searchValue-1398155513728&#x3D;Jose
     * TODO: Would be good to provide a simple object to define list of changes
     * instead of group of parameters.&lt;br/&gt;Batch editing can also be used for
     * similar works. Note, this is equivalent to the other
     * <code>searchAndReplace</code> method, but with the query parameters collected
     * into a single Map parameter. This is convenient for services with optional
     * query parameters, especially when used with the
     * {@link SearchAndReplaceQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>process - process (optional, default to
     *                    massive-content-update)</li>
     *                    <li>uuids - Record UUIDs. If null current selection is
     *                    used. (optional)</li>
     *                    <li>bucket - Selection bucket name (optional)</li>
     *                    <li>isTesting - Test only (ie. metadata are not saved).
     *                    Return the report only. (optional, default to false)</li>
     *                    <li>isCaseInsensitive - Case insensitive search.
     *                    (optional, default to false)</li>
     *                    <li>vacuumMode - &#39;record&#39; to apply vacuum.xsl,
     *                    &#39;element&#39; to remove empty elements. Empty to not
     *                    affect empty elements. (optional)</li>
     *                    </ul>
     * @return MetadataReplacementProcessingReport
     */
    @RequestLine("POST /srv/api/0.1/processes/search-and-replace?process={process}&uuids={uuids}&bucket={bucket}&isTesting={isTesting}&isCaseInsensitive={isCaseInsensitive}&vacuumMode={vacuumMode}")
    @Headers({ "Accept: application/json", })
    MetadataReplacementProcessingReport searchAndReplace(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>searchAndReplace</code> method in a fluent style.
     */
    public static class SearchAndReplaceQueryParams extends HashMap<String, Object> {
        public SearchAndReplaceQueryParams process(final String value) {
            put("process", EncodingUtils.encode(value));
            return this;
        }

        public SearchAndReplaceQueryParams uuids(final List<String> value) {
            put("uuids", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public SearchAndReplaceQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }

        public SearchAndReplaceQueryParams isTesting(final Boolean value) {
            put("isTesting", EncodingUtils.encode(value));
            return this;
        }

        public SearchAndReplaceQueryParams isCaseInsensitive(final Boolean value) {
            put("isCaseInsensitive", EncodingUtils.encode(value));
            return this;
        }

        public SearchAndReplaceQueryParams vacuumMode(final String value) {
            put("vacuumMode", EncodingUtils.encode(value));
            return this;
        }
    }
}
