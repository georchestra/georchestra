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

import java.util.HashSet;
import java.util.Set;

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

    // Outlook csv header
    private final String OUTLOOK_CSV_HEADER = "First Name,Middle Name,Last Name,Title,Suffix,Initials,Web Page,Gender,Birthday,Anniversary,"
    		+ "Location,Language,Internet Free Busy,Notes,E-mail Address,E-mail 2 Address,E-mail 3 Address,Primary Phone,Home Phone,"
    		+ "Home Phone 2,Mobile Phone,Pager,Home Fax,Home Address,Home Street,Home Street 2,Home Street 3,Home Address PO Box,Home City,"
    		+ "Home State,Home Postal Code,Home Country,Spouse,Children,Manager's Name,Assistant's Name,Referred By,Company Main Phone,"
    		+ "Business Phone,Business Phone 2,Business Fax,Assistant's Phone,Company,Job Title,Department,Office Location,Organizational ID Number,"
    		+ "Profession,Account,Business Address,Business Street,Business Street 2,Business Street 3,Business Address PO Box,Business City,"
    		+ "Business State,Business Postal Code,Business Country,Other Phone,Other Fax,Other Address,Other Street,Other Street 2,Other Street 3,"
    		+ "Other Address PO Box,Other City,Other State,Other Postal Code,Other Country,Callback,Car Phone,ISDN,Radio Phone,TTY/TDD Phone,Telex,"
    		+ "User 1,User 2,User 3,User 4,Keywords,Mileage,Hobby,Billing Information,Directory Server,Sensitivity,Priority,Private,Categories\r\n";
    

    @Autowired
    public UsersExport(AccountDao dao) {
        this.accountDao = dao;
    }
    

    @RequestMapping(value = "/private/users.csv", method = RequestMethod.POST,
            produces = "text/csv; charset=utf-8")
    @ResponseBody
    public String getUsersAsCsv(@RequestParam(value="users") String rawUsers) throws Exception {
        Set<String> users = this.parseRequest(rawUsers);
        StringBuilder res = new StringBuilder();
        res.append(OUTLOOK_CSV_HEADER); // add csv outlook header
        
        for(String user: users){
            try {
                Account a = accountDao.findByUID(user);
                res.append(a.toCsv());
            } catch (NameNotFoundException e) {
                LOG.error(String.format("User [%s] not found, skipping", user), e);
            }
        }

        return res.toString();
    }

    @RequestMapping(value = "/private/users.vcf", method = RequestMethod.POST,
            produces = "text/x-vcard; charset=utf-8")
    @ResponseBody
    public String getUsersAsVcard(@RequestParam(value="users") String rawUsers) throws Exception {
        Set<String> users = this.parseRequest(rawUsers);

        StringBuilder ret = new StringBuilder();
        for (String user: users) {
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
     * Parse JSON string and check that connected user has permissions to view data on requested users
     *
     * @param rawUsers JSON string to parse
     * @return Parsed user list
     * @throws AccessDeniedException if current user does not have permissions to view data of all requested users
     */
    private Set<String> parseRequest(String rawUsers) throws JSONException {
        JSONArray jsonUsers = new JSONArray(rawUsers);
        Set<String> users = new HashSet<>();
        for(int i=0;i<jsonUsers.length();i++)
            users.add(jsonUsers.getString(i));

        // check if user is under delegation for delegated admins
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!auth.getAuthorities().contains(this.advancedDelegationDao.ROLE_SUPERUSER))
            if(!this.advancedDelegationDao.findUsersUnderDelegation(auth.getName()).containsAll(users))
                throw new AccessDeniedException("Some user not under delegation");
        return users;
    }

}
