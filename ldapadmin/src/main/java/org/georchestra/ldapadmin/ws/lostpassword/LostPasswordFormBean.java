package org.georchestra.ldapadmin.ws.lostpassword;

import java.io.Serializable;

public class LostPasswordFormBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7773803527246666406L;
	
	private String email;
	private String recaptcha_challenge_field;
	private String recaptcha_response_field;
	
	@Override
	public String toString() {
		return "LostPasswordFormBean [email=" + email
				+ ", recaptcha_challenge_field=" + recaptcha_challenge_field
				+ ", recaptcha_response_field=" + recaptcha_response_field
				+ "]";
	}
	
	
	public String getEmail() {
		return this.email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRecaptcha_challenge_field() {
		return recaptcha_challenge_field;
	}
	public void setRecaptcha_challenge_field(String recaptcha_challenge_field) {
		this.recaptcha_challenge_field = recaptcha_challenge_field;
	}
	public String getRecaptcha_response_field() {
		return recaptcha_response_field;
	}
	public void setRecaptcha_response_field(String recaptcha_response_field) {
		this.recaptcha_response_field = recaptcha_response_field;
	}
	
}
