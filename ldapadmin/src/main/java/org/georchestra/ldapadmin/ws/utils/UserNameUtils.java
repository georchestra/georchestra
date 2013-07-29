/**
 * 
 */
package org.georchestra.ldapadmin.ws.utils;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * Contains useful method that are used in the form validation.  
 * 
 * @author Mauricio Pazos
 *
 */
public class UserNameUtils {

	private UserNameUtils(){
			// utility class
	}
	
	
	public static void validate(String firstName, String surname, Errors errors) {
		if( !StringUtils.hasLength(firstName)){
			errors.rejectValue("firstName", "firstName.error.required", "required");
		}
		
		if( !StringUtils.hasLength( surname ) ){
			errors.rejectValue("surname", "surname.error.required", "required");
		}
	}

}
