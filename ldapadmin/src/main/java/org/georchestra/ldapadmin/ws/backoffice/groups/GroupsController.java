/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice.groups;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.DuplicatedCommonNameException;
import org.georchestra.ldapadmin.ds.GroupDao;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.GroupFactory;
import org.georchestra.ldapadmin.dto.GroupSchema;
import org.georchestra.ldapadmin.ws.backoffice.utils.ResponseUtil;
import org.georchestra.lib.file.FileUtils;
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
	private static final String REQUEST_MAPPING = BASE_MAPPING + "/groups";

	private static final String DUPLICATED_COMMON_NAME = "duplicated_common_name";	
	
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


	private Group createGroupFromRequestBody(ServletInputStream is) throws IOException {
		try {
			String strUser = FileUtils.asString(is);
			JSONObject json = new JSONObject(strUser);
			
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
