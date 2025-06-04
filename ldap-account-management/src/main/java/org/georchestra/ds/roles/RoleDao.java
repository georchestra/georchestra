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

package org.georchestra.ds.roles;

import java.util.List;

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.DuplicatedCommonNameException;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.users.Account;
import org.springframework.ldap.NameNotFoundException;

/**
 * @author Mauricio Pazos
 *
 */
public interface RoleDao {

    /**
     * adds the user to the role
     */
    void addUser(String roleName, Account user) throws DataServiceException, NameNotFoundException;

    void addOrg(String roleName, Org org) throws DataServiceException, NameNotFoundException;

    /**
     * Returns all roles. Each roles will contains its list of users.
     *
     * @return list of {@link Role}
     */
    List<Role> findAll() throws DataServiceException;

    List<Role> findAllForUser(Account account) throws DataServiceException;

    List<Role> findAllForOrg(Org org) throws DataServiceException;

    void deleteUser(Account account) throws DataServiceException;

    /**
     * Deletes the user from the role
     */
    void deleteUser(String roleName, Account account) throws DataServiceException;

    void deleteOrg(String roleName, Org org) throws DataServiceException;

    void modifyUser(Account oldAccount, Account newAccount) throws DataServiceException;

    /**
     * Adds the role
     *
     * @param role
     *
     * @throws DataServiceException
     * @throws DuplicatedCommonNameException if the role is present in the LDAP
     *                                       store
     */
    void insert(Role role) throws DataServiceException, DuplicatedCommonNameException;

    /**
     * Removes the role
     */
    void delete(String commonName) throws DataServiceException, NameNotFoundException;

    Role findByCommonName(String commonName) throws DataServiceException, NameNotFoundException;

    /**
     * Modifies the roles fields in the store
     */
    void update(String roleName, Role modified)
            throws DataServiceException, NameNotFoundException, DuplicatedCommonNameException;

    void addUsersInRoles(List<String> putRole, List<Account> users) throws DataServiceException, NameNotFoundException;

    void deleteUsersInRoles(List<String> deleteRole, List<Account> users)
            throws DataServiceException, NameNotFoundException;

    void addOrgsInRoles(List<String> putRole, List<Org> orgs) throws DataServiceException, NameNotFoundException;

    void deleteOrgsInRoles(List<String> deleteRole, List<Org> orgs) throws DataServiceException, NameNotFoundException;

}
