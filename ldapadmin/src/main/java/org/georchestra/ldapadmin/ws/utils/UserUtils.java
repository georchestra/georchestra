package org.georchestra.ldapadmin.ws.utils;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * Contains useful method that are used in the form validation.
 *
 * @author Mauricio Pazos
 *
 */
public class UserUtils {

	private UserUtils(){
			// utility class
	}

	public static void validate(String uid, String firstName, String surname, Errors errors) {

		// uid validation
		if( !StringUtils.hasLength(uid) && Validation.isFieldRequired("uid") ){
			errors.rejectValue("uid", "uid.error.required", "required");
		} else{

			if( StringUtils.hasLength(uid) && !isUidValid(uid)){
				errors.rejectValue("uid", "uid.error.invalid", "required");
			}
		}

		// name validation
		validate(firstName, surname, errors);
	}

	/**
	 * A valid user identifier (uid) can only contain characters, numbers, hyphens or dot. It must begin with a character.
	 *
	 * @param uid user identifier
	 * @return true if the uid is valid
	 */
	private static boolean isUidValid(String uid) {

		char firstChar = uid.charAt(0);
		if(!Character.isLetter(firstChar)){
			return false;
		}
		for(int i=1; i < uid.length(); i++){
			
			if( !(Character.isLetter( uid.charAt(i)) ||  Character.isDigit( uid.charAt(i)) || ( uid.charAt(i) == '.') || ( uid.charAt(i) == '-')) ){
				
				return false;
			}
		}
		return true;
	}

	public static void validate(String firstName, String surname, Errors errors) {

		if( !StringUtils.hasLength(firstName) && Validation.isFieldRequired("firstName") ){
			errors.rejectValue("firstName", "firstName.error.required", "required");
		}

		if( !StringUtils.hasLength( surname ) && Validation.isFieldRequired("surname") ){
			errors.rejectValue("surname", "surname.error.required", "required");
		}
	}


}
