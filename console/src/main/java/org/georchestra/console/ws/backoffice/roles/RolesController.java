/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.console.ws.backoffice.roles;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.DuplicatedCommonNameException;
import org.georchestra.console.ds.RoleDao;
import org.georchestra.console.ds.ProtectedUserFilter;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.Role;
import org.georchestra.console.dto.RoleFactory;
import org.georchestra.console.dto.RoleSchema;
import org.georchestra.console.ws.backoffice.users.UserRule;
import org.georchestra.console.ws.backoffice.utils.RequestUtil;
import org.georchestra.console.ws.backoffice.utils.ResponseUtil;
import org.georchestra.lib.file.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.ldap.NameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Web Services to maintain the Roles information.
 *
 * @author Mauricio Pazos
 *
 */

@Controller

public class RolesController {

	private static final Log LOG = LogFactory.getLog(RolesController.class.getName());

	private static final String BASE_MAPPING = "/private";
	private static final String BASE_RESOURCE = "roles";
	private static final String REQUEST_MAPPING = BASE_MAPPING + "/" + BASE_RESOURCE;


	private static final String DUPLICATED_COMMON_NAME = "duplicated_common_name";

	private static final String NOT_FOUND = "not_found";

	private static final String USER_NOT_FOUND = "user_not_found";

	public static final String VIRTUAL_TEMPORARY_ROLE_NAME = "TEMPORARY";
	private static final String VIRTUAL_TEMPORARY_ROLE_DESCRIPTION = "Virtual role that contains all temporary users";


	@Autowired
	private AccountDao accountDao;

	private RoleDao roleDao;
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
	public RolesController( RoleDao dao, UserRule userRule){
		this.roleDao = dao;
		this.filter = new ProtectedUserFilter( userRule.getListUidProtected() );
	}

	/**
	 * Returns all roles. Each roles will contains its list of users.
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value=REQUEST_MAPPING, method=RequestMethod.GET)
	public void findAll( HttpServletRequest request, HttpServletResponse response ) throws IOException{

		try {
			List<Role> list = this.roleDao.findAll();

			RoleListResponse listResponse = new RoleListResponse(list, this.filter);

			JSONArray jsonList = listResponse.toJsonArray();
			jsonList.put(this.extractTemporaryRoleInformation());

			ResponseUtil.buildResponse(response, jsonList.toString(4), HttpServletResponse.SC_OK);

		} catch (Exception e) {

			LOG.error(e.getMessage());
			ResponseUtil.buildResponse(response, buildErrorResponse(e.getMessage()),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

			throw new IOException(e);
		}


	}

	/**
	 * Returns the detailed information of the role, with its list of users.
	 *
	 * <p>
	 * If the role identifier is not present in the ldap store an {@link IOException} will be throw.
	 * </p>
	 * <p>
	 * URL Format: [BASE_MAPPING]/roles/{cn}
	 * </p>
	 * <p>
	 * Example: [BASE_MAPPING]/roles/role44
	 * </p>
	 *
	 * @param response Returns the detailed information of the role as json
	 * @param cn Comon name of role
	 * @throws IOException
	 */
	@RequestMapping(value=REQUEST_MAPPING+"/{cn:.+}", method=RequestMethod.GET)
	public void findByCN(HttpServletResponse response, @PathVariable String cn) throws IOException, JSONException {

		String jsonRole;

		if(cn.equals(RolesController.VIRTUAL_TEMPORARY_ROLE_NAME)) {
			jsonRole = this.extractTemporaryRoleInformation().toString();
		} else {

			// searches the role
			Role role;
			try {
				role = this.roleDao.findByCommonName(cn);

			} catch (NameNotFoundException e) {

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

			// sets the role data in the response object
			jsonRole = (new RoleResponse(role, this.filter)).asJsonString();

		}
		ResponseUtil.buildResponse(response, jsonRole, HttpServletResponse.SC_OK);
	}

	private JSONObject extractTemporaryRoleInformation() throws JSONException {
		JSONObject res = new JSONObject();
		res.put("cn", RolesController.VIRTUAL_TEMPORARY_ROLE_NAME);
		res.put("description", RolesController.VIRTUAL_TEMPORARY_ROLE_DESCRIPTION);

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
	 * Creates a new role.
	 * </p>
	 *
	 * <pre>
	 * <b>Request</b>
	 *
	 * role data:
	 * {
     *   "cn": "Name of the role"
     *   "description": "Description for the role"
     *   }
	 * </pre>
	 * <pre>
	 * <b>Response</b>
	 *
	 * <b>- Success case</b>
	 *
	 * {
	 *  "cn": "Name of the role",
	 *  "description": "Description for the role"
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

			Role role = createRoleFromRequestBody(request.getInputStream());

			this.roleDao.insert( role );

			RoleResponse roleResponse = new RoleResponse(role, this.filter);

			String jsonResponse = roleResponse.asJsonString();

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
	 * Deletes the role.
	 *
	 * The request format is:
	 * <pre>
	 * [BASE_MAPPING]/roles/{cn}
	 *
	 * Where <b>cn</b> is the name of role to delete.
	 * </pre>
	 *
	 * @param response
	 * @param cn Common name of role to delete
	 * @throws IOException
	 */
	@RequestMapping(value = REQUEST_MAPPING + "/{cn:.+}", method = RequestMethod.DELETE)
	public void delete(HttpServletResponse response, @PathVariable String cn)
			throws IOException {
		try {

			this.roleDao.delete(cn);

			ResponseUtil.writeSuccess(response);

		} catch (NameNotFoundException e) {
			LOG.error(e.getMessage());
			ResponseUtil.buildResponse(response, buildErrorResponse(e.getMessage()),
					HttpServletResponse.SC_NOT_FOUND);
		} catch (DataServiceException e) {
			LOG.error(e.getMessage());
			ResponseUtil.buildResponse(response, buildErrorResponse(e.getMessage()),
					HttpServletResponse.SC_BAD_REQUEST);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			ResponseUtil.buildResponse(response, buildErrorResponse(e.getMessage()),
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		}
	}

	/**
	 * Modifies the role using the fields provided in the request body.
	 * <p>
	 * The fields that are not present in the parameters will remain untouched
	 * in the LDAP store.
	 * </p>
	 * 
	 * <pre>
	 * The request format is:
	 * [BASE_MAPPING]/roles/{cn}
	 *
	 * Where <b>cn</b> is the name of role to update.
	 * </pre>
	 * <p>
	 * The request body should contains a the fields to modify using the JSON
	 * syntax.
	 * </p>
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * <b>Request</b>
	 * [BASE_MAPPING]/roles/users
	 *
	 * <b>Body request: </b>
	 * role data:
	 * {
	 *   "cn": "newName"
	 *   "description": "new Description"
	 *   }
	 *
	 * </pre>
	 *
	 * @param request
	 *            [BASE_MAPPING]/roles/{cn} body request {"cn": value1,
	 *            "description": value2 }
	 * @param response
	 *
	 * @throws IOException
	 *             if the uid does not exist or fails to access to the LDAP
	 *             store.
	 */
	@RequestMapping(value=REQUEST_MAPPING+ "/{cn:.+}", method=RequestMethod.PUT)
	public void update( HttpServletRequest request, HttpServletResponse response, @PathVariable String cn) throws IOException{

		// searches the role
		Role role;
		try {
			role = this.roleDao.findByCommonName(cn);
		} catch (NameNotFoundException e) {
			ResponseUtil.writeError(response, NOT_FOUND);
			return;
		} catch (DataServiceException e) {
		    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		}

		// modifies the role data
		try{
			final Role modified = modifyRole( role, request.getInputStream());

			this.roleDao.update(cn, modified);

			RoleResponse roleResponse = new RoleResponse(role, this.filter);

			String jsonResponse = roleResponse.asJsonString();

			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_OK);

			ResponseUtil.writeSuccess(response);

		}  catch (NameNotFoundException e) {

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
	 * Updates the users of role. This method will add or delete the role of users from the list of roles.
	 *
	 * @param request	request [BASE_MAPPING]/roles_users body request {"users": [u1,u2,u3], "PUT": [g1,g2], "DELETE":[g3,g4] }
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value=BASE_MAPPING+ "/roles_users", method=RequestMethod.POST)
	public void updateUsers( HttpServletRequest request, HttpServletResponse response) throws IOException{

		try{

			ServletInputStream is = request.getInputStream();
			String strRole = FileUtils.asString(is);
			JSONObject json = new JSONObject(strRole);

			List<String> users = createUserList(json, "users");

			List<String> putRole = createUserList(json, "PUT");
			this.roleDao.addUsersInRoles(putRole, users, request.getHeader("sec-username"));

			List<String> deleteRole = createUserList(json, "DELETE");
			this.roleDao.deleteUsersInRoles(deleteRole, users, request.getHeader("sec-username"));

			ResponseUtil.writeSuccess(response);

		}  catch (NameNotFoundException e) {

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
	 * @param role role to modify
	 * @param inputStream contains the new values
	 *
	 * @return the {@link Role} modified
	 */
	private Role modifyRole(Role role, ServletInputStream inputStream) throws IOException{

		String strRole = FileUtils.asString(inputStream);
		JSONObject json;
		try {
			json = new JSONObject(strRole);
		} catch (JSONException e) {
			LOG.error(e.getMessage());
			throw new IOException(e);
		}

		String cn = RequestUtil.getFieldValue(json, RoleSchema.COMMON_NAME_KEY);
		if (cn != null) {
			role.setName(cn);
		}

		String description = RequestUtil.getFieldValue(json, RoleSchema.DESCRIPTION_KEY);
		if (description != null) {
			role.setDescription(description);
		}

		return role;
	}

	private Role createRoleFromRequestBody(ServletInputStream is) throws IOException {
		try {
			String strRole = FileUtils.asString(is);
			JSONObject json = new JSONObject(strRole);

			String commonName = RequestUtil.getFieldValue(json, RoleSchema.COMMON_NAME_KEY);
			if(commonName == null){
				throw new IllegalArgumentException(RoleSchema.COMMON_NAME_KEY + " is required" );
			}

			String description = RequestUtil.getFieldValue(json, RoleSchema.DESCRIPTION_KEY);

			Role g = RoleFactory.create(commonName, description);

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
    public void setRoleDao(RoleDao gd) {
        roleDao = gd;
    }

	/**
	 * Method used for testing convenience.
	 * @param ad
	 */
    public void setAccountDao(AccountDao ad) {
        this.accountDao = ad;
    }



}
