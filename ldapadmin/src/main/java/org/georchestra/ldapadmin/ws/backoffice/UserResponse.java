package org.georchestra.ldapadmin.ws.backoffice;

import org.georchestra.ldapadmin.dto.Account;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Contains useful method to prepare the responses related with user data object   
 * 
 * 
 * @author Mauricio Pazos
 *
 */
class UserResponse {
	
	public static final String UUID_KEY = "uid";
	public static final String STREET_KEY = "street";
	public static final String HOME_POSTAL_ADDRESS_KEY = "homePostalAddress";
	public static final String TITLE_KEY = "title";
	public static final String FACSIMILE_KEY = "facsimileTelephoneNumber";
	public static final String POSTAL_CODE_KEY = "postalCode";
	public static final String MAIL_KEY = "mail";
	public static final String POSTAL_ADDRESS_KEY = "postalAddress";
	public static final String POSTAL_OFFICE_BOX_KEY = "postOfficeBox";
	public static final String DESCRIPTION_KEY = "description";
	public static final String HOME_PHONE_KEY = "homePhone";
	public static final String TELEPHONE_KEY = "telephoneNumber";
	public static final String PHYSICAL_DELIVERY_OFFICE_NAME_KEY = "physicalDeliveryOfficeName";
	public static final String MOBILE_KEY = "mobile";
	public static final String ROOT_NUMBER_KEY = "roomNumber";
	public static final String L_KEY = "l";
	public static final String ORG_KEY = "o";	
	public static final String ST_KEY = "ST";
	public static final String SURNAME_KEY = "sn";
	public static final String OU_KEY = "ou";
	public static final String GIVEN_NAME_KEY = "givenName";
	
	
	private Account account;

	public UserResponse(Account user) {
		
		this.account = user;
	}

	public String asJsonString() throws JSONException {

		JSONObject jsonAccount = new JSONObject();
		
		jsonAccount.put(UUID_KEY, account.getUid());
		jsonAccount.put(STREET_KEY, account.getPostalAddress());
		jsonAccount.put(TITLE_KEY, account.getTitle());
		// TODO jsonAccount.put(FACSIMILE_KEY, account.getFacsimile());
		jsonAccount.put(POSTAL_CODE_KEY, account.getPostalCode());
		jsonAccount.put(MAIL_KEY, account.getEmail());
		jsonAccount.put(POSTAL_ADDRESS_KEY, account.getPostalAddress());
		// TODO jsonAccount.put(POSTAL_OFFICE_BOX_KEY, account.getPostalOfficeBox());
		jsonAccount.put(DESCRIPTION_KEY, account.getDescription()); 
		// TODO jsonAccount.put(HOME_PHONE_KEY, account.getPhone()); 
		jsonAccount.put(TELEPHONE_KEY, account.getPhone()); 
		jsonAccount.put(PHYSICAL_DELIVERY_OFFICE_NAME_KEY, account.getPhysicalDeliveryOfficeName()); 
		// TODO jsonAccount.put(MOBILE_KEY, account.getMobile()); 
		// TODO jsonAccount.put(ROOT_NUMBER_KEY, account.getRootNumbe()); 
		// TODO jsonAccount.put(L_KEY, account.getL()); 
		jsonAccount.put(ORG_KEY, account.getOrg()); 
		// TODO jsonAccount.put(ST_KEY, account.getSt()); 
		jsonAccount.put(SURNAME_KEY, account.getSurname());
		// TODO jsonAccount.put(OU_KEY, account.getOu());
		
		jsonAccount.put(GIVEN_NAME_KEY, account.getGivenName());
    	
		return jsonAccount.toString();
	}
	
	
	
	

}
