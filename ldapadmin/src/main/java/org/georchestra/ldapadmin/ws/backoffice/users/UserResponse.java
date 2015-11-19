package org.georchestra.ldapadmin.ws.backoffice.users;

import java.io.IOException;

import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.UserSchema;
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

	public String asJsonString() throws IOException {

		try{
			JSONObject jsonAccount = new JSONObject();
			
			jsonAccount.put(UserSchema.UID_KEY, account.getUid());
			jsonAccount.put(UserSchema.STREET_KEY, account.getStreet());
			jsonAccount.put(UserSchema.TITLE_KEY, account.getTitle());
			
			jsonAccount.put(UserSchema.FACSIMILE_KEY, account.getFacsimile());
			
			jsonAccount.put(UserSchema.POSTAL_CODE_KEY, account.getPostalCode());
			jsonAccount.put(UserSchema.MAIL_KEY, account.getEmail());
			jsonAccount.put(UserSchema.POSTAL_ADDRESS_KEY, account.getPostalAddress());
			jsonAccount.put(UserSchema.POST_OFFICE_BOX_KEY, account.getPostOfficeBox());
			jsonAccount.put(UserSchema.DESCRIPTION_KEY, account.getDescription()); 
			jsonAccount.put(UserSchema.TELEPHONE_KEY, account.getPhone()); 
			jsonAccount.put(UserSchema.PHYSICAL_DELIVERY_OFFICE_NAME_KEY, account.getPhysicalDeliveryOfficeName()); 
			jsonAccount.put(UserSchema.MOBILE_KEY, account.getMobile()); 
			jsonAccount.put(UserSchema.ROOM_NUMBER_KEY, account.getRoomNumber()); 
			jsonAccount.put(UserSchema.LOCALITY_KEY, account.getLocality()); 
			jsonAccount.put(UserSchema.ORG_KEY, account.getOrg()); 
			jsonAccount.put(UserSchema.STATE_OR_PROVINCE_KEY, account.getStateOrProvince()); 
			jsonAccount.put(UserSchema.SURNAME_KEY, account.getSurname());
			jsonAccount.put(UserSchema.ORG_UNIT_KEY, account.getOrganizationalUnit());
			
			jsonAccount.put(UserSchema.GIVEN_NAME_KEY, account.getGivenName());
	    	
			return jsonAccount.toString();
			
		} catch (JSONException ex){

			throw new IOException(ex);
		}
	}
	
	
	
	

}
