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
import org.georchestra.ldapadmin.dto.AccountFactory;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.UserSchema;
import org.georchestra.lib.file.FileUtils;
import org.json.JSONObject;
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
			
			buildResponse(response, jsonList, HttpServletResponse.SC_OK);
			
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
			
			buildResponse(response, jsonAccount, HttpServletResponse.SC_OK);
			
		}  catch (Exception e) {
			LOG.error(e.getMessage());
			
			throw new IOException(e);
		} 
		
	}
	
	/**
	 * <p>
	 * Creates a new user.
	 * </p>
	 * 
	 * <pre>
	 * <b>Request</b>
	 * 
	 * user data:
	 * {
     *  "sn": "surname",
     *	"givenName": "first name",
     *	"mail": "e-mail",
     * 	"telephoneNumber": "telephone"
     *	"facsimileTelephoneNumber": "value",
     * 	"street": "street",
     * 	"postalCode": "postal code",
     *	"l": "locality",
     * 	"postOfficeBox": "the post office box",
     * }
	 * </pre>
	 * <pre>
	 * <b>Response</b>
	 * 
	 * <b>- Success case</b>
	 *  
	 * The generated uid is added to the user data. So, a succeeded response should look like: 
	 * {
	 * 	<b>"uid": generated uid</b>
	 *  
     *  "sn": "surname",
     *	"givenName": "first name",
     *	"mail": "e-mail",
     * 	"telephoneNumber": "telephone"
     *	"facsimileTelephoneNumber": "value",
     * 	"street": "street",
     * 	"postalCode": "postal code",
     *	"l": "locality",
     * 	"postOfficeBox": "the post office box",
     * }
	 * </pre>
	 *
	 * <pre>
	 * <b>- Error case</b> 
	 * If the provided e-mail exists in the LDAP store the response will contain:
	 * 
	 * 	{ \"success\": false, \"error\": \"duplicated_email\"}
	 * 
	 * </pre>
	 * 
	 * @param request HTTP POST data contains the user data
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value=BASE_MAPPING + "/users", method=RequestMethod.POST)
	public void create( HttpServletRequest request, HttpServletResponse response ) throws IOException{
		
		int sc = HttpServletResponse.SC_OK;
		String jsonResponse = "";
		try{
			
			Account account = buildAccount(request.getInputStream());
			storeUser(account);
			
			UserResponse userResponse = new UserResponse(account);
			
			jsonResponse = userResponse.asJsonString();
			
			
		} catch (DuplicatedEmailException emailex){
			
			// add error description
			jsonResponse = "{ \"success\": false, \"error\": \"duplicated_email\"}";
			
		} catch (DataServiceException dsex){

			LOG.error(dsex.getMessage());
			
			throw new IOException(dsex);
		}
		buildResponse(response, jsonResponse, sc);
	}
	

	/**
	 * Saves the user in the LDAP store.
	 * 
	 * @param account
	 * @throws DuplicatedEmailException 
	 * @throws DataServiceException 
	 * @throws IOException 
	 */
	private void storeUser(Account account) throws DuplicatedEmailException, DataServiceException, IOException {
		try {
			
			this.accountDao.insert(account, Group.SV_USER);
			
		} catch (DuplicatedEmailException e) {
			throw e;

		} catch (DataServiceException e) {
			
			throw e;
			
		} catch (DuplicatedUidException e) {
			
			// this case is not possible because the uid is generated by the application
			LOG.error(e);
			throw new IOException(e);
		}		
	}

	@RequestMapping(value=BASE_MAPPING + "/users/*", method=RequestMethod.PUT)
	public void update( HttpServletRequest request, HttpServletResponse response) throws IOException{
		try{
			String uid = getUidPathVariable(request);

			Account account = this.accountDao.findByUID(uid);
			
			this.accountDao.update(account);

			response.setStatus(HttpServletResponse.SC_OK);
			
		} catch (Exception e){
			LOG.error(e.getMessage());
			
			throw new IOException(e);
		}
	}

	@RequestMapping(value=BASE_MAPPING + "/users/*", method=RequestMethod.DELETE)
	public void delete( HttpServletRequest request, HttpServletResponse response) throws IOException{
		try{
			String uid = getUidPathVariable(request);

			this.accountDao.delete(uid);
			
			response.setStatus(HttpServletResponse.SC_OK);
			
		} catch (Exception e){

			LOG.error(e.getMessage());
			
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

	private void buildResponse(HttpServletResponse response, String jsonData, int sc) throws IOException {
		
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		response.setStatus(sc);

		PrintWriter out = response.getWriter();
		try {
			out.println(jsonData);
			
		} finally {
			out.close();
		}
	}


	private Account buildAccount(ServletInputStream is) throws IOException {
		
		try {
			String strUser = FileUtils.asString(is);
			JSONObject json = new JSONObject(strUser);
			
			String givenName = json.getString(UserSchema.GIVEN_NAME_KEY);
			if(givenName.length() == 0){
				throw new IllegalArgumentException(UserSchema.GIVEN_NAME_KEY + " is required" );
			}
			String surname= json.getString(UserSchema.SURNAME_KEY);
			if(surname.length() == 0){
				throw new IllegalArgumentException(UserSchema.SURNAME_KEY + " is required" );
			}
			
			String email = json.getString(UserSchema.MAIL_KEY);
			
			String postOfficeBox = json.getString(UserSchema.POSTAL_OFFICE_BOX_KEY);
			String postalCode = json.getString(UserSchema.POSTAL_CODE_KEY);
			
			String street= json.getString(UserSchema.STREET_KEY); 
			String locality = json.getString(UserSchema.LOCALITY_KEY); 

			String phone = json.getString(UserSchema.TELEPHONE_KEY);

			String uid = createUid(givenName, surname);
			
			String commonName = AccountFactory.formatCommonName(givenName, surname);
			
			Account a = AccountFactory.createFull(uid, commonName, surname, givenName, email, "", "", phone, "", "", postalCode, "", postOfficeBox, "", street, locality);
			
			return a;
			
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new IOException(e);
		}
	}

	/**
	 * Creates a uid based on the given name and surname
	 * 
	 * @param givenName
	 * @param surname
	 * @return return the proposed uid
	 * 
	 * @throws DataServiceException 
	 */
	private String createUid(String givenName, String surname) throws DataServiceException {
		
		String proposedUid = givenName.toLowerCase().charAt(0) + surname.toLowerCase();
		
		if(! this.accountDao.exist(proposedUid)){
			return proposedUid;
		} else {
			return this.accountDao.generateUid( proposedUid );
		}
	}
	
}
