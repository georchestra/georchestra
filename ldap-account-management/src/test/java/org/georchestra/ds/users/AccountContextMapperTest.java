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

package org.georchestra.ds.users;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.support.LdapNameBuilder;

public class AccountContextMapperTest {

    @Test
    public void deserializeUserWithPendingOrg() {
        AccountDaoImpl.AccountContextMapper toTest = new AccountDaoImpl.AccountContextMapper(
                LdapNameBuilder.newInstance("ou=pendingusers").build(), "ou=orgs,dc=georchestra,dc=org",
                "ou=pendingorgs,dc=georchestra,dc=org");
        DirContextAdapter mockDirContextAdapter = Mockito.mock(DirContextAdapter.class);
        SortedSet<String> roles = new TreeSet<>();
        roles.add("cn=momorg,ou=pendingorgs,dc=georchestra,dc=org");
        Mockito.when(mockDirContextAdapter.getAttributeSortedStringSet(eq("memberOf"))).thenReturn(roles);
        Mockito.when(mockDirContextAdapter.getDn())
                .thenReturn(LdapNameBuilder.newInstance("uid=momo,ou=pendingusers").build());

        Account account = (Account) toTest.mapFromContext(mockDirContextAdapter);

        assertEquals("momorg", account.getOrg());
        assertTrue(account.isPending());
        assertEquals(0, account.getSshKeys().length);
    }
}
