/**
 *
 */
package org.georchestra.ldapadmin.ws.utils;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 *
 * @author Mauricio Pazos
 *
 */
public final class PasswordUtils  {

	public static final int SIZE = 8;


	private PasswordUtils(){
		// utility class
	}

	public static void validate(final String password, final String confirmPassword, Errors errors) {

		final String pwd1 = password.trim();
		final String pwd2 = confirmPassword.trim();

		if( !StringUtils.hasLength(pwd1) && Validation.isFieldRequired("password") ){

			errors.rejectValue("password", "password.error.required", "required");

		}
		if( !StringUtils.hasLength(pwd2) && Validation.isFieldRequired("confirmPassword") ){

			errors.rejectValue("confirmPassword", "confirmPassword.error.required", "required");
		}
		if( StringUtils.hasLength(pwd1) && StringUtils.hasLength(pwd2) ){

			if(!pwd1.equals(pwd2)){
				errors.rejectValue("confirmPassword", "confirmPassword.error.pwdNotEquals", "These passwords don't match");

			} else {

				if(pwd1.length() < SIZE ){
					errors.rejectValue("password", "password.error.sizeError", "The password does have at least 8 characters");
				}
			}
		}
	}

}
