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
