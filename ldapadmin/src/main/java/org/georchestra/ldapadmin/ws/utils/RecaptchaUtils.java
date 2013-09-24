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
				LOG.info("The user response to recaptcha is not valid. The error message is '" + captchaResponse.getErrorMessage() + "'");
				errors.rejectValue("recaptcha_response_field", "recaptcha_response_field.error.captchaNoMatch", "The texts didn't match");
			}
		}
	}
}
