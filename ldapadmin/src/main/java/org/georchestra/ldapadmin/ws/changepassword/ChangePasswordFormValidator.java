/**
 * 
 */
package org.georchestra.ldapadmin.ws.changepassword;

import org.georchestra.ldapadmin.ws.utils.PasswordUtils;
import org.springframework.validation.BindingResult;

/**
 * Validates the change password form.
 * 
 * @author Mauricio Pazos
 *
 */
public class ChangePasswordFormValidator {

	public void validate(ChangePasswordFormBean form, BindingResult errors) {
		
		PasswordUtils.validate( form.getPassword(), form.getConfirmPassword(), errors);
		
	}

}
