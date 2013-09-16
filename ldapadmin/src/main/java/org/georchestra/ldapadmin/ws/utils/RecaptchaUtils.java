package org.georchestra.ldapadmin.ws.utils;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * Utility class to manage recaptcha.
 * 
 * @author Sylvain Lesage
 *
 */
public class RecaptchaUtils {
	
	private String remoteAddr;
	private ReCaptcha reCaptcha;

	public RecaptchaUtils(final String remoteAddr, final ReCaptcha reCaptcha) {
		this.remoteAddr = remoteAddr;
		this.reCaptcha = reCaptcha;
	}

	public void validate(final String captchaGenerated, final String userResponse, Errors errors) {
		
		final String trimmedCaptcha = userResponse.trim();

		if(!StringUtils.hasLength(trimmedCaptcha)){
			errors.rejectValue("recaptcha_response_field", "recaptcha_response_field.error.required", "required");
		} else {
			
			ReCaptchaResponse captchaResponse = this.reCaptcha.checkAnswer(
					this.remoteAddr, 
					captchaGenerated, 
					userResponse);
			if(!captchaResponse.isValid()){
				if(!captchaGenerated.equals(trimmedCaptcha)){
					errors.rejectValue("recaptcha_response_field", "recaptcha_response_field.error.captchaNoMatch", "The texts didn't match");
					
				}
			}
		}
	}
}
