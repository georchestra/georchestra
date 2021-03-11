package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.OwnerResponse;
import org.fao.geonet.openapi.model.PasswordUpdateParameter;
import org.fao.geonet.openapi.model.ResponseEntity;
import org.fao.geonet.openapi.model.TransferRequest;
import org.fao.geonet.openapi.model.User;
import org.fao.geonet.openapi.model.UserDto;
import org.fao.geonet.openapi.model.UserGroup;
import org.fao.geonet.openapi.model.UserGroupsResponse;
import org.fao.geonet.openapi.model.UserRegisterDto;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface UsersApi extends ApiClient.Api {

    /**
     * Check if a user property already exist
     * 
     * @param property The user property to check (required)
     * @param exist    The value to search (optional)
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/users/properties/{property}?exist={exist}")
    @Headers({ "Accept: */*", })
    String checkUserPropertyExist(@Param("property") String property, @Param("exist") String exist);

    /**
     * Check if a user property already exist
     * 
     * Note, this is equivalent to the other <code>checkUserPropertyExist</code>
     * method, but with the query parameters collected into a single Map parameter.
     * This is convenient for services with optional query parameters, especially
     * when used with the {@link CheckUserPropertyExistQueryParams} class that
     * allows for building up this map in a fluent style.
     * 
     * @param property    The user property to check (required)
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>exist - The value to search (optional)</li>
     *                    </ul>
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/users/properties/{property}?exist={exist}")
    @Headers({ "Accept: */*", })
    String checkUserPropertyExist(@Param("property") String property,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>checkUserPropertyExist</code> method in a fluent style.
     */
    public static class CheckUserPropertyExistQueryParams extends HashMap<String, Object> {
        public CheckUserPropertyExistQueryParams exist(final String value) {
            put("exist", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Creates a user Creates a catalog user.
     * 
     * @param user userDto (optional)
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/users")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    String createUser(UserDto user);

    /**
     * Delete a user Deletes a catalog user by identifier.
     * 
     * @param userIdentifier User identifier. (required)
     * @return String
     */
    @RequestLine("DELETE /srv/api/0.1/users/{userIdentifier}")
    @Headers({ "Accept: application/json", })
    String deleteUser(@Param("userIdentifier") Integer userIdentifier);

    /**
     * Get owners Return users who actually owns one or more records.
     * 
     * @return List&lt;OwnerResponse&gt;
     */
    @RequestLine("GET /srv/api/0.1/users/owners")
    @Headers({ "Accept: application/json", })
    List<OwnerResponse> getOwners();

    /**
     * Get user
     * 
     * @param userIdentifier User identifier. (required)
     * @return User
     */
    @RequestLine("GET /srv/api/0.1/users/{userIdentifier}")
    @Headers({ "Accept: application/json", })
    User getUser(@Param("userIdentifier") Integer userIdentifier);

    /**
     * Get users
     * 
     * @return List&lt;User&gt;
     */
    @RequestLine("GET /srv/api/0.1/users")
    @Headers({ "Accept: application/json", })
    List<User> getUsers();

    /**
     * Create user account User is created with a registered user profile. username
     * field is ignored and the email is used as username. Password is sent by
     * email. Catalog administrator is also notified.
     * 
     * @param userRegisterDto User details (required)
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/user/actions/register")
    @Headers({ "Content-Type: application/json", "Accept: text/plain", })
    String registerUser(UserRegisterDto userRegisterDto);

    /**
     * Resets user password Resets the user password.
     * 
     * @param userIdentifier User identifier. (required)
     * @param password       Password to change. (optional)
     * @param password2      Password to change (repeat). (optional)
     * @return String
     */
    @RequestLine("POST /srv/api/0.1/users/{userIdentifier}/actions/forget-password?password={password}&password2={password2}")
    @Headers({ "Accept: application/json", })
    String resetUserPassword(@Param("userIdentifier") Integer userIdentifier, @Param("password") String password,
            @Param("password2") String password2);

    /**
     * Resets user password Resets the user password. Note, this is equivalent to
     * the other <code>resetUserPassword</code> method, but with the query
     * parameters collected into a single Map parameter. This is convenient for
     * services with optional query parameters, especially when used with the
     * {@link ResetUserPasswordQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param userIdentifier User identifier. (required)
     * @param queryParams    Map of query parameters as name-value pairs
     *                       <p>
     *                       The following elements may be specified in the query
     *                       map:
     *                       </p>
     *                       <ul>
     *                       <li>password - Password to change. (optional)</li>
     *                       <li>password2 - Password to change (repeat).
     *                       (optional)</li>
     *                       </ul>
     * @return String
     */
    @RequestLine("POST /srv/api/0.1/users/{userIdentifier}/actions/forget-password?password={password}&password2={password2}")
    @Headers({ "Accept: application/json", })
    String resetUserPassword(@Param("userIdentifier") Integer userIdentifier,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>resetUserPassword</code> method in a fluent style.
     */
    public static class ResetUserPasswordQueryParams extends HashMap<String, Object> {
        public ResetUserPasswordQueryParams password(final String value) {
            put("password", EncodingUtils.encode(value));
            return this;
        }

        public ResetUserPasswordQueryParams password2(final String value) {
            put("password2", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Retrieve all user groups
     * 
     * @return List&lt;UserGroupsResponse&gt;
     */
    @RequestLine("GET /srv/api/0.1/users/groups")
    @Headers({ "Accept: application/json", })
    List<UserGroupsResponse> retrieveAllUserGroups();

    /**
     * Retrieve user groups Retrieve the user groups.
     * 
     * @param userIdentifier User identifier. (required)
     * @return List&lt;UserGroup&gt;
     */
    @RequestLine("GET /srv/api/0.1/users/{userIdentifier}/groups")
    @Headers({ "Accept: application/json", })
    List<UserGroup> retrieveUserGroups(@Param("userIdentifier") Integer userIdentifier);

    /**
     * Transfer privileges
     * 
     * @param transfer transfer (required)
     * @return ResponseEntity
     */
    @RequestLine("PUT /srv/api/0.1/users/owners")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    ResponseEntity saveOwners(TransferRequest transfer);

    /**
     * Send user password reminder by email An email is sent to the requested user
     * with a link to reset his password. User MUST have an email to get the link.
     * LDAP users will not be able to retrieve their password using this service.
     * 
     * @param username The user name (required)
     * @return String
     */
    @RequestLine("GET /srv/api/0.1/user/{username}/actions/forgot-password")
    @Headers({ "Accept: text/plain", })
    String sendPasswordByEmail(@Param("username") String username);

    /**
     * Update user password Get a valid changekey by email first and then update
     * your password.
     * 
     * @param username             The user name (required)
     * @param passwordAndChangeKey The new password and a valid change key
     *                             (required)
     * @return String
     */
    @RequestLine("PATCH /srv/api/0.1/user/{username}")
    @Headers({ "Content-Type: application/json", "Accept: text/plain", })
    String updatePassword(@Param("username") String username, PasswordUpdateParameter passwordAndChangeKey);

    /**
     * Update a user Updates a catalog user.
     * 
     * @param userIdentifier User identifier. (required)
     * @param user           userDto (optional)
     * @return String
     */
    @RequestLine("PUT /srv/api/0.1/users/{userIdentifier}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    String updateUser(@Param("userIdentifier") Integer userIdentifier, UserDto user);
}
