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

public interface ToolsApi extends ApiClient.Api {

    /**
     * Generate a SLD with a new filter Get the currend SLD for the requested
     * layers, add new filters in, save the SLD and return the new SLD URL.
     * 
     * @param url     The WMS server URL (required)
     * @param layers  The layers (required)
     * @param filters The filters in JSON (required)
     * @return String
     */
    @RequestLine("POST /srv/api/0.1/tools/ogc/sld")
    @Headers({ "Content-Type: application/x-www-form-urlencoded", "Accept: text/plain", })
    String buildSLD(@Param("url") String url, @Param("layers") String layers, @Param("filters") String filters);

    /**
     * Call a migration step
     * 
     * @param stepName Class name to execute corresponding to a migration step. See
     *                 DatabaseMigrationTask. (required)
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/tools/migration/steps/{stepName}")
    @Headers({ "Accept: text/plain", })
    String callStep(@Param("stepName") String stepName);

    /**
     * Remove all SLD files Clean all SLD generated previously
     */
    @RequestLine("DELETE /srv/api/0.1/tools/ogc/sld")
    @Headers({ "Accept: application/json", })
    void deteleSLDUsingDELETE();

    /**
     * Download a SLD
     * 
     * @param id The SLD identifier (required)
     */
    @RequestLine("GET /srv/api/0.1/tools/ogc/sld/{id}{extension}")
    @Headers({ "Accept: application/xml", })
    void downloadSLD(@Param("id") Integer id);

    /**
     * Get the list of SLD available
     * 
     * @return List&lt;String&gt;
     */
    @RequestLine("GET /srv/api/0.1/tools/ogc/sld")
    @Headers({ "Accept: application/json", })
    List<String> getSLDUsingGET();

    /**
     * List translations for database description table
     * 
     * @param type type (optional)
     * @return Map&lt;String, String&gt;
     */
    @RequestLine("GET /srv/api/0.1/tools/i18n/db?type={type}")
    @Headers({ "Accept: application/json", })
    Map<String, String> getTranslations(@Param("type") List<String> type);

    /**
     * List translations for database description table
     * 
     * Note, this is equivalent to the other <code>getTranslations</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link GetTranslationsQueryParams} class that allows for building up
     * this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>type - type (optional)</li>
     *                    </ul>
     * @return Map&lt;String, String&gt;
     */
    @RequestLine("GET /srv/api/0.1/tools/i18n/db?type={type}")
    @Headers({ "Accept: application/json", })
    Map<String, String> getTranslations(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getTranslations</code> method in a fluent style.
     */
    public static class GetTranslationsQueryParams extends HashMap<String, Object> {
        public GetTranslationsQueryParams type(final List<String> value) {
            put("type", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Test mail configuration Send an email to the catalog feedback email.
     * 
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/tools/mail/test")
    @Headers({ "Accept: text/plain", })
    String testMailConfiguration();
}
