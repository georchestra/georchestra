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

import org.georchestra.console.dto.Account;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.filter.Filter;

import java.util.List;

/**
 * Defines the operations to maintain the set of account.
 * 
 * @author Mauricio Pazos
 * 
 */
public interface AccountDao {


	/**
	 * Checks if the uid exist.
	 * @param uid
	 * @return true if the uid exist, false in other case.
	 * @throws DataServiceException
	 */
	boolean exist(final String uid) throws DataServiceException;
	
	/**
	 * Returns all accounts
	 * 
	 * @return List of {@link Account}
	 * @throws DataServiceException
	 */
	List<Account> findAll() throws DataServiceException;
	
	/**
	 * Returns all accounts that accomplish the provided filter.
	 * 
	 * @param uidFilter
	 * @return List of {@link Account}
	 * @throws DataServiceException
	 */
	List<Account> findFilterBy(final ProtectedUserFilter uidFilter) throws DataServiceException;

	/**
	 * Creates a new account
	 * 
	 * @param account
	 * @param roleID
	 * @param originLogin login of admin that create user
	 * @throws DataServiceException
	 * @throws DuplicatedEmailException
	 */
	void insert(final Account account, final String roleID, final String originLogin) throws DataServiceException, DuplicatedUidException, DuplicatedEmailException;

	/**
	 * Updates the user account
	 * @param account
	 * @param originLogin login of admin that issue this modification
	 * @throws DataServiceException
	 * @throws DuplicatedEmailException
	 */
	void update(final Account account, String originLogin) throws DataServiceException, DuplicatedEmailException;

	/**
	 * Updates the user account, given the old and the new state of the account
	 * Needed if a DN update is required (modifying the uid).
	 *
	 * @param account
	 * @param modified
	 * @param originLogin login of admin that issue this modification
	 *
	 * @throws DuplicatedEmailException
	 * @throws DataServiceException
	 * @throws NameNotFoundException
	 */
	void update(Account account, Account modified, String originLogin) throws DataServiceException, DuplicatedEmailException, NameNotFoundException;

	/**
	 * Changes the user password
	 * 
	 * @param uid
	 * @param password
	 * @throws DataServiceException
	 */
	void changePassword(final String uid, final String password)throws DataServiceException;


	/**
	 * Deletes the account
	 * 
	 * @param uid
	 * @param originLogin login of admin that make request
	 * @throws DataServiceException
	 * @throws NameNotFoundException
	 */
	void delete(final String uid, final String originLogin) throws DataServiceException, NameNotFoundException;

	/**
	 * Returns the account that contains the uid provided as parameter.
	 * 
	 * @param uid
	 * 
	 * @return {@link Account}
	 * 
	 * @throws DataServiceException
	 * @throws NameNotFoundException
	 */
	Account findByUID(final String uid)throws DataServiceException, NameNotFoundException;

	/**
	 * Returns the account that contains the email provided as parameter.
	 * 
	 * @param email
	 * @return {@link Account}
	 * 
	 * @throws DataServiceException
	 * @throws NameNotFoundException
	 */
	Account findByEmail(final String email) throws DataServiceException, NameNotFoundException;

	/**
	 * Returns a list of account that have specified role.
	 *
	 * @param role
	 * @return List of {@link Account}
	 *
	 * @throws DataServiceException
	 * @throws NameNotFoundException
	 */
	List<Account> findByRole(final String role) throws DataServiceException, NameNotFoundException;

	/**
	 * Add the new password. This method is part of the "lost password" workflow to maintan the old password and the new password until the
	 * user can confirm that he had asked for a new password.   
	 * 
	 * @param uid
	 * @param newPassword
	 */
	void addNewPassword(String uid, String newPassword);

	/**
	 * Generates a new Id based on the uid provided as parameter.
	 * 
	 * @param uid
	 *  
	 * @return a new uid
	 * 
	 * @throws DataServiceException
	 */
	String generateUid(String uid) throws DataServiceException;

	/**
	 * users in LDAP directory with shadowExpire field filled
	 *
	 * @return List of Account that have a shadowExpire attribute
	 */

	List<Account> findByShadowExpire();

	/**
	 * Finds all accounts given a list of blacklisted users and a LDAP filter
	 *
	 * @return List of Account that are not in the ProtectedUserFilter, and which
	 * complies with the provided LDAP filter.
	 */
	List<Account> find(final ProtectedUserFilter uidFilter, Filter f);

}
