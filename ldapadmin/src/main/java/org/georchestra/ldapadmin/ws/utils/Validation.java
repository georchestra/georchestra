/**
 * 
 */
package org.georchestra.ldapadmin.ws.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Validation class for all forms
 * 
 * @author Sylvain Lesage
 *
 */
public class Validation {

	private static List<String> requiredFields;
	public String getRequiredFields() {
		return requiredFields.toString();
	}
	public void setRequiredFields(String csvRequiredFields) {
		List<String> r = new ArrayList<String>(Arrays.asList(csvRequiredFields.split("\\s*,\\s*")));
		// add mandatory fields (they may be present two times, it's not a problem)
		r.add("email");
		r.add("uid");
		r.add("password");
		this.requiredFields = r;
	}
	public static boolean isFieldRequired (String field) {
		for (String f : requiredFields) {
			if (field.equals(f)) {
				return true;
			}
		}
		return false;
	}
}
