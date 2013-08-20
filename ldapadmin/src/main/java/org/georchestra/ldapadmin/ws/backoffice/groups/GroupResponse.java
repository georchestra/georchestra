/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice.groups;

import java.io.IOException;

import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.GroupSchema;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Mauricio Pazos
 *
 */
public class GroupResponse {

	private Group group;

	public GroupResponse(Group group) {

		this.group = group;
	}

	public String asJsonString() throws IOException {
		try{
			JSONObject jsonAccount = new JSONObject();
			
			jsonAccount.put(GroupSchema.COMMON_NAME_KEY, this.group.getName());

			jsonAccount.put(GroupSchema.DESCRIPTION_KEY, this.group.getDescription()); 
	    	
			return jsonAccount.toString();
			
		} catch (JSONException ex){

			throw new IOException(ex);
		}
	}

}
