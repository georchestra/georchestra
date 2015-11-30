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

	private boolean moderatedSignup = true;

	private String moderatorEmail = "moderator@mail";

	public void setModeratedSignup(boolean moderatedSignup) {
		this.moderatedSignup = moderatedSignup;
	}

	public String getModeratorEmail() {
		return moderatorEmail;
	}

	public void setModeratorEmail(String moderatorEmail) {
		this.moderatorEmail = moderatorEmail;
	}

	public boolean moderatedSignup() {
		return this.moderatedSignup;
	}
}

