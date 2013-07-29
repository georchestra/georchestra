package org.georchestra.ldapadmin.ws.newaccount;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.commons.validator.routines.EmailValidator;
import org.georchestra.ldapadmin.ws.utils.EmailUtils;
import org.georchestra.ldapadmin.ws.utils.PasswordUtils;
import org.georchestra.ldapadmin.ws.utils.UserNameUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;


class AccountFormValidator {
	
	private String remoteAddr;
	private ReCaptcha reCaptcha;

	public AccountFormValidator(final String remoteAddr, final ReCaptcha reCaptcha) {
		
		this.remoteAddr = remoteAddr;
		this.reCaptcha = reCaptcha;
	}

	public void validate(AccountFormBean form, Errors errors) {
		
		UserNameUtils.validate( form.getFirstName(), form.getSurname(), errors ); 

        EmailUtils.validate(form.getEmail(), errors);
		
		PasswordUtils.validate( form.getPassword(), form.getConfirmPassword(), errors);
		
		validatePhone(form.getPhone(), errors); 

		validateCaptcha(form.getRecaptcha_challenge_field(), form.getRecaptcha_response_field(), errors);
			
	}


	private void validateCaptcha(final String captchaGenerated, final String userResponse, Errors errors) {
		
		
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

	private void validatePhone(final String phone, Errors errors) {
		
		if (StringUtils.hasLength(phone.trim())) {
			for (int i = 0; i < phone.length(); ++i) {
				if ((Character.isDigit(phone.charAt(i))) == false) {
					errors.rejectValue("phone", "phone.error.nonNumeric", "The phone should be numeric");
					break;
				}
			}
		}
	}

}
