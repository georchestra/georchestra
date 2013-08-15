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
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controls the access to the user account in order to maintain the information.
 * 
 * @author Mauricio Pazos
 *
 */
@Controller
public class UsersController {
	
	private static final Log LOG = LogFactory.getLog(UsersController.class.getName());

	private static final String BASE_MAPPING = "/private";

	private AccountDao accountDao;
	
	@Autowired
	public UsersController( AccountDao dao){
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

	/**
	 * Returns the detailed information of the user.
	 * 
	 * <p>
	 * If the user identifier is not present in the ldap store an {@link IOException} will be throw.
	 * </p>
	 * <p>
	 * URL Format: [BASE_MAPPING]/users/{uid}
	 * </p>
	 * <p>
	 * Example: [BASE_MAPPING]/users/hsimpson
	 * </p>
	 * 
	 * @param request the request includes the user identifier required
	 * @param response Returns the detailed information of the user as json 
	 * @throws IOException 
	 */
	@RequestMapping(value=BASE_MAPPING + "/users/*", method=RequestMethod.GET)
	public void findByUid( HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		try {
			
			String uid = getUidPathVariable(request);
			
			Account account = this.accountDao.findByUID(uid);
			
			UserResponse userResponse = new UserResponse(account);
			
			String jsonAccount = userResponse.asJsonString();
			
			buildResponse(response, jsonAccount);
			
		}  catch (Exception e) {
			LOG.error(e.getMessage());
		} 
		
	}
	
	@RequestMapping(value=BASE_MAPPING + "/users/*", method=RequestMethod.POST)
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
	

	@RequestMapping(value=BASE_MAPPING + "/users/*", method=RequestMethod.PUT)
	public void update( HttpServletRequest request, HttpServletResponse response) throws IOException{
		try{
			String uid = getUidPathVariable(request);

			Account account = this.accountDao.findByUID(uid);
			
			this.accountDao.update(account);

			// TODO RESPONSE 200 ok
			
		} catch (Exception e){

			throw new IOException(e);
		}
		

	}

	@RequestMapping(value=BASE_MAPPING + "/users/*", method=RequestMethod.DELETE)
	public void delete( HttpServletRequest request, HttpServletResponse response) throws IOException{
		try{
			String uid = getUidPathVariable(request);

			this.accountDao.delete(uid);
			// TODO RESPONSE 200 ok
			
		} catch (Exception e){

			throw new IOException(e);
		}
		

	}


	/**
	 * Gets the uid parameter from request
	 * 
	 * @param request [...]/users/{uid}
	 * @return returns the uid from request
	 */
	private String getUidPathVariable(HttpServletRequest request) {

		String str = request.getRequestURI();
		
		String[] path = str.split("/");
		
		String uid = path[path.length - 1];
		
		return uid;
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
