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

package org.georchestra.extractorapp.ws.extractor;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Utility method to manage JSON object
 * 
 * @author Mauricio Pazos
 *
 */
final class JSONUtil {
	
	
	private JSONUtil(){
		// utility class
	}
    /**
     * Takes a string and parses it to a JSON object structure
     */
    public static JSONObject parseStringToJSon(String jsonData) throws JSONException {
        final String trimmed = jsonData.trim ();
        final JSONTokener tokener = new JSONTokener (trimmed);
        JSONObject jsonObject = new JSONObject (tokener);
        return jsonObject;
    }

}
