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

package org.georchestra.extractorapp.ws.extractor;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Mauricio Pazos
 *
 */
final class ExtractorUpdateAllPrioritiesRequest {

	public static final String  operationName = "updateAllPriorities";
    public static final String	UUID_LIST_KEY	= "uuidList";
    public static final String	UUID_KEY     	= "uuid";

    private List<String> uuidList;
    
	private ExtractorUpdateAllPrioritiesRequest(List<String> uuids) {
		
		assert uuids != null;
		
		uuidList = uuids;
	}

	public List<String> asList() {

    	return uuidList;
	}

	/**
	 * 
	 * @param postUuidList format {["uuid1", "uuid2", ....]}
	 * @return
	 * @throws JSONException
	 */
	public static ExtractorUpdateAllPrioritiesRequest parseJson(String postUuidList) throws JSONException {

    	JSONObject jsonRequest = JSONUtil.parseStringToJSon(postUuidList);
    	
    	JSONArray uuidArray =  jsonRequest.names();
		List<String> uuids = new LinkedList<String>();
		
    	for (int i = 0; i < uuidArray.length(); i++) {

    		String uuid = uuidArray.getString(i);
			uuids.add(uuid );
		}
    	
		ExtractorUpdateAllPrioritiesRequest request = new ExtractorUpdateAllPrioritiesRequest(uuids);
		
		return request;
	}

}
