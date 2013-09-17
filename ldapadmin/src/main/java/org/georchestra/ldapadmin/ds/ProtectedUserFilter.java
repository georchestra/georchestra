package org.georchestra.ldapadmin.ds;

import java.util.LinkedList;
import java.util.List;

/**
 * Filter the user identifier (uid), if it is a protected user. 
 * 
 * @author Mauricio Pazos
 *
 */
public class ProtectedUserFilter {

	private List<String> uidList = new LinkedList<String>();
	
	/**
	 * New instance of filter.
	 * 
	 * @param listOfUid list of protected users
	 */
	public ProtectedUserFilter(final List<String> listOfUid) {
		
		uidList.addAll(listOfUid);
	}
	
	/**
	 * Adds the uid to the list of protectd users
	 * @param uid
	 */
	public void add(final String uid) {
		uidList.add(uid);
	}
	
	/**
	 * True if the uid is a protected user 
	 * @param uid
	 * @return true if is protected, false in other case.
	 */ 
	public boolean isTrue(final String uid) {
		
		return uidList.contains(uid);
	}
	
 }
