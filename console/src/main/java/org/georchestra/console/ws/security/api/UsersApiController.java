/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.console.ws.security.api;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.ds.ProtectedUserFilter;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.ws.backoffice.users.UserRule;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/internal", produces = "application/json; charset=utf-8")
public class UsersApiController {

    private @Autowired AccountDao accountsDao;
    private @Autowired OrgsDao orgsDao;
    private @Autowired UserRule userRule;
    private @Autowired OrganizationMapper orgMapper;

    /**
     * Return a list of available users as a json array
     */
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<GeorchestraUser> findAllUsers() {
        final UserMapper mapper = newUserMapper();
        Stream<Account> accounts = getAccounts().stream()
                .filter(u -> !u.isPending() && StringUtils.hasLength(u.getOrg()));
        return accounts.map(mapper::map).collect(Collectors.toList());
    }

    /**
     * Return a list of available users as a json array
     */
    @GetMapping(value = "/organizations", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<Organization> findAllOrganizations() {
        Stream<Org> orgs = getOrgs().filter(o -> !o.isPending());
        return orgs.map(orgMapper::map).collect(Collectors.toList());
    }

    private Stream<Org> getOrgs() {
        return orgsDao.findAllWithExt();
    }

    private List<Account> getAccounts() {
        List<Account> accounts;
        try {
            ProtectedUserFilter filter = new ProtectedUserFilter(this.userRule.getListUidProtected());
            accounts = this.accountsDao.findFilterBy(filter);
        } catch (DataServiceException e) {
            throw new RuntimeException(e);
        }
        return accounts;
    }

    /**
     * @return a new instanceof {@link UserMapper}, since it's a bean with prototype
     *         scope
     */
    @Lookup
    UserMapper newUserMapper() {
        return null;
    }
}