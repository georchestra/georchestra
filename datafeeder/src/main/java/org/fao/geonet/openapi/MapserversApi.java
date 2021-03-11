package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.AnonymousMapserver;
import org.fao.geonet.openapi.model.MapServer;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface MapserversApi extends ApiClient.Api {

    /**
     * Add a mapserver Return the id of the newly created mapserver.
     * 
     * @param mapserver Mapserver details (required)
     * @return Integer
     */
    @RequestLine("PUT /srv/api/0.1/mapservers")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    Integer addMapserver(MapServer mapserver);

    /**
     * Remove a mapserver
     * 
     * @param mapserverId Mapserver identifier (required)
     */
    @RequestLine("DELETE /srv/api/0.1/mapservers/{mapserverId}")
    @Headers({ "Accept: application/json", })
    void deleteMapserver(@Param("mapserverId") Integer mapserverId);

    /**
     * Remove a metadata mapserver resource
     * 
     * @param mapserverId      Mapserver identifier (required)
     * @param metadataUuid     Record UUID. (required)
     * @param resource         Resource name (could be a file or a db connection)
     *                         (required)
     * @param metadataTitle    Metadata title (optional)
     * @param metadataAbstract Metadata abstract (optional)
     * @return String
     */
    @RequestLine("DELETE /srv/api/0.1/mapservers/{mapserverId}/records/{metadataUuid}?resource={resource}&metadataTitle={metadataTitle}&metadataAbstract={metadataAbstract}")
    @Headers({ "Accept: text/plain", })
    String deleteMapserverResource(@Param("mapserverId") String mapserverId, @Param("metadataUuid") String metadataUuid,
            @Param("resource") String resource, @Param("metadataTitle") String metadataTitle,
            @Param("metadataAbstract") String metadataAbstract);

    /**
     * Remove a metadata mapserver resource
     * 
     * Note, this is equivalent to the other <code>deleteMapserverResource</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link DeleteMapserverResourceQueryParams} class that
     * allows for building up this map in a fluent style.
     * 
     * @param mapserverId  Mapserver identifier (required)
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>resource - Resource name (could be a file or a db
     *                     connection) (required)</li>
     *                     <li>metadataTitle - Metadata title (optional)</li>
     *                     <li>metadataAbstract - Metadata abstract (optional)</li>
     *                     </ul>
     * @return String
     */
    @RequestLine("DELETE /srv/api/0.1/mapservers/{mapserverId}/records/{metadataUuid}?resource={resource}&metadataTitle={metadataTitle}&metadataAbstract={metadataAbstract}")
    @Headers({ "Accept: text/plain", })
    String deleteMapserverResource(@Param("mapserverId") String mapserverId, @Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deleteMapserverResource</code> method in a fluent style.
     */
    public static class DeleteMapserverResourceQueryParams extends HashMap<String, Object> {
        public DeleteMapserverResourceQueryParams resource(final String value) {
            put("resource", EncodingUtils.encode(value));
            return this;
        }

        public DeleteMapserverResourceQueryParams metadataTitle(final String value) {
            put("metadataTitle", EncodingUtils.encode(value));
            return this;
        }

        public DeleteMapserverResourceQueryParams metadataAbstract(final String value) {
            put("metadataAbstract", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get a mapserver
     * 
     * @param mapserverId Mapserver identifier (required)
     * @return AnonymousMapserver
     */
    @RequestLine("GET /srv/api/0.1/mapservers/{mapserverId}")
    @Headers({ "Accept: application/json", })
    AnonymousMapserver getMapserver(@Param("mapserverId") String mapserverId);

    /**
     * Check metadata mapserver resource is published
     * 
     * @param mapserverId      Mapserver identifier (required)
     * @param metadataUuid     Record UUID. (required)
     * @param resource         Resource name (could be a file or a db connection)
     *                         (required)
     * @param metadataTitle    Metadata title (optional)
     * @param metadataAbstract Metadata abstract (optional)
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/mapservers/{mapserverId}/records/{metadataUuid}?resource={resource}&metadataTitle={metadataTitle}&metadataAbstract={metadataAbstract}")
    @Headers({ "Accept: text/plain", })
    String getMapserverResource(@Param("mapserverId") String mapserverId, @Param("metadataUuid") String metadataUuid,
            @Param("resource") String resource, @Param("metadataTitle") String metadataTitle,
            @Param("metadataAbstract") String metadataAbstract);

    /**
     * Check metadata mapserver resource is published
     * 
     * Note, this is equivalent to the other <code>getMapserverResource</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link GetMapserverResourceQueryParams} class that allows
     * for building up this map in a fluent style.
     * 
     * @param mapserverId  Mapserver identifier (required)
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>resource - Resource name (could be a file or a db
     *                     connection) (required)</li>
     *                     <li>metadataTitle - Metadata title (optional)</li>
     *                     <li>metadataAbstract - Metadata abstract (optional)</li>
     *                     </ul>
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/mapservers/{mapserverId}/records/{metadataUuid}?resource={resource}&metadataTitle={metadataTitle}&metadataAbstract={metadataAbstract}")
    @Headers({ "Accept: text/plain", })
    String getMapserverResource(@Param("mapserverId") String mapserverId, @Param("metadataUuid") String metadataUuid,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getMapserverResource</code> method in a fluent style.
     */
    public static class GetMapserverResourceQueryParams extends HashMap<String, Object> {
        public GetMapserverResourceQueryParams resource(final String value) {
            put("resource", EncodingUtils.encode(value));
            return this;
        }

        public GetMapserverResourceQueryParams metadataTitle(final String value) {
            put("metadataTitle", EncodingUtils.encode(value));
            return this;
        }

        public GetMapserverResourceQueryParams metadataAbstract(final String value) {
            put("metadataAbstract", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get mapservers Mapservers are used by the catalog to publish record
     * attachements (eg. ZIP file with shape) or record associated resources (eg.
     * database table, file on the local network) in a remote mapserver like
     * GeoServer or MapServer. The catalog communicate with the mapserver using
     * GeoServer REST API.
     * 
     * @return List&lt;AnonymousMapserver&gt;
     */
    @RequestLine("GET /srv/api/0.1/mapservers")
    @Headers({ "Accept: application/json", })
    List<AnonymousMapserver> getMapservers();

    /**
     * Publish a metadata resource in a mapserver
     * 
     * @param mapserverId      Mapserver identifier (required)
     * @param metadataUuid     Record UUID. (required)
     * @param resource         Resource name (could be a file or a db connection)
     *                         (required)
     * @param metadataTitle    Metadata title (optional)
     * @param metadataAbstract Metadata abstract (optional)
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/mapservers/{mapserverId}/records/{metadataUuid}?resource={resource}&metadataTitle={metadataTitle}&metadataAbstract={metadataAbstract}")
    @Headers({ "Accept: text/plain", })
    String publishMapserverResource(@Param("mapserverId") String mapserverId,
            @Param("metadataUuid") String metadataUuid, @Param("resource") String resource,
            @Param("metadataTitle") String metadataTitle, @Param("metadataAbstract") String metadataAbstract);

    /**
     * Publish a metadata resource in a mapserver
     * 
     * Note, this is equivalent to the other <code>publishMapserverResource</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link PublishMapserverResourceQueryParams} class that
     * allows for building up this map in a fluent style.
     * 
     * @param mapserverId  Mapserver identifier (required)
     * @param metadataUuid Record UUID. (required)
     * @param queryParams  Map of query parameters as name-value pairs
     *                     <p>
     *                     The following elements may be specified in the query map:
     *                     </p>
     *                     <ul>
     *                     <li>resource - Resource name (could be a file or a db
     *                     connection) (required)</li>
     *                     <li>metadataTitle - Metadata title (optional)</li>
     *                     <li>metadataAbstract - Metadata abstract (optional)</li>
     *                     </ul>
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/mapservers/{mapserverId}/records/{metadataUuid}?resource={resource}&metadataTitle={metadataTitle}&metadataAbstract={metadataAbstract}")
    @Headers({ "Accept: text/plain", })
    String publishMapserverResource(@Param("mapserverId") String mapserverId,
            @Param("metadataUuid") String metadataUuid, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>publishMapserverResource</code> method in a fluent style.
     */
    public static class PublishMapserverResourceQueryParams extends HashMap<String, Object> {
        public PublishMapserverResourceQueryParams resource(final String value) {
            put("resource", EncodingUtils.encode(value));
            return this;
        }

        public PublishMapserverResourceQueryParams metadataTitle(final String value) {
            put("metadataTitle", EncodingUtils.encode(value));
            return this;
        }

        public PublishMapserverResourceQueryParams metadataAbstract(final String value) {
            put("metadataAbstract", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Update a mapserver
     * 
     * @param mapserverId Mapserver identifier (required)
     * @param mapserver   Mapserver details (required)
     */
    @RequestLine("PUT /srv/api/0.1/mapservers/{mapserverId}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    void updateMapserver(@Param("mapserverId") Integer mapserverId, MapServer mapserver);

    /**
     * Update a mapserver authentication The remote mapserver REST API may require
     * basic authentication. This operation set the username and password.
     * 
     * @param mapserverId Mapserver identifier (required)
     * @param username    User name (required)
     * @param password    Password (required)
     */
    @RequestLine("POST /srv/api/0.1/mapservers/{mapserverId}/auth?username={username}&password={password}")
    @Headers({ "Accept: application/json", })
    void updateMapserverAuth(@Param("mapserverId") Integer mapserverId, @Param("username") String username,
            @Param("password") String password);

    /**
     * Update a mapserver authentication The remote mapserver REST API may require
     * basic authentication. This operation set the username and password. Note,
     * this is equivalent to the other <code>updateMapserverAuth</code> method, but
     * with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link UpdateMapserverAuthQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param mapserverId Mapserver identifier (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>username - User name (required)</li>
     *                    <li>password - Password (required)</li>
     *                    </ul>
     */
    @RequestLine("POST /srv/api/0.1/mapservers/{mapserverId}/auth?username={username}&password={password}")
    @Headers({ "Accept: application/json", })
    void updateMapserverAuth(@Param("mapserverId") Integer mapserverId,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>updateMapserverAuth</code> method in a fluent style.
     */
    public static class UpdateMapserverAuthQueryParams extends HashMap<String, Object> {
        public UpdateMapserverAuthQueryParams username(final String value) {
            put("username", EncodingUtils.encode(value));
            return this;
        }

        public UpdateMapserverAuthQueryParams password(final String value) {
            put("password", EncodingUtils.encode(value));
            return this;
        }
    }
}
