/**
 * 
 */
package org.georchestra.ldapadmin.ws.newaccount;

import java.io.Serializable;
import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;


/**
 * This model maintains the account form data.
 * 
 * @author Mauricio Pazos
 *
 */
public class AccountFormBean implements Serializable{

	private static final long serialVersionUID = 6955470190631684934L;
	
	private String uid;
	private String firstName;
	private String surname;
	
	private String org;
	private String email;
	private String phone;
	private String details;
	private String password;
	private String confirmPassword;
	
	private String recaptcha_challenge_field;
	private String recaptcha_response_field;	
	
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
	
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String name) {
		this.firstName = name;
	}
	public String getOrg() {
		return org;
	}
	public void setOrg(String org) {
		this.org = org;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getConfirmPassword() {
		return confirmPassword;
	}
	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
	
	public String getSurname() {
		return surname;
	}
	
	public void setSurname(String sn){

		this.surname = sn;
	}
	@Override
	public String toString() {
		return "AccountFormBean [uid=" + uid + ", firstName=" + firstName
				+ ", surname=" + surname + ", org=" + org + ", email=" + email
				+ ", phone=" + phone + ", details=" + details + ", password="
				+ password + ", confirmPassword=" + confirmPassword
				+ ", recaptcha_challenge_field=" + recaptcha_challenge_field
				+ ", recaptcha_response_field=" + recaptcha_response_field
				+ "]";
	}
	

	
}
