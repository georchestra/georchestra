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

package org.georchestra.ldapadmin.ws.utils;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class to manage recaptcha.
 * 
 * @author Sylvain Lesage
 *
 */
public class RecaptchaUtils {
	
	private static final Log LOG = LogFactory.getLog(RecaptchaUtils.class.getName());
	private String remoteAddr;
	private ReCaptcha reCaptcha;

	public RecaptchaUtils(final String remoteAddr, final ReCaptcha reCaptcha) {
		this.remoteAddr = remoteAddr;
		this.reCaptcha = reCaptcha;
	}

	public void validate(final String captchaGenerated, final String userResponse, Errors errors) {
		
		final String trimmedCaptcha = userResponse.trim();

		if(!StringUtils.hasLength(trimmedCaptcha)){
			LOG.info("The user response to recaptcha is empty.");
			errors.rejectValue("recaptcha_response_field", "recaptcha_response_field.error.required", "required");
		} else {
			
			ReCaptchaResponse captchaResponse = this.reCaptcha.checkAnswer(
					this.remoteAddr, 
					captchaGenerated, 
					userResponse);
			if(!captchaResponse.isValid()){
				LOG.info("The user response to recaptcha is not valid. The error message is '" + captchaResponse.getErrorMessage() + "' - see Error Code Reference at https://developers.google.com/recaptcha/docs/verify.");
				errors.rejectValue("recaptcha_response_field", "recaptcha_response_field.error.captchaNoMatch", "The texts didn't match");
			} else {
				LOG.debug("The user response to recaptcha is valid.");
			}
		}
	}
}
