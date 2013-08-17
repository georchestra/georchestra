/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice.groups;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.GroupDao;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.ws.backoffice.utils.ResponseUtil;
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
	
	private GroupDao groupDao;
	
	@Autowired
	public GroupsController( GroupDao dao){
		this.groupDao = dao;
	}

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
	
}
