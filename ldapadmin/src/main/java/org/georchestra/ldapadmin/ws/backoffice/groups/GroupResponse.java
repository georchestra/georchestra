/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice.groups;

import java.io.IOException;
import java.util.List;

import org.georchestra.ldapadmin.ds.ProtectedUserFilter;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.GroupSchema;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Mauricio Pazos
 *
 */
public class GroupResponse {

	private Group group;
	private ProtectedUserFilter filter;

	public GroupResponse(Group group, ProtectedUserFilter filter) {

		this.group = group;
		this.filter = filter;
	}
	@RequestMapping(produces = "application/json; charset=utf-8")
	public String asJsonString() throws IOException {
		try{
			JSONObject jsonGroup = new JSONObject();
			
			jsonGroup.put(GroupSchema.COMMON_NAME_KEY, this.group.getName());

			jsonGroup.put(GroupSchema.DESCRIPTION_KEY, this.group.getDescription());

			// adds the list of users
			List<String> list = filter.filterStringList(this.group.getUserList());

			JSONArray membersArray = new JSONArray();
			int j = 0;
			for(String userUid: list ){

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
