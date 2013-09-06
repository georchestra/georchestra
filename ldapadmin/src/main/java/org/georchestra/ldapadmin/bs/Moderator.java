/**
 * 
 */
package org.georchestra.ldapadmin.bs;



/**
 * Moderator attribute
 * 
 * 
 * @author Mauricio Pazos
 *
 */
public final class Moderator {

	private boolean requiresSignup = false;

	private String moderatorEmail = "moderator@mail";
	
	public boolean isRequiresSignup() {
		return requiresSignup;
	}
	public void setRequiresSignup(boolean requiresSignup) {
		this.requiresSignup = requiresSignup;
	}


	public String getModeratorEmail() {
		return moderatorEmail;
	}
	public void setModeratorEmail(String moderatorEmail) {
		this.moderatorEmail = moderatorEmail;
	}

	public boolean requiresSignup() {
		return this.requiresSignup;
	}
	
	

}
