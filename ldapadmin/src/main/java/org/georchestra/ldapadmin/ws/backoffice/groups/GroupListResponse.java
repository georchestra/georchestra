/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice.groups;

import java.util.List;

import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.GroupSchema;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Mauricio Pazos
 *
 */
final class GroupListResponse {

	private List<Group> groupList;

	public GroupListResponse(List<Group> list) {
		this.groupList = list;
	}

	public String asJsonString() throws JSONException {

		JSONArray jsonGroupArray = new JSONArray();
		int i = 0;
    	for (Group group: this.groupList) {
    		
    		JSONObject jsonGroup = new JSONObject();
    		
    		jsonGroup.put(GroupSchema.COMMON_NAME_KEY, group.getName());

    		jsonGroup.put(GroupSchema.DESCRIPTION_KEY, group.getDescription());

    		// adds the list of users
    		JSONArray membersArray = new JSONArray();
    		int j = 0;
    		for(String userUid: group.getUserList() ){
    			
    			membersArray.put(j, userUid);
    			j++;
    		}
    		jsonGroup.put("users", membersArray);
    		
    		jsonGroupArray.put(i, jsonGroup);
    		i++;
		}
		String strTaskQueue = jsonGroupArray.toString();
		
		return strTaskQueue;
	}

}
