package org.fao.geonet.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApiClient;
import org.fao.geonet.EncodingUtils;
import org.fao.geonet.openapi.model.Group;
import org.fao.geonet.openapi.model.User;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

public interface GroupsApi extends ApiClient.Api {

    /**
     * Add a group Return the identifier of the group created.
     * 
     * @param group Group details (optional)
     * @return Integer
     */
    @RequestLine("PUT /srv/api/0.1/groups")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    Integer addGroup(Group group);

    /**
     * Remove a group Remove a group by first removing sharing settings, link to
     * users and finally reindex all affected records.
     * 
     * @param groupIdentifier Group identifier. (required)
     * @param force           Force removal even if records are assigned to that
     *                        group. (optional, default to false)
     */
    @RequestLine("DELETE /srv/api/0.1/groups/{groupIdentifier}?force={force}")
    @Headers({ "Accept: application/json", })
    void deleteGroup(@Param("groupIdentifier") Integer groupIdentifier, @Param("force") Boolean force);

    /**
     * Remove a group Remove a group by first removing sharing settings, link to
     * users and finally reindex all affected records. Note, this is equivalent to
     * the other <code>deleteGroup</code> method, but with the query parameters
     * collected into a single Map parameter. This is convenient for services with
     * optional query parameters, especially when used with the
     * {@link DeleteGroupQueryParams} class that allows for building up this map in
     * a fluent style.
     * 
     * @param groupIdentifier Group identifier. (required)
     * @param queryParams     Map of query parameters as name-value pairs
     *                        <p>
     *                        The following elements may be specified in the query
     *                        map:
     *                        </p>
     *                        <ul>
     *                        <li>force - Force removal even if records are assigned
     *                        to that group. (optional, default to false)</li>
     *                        </ul>
     */
    @RequestLine("DELETE /srv/api/0.1/groups/{groupIdentifier}?force={force}")
    @Headers({ "Accept: application/json", })
    void deleteGroup(@Param("groupIdentifier") Integer groupIdentifier,
            @QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>deleteGroup</code> method in a fluent style.
     */
    public static class DeleteGroupQueryParams extends HashMap<String, Object> {
        public DeleteGroupQueryParams force(final Boolean value) {
            put("force", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Get the group logo image. If last-modified header is present it is used to
     * check if the logo has been modified since the header date. If it hasn&#39;t
     * been modified returns an empty 304 Not Modified response. If modified returns
     * the image. If the group has no logo then returns a transparent 1x1 px PNG
     * image.
     * 
     * @param groupId Group identifier (required)
     */
    @RequestLine("GET /srv/api/0.1/groups/{groupId}/logo")
    @Headers({ "Accept: */*", })
    void get(@Param("groupId") Integer groupId);

    /**
     * Get group Return the requested group details.
     * 
     * @param groupIdentifier Group identifier (required)
     * @return Group
     */
    @RequestLine("GET /srv/api/0.1/groups/{groupIdentifier}")
    @Headers({ "Accept: application/json", })
    Group getGroup(@Param("groupIdentifier") Integer groupIdentifier);

    /**
     * Get group users
     * 
     * @param groupIdentifier Group identifier (required)
     * @return List&lt;User&gt;
     */
    @RequestLine("GET /srv/api/0.1/groups/{groupIdentifier}/users")
    @Headers({ "Accept: application/json", })
    List<User> getGroupUsers(@Param("groupIdentifier") Integer groupIdentifier);

    /**
     * Get groups The catalog contains one or more groups. By default, there is 3
     * reserved groups (Internet, Intranet, Guest) and a sample
     * group.&lt;br/&gt;This service returns all catalog groups when not
     * authenticated or when current is user is an administrator. The list can
     * contains or not reserved groups depending on the parameters.&lt;br/&gt;When
     * authenticated, return user groups optionally filtered on a specific user
     * profile.
     * 
     * @param withReservedGroup Including Internet, Intranet, Guest groups or not
     *                          (optional, default to false)
     * @param profile           For a specific profile (optional)
     * @return List&lt;Group&gt;
     */
    @RequestLine("GET /srv/api/0.1/groups?withReservedGroup={withReservedGroup}&profile={profile}")
    @Headers({ "Accept: application/json", })
    List<Group> getGroups(@Param("withReservedGroup") Boolean withReservedGroup, @Param("profile") String profile);

    /**
     * Get groups The catalog contains one or more groups. By default, there is 3
     * reserved groups (Internet, Intranet, Guest) and a sample
     * group.&lt;br/&gt;This service returns all catalog groups when not
     * authenticated or when current is user is an administrator. The list can
     * contains or not reserved groups depending on the parameters.&lt;br/&gt;When
     * authenticated, return user groups optionally filtered on a specific user
     * profile. Note, this is equivalent to the other <code>getGroups</code> method,
     * but with the query parameters collected into a single Map parameter. This is
     * convenient for services with optional query parameters, especially when used
     * with the {@link GetGroupsQueryParams} class that allows for building up this
     * map in a fluent style.
     * 
     * @param queryParams Map of query parameters as name-value pairs
     *                    <p>
     *                    The following elements may be specified in the query map:
     *                    </p>
     *                    <ul>
     *                    <li>withReservedGroup - Including Internet, Intranet,
     *                    Guest groups or not (optional, default to false)</li>
     *                    <li>profile - For a specific profile (optional)</li>
     *                    </ul>
     * @return List&lt;Group&gt;
     */
    @RequestLine("GET /srv/api/0.1/groups?withReservedGroup={withReservedGroup}&profile={profile}")
    @Headers({ "Accept: application/json", })
    List<Group> getGroups(@QueryMap(encoded = true) Map<String, Object> queryParams);

    /**
     * A convenience class for generating query parameters for the
     * <code>getGroups</code> method in a fluent style.
     */
    public static class GetGroupsQueryParams extends HashMap<String, Object> {
        public GetGroupsQueryParams withReservedGroup(final Boolean value) {
            put("withReservedGroup", EncodingUtils.encode(value));
            return this;
        }

        public GetGroupsQueryParams profile(final String value) {
            put("profile", EncodingUtils.encode(value));
            return this;
        }
    }

    /**
     * Update a group
     * 
     * @param groupIdentifier Group identifier (required)
     * @param group           Group details (optional)
     */
    @RequestLine("PUT /srv/api/0.1/groups/{groupIdentifier}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    void updateGroup(@Param("groupIdentifier") Integer groupIdentifier, Group group);
}
