/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

package org.georchestra.console.ws.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ReCaptchaV2;
import org.georchestra.ldapadmin.bs.ReCaptchaParameters;
import org.springframework.validation.Errors;

/**
 * Utility class to manage recaptcha.
 * Updated to use recaptcha V2
 * 
 * @author Sylvain Lesage
 *
 */
public class RecaptchaUtils {
	
	private static final Log LOG = LogFactory.getLog(RecaptchaUtils.class.getName());

	private RecaptchaUtils() {
		
	}

	/**
	 * This validate from server side the captchaV2 response from client
	 * 
	 * @param reCaptchaParameters to get url call and captcha private key
	 * @param gRecaptchaResponse g-recaptcha-reponse from client side
	 * @param errors Errors already existing, and used to display validation errors
	 * 
	 * @throws IOException
	 */
	public static void validate(ReCaptchaParameters reCaptchaParameters, String gRecaptchaResponse, Errors errors) {

		if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
			LOG.info("The user response to recaptcha is empty.");
			errors.rejectValue("recaptcha_response_field", "recaptcha_response_field.error.required", "required");
		}else{
			ReCaptchaV2 rec = new ReCaptchaV2();
			if(!rec.isValid(reCaptchaParameters.getVerifyUrl(), reCaptchaParameters.getPrivateKey(), gRecaptchaResponse)){
				errors.rejectValue("recaptcha_response_field", "recaptcha.incorrect", "Validation error");
			} else {
				LOG.debug("The user response to recaptcha is valid.");
			}
		}
	}

}
