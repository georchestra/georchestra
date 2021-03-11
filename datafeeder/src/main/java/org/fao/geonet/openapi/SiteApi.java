package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.HttpEntity;
import org.fao.geonet.openapi.model.LogFileResponse;
import org.fao.geonet.openapi.model.Setting;
import org.fao.geonet.openapi.model.SettingsListResponse;
import org.fao.geonet.openapi.model.SiteInformation;
import org.fao.geonet.openapi.model.SystemInfo;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface SiteApi extends ApiClient.Api {

    /**
     * Delete index in Elastic
     * 
     * @return HttpEntity
     */
    @RequestLine("DELETE /srv/api/0.1/site/index/es")
    @Headers({ "Accept: application/json", })
    HttpEntity deleteIndexes();

    /**
     * Get site (or portal) description
     * 
     * @return SettingsListResponse
     */
    @RequestLine("GET /srv/api/0.1/site")
    @Headers({ "Accept: application/json", })
    SettingsListResponse getDescription();

    /**
     * Get site informations
     * 
     * @return SiteInformation
     */
    @RequestLine("GET /srv/api/0.1/site/info")
    @Headers({ "Accept: application/json", })
    SiteInformation getInformation();

    /**
     * Get last activity
     * 
     * @param lines Number of lines to return (optional, default to 2000)
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/site/logging/activity?lines={lines}")
    @Headers({ "Accept: text/plain", })
    String getLastActivity(@Param("lines") Integer lines);

    /**
     * Get last activity
     * 
     * Note, this is equivalent to the other <code>getLastActivity</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link GetLastActivityQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>lines - Number of lines to return (optional, default
     *                    to 2000)</li>
     *                    </ul>
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/site/logging/activity?lines={lines}")
    @Headers({ "Accept: text/plain", })
    String getLastActivity(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getLastActivity</code> method in a fluent style.
     */
    public static class GetLastActivityQueryParams extends HashMap<String, Object> {
        public GetLastActivityQueryParams lines(final Integer value) {
            put("lines", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get last activity in a ZIP
     * 
     */
    @RequestLine("GET /srv/api/0.1/site/logging/activity/zip")
    @Headers({ "Accept: application/zip", })
    void getLastActivityInAZip();

    /**
     * Get log files
     * 
     * @return List&lt;LogFileResponse&gt;
     */
    @RequestLine("GET /srv/api/0.1/site/logging")
    @Headers({ "Accept: application/json", })
    List<LogFileResponse> getLogFiles();

    /**
     * Get settings Return public settings for anonymous users, internals are
     * allowed for authenticated.
     * 
     * @param set Setting set. A common set of settings to retrieve. (optional)
     * @param key Setting key (optional)
     * @return SettingsListResponse
     */
    @RequestLine("GET /srv/api/0.1/site/settings?set={set}&key={key}")
    @Headers({ "Accept: application/json", })
    SettingsListResponse getSettings(@Param("set") List<String> set, @Param("key") List<String> key);

    /**
     * Get settings Return public settings for anonymous users, internals are
     * allowed for authenticated. Note, this is equivalent to the other
     * <code>getSettings</code> method, but with the query parameters collected into
     * a single Map parameter. This is convenient for services with optional query
     * parameters, especially when used with the {@link GetSettingsQueryParams}
     * class that allows for building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>set - Setting set. A common set of settings to
     *                    retrieve. (optional)</li>
     *                    <li>key - Setting key (optional)</li>
     *                    </ul>
     * @return SettingsListResponse
     */
    @RequestLine("GET /srv/api/0.1/site/settings?set={set}&key={key}")
    @Headers({ "Accept: application/json", })
    SettingsListResponse getSettings(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getSettings</code> method in a fluent style.
     */
    public static class GetSettingsQueryParams extends HashMap<String, Object> {
        public GetSettingsQueryParams set(final List<String> value) {
            put("set", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public GetSettingsQueryParams key(final List<String> value) {
            put("key", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Get settings with details Provides also setting properties.
     * 
     * @param set Setting set. A common set of settings to retrieve. (optional)
     * @param key Setting key (optional)
     * @return List&lt;Setting&gt;
     */
    @RequestLine("GET /srv/api/0.1/site/settings/details?set={set}&key={key}")
    @Headers({ "Accept: application/json", })
    List<Setting> getSettingsDetails(@Param("set") List<String> set, @Param("key") List<String> key);

    /**
     * Get settings with details Provides also setting properties. Note, this is
     * equivalent to the other <code>getSettingsDetails</code> method, but with the
     * query parameters collected into a single Map parameter. This is convenient
     * for services with optional query parameters, especially when used with the
     * {@link GetSettingsDetailsQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>set - Setting set. A common set of settings to
     *                    retrieve. (optional)</li>
     *                    <li>key - Setting key (optional)</li>
     *                    </ul>
     * @return List&lt;Setting&gt;
     */
    @RequestLine("GET /srv/api/0.1/site/settings/details?set={set}&key={key}")
    @Headers({ "Accept: application/json", })
    List<Setting> getSettingsDetails(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getSettingsDetails</code> method in a fluent style.
     */
    public static class GetSettingsDetailsQueryParams extends HashMap<String, Object> {
        public GetSettingsDetailsQueryParams set(final List<String> value) {
            put("set", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }

        public GetSettingsDetailsQueryParams key(final List<String> value) {
            put("key", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Get build details To know when and how this version of the application was
     * built.
     * 
     * @return SystemInfo
     */
    @RequestLine("GET /srv/api/0.1/site/info/build")
    @Headers({ "Accept: application/json", })
    SystemInfo getSystemInfo();

    /**
     * Get XSL tranformations available XSL transformations may be applied while
     * importing or harvesting records.
     * 
     * @return List&lt;String&gt;
     */
    @RequestLine("GET /srv/api/0.1/site/info/transforms")
    @Headers({ "Accept: application/json", })
    List<String> getXslTransformations();

    /**
     * Index
     * 
     * @param reset           Drop and recreate index (optional, default to true)
     * @param havingXlinkOnly Records having only XLinks (optional, default to
     *                        false)
     * @param bucket          Selection bucket name (optional)
     * @return HttpEntity
     */
    @RequestLine("PUT /srv/api/0.1/site/index?reset={reset}&havingXlinkOnly={havingXlinkOnly}&bucket={bucket}")
    @Headers({ "Accept: application/json", })
    HttpEntity index(@Param("reset") Boolean reset, @Param("havingXlinkOnly") Boolean havingXlinkOnly,
            @Param("bucket") String bucket);

    /**
     * Index
     * 
     * Note, this is equivalent to the other <code>index</code> method, but with the
     * query parameters collected into a single Map parameter. This is convenient
     * for services with optional query parameters, especially when used with the
     * {@link IndexQueryParams} class that allows for building up this map in a
     * fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>reset - Drop and recreate index (optional, default to
     *                    true)</li>
     *                    <li>havingXlinkOnly - Records having only XLinks
     *                    (optional, default to false)</li>
     *                    <li>bucket - Selection bucket name (optional)</li>
     *                    </ul>
     * @return HttpEntity
     */
    @RequestLine("PUT /srv/api/0.1/site/index?reset={reset}&havingXlinkOnly={havingXlinkOnly}&bucket={bucket}")
    @Headers({ "Accept: application/json", })
    HttpEntity index(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>index</code> method in a fluent style.
     */
    public static class IndexQueryParams extends HashMap<String, Object> {
        public IndexQueryParams reset(final Boolean value) {
            put("reset", EncodingUtils.encode(value));
            return this;
        }

        public IndexQueryParams havingXlinkOnly(final Boolean value) {
            put("havingXlinkOnly", EncodingUtils.encode(value));
            return this;
        }

        public IndexQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Index in Elastic
     * 
     * @param reset           Drop and recreate index (optional, default to true)
     * @param havingXlinkOnly Records having only XLinks (optional, default to
     *                        false)
     * @param bucket          Selection bucket name (optional)
     * @return HttpEntity
     */
    @RequestLine("PUT /srv/api/0.1/site/index/es?reset={reset}&havingXlinkOnly={havingXlinkOnly}&bucket={bucket}")
    @Headers({ "Accept: application/json", })
    HttpEntity indexes(@Param("reset") Boolean reset, @Param("havingXlinkOnly") Boolean havingXlinkOnly,
            @Param("bucket") String bucket);

    /**
     * Index in Elastic
     * 
     * Note, this is equivalent to the other <code>indexes</code> method, but with
     * the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link IndexesQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>reset - Drop and recreate index (optional, default to
     *                    true)</li>
     *                    <li>havingXlinkOnly - Records having only XLinks
     *                    (optional, default to false)</li>
     *                    <li>bucket - Selection bucket name (optional)</li>
     *                    </ul>
     * @return HttpEntity
     */
    @RequestLine("PUT /srv/api/0.1/site/index/es?reset={reset}&havingXlinkOnly={havingXlinkOnly}&bucket={bucket}")
    @Headers({ "Accept: application/json", })
    HttpEntity indexes(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>indexes</code> method in a fluent style.
     */
    public static class IndexesQueryParams extends HashMap<String, Object> {
        public IndexesQueryParams reset(final Boolean value) {
            put("reset", EncodingUtils.encode(value));
            return this;
        }

        public IndexesQueryParams havingXlinkOnly(final Boolean value) {
            put("havingXlinkOnly", EncodingUtils.encode(value));
            return this;
        }

        public IndexesQueryParams bucket(final String value) {
            put("bucket", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Is CAS enabled?
     * 
     * @return Boolean
     */
    @RequestLine("GET /srv/api/0.1/site/info/isCasEnabled")
    @Headers({ "Accept: application/json", })
    Boolean isCasEnabled();

    /**
     * Is indexing?
     * 
     * @return Boolean
     */
    @RequestLine("GET /srv/api/0.1/site/indexing")
    @Headers({ "Accept: application/json", })
    Boolean isIndexing();

    /**
     * Is in read-only mode?
     * 
     * @return Boolean
     */
    @RequestLine("GET /srv/api/0.1/site/info/readonly")
    @Headers({ "Accept: application/json", })
    Boolean isReadOnly();

    /**
     * Save settings
     * 
     */
    @RequestLine("POST /srv/api/0.1/site/settings")
    @Headers({ "Accept: application/json", })
    void saveSettingsDetails();

    /**
     * Set catalog logo Logos are stored in the data directory
     * resources/images/harvesting as PNG or GIF images. When a logo is assigned to
     * the catalog, a new image is created in images/logos/&lt;catalogUuid&gt;.png.
     * 
     * @param file      Logo to use for the catalog (optional)
     * @param asFavicon Create favicon too (optional, default to false)
     */
    @RequestLine("PUT /srv/api/0.1/site/logo?file={file}&asFavicon={asFavicon}")
    @Headers({ "Accept: application/json", })
    void setLogo(@Param("file") String file, @Param("asFavicon") Boolean asFavicon);

    /**
     * Set catalog logo Logos are stored in the data directory
     * resources/images/harvesting as PNG or GIF images. When a logo is assigned to
     * the catalog, a new image is created in images/logos/&lt;catalogUuid&gt;.png.
     * Note, this is equivalent to the other <code>setLogo</code> method, but with
     * the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link SetLogoQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>file - Logo to use for the catalog (optional)</li>
     *                    <li>asFavicon - Create favicon too (optional, default to
     *                    false)</li>
     *                    </ul>
     */
    @RequestLine("PUT /srv/api/0.1/site/logo?file={file}&asFavicon={asFavicon}")
    @Headers({ "Accept: application/json", })
    void setLogo(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>setLogo</code> method in a fluent style.
     */
    public static class SetLogoQueryParams extends HashMap<String, Object> {
        public SetLogoQueryParams file(final String value) {
            put("file", EncodingUtils.encode(value));
            return this;
        }

        public SetLogoQueryParams asFavicon(final Boolean value) {
            put("asFavicon", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Update staging profile TODO: Needs doc
     * 
     * @param profile profile (required)
     */
    @RequestLine("PUT /srv/api/0.1/site/info/staging/{profile}")
    @Headers({ "Accept: application/json", })
    void updateStagingProfile(@Param("profile") String profile);
}
