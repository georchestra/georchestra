/**
 *
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
			value = null;
		}
		return value;
	}


}
