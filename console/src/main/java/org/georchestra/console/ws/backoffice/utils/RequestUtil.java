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

package org.georchestra.console.ws.backoffice.utils;


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
		return json.optString(fieldName, null);
	}

	/**
	 * Returns the boolean value associated to the fieldName.
	 * @param json
	 * @param fieldName
	 *
	 * @return the value as boolean
	 */
	public static Boolean getBooleanFieldValue(final JSONObject json, final String fieldName) {
		Boolean value;
		try {
			value = json.getBoolean(fieldName);
		} catch (JSONException e) {
			return false;
		}
		return value;
	}
}
