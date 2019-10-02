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

package org.georchestra.console.ws.backoffice.users;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.dto.Account;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class UsersExport {

    private AccountDao accountDao;

    @Autowired
    private AdvancedDelegationDao advancedDelegationDao;

    // Used for testing only
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    private static final Log LOG = LogFactory.getLog(UsersController.class.getName());

    @Autowired
    public UsersExport(AccountDao dao) {
        this.accountDao = dao;
    }

    @RequestMapping(value = "/private/users.csv", method = RequestMethod.POST, produces = "text/csv; charset=utf-8")
    @ResponseBody
    public String getUsersAsCsv(@RequestParam(value = "users") String rawUsers) throws Exception {
        String[] users = this.parseRequest(rawUsers);
        StringBuilder target = new StringBuilder();
        List<Account> accounts = new ArrayList<>();
        for (String user : users) {
            try {
                Account a = accountDao.findByUID(user);
                accounts.add(a);
            } catch (NameNotFoundException e) {
                LOG.error(String.format("User [%s] not found, skipping", user), e);
            }
        }
        new CSVAccountExporter().export(accounts, target);
        return target.toString();
    }

    @RequestMapping(value = "/private/users.vcf", method = RequestMethod.POST, produces = "text/x-vcard; charset=utf-8")
    @ResponseBody
    public String getUsersAsVcard(@RequestParam(value = "users") String rawUsers) throws Exception {
        String[] users = this.parseRequest(rawUsers);

        StringBuilder ret = new StringBuilder();
        for (String user : users) {
            try {
                Account a = accountDao.findByUID(user);
                ret.append(a.toVcf());
            } catch (NameNotFoundException e) {
                LOG.error(String.format("User [%s] not found, skipping", user), e);
            }
        }

        return ret.toString();
    }

    /**
     * Parse JSON string and check that connected user has permissions to view data
     * on requested users
     *
     * @param rawUsers JSON string to parse
     * @return Parsed user list
     * @throws AccessDeniedException if current user does not have permissions to
     *                               view data of all requested users
     */
    private String[] parseRequest(String rawUsers) throws JSONException {
        JSONArray jsonUsers = new JSONArray(rawUsers);
        List<String> users = new ArrayList<>();
        for (int i = 0; i < jsonUsers.length(); i++)
            users.add(jsonUsers.getString(i));

        // check if user is under delegation for delegated admins
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getAuthorities().contains(this.advancedDelegationDao.ROLE_SUPERUSER))
            if (!this.advancedDelegationDao.findUsersUnderDelegation(auth.getName()).containsAll(users))
                throw new AccessDeniedException("Some user not under delegation");
        return users.toArray(new String[users.size()]);
    }

}
