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

package org.georchestra.ldapadmin.ws.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * Contains useful method that are used in the form validation.
 *
 * @author Mauricio Pazos
 *
 */
public class UserUtils {

	@Autowired
	private static Validation validation;


	private UserUtils(){
			// utility class
	}

	public static void validate(String uid, String firstName, String surname, Errors errors) {

		// uid validation
		if( !StringUtils.hasLength(uid) && UserUtils.validation.isFieldRequired("uid") ){
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

		if( !StringUtils.hasLength(firstName) && UserUtils.validation.isFieldRequired("firstName") ){
			errors.rejectValue("firstName", "firstName.error.required", "required");
		}

		if( !StringUtils.hasLength( surname ) && UserUtils.validation.isFieldRequired("surname") ){
			errors.rejectValue("surname", "surname.error.required", "required");
		}
	}


}
