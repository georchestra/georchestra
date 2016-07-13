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

import java.util.List;

import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.UserSchema;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * Contains useful method to prepare the responses related with a list of user data.
 * 
 * The {@link UserListResponse#asJsonString()} is the responsible to convert the account list to json syntax
 * 
 * 
 * </pre>
 * 
 * @author Mauricio Pazos
 *
 */

final class UserListResponse {
	
	private List<Account> accountList;

	public UserListResponse(List<Account> accountList) {
		
		this.accountList = accountList;
	}

	/**
	 * Transforms the account list to a json string.
	 * <p>
	 * For example:
	 * </p>
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
	 * @return the list of account formatted as json syntax
	 * 
	 * @throws JSONException
	 */

	public String asJsonString() throws JSONException {

		JSONArray jsonArray = new JSONArray();
		int i = 0;
    	for (Account account: this.accountList) {
    		
    		JSONObject jsonAccount = new JSONObject();
    		jsonAccount.put(UserSchema.UID_KEY, account.getUid());
    		jsonAccount.put(UserSchema.GIVEN_NAME_KEY, account.getGivenName());
    		jsonAccount.put(UserSchema.SURNAME_KEY, account.getSurname());

    		jsonArray.put(i, jsonAccount);
    		i++;
		}
		String strTaskQueue = jsonArray.toString();
		
		return strTaskQueue;
	}
	
	
}
