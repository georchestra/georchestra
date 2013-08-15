/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.DuplicatedEmailException;
import org.georchestra.ldapadmin.ds.DuplicatedUidException;
import org.georchestra.ldapadmin.ds.NotFoundException;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.Group;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controls the access to the user account in order to maintain the information.
 * 
 * @author Mauricio Pazos
 *
 */
@Controller
public class UserController {
	
	private static final Log LOG = LogFactory.getLog(UserController.class.getName());

	private static final String BASE_MAPPING = "/private";

	private AccountDao accountDao;
	
	@Autowired
	public UserController( AccountDao dao){
		this.accountDao = dao;
	}
	
	/**
	 * Returns array of users using json syntax.
	 * <pre>
	 * 
	 *	[
	 *	    {
	 *	        "o": "Zogak",
	 *	        "givenName": "Walsh",
	 *	        "sn": "Atkins",
	 *	        "uid": "watkins"
	 *	    },
	 *	        ...
	 *	]
	 * </pre>
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value=BASE_MAPPING + "/users", method=RequestMethod.GET)
	public void findAll( HttpServletRequest request, HttpServletResponse response ) throws IOException{
		
		try {
			List<Account> list = this.accountDao.findAll();

			UserListResponse userListResponse = new UserListResponse(list);
			
			String jsonList = userListResponse.asJsonString();
			
			buildResponse(response, jsonList);
			
		} catch (Exception e) {
			
			LOG.error(e.getMessage());
			
			throw new IOException(e);
		} 
		
		
	}

	@RequestMapping(value="/users/*", method=RequestMethod.GET)
	public void findByUid( HttpServletRequest request, HttpServletResponse response, @RequestParam("uid") String uid ) throws IOException{
		
		try {
			Account account = this.accountDao.findByUID(uid);
			
			String jsonAccount = accountAsJson(account);
			
			buildResponse(response, jsonAccount);
			
		}  catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (DataServiceException e) {
			
			LOG.error(e.getMessage());
			
			throw new IOException(e);
		}
		
	}
	
	@RequestMapping(value="/users/*", method=RequestMethod.GET)
	public void update( HttpServletRequest request, HttpServletResponse response, @RequestParam("uid") String uid ) throws IOException{
		String jsonResponse = "";
		try{
			Account account = this.accountDao.findByUID(uid);
			
			this.accountDao.update(account);
			
		} catch (Exception e){

			throw new IOException(e);
		}
		
		// TODO RESPONSE 200 ok

	}

	@RequestMapping(value="/users/*", method=RequestMethod.DELETE)
	public void delete( HttpServletRequest request, HttpServletResponse response, @RequestParam("uid") String uid ) throws IOException{
		String jsonResponse = "";
		try{
			this.accountDao.delete(uid);
			
		} catch (Exception e){

			throw new IOException(e);
		}
		
		// TODO RESPONSE 200 ok

	}

	private String accountAsJson(Account account) {
		// TODO Auto-generated method stub
		return null;
	}

	@RequestMapping(value="/users", method=RequestMethod.POST)
	public void create( HttpServletRequest request, HttpServletResponse response ) throws IOException{
		
		String jsonResponse = "";
		try{
			
			Account account = buildAccount(request.getInputStream());
			this.accountDao.insert(account, Group.SV_USER);
			
			jsonResponse = ""; // TODO 
			
		} catch(DuplicatedUidException uidex){
			
			// add error description
			jsonResponse = "{ \"success\": false }";

		} catch (DuplicatedEmailException emailex){
			
			// add error description
			jsonResponse = "{ \"success\": false }";
			
		} catch (DataServiceException dsex){

			jsonResponse = "{ \"success\": false }";
		}

		
		buildResponse(response, jsonResponse);
	}
	
	private void buildResponse(HttpServletResponse response, String jsonData) throws IOException {
		
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		
		PrintWriter out = response.getWriter();
		try {
			out.println(jsonData);
			
		} finally {
			out.close();
		}
	}


	private Account buildAccount(ServletInputStream inputStream) {
		// TODO Auto-generated method stub
		
		// extract the fields from json parameter
// example		
//		{
//		    "facsimileTelephoneNumber": "fsdfdf",
//		    "givenName": "GIRAUD",
//		    "l": "dfdf",
//		    "mail": "pierre.giraud@gmail.com",
//		    "postOfficeBox": "dfdf",
//		    "postalCode": "dfdf",
//		    "sn": "Pierre",
//		    "street": "fdsf",
//		    "telephoneNumber": "fdsfd"
//		}		
		//Account a = AccountFactory.createFull(uid, cn, surname, givenName, email, org, title, phone, description, postalAddress, postalCode, registeredAddress, postOfficeBox, physicalDeliveryOfficeName)

		return null;
	}
	
}
