/**
 *
 */
package org.georchestra.ldapadmin.ws.backoffice.groups;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ds.*;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.GroupFactory;
import org.georchestra.ldapadmin.dto.GroupSchema;
import org.georchestra.ldapadmin.ws.backoffice.users.UserRule;
import org.georchestra.ldapadmin.ws.backoffice.utils.RequestUtil;
import org.georchestra.ldapadmin.ws.backoffice.utils.ResponseUtil;
import org.georchestra.lib.file.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Web Services to maintain the Groups information.
 *
 * @author Mauricio Pazos
 *
 */

@Controller

public class GroupsController {

	private static final Log LOG = LogFactory.getLog(GroupsController.class.getName());

	private static final String BASE_MAPPING = "/private";
	private static final String BASE_RESOURCE = "groups";
	private static final String REQUEST_MAPPING = BASE_MAPPING + "/" + BASE_RESOURCE;


	private static final String DUPLICATED_COMMON_NAME = "duplicated_common_name";

	private static final String NOT_FOUND = "not_found";

	private static final String USER_NOT_FOUND = "user_not_found";

	public static final String VIRTUAL_TEMPORARY_GROUP_NAME = "TEMPORARY-USER";
	private static final String VIRTUAL_TEMPORARY_GROUP_DESCRIPTION = "Virtual group that contains all temporary users";


	@Autowired
	private AccountDao accountDao;

	private GroupDao groupDao;
	private ProtectedUserFilter filter;

        /**
     * Builds a JSON response in case of error.
     *
     * @param mesg
     *            a descriptive message of the encountered error.
     * @return a string of the response.
     *
     * TODO: This code sounds pretty similar to what is done in
     * ResponseUtil.java:buildResponseMessage() and might deserve
     * a refactor.
     */

	private String buildErrorResponse(String mesg) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("success", false);
		map.put("error_message", mesg);
		return new JSONObject(map).toString();
	}

	@Autowired
	public GroupsController( GroupDao dao, UserRule userRule){
		this.groupDao = dao;
		this.filter = new ProtectedUserFilter( userRule.getListUidProtected() );
	}

	/**
	 * Returns all groups. Each groups will contains its list of users.
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value=REQUEST_MAPPING, method=RequestMethod.GET)
	public void findAll( HttpServletRequest request, HttpServletResponse response ) throws IOException{

		try {
			List<Group> list = this.groupDao.findAll();

			GroupListResponse listResponse = new GroupListResponse(list, this.filter);

			JSONArray jsonList = listResponse.toJsonArray();
			jsonList.put(this.extractTemporaryGroupInformation());

			ResponseUtil.buildResponse(response, jsonList.toString(4), HttpServletResponse.SC_OK);

		} catch (Exception e) {

			LOG.error(e.getMessage());
			ResponseUtil.buildResponse(response, buildErrorResponse(e.getMessage()),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

			throw new IOException(e);
		}


	}

	/**
	 * Returns the detailed information of the group, with its list of users.
	 *
	 * <p>
	 * If the group identifier is not present in the ldap store an {@link IOException} will be throw.
	 * </p>
	 * <p>
	 * URL Format: [BASE_MAPPING]/groups/{cn}
	 * </p>
	 * <p>
	 * Example: [BASE_MAPPING]/groups/group44
	 * </p>
	 *
	 * @param request the request includes the group identifier required
	 * @param response Returns the detailed information of the group as json
	 * @throws IOException
	 */
	@RequestMapping(value=REQUEST_MAPPING+"/*", method=RequestMethod.GET)
	public void findByCN( HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {

		String cn = RequestUtil.getKeyFromPathVariable(request);

		if(cn.equals(GroupsController.VIRTUAL_TEMPORARY_GROUP_NAME))
			ResponseUtil.buildResponse(response, this.extractTemporaryGroupInformation().toString(), HttpServletResponse.SC_OK);

		// searches the group
		Group group = null;
		try {
			group = this.groupDao.findByCommonName(cn);

		} catch (NotFoundException e) {

			ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(Boolean.FALSE, NOT_FOUND), HttpServletResponse.SC_NOT_FOUND);

			return;

		} catch (DataServiceException e) {
			LOG.error(e.getMessage());
			ResponseUtil.buildResponse(response, buildErrorResponse(e.getMessage()),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		} catch (IllegalArgumentException e) {
	          LOG.error(e.getMessage());
              ResponseUtil.buildResponse(response, buildErrorResponse(e.getMessage()), HttpServletResponse.SC_NOT_FOUND);
	          return;
		}

		// sets the group data in the response object
		GroupResponse groupResponse = new GroupResponse(group, this.filter);

		String jsonGroup = groupResponse.asJsonString();

		ResponseUtil.buildResponse(response, jsonGroup, HttpServletResponse.SC_OK);

	}

	private JSONObject extractTemporaryGroupInformation() throws JSONException {
		JSONObject res = new JSONObject();
		res.put("cn", GroupsController.VIRTUAL_TEMPORARY_GROUP_NAME);
		res.put("description", GroupsController.VIRTUAL_TEMPORARY_GROUP_DESCRIPTION);

		// Search temporary users in LDAP
		JSONArray temporaryUsers = new JSONArray();
		for(Account a : this.accountDao.findByShadowExpire())
			temporaryUsers.put(a.getUid());
		res.put("users",temporaryUsers);

		return res;
	}

	/**
	 *
	 * <p>
	 * Creates a new group.
	 * </p>
	 *
	 * <pre>
	 * <b>Request</b>
	 *
	 * group data:
	 * {
     *   "cn": "Name of the group"
     *   "description": "Description for the group"
     *   }
	 * </pre>
	 * <pre>
	 * <b>Response</b>
	 *
	 * <b>- Success case</b>
	 *
	 * {
	 *  "cn": "Name of the group",
	 *  "description": "Description for the group"
	 * }
	 * </pre>
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value=REQUEST_MAPPING, method=RequestMethod.POST)
	public void create( HttpServletRequest request, HttpServletResponse response ) throws IOException{

		try{

			Group group = createGroupFromRequestBody(request.getInputStream());

			this.groupDao.insert( group );

			GroupResponse groupResponse = new GroupResponse(group, this.filter);

			String jsonResponse = groupResponse.asJsonString();

			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_OK);


		} catch (DuplicatedCommonNameException emailex){

			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, DUPLICATED_COMMON_NAME);

			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);

		} catch (DataServiceException dsex){
			LOG.error(dsex.getMessage());
			ResponseUtil.buildResponse(response, buildErrorResponse(dsex.getMessage()),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(dsex);
		}
	}


	/**
	 * Deletes the group.
	 *
	 * The request format is:
	 * <pre>
	 * [BASE_MAPPING]/groups/{cn}
	 *
	 * Where <b>cn</b> is the name of group to delete.
	 * </pre>
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value=REQUEST_MAPPING + "/*", method=RequestMethod.DELETE)
	public void delete( HttpServletRequest request, HttpServletResponse response) throws IOException{
		try{
			String cn = RequestUtil.getKeyFromPathVariable(request);

			this.groupDao.delete(cn);

			ResponseUtil.writeSuccess(response);

		} catch (NotFoundException e) {
	          LOG.error(e.getMessage());
	            ResponseUtil.buildResponse(response, buildErrorResponse(e.getMessage()),
	                    HttpServletResponse.SC_NOT_FOUND);
		} catch (Exception e){
			LOG.error(e.getMessage());
			ResponseUtil.buildResponse(response, buildErrorResponse(e.getMessage()),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		}
	}

	/**
	 * Modifies the group using the fields provided in the request body.
	 * <p>
	 * The fields that are not present in the parameters will remain untouched in the LDAP store.
	 * </p>
	 * <pre>
	 * The request format is:
	 * [BASE_MAPPING]/groups/{cn}
	 *
	 * Where <b>cn</b> is the name of group to update.
	 * </pre>
	 * <p>
	 * The request body should contains a the fields to modify using the JSON syntax.
	 * </p>
	 * <p>
	 * Example:
	 * </p>
	 * <pre>
	 * <b>Request</b>
	 * [BASE_MAPPING]/groups/users
	 *
	 * <b>Body request: </b>
	 * group data:
	 * {
     *   "cn": "newName"
     *   "description": "new Description"
     *   }
	 *
	 * </pre>
	 *
	 * @param request [BASE_MAPPING]/groups/{cn}  body request {"cn": value1, "description": value2 }
	 * @param response
	 *
	 * @throws IOException if the uid does not exist or fails to access to the LDAP store.
	 */
	@RequestMapping(value=REQUEST_MAPPING+ "/*", method=RequestMethod.PUT)
	public void update( HttpServletRequest request, HttpServletResponse response) throws IOException{

		String cn = RequestUtil.getKeyFromPathVariable(request);

		// searches the group
		Group group = null;
		try {
			group = this.groupDao.findByCommonName(cn);

		} catch (NotFoundException e) {

			ResponseUtil.writeError(response, NOT_FOUND);

			return;
		} catch (DataServiceException e) {
		    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		}

		// modifies the group data
		try{
			final Group modified = modifyGroup( group, request.getInputStream());

			this.groupDao.update(cn, modified);

			ResponseUtil.writeSuccess(response);

		}  catch (NotFoundException e) {

			ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(Boolean.FALSE, NOT_FOUND), HttpServletResponse.SC_NOT_FOUND);

			return;

		} catch (DuplicatedCommonNameException e) {

			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, DUPLICATED_COMMON_NAME);

			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);

			return;

		}  catch (DataServiceException e){
			LOG.error(e.getMessage());
			ResponseUtil.buildResponse(response, buildErrorResponse(e.getMessage()),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		}
	}

	/**
	 * Updates the users of group. This method will add or delete the group of users from the list of groups.
	 *
	 * @param request	request [BASE_MAPPING]/groups_users body request {"users": [u1,u2,u3], "PUT": [g1,g2], "DELETE":[g3,g4] }
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value=BASE_MAPPING+ "/groups_users", method=RequestMethod.POST)
	public void updateUsers( HttpServletRequest request, HttpServletResponse response) throws IOException{

		try{

			ServletInputStream is = request.getInputStream();
			String strGroup = FileUtils.asString(is);
			JSONObject json = new JSONObject(strGroup);

			List<String> users = createUserList(json, "users");

			List<String> putGroup = createUserList(json, "PUT");
			this.groupDao.addUsersInGroups(putGroup, users);

			List<String> deleteGroup = createUserList(json, "DELETE");
			this.groupDao.deleteUsersInGroups(deleteGroup, users);

			ResponseUtil.writeSuccess(response);

		}  catch (NotFoundException e) {

			ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(Boolean.FALSE, USER_NOT_FOUND), HttpServletResponse.SC_NOT_FOUND);

			return;

		}  catch (DataServiceException e){
			LOG.error(e.getMessage());
			ResponseUtil.buildResponse(response, buildErrorResponse(e.getMessage()),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		} catch (JSONException e) {
			LOG.error(e.getMessage());
			ResponseUtil.buildResponse(response, buildErrorResponse(e.getMessage()),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		}
	}



	private List<String> createUserList(JSONObject json, String arrayKey) throws IOException {


		try {

			List<String> list = new LinkedList<String>();

			JSONArray jsonArray = json.getJSONArray(arrayKey);
			for (int i = 0; i < jsonArray.length(); i++) {

				list.add(jsonArray.getString(i));
			}

			return list;

		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new IOException(e);
		}
	}

	/**
	 * Modifies the original field using the values in the inputStream.
	 *
	 * @param group group to modify
	 * @param inputStream contains the new values
	 *
	 * @return the {@link Group} modified
	 */
	private Group modifyGroup(Group group, ServletInputStream inputStream) throws IOException{

		String strGroup = FileUtils.asString(inputStream);
		JSONObject json;
		try {
			json = new JSONObject(strGroup);
		} catch (JSONException e) {
			LOG.error(e.getMessage());
			throw new IOException(e);
		}

		String cn = RequestUtil.getFieldValue(json, GroupSchema.COMMON_NAME_KEY);
		if (cn != null) {
			group.setName(cn);
		}

		String description = RequestUtil.getFieldValue(json, GroupSchema.DESCRIPTION_KEY);
		if (description != null) {
			group.setDescription(description);
		}

		return group;
	}

	private Group createGroupFromRequestBody(ServletInputStream is) throws IOException {
		try {
			String strGroup = FileUtils.asString(is);
			JSONObject json = new JSONObject(strGroup);

			String commonName = RequestUtil.getFieldValue(json, GroupSchema.COMMON_NAME_KEY);
			if(commonName == null){
				throw new IllegalArgumentException(GroupSchema.COMMON_NAME_KEY + " is required" );
			}

			String description = RequestUtil.getFieldValue(json, GroupSchema.DESCRIPTION_KEY);

			Group g = GroupFactory.create(commonName, description);

			return g;

		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new IOException(e);
		}
	}

	/**
	 * Method used for testing convenience.
	 * @param gd
	 */
    public void setGroupDao(GroupDao gd) {
        groupDao = gd;
    }

	/**
	 * Method used for testing convenience.
	 * @param ad
	 */
    public void setAccountDao(AccountDao ad) {
        this.accountDao = ad;
    }



}
