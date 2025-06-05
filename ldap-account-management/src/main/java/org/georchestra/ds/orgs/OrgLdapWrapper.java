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

package org.georchestra.ds.orgs;

import org.georchestra.ds.users.AccountDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class OrgLdapWrapper extends LdapWrapper<Org> {

    private AccountDaoImpl accountDao;

    @Autowired
    public void setAccountDao(AccountDaoImpl accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public void mapPayloadToContext(Org org, DirContextOperations context) {
        String seeAlsoValueExt = LdapNameBuilder
                .newInstance((org.isPending() ? props.getPendingOrgSearchBaseDN() : props.getOrgSearchBaseDN()) + ","
                        + props.getBasePath())
                .add("o", org.getId()).build().toString();

        context.setAttributeValue("seeAlso", seeAlsoValueExt);

        // Mandatory attribute
        context.setAttributeValue("o", org.getName());

        if (org.getMembers() != null) {
            context.setAttributeValues("member", //
                    org.getMembers().stream() //
                            .map(accountDao::findByUID) //
                            .filter(Objects::nonNull) //
                            .map(account -> accountDao.buildFullUserDn(account)) //
                            .collect(Collectors.toList()).toArray(new String[] {}));
        }

        // Optional ones
        if (org.getShortName() != null)
            context.setAttributeValue("ou", org.getShortName());

        if (org.getCities() != null) {
            StringBuilder buffer = new StringBuilder();
            List<String> descriptions = new ArrayList<>();
            int maxFieldSize = 1000;

            // special case where cities is empty
            if (org.getCities().size() == 0) {
                Object[] values = context.getObjectAttributes("description");
                if (values != null) {
                    Arrays.asList(values).stream().forEach(v -> context.removeAttributeValue("description", v));
                }
            } else {
                for (String city : org.getCities()) {
                    if (buffer.length() > maxFieldSize) {
                        descriptions.add(buffer.substring(1));
                        buffer = new StringBuilder();
                    }
                    buffer.append("," + city);
                }
            }
            if (buffer.length() > 0)
                descriptions.add(buffer.substring(1));

            if (descriptions.size() > 0)
                context.setAttributeValues("description", descriptions.toArray());
        }
    }

    @Override
    protected String getLdapKeyField() {
        return "cn";
    }

    @Override
    protected String[] getObjectClass() {
        return new String[] { "top", "groupOfMembers" };
    }

    @Override
    public AttributesMapper<Org> getAttributeMapper(boolean pending) {
        return attrs -> {
            Org org = new Org();
            org.setId(asStringStream(attrs, "cn").collect(joining(",")));
            org.setName(asStringStream(attrs, "o").collect(joining(",")));
            org.setShortName(asStringStream(attrs, "ou").collect(joining(",")));
            org.setCities(asStringStream(attrs, "description").flatMap(Pattern.compile(",")::splitAsStream)
                    .collect(Collectors.toList()));
            org.setMembers(asStringStream(attrs, "member").map(LdapNameBuilder::newInstance).map(LdapNameBuilder::build)
                    .map(name -> name.getRdn(name.size() - 1).getValue().toString()).collect(Collectors.toList()));
            org.setPending(pending);
            return org;
        };
    }
}