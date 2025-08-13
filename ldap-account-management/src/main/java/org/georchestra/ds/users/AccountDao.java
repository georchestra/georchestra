/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.ds.users;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.georchestra.ds.DataServiceException;
import org.springframework.ldap.NameNotFoundException;

import lombok.NonNull;

/**
 * Defines the operations to maintain the set of account.
 *
 * @author Mauricio Pazos
 *
 */
public interface AccountDao {

    /**
     * Checks if the uid exist.
     *
     * @param uid
     * @return true if the uid exist, false in other case.
     * @throws DataServiceException
     */
    boolean exists(final String uid) throws DataServiceException;

    /**
     * users in LDAP directory with shadowExpire field filled
     *
     * @return List of Account that have a shadowExpire attribute
     */

    List<Account> findByShadowExpire();

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
     * Returns the account that contains the External provider provided as parameter.
     *
     * @param externalProvider
     * @param externalUid
     * @return {@link Account}
     *
     * @throws DataServiceException
     * @throws NameNotFoundException
     */
    Account findByExternalUid(final String externalProvider, final String externalUid)
            throws DataServiceException, NameNotFoundException;

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
     * Returns all accounts that accomplish the provided filter.
     *
     * @param uidFilter
     * @return List of {@link Account}
     * @throws DataServiceException
     */
    List<Account> findFilterBy(final ProtectedUserFilter uidFilter) throws DataServiceException;

    List<Account> findAll(Predicate<Account> filter) throws DataServiceException;

    /**
     * Creates a new account
     *
     * @param account
     * @throws DataServiceException
     * @throws DuplicatedEmailException
     */
    void insert(@NonNull final Account account)
            throws DataServiceException, DuplicatedUidException, DuplicatedEmailException;

    /**
     * Updates the user account
     *
     * @param account
     * @throws DataServiceException
     * @throws DuplicatedEmailException
     */
    void update(final Account account) throws DataServiceException, DuplicatedEmailException;

    /**
     * Updates the user account, given the old and the new state of the account
     * Needed if a DN update is required (modifying the uid).
     *
     * @param account
     * @param modified
     *
     * @throws DuplicatedEmailException
     * @throws DataServiceException
     * @throws NameNotFoundException
     */
    void update(Account account, Account modified)
            throws DataServiceException, DuplicatedEmailException, NameNotFoundException;

    /**
     * Changes the user password
     *
     * @param uid
     * @param password
     * @throws DataServiceException
     */
    void changePassword(final String uid, final String password) throws DataServiceException;

    void delete(Account account) throws DataServiceException, NameNotFoundException;

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
    Account findByUID(final String uid) throws DataServiceException, NameNotFoundException;

    /**
     * Find an {@link Account} by {@link Account#getUniqueIdentifier()
     * uniqueIdentifier}
     *
     * @return the Account with {@link Account#getUniqueIdentifier()
     *         uniqueIdentifier == id}
     * @throws NameNotFoundException if no such account is found
     * @throws DataServiceException
     */
    Account findById(final UUID id) throws NameNotFoundException, DataServiceException;

    /**
     * Generates a new Id based on the uid provided as parameter.
     *
     * @param uid
     *
     * @return a new uid
     *
     */
    String generateUid(String uid);

    boolean hasUserDnChanged(Account account, Account modified);

    boolean hasUserLoginChanged(Account account, Account modified);

}
