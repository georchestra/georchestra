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
