/**
 * 
 */
package org.georchestra.ldapadmin.ws.changepassword;

import java.io.Serializable;

/**
 * @author Mauricio Pazos
 *
 */
public class ChangePasswordFormBean implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -546015147230737054L;
	
	
	private String uid;
	private String confirmPassword;
	private String password;
	
	@Override
	public String toString() {
		return "ChangePasswordFormBean [uid=" + uid + ", confirmPassword="
				+ confirmPassword + ", password=" + password + "]";
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getConfirmPassword() {
		return confirmPassword;
	}
	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	

	
}
