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

package org.georchestra.ldapadmin.ws.backoffice.utils;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request useful methods to manage request data structure.
 *
 * @author Mauricio Pazos
 *
 */
public class RequestUtil {


	private RequestUtil(){
		// utility class
	}

	/**
	 * Retrieve the resource's <b>key</b> from the path.
	 *
	 * @param request [BASE_MAPPING]/resource/{key}
	 *
	 * @return returns the <b>key</b> from request
	 */
	public static String getKeyFromPathVariable(final HttpServletRequest request) {

		String str = request.getRequestURI();

		String[] path = str.split("/");

		String uid = path[path.length - 1];

		return uid;
	}


	/**
	 * Searches the resource's key in the URI
	 *
	 * @param request format http://BASE_MAPPING/{resourceName}/{resourceKey}
	 * @param resourceName
	 *
	 * @return the resource key
	 */
	public static String getKeyFromPathVariable(final HttpServletRequest request, final String resourceName) {

		String str = request.getRequestURI();

		String[] path = str.split("/");

		int resourcePosition = -1;
		for (int i = 0; i < path.length; i++) {

			if(path[i].equalsIgnoreCase(resourceName)){
				resourcePosition = i;
			}
		}
		if ((resourcePosition == -1) || (resourcePosition >= path.length - 1)) {
			throw new IllegalArgumentException("resource not found:  " + resourceName);
		}
		String resourceKey = path[resourcePosition + 1];

		return resourceKey;
	}
	/**
	 * Returns the value associated to the fieldName.
	 *
	 * If the fieldName value is not present in the JSON object a null value is returned.
	 *
	 * @param json
	 * @param fieldName
	 *
	 * @return the value
	 */
	public static String getFieldValue(final JSONObject json, final String fieldName) {
		String value;
		try {
			value = json.getString(fieldName);
		} catch (JSONException e) {
			return null;
		}
		return value;
	}


}
