/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice.users;

import java.util.LinkedList;
import java.util.List;


/**
 * 
 * Rules to valid the user status.
 * 
 * 
 * @author Mauricio Pazos
 *
 */
public class UserRule {

	private List<String> listOfprotectedUsers = new LinkedList<String>(); // FIXME should be retrieved from the configuration;
	
	public UserRule(){
		
		this.listOfprotectedUsers.add("extractorapp_privileged_admin");
	}

	public boolean isProtected(final String uid) {
		
		return this.listOfprotectedUsers.contains(uid);
	}

	public List<String> getListUidProtected() {

		return this.listOfprotectedUsers;
	}

}
