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
	
	private Account account;

	public UserResponse(Account user) {
		
		this.account = user;
	}

	public String asJsonString() throws JSONException {

		JSONObject jsonAccount = new JSONObject();
		
		jsonAccount.put(UserSchema.UUID_KEY, account.getUid());
		jsonAccount.put(UserSchema.STREET_KEY, account.getPostalAddress());
		jsonAccount.put(UserSchema.TITLE_KEY, account.getTitle());
		// TODO jsonAccount.put(UserSchema.FACSIMILE_KEY, account.getFacsimile());
		jsonAccount.put(UserSchema.POSTAL_CODE_KEY, account.getPostalCode());
		jsonAccount.put(UserSchema.MAIL_KEY, account.getEmail());
		jsonAccount.put(UserSchema.POSTAL_ADDRESS_KEY, account.getPostalAddress());
		// TODO jsonAccount.put(UserSchema.POSTAL_OFFICE_BOX_KEY, account.getPostalOfficeBox());
		jsonAccount.put(UserSchema.DESCRIPTION_KEY, account.getDescription()); 
		// TODO jsonAccount.put(UserSchema.HOME_PHONE_KEY, account.getPhone()); 
		jsonAccount.put(UserSchema.TELEPHONE_KEY, account.getPhone()); 
		jsonAccount.put(UserSchema.PHYSICAL_DELIVERY_OFFICE_NAME_KEY, account.getPhysicalDeliveryOfficeName()); 
		// TODO jsonAccount.put(UserSchema.MOBILE_KEY, account.getMobile()); 
		// TODO jsonAccount.put(UserSchema.ROOT_NUMBER_KEY, account.getRootNumbe()); 
		// TODO jsonAccount.put(UserSchema.L_KEY, account.getL()); 
		jsonAccount.put(UserSchema.ORG_KEY, account.getOrg()); 
		// TODO jsonAccount.put(ST_KEY, account.getSt()); 
		jsonAccount.put(UserSchema.SURNAME_KEY, account.getSurname());
		// TODO jsonAccount.put(OU_KEY, account.getOu());
		
		jsonAccount.put(UserSchema.GIVEN_NAME_KEY, account.getGivenName());
    	
		return jsonAccount.toString();
	}
	
	
	
	

}
