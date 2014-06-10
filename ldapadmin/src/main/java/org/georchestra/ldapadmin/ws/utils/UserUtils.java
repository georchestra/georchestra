/**
 * 
 */
package org.georchestra.ldapadmin.ws.utils;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import org.georchestra.ldapadmin.ws.utils.Validation;

/**
 * Contains useful method that are used in the form validation.  
 * 
 * @author Mauricio Pazos
 *
 */
public class UserUtils {

	private static String uidRegExp = "[A-Za-z]+[A-Za-z0-9.]*";

	public static String getUidRegExp() {
		return uidRegExp;
	}
	public void setUidRegExp(String uidRegExp) {
		this.uidRegExp = uidRegExp;
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
	 * An user identifier (uid) valid only can contain characters, numbers or dot. It must begin with a character.
	 * 
	 * @param uid user identifier
	 * @return true if the uid is valid
	 */
	public static boolean isUidValid(String uid) {
		return uid.matches(uidRegExp);
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
