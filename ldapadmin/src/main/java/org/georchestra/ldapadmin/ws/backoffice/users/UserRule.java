/**
 * 
 */
package org.georchestra.ldapadmin.ws.backoffice.users;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 * Rules to valid whether the user is protected.
 * <p>
 * A protected user is only known by the system. Thus this class maintains the list of the protected users, 
 * configured by the system administrator.  
 * </p>
 * 
 * 
 * @author Mauricio Pazos
 *
 */
public class UserRule {

	private static final Log LOG = LogFactory.getLog(UserRule.class.getName());
	
	private List<String> listOfprotectedUsers = new LinkedList<String>(); 
	
	public List<String> getListOfprotectedUsers() {
		return listOfprotectedUsers;
	}

	public void setListOfprotectedUsers(List<String> listOfprotectedUsers) {
		this.listOfprotectedUsers = listOfprotectedUsers;
	}

	public UserRule(){}

	/**
	 * True if the uid is a protected user
	 * @param uid
	 * @return
	 */
	public boolean isProtected(final String uid) {
		
		assert uid != null;
		
		if(this.listOfprotectedUsers.isEmpty()){
			LOG.warn("There isn't any protected user configured");
		}

		return this.listOfprotectedUsers.contains(uid);
	}

	public List<String> getListUidProtected() {

		if(this.listOfprotectedUsers.isEmpty()){
			LOG.warn("There isn't any protected user configured");
		}
		
		return this.listOfprotectedUsers;
	}

}
