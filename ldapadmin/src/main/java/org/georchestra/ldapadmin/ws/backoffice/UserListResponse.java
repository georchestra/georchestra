/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice;

import java.util.List;

import org.georchestra.ldapadmin.dto.Account;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;

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
	
	public static final String UUID_KEY = "uid";
	public static final String GIVEN_NAME_KEY = "givenName";
	public static final String SURNAME_KEY = "sn";
	public static final String ORG_KEY = "o";	

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

		JSONArray jsonTaskArray = new JSONArray();
		int i = 0;
    	for (Account account: this.accountList) {
    		
    		JSONObject jsonAccount = new JSONObject();
    		jsonAccount.put(UUID_KEY, account.getUid());
    		jsonAccount.put(GIVEN_NAME_KEY, account.getGivenName());
    		jsonAccount.put(SURNAME_KEY, account.getSurname());
    		jsonAccount.put(ORG_KEY, account.getOrg());

    		jsonTaskArray.put(i, jsonAccount);
    		i++;
		}
		String strTaskQueue = jsonTaskArray.toString();
		
		return strTaskQueue;
	}
	
	
}
