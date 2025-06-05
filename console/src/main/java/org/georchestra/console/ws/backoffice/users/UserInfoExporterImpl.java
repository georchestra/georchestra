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

package org.georchestra.console.ws.backoffice.users;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchResult;
import org.ldaptive.io.LdifWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.NameNotFoundException;

import ezvcard.VCard;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.FormattedName;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to export user account information in different formats (e.g. CSV,
 * V-CARD, LDIF).
 */
@Slf4j
public class UserInfoExporterImpl implements UserInfoExporter {

    private @Value("${ldapScheme}://${ldapHost}:${ldapPort}") String ldapUrl;
    private @Value("${ldapAdminDn:cn=admin,dc=georchestra,dc=org}") String ldapUserName;
    private @Value("${ldapAdminPassword:secret}") String ldapPassword;
    private @Value("${ldapUsersRdn:ou=users},${ldapBaseDn:dc=georchestra,dc=org}") String userSearchBaseDn;

    private AccountDao accountDao;
    private OrgsDao orgsDao;

    public @Autowired UserInfoExporterImpl(AccountDao accountDao, OrgsDao orgsDao) {
        this.accountDao = accountDao;
        this.orgsDao = orgsDao;
    }

    @Override
    public String exportAsLdif(@NonNull String user) throws NameNotFoundException, DataServiceException {
        Account a = accountDao.findByUID(user);
        return exportAsLdif(a);
    }

    @Override
    public String exportAsLdif(@NonNull Account account) {
        log.info("Exporting personal user data for account {}", account.getUid());
        ConnectionConfig connConfig = new ConnectionConfig(this.ldapUrl);
        // connConfig.setUseStartTLS("true".equals(this.getUseStartTLS()));
        connConfig.setConnectionInitializer(
                new BindConnectionInitializer(this.ldapUserName, new Credential(this.ldapPassword)));
        ConnectionFactory cf = new DefaultConnectionFactory(connConfig);
        SearchResult result;
        final String filter = String.format("(uid=%s)", account.getUid());
        try {
            SearchExecutor executor = new SearchExecutor();
            executor.setBaseDn(this.userSearchBaseDn);
            log.info("Running search '{}'", filter);
            result = executor.search(cf, filter).getResult();
            log.info("User query successful: {}", result);
        } catch (LdapException e) {
            log.error("Error running {}", filter, e);
            throw new IllegalStateException(e);
        }

        StringWriter writer = new StringWriter();
        try {
            new LdifWriter(writer).write(result);
        } catch (IOException e) {
            log.error("Error generating LDIF file", e);
            throw new IllegalStateException(e);
        }
        String ldifContents = writer.toString();
        log.info("Returning LDIF: {}", ldifContents);
        return ldifContents;
    }

    private @NonNull String toVcf(@NonNull Account account) {
        VCard v = new VCard();
        FormattedName f = new FormattedName(account.getGivenName() + " " + account.getSurname());
        v.addFormattedName(f);
        v.addEmail(account.getEmail(), EmailType.WORK);
        v.addTelephoneNumber(account.getPhone(), TelephoneType.WORK);
        v.addTitle(account.getTitle());
        Address a = new Address();
        a.setPostalCode(account.getPostalCode());
        a.setStreetAddress(account.getPostalAddress());
        a.setPoBox(account.getPostOfficeBox());
        a.setLocality(account.getLocality());
        v.addAddress(a);
        v.addTelephoneNumber(account.getMobile(), TelephoneType.CELL);

        return v.write();
    }

    @Override
    public @NonNull String exportUsersAsCsv(@NonNull String... userNames) throws DataServiceException {
        List<Account> accounts = new ArrayList<>();
        for (String name : userNames) {
            Account a = getAccount(name);
            if (a != null) {
                accounts.add(a);
            }
        }
        StringBuilder target = new StringBuilder();
        try {
            new CSVAccountExporter(orgsDao).export(accounts, target);
        } catch (IOException e) {
            throw new DataServiceException(e);
        }
        return target.toString();
    }

    private @Nullable Account getAccount(@NonNull String user) throws DataServiceException {
        try {
            Account a = accountDao.findByUID(user);
            return a;
        } catch (NameNotFoundException e) {
            log.error("User [{}] not found, skipping", user, e);
        }
        return null;
    }

    @Override
    public @NonNull String exportUsersAsVcard(@NonNull String... users) throws Exception {
        StringBuilder ret = new StringBuilder();
        for (String user : users) {
            Account a = getAccount(user);
            if (a != null) {
                ret.append(toVcf(a));
            }
        }

        return ret.toString();
    }
}