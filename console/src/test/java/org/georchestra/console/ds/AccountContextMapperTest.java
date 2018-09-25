package org.georchestra.console.ds;


import org.georchestra.console.dto.Account;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.support.LdapNameBuilder;

import java.util.SortedSet;
import java.util.TreeSet;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.eq;

public class AccountContextMapperTest {

    @Test
    public void deserializeUserWithPendingOrg() {
        AccountDaoImpl.AccountContextMapper toTest = new AccountDaoImpl.AccountContextMapper(
                LdapNameBuilder.newInstance("ou=pendingusers").build(),
                "ou=orgs,dc=georchestra,dc=org",
                "ou=pendingorgs,dc=georchestra,dc=org");
        DirContextAdapter mockDirContextAdapter = Mockito.mock(DirContextAdapter.class);
        SortedSet<String> roles= new TreeSet<String>();
        roles.add("cn=momorg,ou=pendingorgs,dc=georchestra,dc=org");
        Mockito.when(mockDirContextAdapter.getAttributeSortedStringSet(eq("memberOf"))).thenReturn(roles);
        Mockito.when(mockDirContextAdapter.getDn()).thenReturn( LdapNameBuilder.newInstance("uid=momo,ou=pendingusers").build());

        Account account = (Account) toTest.mapFromContext(mockDirContextAdapter);

        assertEquals("momorg", account.getOrg());
        assertEquals(true, account.isPending());
    }
}
