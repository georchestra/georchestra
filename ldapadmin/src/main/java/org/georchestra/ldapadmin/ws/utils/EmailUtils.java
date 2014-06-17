/**
 *
 */
package org.georchestra.ldapadmin.ws.utils;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * Utility class to manage the email.
 *
 * @author Mauricio Pazos
 *
 */
public class EmailUtils {

	public static void validate(String email, Errors errors) {

		if ( !StringUtils.hasLength(email) && Validation.isFieldRequired("email") ) {
			errors.rejectValue("email", "email.error.required", "required");
		} else {
			if (!EmailValidator.getInstance().isValid(email)) {
				errors.rejectValue("email", "email.error.invalidFormat", "Invalid Format");
			}
		}
	}

}
