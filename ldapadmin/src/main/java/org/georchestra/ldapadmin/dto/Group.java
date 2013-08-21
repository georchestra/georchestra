package org.georchestra.ldapadmin.dto;

import java.util.List;

/**
 * This class represents a Group stored in the LDAP tree.
 * 
 * @author Mauricio Pazos
 */
public interface Group {
	
	final String SV_USER = "SV_USER";
	final String PENDING_USERS = "PENDING_USER";

	/**
	 * 
	 * @return the name of this group
	 */
	String getName();
	void  setName(String cn );

	/**
	 * Users of this group
	 * 
	 * @return the list of user 
	 */
	List<String> getUserList();
	
	void  setUserList(List<String> userUidList);
	
	/**
	 * adds a user to this group
	 * @param userUid a user uid
	 */
	void  addUser(String userUid);	

}
