package org.fao.geonet.openapi;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.ResponseEntity;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface LogosApi extends ApiClient.Api {

    /**
     * Add a logo
     * 
     * @param file      The logo image to upload (optional)
     * @param overwrite Overwrite if exists (optional, default to false)
     * @return ResponseEntity
     */
    @RequestLine("POST /srv/api/0.1/logos?overwrite={overwrite}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    ResponseEntity addLogo(@Param("file") List<File> file, @Param("overwrite") Boolean overwrite);

    /**
     * Add a logo
     * 
     * Note, this is equivalent to the other <code>addLogo</code> method, but with
     * the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link AddLogoQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param file        The logo image to upload (optional)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>overwrite - Overwrite if exists (optional, default to
     *                    false)</li>
     *                    </ul>
     * @return ResponseEntity
     */
    @RequestLine("POST /srv/api/0.1/logos?overwrite={overwrite}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    ResponseEntity addLogo(@Param("file") List<File> file, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>addLogo</code> method in a fluent style.
     */
    public static class AddLogoQueryParams extends HashMap<String, Object> {
        public AddLogoQueryParams overwrite(final Boolean value) {
            put("overwrite", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Remove a logo
     * 
     * @param file The logo filename to delete (required)
     */
    @RequestLine("DELETE /srv/api/0.1/logos/{file}")
    @Headers({ "Accept: application/json", })
    void deleteLogo(@Param("file") String file);

    /**
     * Get all logos Logos are used for the catalog, the groups logos, and harvester
     * icons. Logos are stored in the data directory in
     * &lt;dataDirectory&gt;/resources/images/harvesting.&lt;br/&gt; Records are
     * attached to a source. A source can be the local catalog or a harvester node.
     * When a source is created, its logo is located in the images/logos folder with
     * the source UUID as filename. For some sources the logo can be automatically
     * retrieved (eg. when harvesting GeoNetwork catalogs). For others, the logo is
     * usually manually defined when configuring the harvester.
     * 
     * @return List&lt;String&gt;
     */
    @RequestLine("GET /srv/api/0.1/logos")
    @Headers({ "Accept: application/json", })
    List<String> getLogos();
}
