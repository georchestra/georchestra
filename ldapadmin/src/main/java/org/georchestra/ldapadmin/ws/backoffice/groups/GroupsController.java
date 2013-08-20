/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice.groups;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.DuplicatedCommonNameException;
import org.georchestra.ldapadmin.ds.GroupDao;
import org.georchestra.ldapadmin.ds.NotFoundException;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.GroupFactory;
import org.georchestra.ldapadmin.dto.GroupSchema;
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

	
	private GroupDao groupDao;
	
	@Autowired
	public GroupsController( GroupDao dao){
		this.groupDao = dao;
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

			GroupListResponse listResponse = new GroupListResponse(list);
			
			String jsonList = listResponse.asJsonString();
			
			ResponseUtil.buildResponse(response, jsonList, HttpServletResponse.SC_OK);
			
		} catch (Exception e) {
			
			LOG.error(e.getMessage());
			
			throw new IOException(e);
		} 
		
		
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
			
			group.setGidNumber(generateGIDNumber(group.getName()));
				
			this.groupDao.insert( group );
			
			GroupResponse groupResponse = new GroupResponse(group);
			
			String jsonResponse = groupResponse.asJsonString();

			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_OK);
			
			
		} catch (DuplicatedCommonNameException emailex){
			
			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, DUPLICATED_COMMON_NAME); 

			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);
			
		} catch (DataServiceException dsex){

			LOG.error(dsex.getMessage());
			
			throw new IOException(dsex);
		}
	}
	/**
	 * Generate the id value based on the ASCII values.
	 * 
	 * @param commonName
	 * @return a number as string
	 */
	private String generateGIDNumber(final String commonName) {
		
		char[] charArray = commonName.toCharArray();

		int i = 0;
		for (char c : charArray) {
			i = i + c;
		}
		return String.valueOf(i);
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
			
		} catch (Exception e){

			LOG.error(e.getMessage());
			
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
	 * Where <b>cn</b> is the name of group to delete.
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
			
			throw new IOException(e);
		}
	}
	
	/**
	 * Updates the users of group. This method will add or delete the group of users from the list of groups.
	 *  
	 * @param request	request [BASE_MAPPING]/groups_users/{cn} body request {"users": [u1,ud,u3], "PUT": [g1,g2], "DELETE":[g3,g4] }
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
			
			throw new IOException(e);
		} catch (JSONException e) {
			LOG.error(e.getMessage());
			
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
			group.setGidNumber(generateGIDNumber(cn));
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
			
			String commonName = json.getString(GroupSchema.COMMON_NAME_KEY);
			if(commonName.length() == 0){
				throw new IllegalArgumentException(GroupSchema.COMMON_NAME_KEY + " is required" );
			}
			
			String description = json.getString(GroupSchema.DESCRIPTION_KEY);
			
			Group g = GroupFactory.create(commonName, description);
			
			return g;
			
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new IOException(e);
		}
	}
	
	
}
