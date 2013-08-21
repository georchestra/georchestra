/**
 * 
 */
package org.georchestra.ldapadmin.ws.lostpassword;

import java.io.Serializable;

/**
 * Maintains the new password typed by the user. 
 * 
 * @author Mauricio Pazos
 */
public class NewPasswordFormBean implements Serializable {
	
	private static final long serialVersionUID = 3239632432961416372L;

	private String uid;
	private String token;
	private String password;
	private String confirmPassword;
	
	
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
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
	
	@Override
	public String toString() {
		return "NewPasswordFormBean [uid=" + uid + ", token=" + token
				+ ", password=" + password + ", confirmPassword="
				+ confirmPassword + "]";
	}
	

}
