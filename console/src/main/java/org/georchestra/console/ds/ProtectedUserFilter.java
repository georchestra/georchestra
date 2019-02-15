/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.console.ds;

import java.util.*;

import org.georchestra.console.dto.Account;

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
