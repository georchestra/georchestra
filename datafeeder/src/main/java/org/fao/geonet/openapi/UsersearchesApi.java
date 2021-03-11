package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.PaginatedUserSearchResponse;
import org.fao.geonet.openapi.model.UserSearchDto;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface UsersearchesApi extends ApiClient.Api {

    /**
     * Creates a user search Creates a user search.
     * 
     * @param userSearchDto User search details (optional)
     * @return Integer
     */
    @RequestLine("PUT /srv/api/0.1/usersearches")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    Integer createUserCustomSearch(UserSearchDto userSearchDto);

    /**
     * Delete a user search Deletes a user search by identifier.
     * 
     * @param searchIdentifier Search identifier. (required)
     * @return String
     */
    @RequestLine("DELETE /srv/api/0.1/usersearches/{searchIdentifier}")
    @Headers({ "Accept: application/json", })
    String deleteUserCustomSearch(@Param("searchIdentifier") Integer searchIdentifier);

    /**
     * Get user custom searches for all users (no paginated)
     * 
     * @param featuredType Featured type search. (optional)
     * @return List&lt;UserSearchDto&gt;
     */
    @RequestLine("GET /srv/api/0.1/usersearches/all?featuredType={featuredType}")
    @Headers({ "Accept: application/json", })
    List<UserSearchDto> getAllUserCustomSearches(@Param("featuredType") String featuredType);

    /**
     * Get user custom searches for all users (no paginated)
     * 
     * Note, this is equivalent to the other <code>getAllUserCustomSearches</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link GetAllUserCustomSearchesQueryParams} class that
     * allows for building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>featuredType - Featured type search. (optional)</li>
     *                    </ul>
     * @return List&lt;UserSearchDto&gt;
     */
    @RequestLine("GET /srv/api/0.1/usersearches/all?featuredType={featuredType}")
    @Headers({ "Accept: application/json", })
    List<UserSearchDto> getAllUserCustomSearches(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getAllUserCustomSearches</code> method in a fluent style.
     */
    public static class GetAllUserCustomSearchesQueryParams extends HashMap<String, Object> {
        public GetAllUserCustomSearchesQueryParams featuredType(final String value) {
            put("featuredType", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get user custom searches for all users (paginated)
     * 
     * @param featuredType Featured type search. (optional)
     * @param search       search (optional)
     * @param offset       From page (optional, default to 0)
     * @param limit        Number of records to return (optional, default to 10)
     * @return PaginatedUserSearchResponse
     */
    @RequestLine("GET /srv/api/0.1/usersearches/allpaginated?featuredType={featuredType}&search={search}&offset={offset}&limit={limit}")
    @Headers({ "Accept: application/json", })
    PaginatedUserSearchResponse getAllUserCustomSearchesPaginated(@Param("featuredType") String featuredType,
            @Param("search") String search, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * Get user custom searches for all users (paginated)
     * 
     * Note, this is equivalent to the other
     * <code>getAllUserCustomSearchesPaginated</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link GetAllUserCustomSearchesPaginatedQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>featuredType - Featured type search. (optional)</li>
     *                    <li>search - search (optional)</li>
     *                    <li>offset - From page (optional, default to 0)</li>
     *                    <li>limit - Number of records to return (optional, default
     *                    to 10)</li>
     *                    </ul>
     * @return PaginatedUserSearchResponse
     */
    @RequestLine("GET /srv/api/0.1/usersearches/allpaginated?featuredType={featuredType}&search={search}&offset={offset}&limit={limit}")
    @Headers({ "Accept: application/json", })
    PaginatedUserSearchResponse getAllUserCustomSearchesPaginated(
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getAllUserCustomSearchesPaginated</code> method in a fluent style.
     */
    public static class GetAllUserCustomSearchesPaginatedQueryParams extends HashMap<String, Object> {
        public GetAllUserCustomSearchesPaginatedQueryParams featuredType(final String value) {
            put("featuredType", EncodingUtils.encode(value));
            return this;
        }

        public GetAllUserCustomSearchesPaginatedQueryParams search(final String value) {
            put("search", EncodingUtils.encode(value));
            return this;
        }

        public GetAllUserCustomSearchesPaginatedQueryParams offset(final Integer value) {
            put("offset", EncodingUtils.encode(value));
            return this;
        }

        public GetAllUserCustomSearchesPaginatedQueryParams limit(final Integer value) {
            put("limit", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get featured user custom searches
     * 
     * @param type Number of records to return (optional)
     * @return List&lt;UserSearchDto&gt;
     */
    @RequestLine("GET /srv/api/0.1/usersearches/featured?type={type}")
    @Headers({ "Accept: application/json", })
    List<UserSearchDto> getFeaturedUserCustomSearches(@Param("type") String type);

    /**
     * Get featured user custom searches
     * 
     * Note, this is equivalent to the other
     * <code>getFeaturedUserCustomSearches</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link GetFeaturedUserCustomSearchesQueryParams} class that allows for
     * building up this map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>type - Number of records to return (optional)</li>
     *                    </ul>
     * @return List&lt;UserSearchDto&gt;
     */
    @RequestLine("GET /srv/api/0.1/usersearches/featured?type={type}")
    @Headers({ "Accept: application/json", })
    List<UserSearchDto> getFeaturedUserCustomSearches(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getFeaturedUserCustomSearches</code> method in a fluent style.
     */
    public static class GetFeaturedUserCustomSearchesQueryParams extends HashMap<String, Object> {
        public GetFeaturedUserCustomSearchesQueryParams type(final String value) {
            put("type", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get custom search
     * 
     * @param searchIdentifier User search identifier (required)
     * @return UserSearchDto
     */
    @RequestLine("GET /srv/api/0.1/usersearches/{searchIdentifier}")
    @Headers({ "Accept: application/json", })
    UserSearchDto getUserCustomSearch(@Param("searchIdentifier") Integer searchIdentifier);

    /**
     * Get user custom searches
     * 
     * @return List&lt;UserSearchDto&gt;
     */
    @RequestLine("GET /srv/api/0.1/usersearches")
    @Headers({ "Accept: application/json", })
    List<UserSearchDto> getUserCustomSearches();

    /**
     * Update a user search
     * 
     * @param searchIdentifier User search identifier (required)
     * @param userSearchDto    User search details (optional)
     */
    @RequestLine("PUT /srv/api/0.1/usersearches/{searchIdentifier}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    void updateCustomUserSearch(@Param("searchIdentifier") Integer searchIdentifier, UserSearchDto userSearchDto);
}
