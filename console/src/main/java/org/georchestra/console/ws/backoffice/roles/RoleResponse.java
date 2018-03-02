/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

package org.georchestra.console.ws.backoffice.roles;

import java.io.IOException;
import java.util.List;

import org.georchestra.console.ds.ProtectedUserFilter;
import org.georchestra.console.dto.Role;
import org.georchestra.console.dto.RoleSchema;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Mauricio Pazos
 *
 */
public class RoleResponse {

	private Role role;
	private ProtectedUserFilter filter;

	public RoleResponse(Role role, ProtectedUserFilter filter) {

		this.role = role;
		this.filter = filter;
	}
	
	public String asJsonString() throws IOException {
		try{
			JSONObject jsonRole = new JSONObject();
			
			jsonRole.put(RoleSchema.COMMON_NAME_KEY, this.role.getName());

			jsonRole.put(RoleSchema.DESCRIPTION_KEY, this.role.getDescription());

			// adds the list of users
			List<String> list = filter.filterStringList(this.role.getUserList());

			JSONArray membersArray = new JSONArray();
			int j = 0;
			for(String userUid: list ){

				membersArray.put(j, userUid);
				j++;
			}
			jsonRole.put("users", membersArray);
			
			return jsonRole.toString();
			
		} catch (JSONException ex){

			throw new IOException(ex);
		}
	}

}
