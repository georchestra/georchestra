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

package org.georchestra.console.dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A role and its users.
 *
 * @author Mauricio Pazos
 *
 */
class RoleImpl implements Role, Comparable<Role> {

	private String name;
	private List<String> userList = new LinkedList<String>();
	private String description;
	private boolean isFavorite;

	/* (non-Javadoc)
	 * @see org.georchestra.console.dto.Role#getCommonName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see org.georchestra.console.dto.Role#setCommonName(java.lang.String)
	 */
	@Override
	public void setName(String cn) {
		this.name = cn;
	}

	/* (non-Javadoc)
	 * @see org.georchestra.console.dto.Role#getMemberUid()
	 */
	@Override
	public List<String> getUserList() {
		return this.userList;
	}

	/* (non-Javadoc)
	 * @see org.georchestra.console.dto.Role#setMemberUid(java.util.List)
	 */
	@Override
	public void setUserList(List<String> userUidList) {
		this.userList = userUidList;
		// The full DN is stored LDAP-side, we only need
		// the user identifier (uid).
		Iterator<String> uids = userUidList.iterator();
		while (uids.hasNext()) {
			String cur = uids.next();
			cur = cur.replaceAll("uid=([^,]+).*$", "$1");
		}
	}

	/* (non-Javadoc)
	 * @see org.georchestra.console.dto.Role#addMemberUid(java.lang.String)
	 */
	@Override
	public void addUser(String userUid) {
		// Extracting the uid
		this.userList.add(userUid.replaceAll("uid=([^,]+).*$", "$1"));
	}

	@Override
	public void setDescription(String description) {
		this.description = description;

	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}

	@Override
	public boolean isFavorite() {
		return this.isFavorite;
	}


	@Override
	public String toString() {
		return "RoleImpl [name=" + name + ", userList=" + userList
				+ ", description=" + description
				+ "]";
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    @Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((name == null) ? 0 : name.hashCode());
	    return result;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    @Override
    public boolean equals(Object obj) {
	    if (this == obj) {
		    return true;
	    }
	    if (obj == null) {
		    return false;
	    }
	    if (!(obj instanceof RoleImpl)) {
		    return false;
	    }
	    RoleImpl other = (RoleImpl) obj;
	    if (name == null) {
		    if (other.name != null) {
			    return false;
		    }
	    } else if (!name.equals(other.name)) {
		    return false;
	    }
	    return true;
    }

	@Override
    public int compareTo(Role o) {

	    return this.name.compareTo(o.getName());
    }


}
