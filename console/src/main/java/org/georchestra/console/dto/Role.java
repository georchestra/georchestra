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

import java.util.List;

/**
 * This class represents a Group stored in the LDAP tree.
 * 
 * @author Mauricio Pazos
 */
public interface Group {
	
	final String USER = "USER";
	final String PENDING = "PENDING";

	/**
	 * 
	 * @return the name of this role
	 */
	String getName();
	void  setName(String cn );

	/**
	 * Users of this role
	 * 
	 * @return the list of user 
	 */
	List<String> getUserList();
	
	void  setUserList(List<String> userUidList);
	
	/**
	 * adds a user to this role
	 * @param userUid a user dn
	 */
	void  addUser(String userUid);
	
	void setDescription(String description);	

	String getDescription();
		
}
