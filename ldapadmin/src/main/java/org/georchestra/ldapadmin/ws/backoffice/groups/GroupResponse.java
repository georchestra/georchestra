/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice.groups;

import java.io.IOException;

import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.GroupSchema;
import org.json.JSONArray;
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
			JSONObject jsonGroup = new JSONObject();
			
			jsonGroup.put(GroupSchema.COMMON_NAME_KEY, this.group.getName());

			jsonGroup.put(GroupSchema.DESCRIPTION_KEY, this.group.getDescription());

			// adds the list of users
			JSONArray membersArray = new JSONArray();
			int j = 0;
			for(String userUid: this.group.getUserList() ){

				membersArray.put(j, userUid);
				j++;
			}
			jsonGroup.put("users", membersArray);
			
			return jsonGroup.toString();
			
		} catch (JSONException ex){

			throw new IOException(ex);
		}
	}

}
