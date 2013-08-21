/**
 * 
 */
package org.georchestra.ldapadmin.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.bs.ExpiredTokenManagement;
import org.georchestra.ldapadmin.ds.UserTokenDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Display the home page 
 * 
 * 
 * @author Mauricio Pazos
 *
 */
@Controller
public class HomeController {

	private static final Log LOG = LogFactory.getLog(HomeController.class.getName());
	private ExpiredTokenManagement tokenManagement;
	
	@Autowired
	public HomeController(ExpiredTokenManagement tokenManagment) {
		if(LOG.isDebugEnabled()){
			LOG.debug("home controller initialization");
		}
		this.tokenManagement = tokenManagment;
		this.tokenManagement.start();
	}
	
	
	@RequestMapping(value="/public/")
	public String home(){

		if(LOG.isDebugEnabled()){
			LOG.debug("home page request");
		}
		
		return "home";
	}
}
