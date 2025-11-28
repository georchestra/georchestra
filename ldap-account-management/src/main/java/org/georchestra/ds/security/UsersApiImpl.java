/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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
package org.georchestra.ds.security;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.UserRule;
import org.georchestra.security.api.UsersApi;
import org.georchestra.security.model.GeorchestraUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.Setter;

@Service
public class UsersApiImpl implements UsersApi {

    private @Autowired @Setter UserRule userRule;
    private @Autowired @Setter AccountDao accountsDao;
    private @Autowired @Setter UserMapper mapper;

    @Override
    public List<GeorchestraUser> findAll() {
        try {
            return this.accountsDao.findAll(notPending().and(notProtected()))//
                    .stream()//
                    .map(mapper::map)//
                    .collect(Collectors.toList());
        } catch (DataServiceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<GeorchestraUser> findById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return Optional.of(//
                    this.accountsDao.findById(uuid))//
                    .filter(notPending().and(notProtected()))//
                    .map(mapper::map);
        } catch (NameNotFoundException e) {
            return Optional.empty();
        } catch (DataServiceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<GeorchestraUser> findByUsername(String username) {
        try {
            return Optional.of(this.accountsDao.findByUID(username))//
                    .filter(notPending().and(notProtected()))//
                    .map(mapper::map);
        } catch (NameNotFoundException e) {
            return Optional.empty();
        } catch (DataServiceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<GeorchestraUser> findByOAuth2Uid(String oauth2Provider, String oauth2Uid) {
        try {
            return Optional.of(this.accountsDao.findByOAuth2Uid(oauth2Provider, oauth2Uid))//
                    .filter(notPending().and(notProtected()))//
                    .map(mapper::map);
        } catch (NameNotFoundException e) {
            return Optional.empty();
        } catch (DataServiceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<GeorchestraUser> findByEmail(String email) {
        return findByEmail(email, true);
    }

    public Optional<GeorchestraUser> findByEmail(String email, boolean filterPending) {
        try {
            Predicate<Account> predicate = notProtected();
            if (filterPending) {
                predicate = predicate.and(notPending());
            }

            return Optional.ofNullable(accountsDao.findByEmail(email)).filter(predicate).map(mapper::map);
        } catch (NameNotFoundException e) {
            return Optional.empty();
        } catch (DataServiceException e) {
            throw new RuntimeException(e);
        }
    }

    private Predicate<Account> notProtected() {
        return this.userRule.isProtected().negate();
    }

    private Predicate<Account> pending() {
        return Account::isPending;
    }

    private Predicate<Account> notPending() {
        return pending().negate();
    }

}
