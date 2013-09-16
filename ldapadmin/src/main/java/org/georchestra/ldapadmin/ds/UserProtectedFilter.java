package org.georchestra.ldapadmin.ds;

import java.util.LinkedList;
import java.util.List;

/**
 * Filter the uid. 
 * 
 * @author Mauricio Pazos
 *
 */
public class UserProtectedFilter {

	private List<String> uidList = new LinkedList<String>();
	
	public UserProtectedFilter(final List<String> list) {
		
		uidList.addAll(list);
	}
	
	public void add(final String uid) {
		uidList.add(uid);
	}
	
	public boolean isTrue(final String uid) {
		
		return uidList.contains(uid);
	}
	
 }
