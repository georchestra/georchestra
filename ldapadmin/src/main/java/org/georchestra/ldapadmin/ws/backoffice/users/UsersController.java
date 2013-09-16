/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice.users;

import java.io.IOException;
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
import org.georchestra.ldapadmin.dto.AccountFactory;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.UserSchema;
import org.georchestra.ldapadmin.ws.backoffice.utils.RequestUtil;
import org.georchestra.ldapadmin.ws.backoffice.utils.ResponseUtil;
import org.georchestra.lib.file.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Web Services to maintain the User information.
 * 
 * @author Mauricio Pazos
 *
 */
@Controller
public class UsersController {
	
	private static final Log LOG = LogFactory.getLog(UsersController.class.getName());

	private static final String BASE_MAPPING = "/private";
	private static final String REQUEST_MAPPING = BASE_MAPPING + "/users";	
	

	private static final String DUPLICATED_EMAIL = "duplicated_email";

	private static final String NOT_FOUND = "not_found";


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
	@RequestMapping(value=REQUEST_MAPPING, method=RequestMethod.GET)
	public void findAll( HttpServletRequest request, HttpServletResponse response ) throws IOException{
		
		try {
			List<Account> list = this.accountDao.findAll();

			UserListResponse userListResponse = new UserListResponse(list);
			
			String jsonList = userListResponse.asJsonString();
			
			ResponseUtil.buildResponse(response, jsonList, HttpServletResponse.SC_OK);
			
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
	@RequestMapping(value=REQUEST_MAPPING+"/*", method=RequestMethod.GET)
	public void findByUid( HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		String uid = RequestUtil.getKeyFromPathVariable(request);

		// searches the account
		Account account = null;
		try {
			account = this.accountDao.findByUID(uid);
			
		} catch (NotFoundException e) {
			
			ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(Boolean.FALSE, NOT_FOUND), HttpServletResponse.SC_NOT_FOUND);
			
			return;

		} catch (DataServiceException e) {
			throw new IOException(e);
		}

		// sets the account data in the response object
		UserResponse userResponse = new UserResponse(account);
		
		String jsonAccount = userResponse.asJsonString();
		
		ResponseUtil.buildResponse(response, jsonAccount, HttpServletResponse.SC_OK);

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
     * 	"postOfficeBox": "the post office box"
     * }
	 * </pre>
	 *
	 * <pre>
	 * <b>- Error case</b> 
	 * If the provided e-mail exists in the LDAP store the response will contain:
	 * 
	 * 	{ \"success\": false, \"error\": \"duplicated_email\"}
	 * 
	 * Error: 409 conflict with the current state of resource
	 * 
	 * </pre>
	 * 
	 * @param request HTTP POST data contains the user data
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value=REQUEST_MAPPING, method=RequestMethod.POST)
	public void create( HttpServletRequest request, HttpServletResponse response ) throws IOException{
		
		try{
			
			Account account = createAccountFromRequestBody(request.getInputStream());
			storeUser(account);
			
			UserResponse userResponse = new UserResponse(account);
			
			String jsonResponse = userResponse.asJsonString();

			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_OK);
			
			
		} catch (DuplicatedEmailException emailex){
			
			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, DUPLICATED_EMAIL); 

			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);
			
		} catch (DataServiceException dsex){

			LOG.error(dsex.getMessage());
			
			throw new IOException(dsex);
		}
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

	/**
	 * Modifies the user data using the fields provided in the request body.
	 * <p>
	 * The fields that are not present in the parameters will remain untouched in the LDAP store.
	 * </p>
	 * <p>
	 * The request format is:
	 * [BASE_MAPPING]/users/{uid}
	 * </p>
	 * <p>
	 * The request body should contains a the fields to modify using the JSON syntax. 
	 * </p>
	 * <p>
	 * Example: 
	 * </p>
	 * <pre>
	 * <b>Request</b>
	 * [BASE_MAPPING]/users/hsimpson
	 * 
	 * <b>Body request: </b>
	 * {"sn": "surname",
	 *  "givenName": "first name",
	 *  "mail": "e-mail",
	 *  "telephoneNumber": "telephone",
	 *  "facsimileTelephoneNumber": "value",
     * 	"street": "street",
     *  "postalCode": "postal code",
     *  "l": "locality",
     *  "postOfficeBox": "the post office box"
     * }
	 * 
	 * </pre>
	 * @param request
	 * @param response
	 * 
	 * @throws IOException if the uid does not exist or fails to access to the LDAP store.
	 */
	@RequestMapping(value=REQUEST_MAPPING+ "/*", method=RequestMethod.PUT)
	public void update( HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		String uid = RequestUtil.getKeyFromPathVariable(request);

		// searches the account
		Account account = null;
		try {
			account = this.accountDao.findByUID(uid);
			
		} catch (NotFoundException e) {
			
			ResponseUtil.writeError(response, NOT_FOUND);
			
			return;

		} catch (DataServiceException e) {
			throw new IOException(e);
		}
		
		// modifies the account data
		try{
			final Account modified = modifyAccount( account, request.getInputStream());
			
			this.accountDao.update(modified);

			ResponseUtil.writeSuccess(response);
			
		} catch (DuplicatedEmailException e) {
			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, DUPLICATED_EMAIL); 

			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);
			
		} catch (DataServiceException e){
			LOG.error(e.getMessage());
			
			throw new IOException(e);
		}
	}

	/**
	 * Deletes the user.
	 * 
	 * The request format is:
	 * <pre>
	 * [BASE_MAPPING]/users/{uid}
	 * </pre>
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value=REQUEST_MAPPING + "/*", method=RequestMethod.DELETE)
	public void delete( HttpServletRequest request, HttpServletResponse response) throws IOException{
		try{
			final String uid = RequestUtil.getKeyFromPathVariable(request);

			this.accountDao.delete(uid);
			
			ResponseUtil.writeSuccess(response);
			
		} catch (Exception e){

			LOG.error(e.getMessage());
			
			throw new IOException(e);
		}
		

	}

	/**
	 * Modify only the account's fields that are present in the request body.
	 *  
	 * @param accont
	 * @param inputStream
	 * 
	 * @return the modified account
	 * 
	 * @throws IOException
	 */
	private Account modifyAccount(Account accont, ServletInputStream inputStream) throws IOException {

		String strUser = FileUtils.asString(inputStream);
		JSONObject json;
		try {
			json = new JSONObject(strUser);
		} catch (JSONException e) {
			LOG.error(e.getMessage());
			throw new IOException(e);
		}

		String givenName = RequestUtil.getFieldValue(json, UserSchema.GIVEN_NAME_KEY);
		if (givenName != null) {
			accont.setGivenName(givenName);
		}

		String surname = RequestUtil.getFieldValue(json, UserSchema.SURNAME_KEY);
		if (surname != null) {
			accont.setSurname(surname);
		}

		String email = RequestUtil.getFieldValue(json, UserSchema.MAIL_KEY);
		if (email != null) {
			accont.setEmail(email);
		}

		String postOfficeBox = RequestUtil.getFieldValue(json,
				UserSchema.POST_OFFICE_BOX_KEY);
		if (postOfficeBox != null) {
			accont.setPostOfficeBox(postOfficeBox);
		}

		String postalCode = RequestUtil.getFieldValue(json, UserSchema.POSTAL_CODE_KEY);
		if (postalCode != null) {
			accont.setPostalCode(postalCode);
		}

		String street = RequestUtil.getFieldValue(json, UserSchema.STREET_KEY);
		if (street != null) {
			accont.setStreet(street);
		}

		String locality = RequestUtil.getFieldValue(json, UserSchema.LOCALITY_KEY);
		if (locality != null) {
			accont.setLocality(locality);
		}

		String phone = RequestUtil.getFieldValue(json, UserSchema.TELEPHONE_KEY);
		if (phone != null) {
			accont.setPhone(phone);
		}

		String facsimile = RequestUtil.getFieldValue(json, UserSchema.FACSIMILE_KEY);
		if (facsimile != null) {
			accont.setFacsimile(facsimile);
		}

		String commonName = AccountFactory.formatCommonName(
				accont.getGivenName(), accont.getSurname());

		accont.setCommonName(commonName);

		return accont;
			
	}



	/**
	 * Create a new account from the body request.
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private Account createAccountFromRequestBody(ServletInputStream is) throws IOException {
		
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
			
			String postOfficeBox = json.getString(UserSchema.POST_OFFICE_BOX_KEY);
			String postalCode = json.getString(UserSchema.POSTAL_CODE_KEY);
			
			String street= json.getString(UserSchema.STREET_KEY); 
			String locality = json.getString(UserSchema.LOCALITY_KEY); 

			String phone = json.getString(UserSchema.TELEPHONE_KEY);
			
			String facsimile = json.getString(UserSchema.FACSIMILE_KEY);

			String uid = createUid(givenName, surname);
			
			String commonName = AccountFactory.formatCommonName(givenName, surname);
			
			Account a = AccountFactory.createFull(uid, commonName, surname, givenName, email, "", "", phone, "", "", postalCode, "", postOfficeBox, "", street, locality, facsimile, "","","","","");
			
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
