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

package org.georchestra.ldapadmin.ws.backoffice.users;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.dto.Account;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UsersExport {

    private AccountDao accountDao;

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
    public String getUsersAsCsv(@RequestParam(value="users") String users) throws Exception {
        JSONArray jsonUsers = new JSONArray(users);
        StringBuilder ret1 = new StringBuilder();
        ret1.append(OUTLOOK_CSV_HEADER); // add csv outlook header
        
        for(int i = 0; i < jsonUsers.length(); ++ i){
            try {
                Account a = accountDao.findByUID(jsonUsers.getString(i));
                ret1.append(a.toCsv());
            } catch (NameNotFoundException e) {
                LOG.error(String.format("User [%s] not found, skipping", jsonUsers.getString(i)), e);
            }
        }

        return ret1.toString();
    }
    

    

    @RequestMapping(value = "/private/users.vcf", method = RequestMethod.POST,
            produces = "text/x-vcard; charset=utf-8")
    @ResponseBody
    public String getUsersAsVcard(@RequestParam(value="users") String users) throws Exception {
        JSONArray usersAr = new JSONArray(users);
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < usersAr.length(); ++ i) {
            try {
                Account a = accountDao.findByUID(usersAr.getString(i));
                ret.append(a.toVcf());
            } catch (NameNotFoundException e) {
                LOG.error(String.format("User [%s] not found, skipping", usersAr.getString(i)), e);
            }
        }

        return ret.toString();
        
    }

    
    
    
    

}
