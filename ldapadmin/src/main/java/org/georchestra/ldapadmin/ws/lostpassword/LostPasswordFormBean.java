package org.georchestra.ldapadmin.ws.lostpassword;

import java.io.Serializable;

public class LostPasswordFormBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7773803527246666406L;
	
	private String email;

	
	@Override
	public String toString() {
		return "LostPasswordFormBean [email=" + email + "]";
	}
	
	
	public String getEmail() {
		return this.email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
