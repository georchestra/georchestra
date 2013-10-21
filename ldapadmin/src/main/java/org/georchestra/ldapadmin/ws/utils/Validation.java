/**
 * 
 */
package org.georchestra.ldapadmin.ws.utils;

import java.util.Arrays;
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
		List<String> requiredFields = Arrays.asList(csvRequiredFields.split("\\s*,\\s*"));
		this.requiredFields = requiredFields;
	}
	public static String isFieldRequired (String field) {
		for (String f : requiredFields) {
			if (field.equals(f)) {
				return "true";
			}
		}
		return "false";
	}
}
