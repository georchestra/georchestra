/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice.users;

import java.util.List;

import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.UserSchema;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
@RequestMapping(produces = "application/json; charset=utf-8")
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
    		jsonAccount.put(UserSchema.UUID_KEY, account.getUid());
    		jsonAccount.put(UserSchema.GIVEN_NAME_KEY, account.getGivenName());
    		jsonAccount.put(UserSchema.SURNAME_KEY, account.getSurname());
    		jsonAccount.put(UserSchema.ORG_KEY, account.getOrg());

    		jsonArray.put(i, jsonAccount);
    		i++;
		}
		String strTaskQueue = jsonArray.toString();
		
		return strTaskQueue;
	}
	
	
}
