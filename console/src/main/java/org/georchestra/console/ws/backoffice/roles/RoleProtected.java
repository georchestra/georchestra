/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

package org.georchestra.console.ws.backoffice.roles;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RoleProtected {

	private static final Log LOG = LogFactory.getLog(RoleProtected.class.getName());

	private Set<String> listOfprotectedRoles = new HashSet<String>();

	public RoleProtected() {
	}

	public Set<String> getListOfprotectedRoles() {
		return listOfprotectedRoles;
	}

	public void setListOfprotectedRoles(String[] listOfprotectedRoles) {

		HashSet<String> res = new HashSet<String>();
		res.addAll(Arrays.asList(listOfprotectedRoles));
		this.listOfprotectedRoles = res;

	}

	/**
	 * True if the Roles is a protected roles
	 * 
	 * @param uid
	 *            uid of role
	 * 
	 * @return True if the Roles is a protected roles
	 */
	public boolean isProtected(final String uid) {

		if (this.listOfprotectedRoles.isEmpty())
			RoleProtected.LOG.warn("There isn't any protected roles configured");

		for (String reg : listOfprotectedRoles) {
			if (Pattern.matches(reg, uid))
				return true;
		}

		return false;
	}
}
