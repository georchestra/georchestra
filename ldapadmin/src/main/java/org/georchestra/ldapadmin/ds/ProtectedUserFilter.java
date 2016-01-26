package org.georchestra.ldapadmin.ds;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.georchestra.ldapadmin.Configuration;
import org.georchestra.ldapadmin.dto.Account;

/**
 * This class filters the user identifier (uid), if it is a protected user.
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
	 * Checks if the uid given as argument is a protected user
	 * @param uid
	 * @return true if is protected, false in other case.
	 */
	public boolean isProtected(final String uid) {
		return uidList.contains(uid);
	}

        public List<Account> filterUsersList(final List<Account> users) {

		// removes the protected users.
		TreeSet<Account> filtered = new TreeSet<Account>();

		for (Account account : users) {
			if(!this.isProtected(account.getUid())) {
				filtered.add(account);
			}
		}

		List<Account> list = new LinkedList<Account>(filtered);

		return list;
	}

        public List<String> filterStringList(final List<String> users) {

		// removes the protected users.
		TreeSet<String> filtered = new TreeSet<String>();
		for (String uid : users) {

			if (!this.isProtected(uid)) {
				filtered.add(uid);
			}
		}

		List<String> list = new LinkedList<String>(filtered);

		return list;
	}
}
