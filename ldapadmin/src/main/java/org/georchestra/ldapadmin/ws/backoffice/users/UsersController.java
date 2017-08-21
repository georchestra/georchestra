/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

package org.georchestra.ldapadmin.ws.backoffice.users;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.DuplicatedEmailException;
import org.georchestra.ldapadmin.ds.DuplicatedUidException;
import org.georchestra.ldapadmin.ds.OrgsDao;
import org.georchestra.ldapadmin.ds.ProtectedUserFilter;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.AccountFactory;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.Org;
import org.georchestra.ldapadmin.dto.UserSchema;
import org.georchestra.ldapadmin.mailservice.MailService;
import org.georchestra.ldapadmin.ws.backoffice.utils.RequestUtil;
import org.georchestra.ldapadmin.ws.backoffice.utils.ResponseUtil;
import org.georchestra.ldapadmin.ws.utils.Validation;
import org.georchestra.lib.file.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.InvalidAttributeValueException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web Services to maintain the User information.
 *
 * <p>
 * This class provides the operations to access the data layer to update and read the user data.
 * Those operations will be consistent with the business rules.
 * </p>
 *
 * @author Mauricio Pazos
 *
 */
@Controller
public class UsersController {

	private static final Log LOG = LogFactory.getLog(UsersController.class.getName());

	private static final String BASE_MAPPING = "/private";
	private static final String REQUEST_MAPPING = BASE_MAPPING + "/users";
	private static final String PUBLIC_REQUEST_MAPPING = "/public/users";

	private static final String DUPLICATED_EMAIL = "duplicated_email";
	private static final String PARAMS_NOT_UNDERSTOOD = "params_not_understood";
	private static final String NOT_FOUND = "not_found";
	private static final String UNABLE_TO_ENCODE = "unable_to_encode";
	private static final String INVALID_VALUE = "invalid_value";
	private static final String OTHER_ERROR = "other_error";
	private static final String INVALID_DATE_FORMAT = "invalid_date_format";

	private AccountDao accountDao;
	@Autowired
	private OrgsDao orgDao;

	@Autowired
	private Validation validation;

	public void setOrgDao(OrgsDao orgDao) {
		this.orgDao = orgDao;
	}

	private UserRule userRule;
	
	@Autowired
	private MailService mailService;

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	@Autowired
	private Boolean warnUserIfUidModified = false;

	public void setWarnUserIfUidModified(boolean warnUserIfUidModified) {
		this.warnUserIfUidModified = warnUserIfUidModified;
	}

	@Autowired
	public UsersController(AccountDao dao, UserRule userRule){
		this.accountDao = dao;
		this.userRule = userRule;
	}

	/**
	 * Returns array of users using json syntax.
	 * <pre>
	 *
	 *	[
	 *	    {
	 *	        "org": "Zogak",
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
			ProtectedUserFilter filter = new ProtectedUserFilter( this.userRule.getListUidProtected() );
			List<Account> list = this.accountDao.findFilterBy(filter);

			// Retrieve organizations list to display org name instead of org DN
			List<Org> orgs = this.orgDao.findAll();
			Map<String, String> orgNames = new HashMap<String, String>();
			for(Org org: orgs)
				orgNames.put(org.getId(), org.getName());

			JSONArray res = new JSONArray();
			for (Account account: list) {
				JSONObject jsonAccount = new JSONObject();
				jsonAccount.put(UserSchema.UID_KEY, account.getUid());
				jsonAccount.put(UserSchema.GIVEN_NAME_KEY, account.getGivenName());
				jsonAccount.put(UserSchema.SURNAME_KEY, account.getSurname());
				jsonAccount.put(UserSchema.ORG_KEY, orgNames.get(account.getOrg()));
				jsonAccount.put(UserSchema.MAIL_KEY, account.getEmail());
				res.put(jsonAccount);
			}

			ResponseUtil.buildResponse(response, res.toString(), HttpServletResponse.SC_OK);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		}
	}
	
	/**
	 * Looks up a list of user given a pattern to search against the LDAP tree.
	 * The returned format is the same as for the findAll operation.
	 *
	 * @param response Returns the detailed information of the user as json
	 * @throws IOException
	 */
	@RequestMapping(value=BASE_MAPPING+"/usersearch/{userPattern:.+}", method=RequestMethod.GET, produces="application/json")
	@ResponseBody
	public String findWithPattern(@PathVariable String userPattern, HttpServletResponse response) throws IOException, JSONException {
		try {
			ProtectedUserFilter filter = new ProtectedUserFilter( this.userRule.getListUidProtected() );
			List<Account> list = this.accountDao.find(filter, new LikeFilter("uid", "*" + userPattern + "*"));
			JSONArray ret = new JSONArray();
			for (Account a: list) {
				ret.put(a.toJSON());
			}
			return new JSONObject().put("users", ret).toString();
		} catch (Exception e) {
			LOG.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
	 * @param response Returns the detailed information of the user as json
	 * @throws IOException
	 * @throws NameNotFoundException
	 */
	@RequestMapping(value=REQUEST_MAPPING+"/{uid:.+}", method=RequestMethod.GET,
					produces = "application/json")
	public void findByUid(@PathVariable String uid, HttpServletResponse response) throws IOException, JSONException, NameNotFoundException {

		// Check for protected accounts
		if(this.userRule.isProtected(uid) ){

			response.setStatus(HttpServletResponse.SC_CONFLICT);
			JSONObject res = new JSONObject();
			res.put("success", "false");
			res.put("error", "The user is protected: " + uid);
			response.getWriter().write(res.toString());

		} else {

			// searches the account
			Account account = null;
			try {
				account = this.accountDao.findByUID(uid);
				response.getWriter().write(account.toJSON().toString());
			} catch (NameNotFoundException e) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				JSONObject res = new JSONObject();
				res.put("success", "false");
				res.put("error", NOT_FOUND);
				response.getWriter().write(res.toString());
			} catch (DataServiceException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				throw new IOException(e);

			}
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
     *  "org": "the_organization"
     * }
     *
     * where <b>sn, givenName, mail</b> are mandatories
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

			if(this.userRule.isProtected(account.getUid()) ){

				String message = "The user is protected: " + account.getUid();
				LOG.warn(message );

				String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, message);

				ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);

				return;
			}

			storeUser(account, request.getHeader("sec-username"));

			ResponseUtil.buildResponse(response, account.toJSON().toString(), HttpServletResponse.SC_OK);

		} catch (IllegalArgumentException e ){
			LOG.warn(e.getMessage());

			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, e.getMessage());

			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);

		} catch (DuplicatedEmailException emailex){

			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, DUPLICATED_EMAIL);

			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);

		} catch (DataServiceException dsex){
			LOG.error(dsex.getMessage(), dsex);
			ResponseUtil.buildResponse(response, "{ \"success\": false }",
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(dsex);
		} catch (JSONException e) {
			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, UNABLE_TO_ENCODE);
			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);
		} catch (InvalidAttributeValueException ex){
			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, INVALID_VALUE);
			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);
		} catch (Exception ex){
			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, OTHER_ERROR);
			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);
		}

	}


	/**
	 * Saves the user in the LDAP store.
	 *
	 * @param account
	 * @param originLogin login of admin that issue request
	 * @throws DuplicatedEmailException
	 * @throws DataServiceException
	 * @throws IOException
	 */
	private void storeUser(Account account, String originLogin) throws DuplicatedEmailException, DataServiceException, IOException {
		try {
			this.accountDao.insert(account, Group.USER, originLogin);

		} catch (DuplicatedEmailException e) {
			LOG.error(e);
			throw e;

		} catch (DataServiceException e) {
			LOG.error(e);
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
	 * @throws NameNotFoundException
	 */
	@RequestMapping(value=REQUEST_MAPPING+ "/{uid:.+}", method=RequestMethod.PUT)
	public void update( HttpServletRequest request, HttpServletResponse response, @PathVariable String uid) throws IOException, NameNotFoundException{

		if(this.userRule.isProtected(uid) ){

			String message = "The user is protected, it cannot be updated: " + uid;
			LOG.warn(message );

			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, message);

			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);

			return;
		}

		// searches the account
		Account account = null;
		String originalOrg = null;
		try {
			account = this.accountDao.findByUID(uid);
			originalOrg = account.getOrg();

		} catch (NameNotFoundException e) {

			ResponseUtil.writeError(response, NOT_FOUND);
			return;

		} catch (DataServiceException e) {
		    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		}

		// modifies the account data
		try{
			final Account modified = modifyAccount(AccountFactory.create(account), request.getInputStream());
			this.accountDao.update(account, modified, request.getHeader("sec-username"));

			if(!modified.getOrg().equals(originalOrg)){
				if(originalOrg.length() > 0)
					this.orgDao.removeUser(originalOrg, uid);
				if(modified.getOrg().length() > 0)
					this.orgDao.addUser(modified.getOrg(), uid);
			}


			boolean uidChanged = ( ! modified.getUid().equals(account.getUid()));
			if ((uidChanged) && (warnUserIfUidModified)) {
				this.mailService.sendAccountUidRenamed(request.getSession().getServletContext(),
						modified.getUid(), modified.getCommonName(), modified.getEmail());
			}
			ResponseUtil.buildResponse(response, modified.toJSON().toString(), HttpServletResponse.SC_OK);

		} catch (DuplicatedEmailException e) {
			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, DUPLICATED_EMAIL);
			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);
		} catch (IOException e) {
			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, PARAMS_NOT_UNDERSTOOD);
			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_BAD_REQUEST);
			throw e;
		} catch (DataServiceException e){
			LOG.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new IOException(e);
		} catch (NameNotFoundException e) {
			LOG.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} catch (JSONException e) {
			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, UNABLE_TO_ENCODE);
			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);
		} catch (ParseException e){
			String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, INVALID_DATE_FORMAT);
			ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_BAD_REQUEST);
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
	@RequestMapping(value=REQUEST_MAPPING + "/{uid:.+}", method=RequestMethod.DELETE)
	public void delete(@PathVariable String uid, HttpServletRequest request, HttpServletResponse response) throws IOException{
		try{
			if(this.userRule.isProtected(uid) ){

				String message = "The user is protected, it cannot be deleted: " + uid;
				LOG.warn(message );

				String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, message);

				ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_CONFLICT);

				return;
			}

			this.accountDao.delete(uid, request.getHeader("sec-username"));

			ResponseUtil.writeSuccess(response);

		} catch (DataServiceException e){
			LOG.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new IOException(e);
		} catch (NameNotFoundException e) {
            String jsonResponse = ResponseUtil.buildResponseMessage(Boolean.FALSE, NOT_FOUND);
            ResponseUtil.buildResponse(response, jsonResponse, HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/**
     * Return a list of required fields for user creation
     *
     * return a JSON array with required fields.
     */
    @RequestMapping(value = PUBLIC_REQUEST_MAPPING + "/requiredFields", method = RequestMethod.GET)
    public void getUserRequiredFields(HttpServletResponse response) throws IOException{
        try {
            JSONArray fields = new JSONArray();
            fields.put(UserSchema.UID_KEY);
            fields.put(UserSchema.MAIL_KEY);
            fields.put(UserSchema.SURNAME_KEY);
            fields.put(UserSchema.GIVEN_NAME_KEY);
            ResponseUtil.buildResponse(response, fields.toString(4), HttpServletResponse.SC_OK);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new IOException(e);
        }
    }

	/**
	 * Modify only the account's fields that are present in the request body.
	 *
	 * @param account
	 * @param inputStream
	 *
	 * @return the modified account
	 *
	 * @throws IOException
	 */
	private Account modifyAccount(Account account, ServletInputStream inputStream) throws IOException, ParseException {

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
			account.setGivenName(givenName);
		}

		String surname = RequestUtil.getFieldValue(json, UserSchema.SURNAME_KEY);
		if (surname != null) {
			account.setSurname(surname);
		}

		String email = RequestUtil.getFieldValue(json, UserSchema.MAIL_KEY);
		if (email != null) {
			account.setEmail(email);
		}

		String postalAddress = RequestUtil.getFieldValue(json, UserSchema.POSTAL_ADDRESS_KEY);
		if (postalAddress != null) {
			account.setPostalAddress(postalAddress);
		}

		String postOfficeBox = RequestUtil.getFieldValue(json, UserSchema.POST_OFFICE_BOX_KEY);
		if (postOfficeBox != null) {
			account.setPostOfficeBox(postOfficeBox);
		}

		String postalCode = RequestUtil.getFieldValue(json, UserSchema.POSTAL_CODE_KEY);
		if (postalCode != null) {
			account.setPostalCode(postalCode);
		}

		String street = RequestUtil.getFieldValue(json, UserSchema.STREET_KEY);
		if (street != null) {
			account.setStreet(street);
		}

		String locality = RequestUtil.getFieldValue(json, UserSchema.LOCALITY_KEY);
		if (locality != null) {
			account.setLocality(locality);
		}

		String phone = RequestUtil.getFieldValue(json, UserSchema.TELEPHONE_KEY);
		if (phone != null) {
			account.setPhone(phone);
		}

		String facsimile = RequestUtil.getFieldValue(json, UserSchema.FACSIMILE_KEY);
		if (facsimile != null) {
			account.setFacsimile(facsimile);
		}

		String title = RequestUtil.getFieldValue(json, UserSchema.TITLE_KEY);
		if (title != null) {
			account.setTitle(title);
		}

		String description = RequestUtil.getFieldValue(json, UserSchema.DESCRIPTION_KEY);
		if (description != null) {
			account.setDescription(description);
		}

		String manager = RequestUtil.getFieldValue(json, UserSchema.MANAGER_KEY);
		account.setManager(manager);

		String context = RequestUtil.getFieldValue(json, UserSchema.CONTEXT_KEY);
		if (context != null) {
			account.setContext(context);
		}
		
		String commonName = AccountFactory.formatCommonName(
				account.getGivenName(), account.getSurname());
		account.setCommonName(commonName);

		String uid = RequestUtil.getFieldValue(json, UserSchema.UID_KEY);
		if (uid != null) {
			account.setUid(uid);
		}

		String org = RequestUtil.getFieldValue(json, UserSchema.ORG_KEY);
		if(org != null)
			account.setOrg(org);

		String shadowExpire = RequestUtil.getFieldValue(json, UserSchema.SHADOW_EXPIRE_KEY);
		if(shadowExpire != null) {
			if("".equals(shadowExpire))
				account.setShadowExpire(null);
			else
				account.setShadowExpire((new SimpleDateFormat("yyyy-MM-dd")).parse(shadowExpire));
		}

		return account;
	}

	/**
	 * Create a new account from the body request.
	 *
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private Account createAccountFromRequestBody(ServletInputStream is) throws IllegalArgumentException, IOException {

		JSONObject json;
		try {
			json = new JSONObject(FileUtils.asString(is));
		} catch (JSONException e) {
			LOG.error(e.getMessage());
			throw new IOException(e);
		}

		String givenName     = RequestUtil.getFieldValue(json, UserSchema.GIVEN_NAME_KEY);
		String surname       = RequestUtil.getFieldValue(json, UserSchema.SURNAME_KEY);
		String email         = RequestUtil.getFieldValue(json, UserSchema.MAIL_KEY);
		String postalAddress = RequestUtil.getFieldValue(json, UserSchema.POSTAL_ADDRESS_KEY );
		String postOfficeBox = RequestUtil.getFieldValue(json, UserSchema.POST_OFFICE_BOX_KEY );
		String postalCode    = RequestUtil.getFieldValue(json, UserSchema.POSTAL_CODE_KEY);
		String street        = RequestUtil.getFieldValue(json, UserSchema.STREET_KEY);
		String locality      = RequestUtil.getFieldValue(json, UserSchema.LOCALITY_KEY);
		String phone         = RequestUtil.getFieldValue(json, UserSchema.TELEPHONE_KEY);
		String facsimile     = RequestUtil.getFieldValue(json, UserSchema.FACSIMILE_KEY);
		String title         = RequestUtil.getFieldValue(json, UserSchema.TITLE_KEY);
		String description   = RequestUtil.getFieldValue(json, UserSchema.DESCRIPTION_KEY);
		String manager       = RequestUtil.getFieldValue(json, UserSchema.MANAGER_KEY);
		String context       = RequestUtil.getFieldValue(json, UserSchema.CONTEXT_KEY);
		String org           = RequestUtil.getFieldValue(json, UserSchema.ORG_KEY);

		if(givenName == null)
			throw new IllegalArgumentException("First Name is required");

		if(surname == null)
			throw new IllegalArgumentException("Last Name is required");

		if(email == null)
			throw new IllegalArgumentException("EMail is required");

		// Use specified login if not empty
		String uid = RequestUtil.getFieldValue(json, UserSchema.UID_KEY);
		if(!StringUtils.hasLength(uid))
			try {
				uid = createUid(givenName, surname);
			} catch (DataServiceException e) {
				LOG.error(e.getMessage());
				throw new IOException(e);
			}

		String commonName = AccountFactory.formatCommonName(givenName, surname);

		Account a = AccountFactory.createFull(uid, commonName, surname, givenName, email, title, phone, description,
				postalAddress, postalCode, "", postOfficeBox, "", street, locality, facsimile, "", "", "", "", manager,
				context, org);

		String shadowExpire = RequestUtil.getFieldValue(json, UserSchema.SHADOW_EXPIRE_KEY);
		if (StringUtils.hasLength(shadowExpire)) {
			try {
				a.setShadowExpire((new SimpleDateFormat("yyyy-MM-dd")).parse(shadowExpire));
			} catch (ParseException e) {
				LOG.error(e.getMessage());
				throw new IllegalArgumentException(e);
			}
		}

		return a;
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

		String proposedUid = normalizeString(givenName.toLowerCase().charAt(0) + surname.toLowerCase());

		if(! this.accountDao.exist(proposedUid)){
			return proposedUid;
		} else {
			return this.accountDao.generateUid( proposedUid );
		}
	}

	/**
	 * Deaccentuate a string and remove non-word characters
	 *
	 * references: http://stackoverflow.com/a/8523728 and
	 * http://stackoverflow.com/a/2397830
	 *
	 * @param string an accentuated string, eg. "Joá+o"
	 * @return return the deaccentuated string, eg. "Joao"
	 */
	public static String normalizeString(String string) {
		return Normalizer.normalize(string, Normalizer.Form.NFD)
			.replaceAll("\\W", "");
	}
}
