package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.ResponseEntity;
import org.fao.geonet.openapi.model.Selection;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface UserselectionsApi extends ApiClient.Api {

    /**
     * Add items to a user selection set
     * 
     * @param selectionIdentifier Selection identifier (required)
     * @param userIdentifier      User identifier (required)
     * @param uuid                One or more record UUIDs. (optional)
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/userselections/{selectionIdentifier}/{userIdentifier}?uuid={uuid}")
    @Headers({ "Accept: application/json", })
    String addToUserSelection(@Param("selectionIdentifier") Integer selectionIdentifier,
            @Param("userIdentifier") Integer userIdentifier, @Param("uuid") List<String> uuid);

    /**
     * Add items to a user selection set
     * 
     * Note, this is equivalent to the other <code>addToUserSelection</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link AddToUserSelectionQueryParams} class that allows for building
     * up this map in a fluent style.
     * 
     * @param selectionIdentifier Selection identifier (required)
     * @param userIdentifier      User identifier (required)
     * @param queryParams         Map of query parameters as name-value pairs
     *                            <p>
     *                            The following elements may be specified in the
     *                            query map:
     *                            </p>
     *                            <ul>
     *                            <li>uuid - One or more record UUIDs.
     *                            (optional)</li>
     *                            </ul>
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/userselections/{selectionIdentifier}/{userIdentifier}?uuid={uuid}")
    @Headers({ "Accept: application/json", })
    String addToUserSelection(@Param("selectionIdentifier") Integer selectionIdentifier,
            @Param("userIdentifier") Integer userIdentifier, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>addToUserSelection</code> method in a fluent style.
     */
    public static class AddToUserSelectionQueryParams extends HashMap<String, Object> {
        public AddToUserSelectionQueryParams uuid(final List<String> value) {
            put("uuid", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Add a user selection set
     * 
     * @param selection selection (optional)
     * @return ResponseEntity
     */
    @RequestLine("PUT /srv/api/0.1/userselections")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    ResponseEntity createUserSelectionType(Selection selection);

    /**
     * Remove items to a user selection set
     * 
     * @param selectionIdentifier Selection identifier (required)
     * @param userIdentifier      User identifier (required)
     * @param uuid                One or more record UUIDs. If null, remove all.
     *                            (optional)
     * @return ResponseEntity
     */
    @RequestLine("DELETE /srv/api/0.1/userselections/{selectionIdentifier}/{userIdentifier}?uuid={uuid}")
    @Headers({ "Accept: application/json", })
    ResponseEntity deleteFromUserSelection(@Param("selectionIdentifier") Integer selectionIdentifier,
            @Param("userIdentifier") Integer userIdentifier, @Param("uuid") List<String> uuid);

    /**
     * Remove items to a user selection set
     * 
     * Note, this is equivalent to the other <code>deleteFromUserSelection</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link DeleteFromUserSelectionQueryParams} class that
     * allows for building up this map in a fluent style.
     * 
     * @param selectionIdentifier Selection identifier (required)
     * @param userIdentifier      User identifier (required)
     * @param queryParams         Map of query parameters as name-value pairs
     *                            <p>
     *                            The following elements may be specified in the
     *                            query map:
     *                            </p>
     *                            <ul>
     *                            <li>uuid - One or more record UUIDs. If null,
     *                            remove all. (optional)</li>
     *                            </ul>
     * @return ResponseEntity
     */
    @RequestLine("DELETE /srv/api/0.1/userselections/{selectionIdentifier}/{userIdentifier}?uuid={uuid}")
    @Headers({ "Accept: application/json", })
    ResponseEntity deleteFromUserSelection(@Param("selectionIdentifier") Integer selectionIdentifier,
            @Param("userIdentifier") Integer userIdentifier, @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deleteFromUserSelection</code> method in a fluent style.
     */
    public static class DeleteFromUserSelectionQueryParams extends HashMap<String, Object> {
        public DeleteFromUserSelectionQueryParams uuid(final List<String> value) {
            put("uuid", EncodingUtils.encodeCollection(value, "multi"));
            return this;
        }
    }

    /**
     * Remove a user selection set
     * 
     * @param selectionIdentifier Selection identifier (required)
     * @return ResponseEntity
     */
    @RequestLine("DELETE /srv/api/0.1/userselections/{selectionIdentifier}")
    @Headers({ "Accept: */*", })
    ResponseEntity deleteUserSelection(@Param("selectionIdentifier") Integer selectionIdentifier);

    /**
     * Get record in a user selection set
     * 
     * @param selectionIdentifier Selection identifier (required)
     * @param userIdentifier      User identifier (required)
     * @return List&lt;String&gt;
     */
    @RequestLine("GET /srv/api/0.1/userselections/{selectionIdentifier}/{userIdentifier}")
    @Headers({ "Accept: application/json", })
    List<String> getSelection1(@Param("selectionIdentifier") Integer selectionIdentifier,
            @Param("userIdentifier") Integer userIdentifier);

    /**
     * Get list of user selection sets
     * 
     * @return List&lt;Selection&gt;
     */
    @RequestLine("GET /srv/api/0.1/userselections")
    @Headers({ "Accept: application/json", })
    List<Selection> getUserSelectionType();

    /**
     * Update a user selection set
     * 
     * @param selectionIdentifier Selection identifier (required)
     * @param selection           selection (optional)
     * @return ResponseEntity
     */
    @RequestLine("PUT /srv/api/0.1/userselections/{selectionIdentifier}")
    @Headers({ "Content-Type: application/json", "Accept: */*", })
    ResponseEntity updateUserSelection(@Param("selectionIdentifier") Integer selectionIdentifier, Selection selection);
}
