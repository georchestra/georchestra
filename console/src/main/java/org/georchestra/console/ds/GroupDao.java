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

package org.georchestra.console.ds;

import java.util.List;

import org.georchestra.console.dto.Group;
import org.springframework.ldap.NameNotFoundException;

/**
 * @author Mauricio Pazos
 *
 */
public interface GroupDao {

	/**
	 * adds the user to the role
	 *
	 * @param roleID
	 * @param userId
	 * @param originLogin login of admin that generate this request
	 * @throws NameNotFoundException
	 * @throws DataServiceException 
	 */
	void addUser(String  roleID, String userId, final String originLogin) throws DataServiceException, NameNotFoundException;


	void addUsers(String cn, List<String> addList, final String originLogin) throws DataServiceException, NameNotFoundException;

	/**
	 * Returns all roles. Each roles will contains its list of users.
	 * 
	 * @return list of {@link Group}
	 */
	List<Group> findAll() throws DataServiceException;

	/**
	 * Returns all roles for a given uid.
	 *
	 * @return list of {@link Group}
	 */
	List<Group> findAllForUser(String userId) throws DataServiceException;

	/**
	 * Returns the role's users
	 * 
	 * @return list of user uid
	 */
	List<String> findUsers(final String roleName) throws DataServiceException;

	/**
	 * Deletes the user from all roles 
	 *
	 * @param uid
	 * @param originLogin login of admin that generate this request
	 * @throws DataServiceException
	 */
	void deleteUser(String uid, final String originLogin) throws DataServiceException;

	void deleteUsers(String cn, List<String> deleteList, String originLogin) throws DataServiceException, NameNotFoundException;

	/**
	 * Deletes the user from the role
	 * 
	 * @param roleName
	 * @param uid
	 * @param originLogin login of admin that generate this request
	 * @throws DataServiceException
	 */
	void deleteUser(String roleName, String uid, final String originLogin) throws DataServiceException;

	/**
	 * Modifies the user (e.g. rename) from the role
	 *
	 * @param roleName
	 * @param oldUid
	 * @param newUid
	 * @throws DataServiceException
	 */
	void modifyUser(String roleName, String oldUid, String newUid) throws DataServiceException;

	/**
	 * Adds the role
	 * 
	 * @param role
	 * 
	 * @throws DataServiceException 
	 * @throws DuplicatedCommonNameException if the role es present in the LDAP store
	 */
	void insert(Group role) throws DataServiceException, DuplicatedCommonNameException;

	/**
	 * Removes the role
	 * 
	 * @param commonName
	 * @throws DataServiceException
	 * @throws NameNotFoundException
	 */
	void delete(String commonName) throws DataServiceException,	NameNotFoundException;

	/**
	 * Search the role based on the common name (cn)
	 * @param commonName
	 * @return {@link Group}
	 * 
	 * @throws NameNotFoundException
	 */
	Group findByCommonName(String commonName) throws DataServiceException, NameNotFoundException;

	/**
	 * Modifies the roles fields in the store
	 * 
	 * @param roleName
	 * @param modified
	 * 
	 */
	void update(String roleName, Group modified) throws DataServiceException, NameNotFoundException, DuplicatedCommonNameException;

	void addUsersInGroups(List<String> putGroup, List<String> users, final String originLogin)  throws DataServiceException, NameNotFoundException;

	void deleteUsersInGroups(List<String> deleteGroup, List<String> users, final String originLogin) throws DataServiceException, NameNotFoundException;
	
}
