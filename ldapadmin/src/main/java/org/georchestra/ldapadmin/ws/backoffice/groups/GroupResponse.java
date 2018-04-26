/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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
