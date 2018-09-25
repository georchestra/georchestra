package org.georchestra.console.ds;

import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountImpl;
import org.georchestra.console.dto.Org;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrgsDaoTest {

    @Test
    public void addUserToPendingOrg() throws NamingException {
        LdapTemplate mockLdapTemplate = mock(LdapTemplate.class);
        OrgsDao toTest = createToTest(mockLdapTemplate);

        Account account = new AccountImpl();
        account.setPending(false);
        account.setUid("iamnotpending");

        Org targetOrg = new Org();
        targetOrg.setId("csc");
        targetOrg.setPending(true);

        toTest.addUser(targetOrg, account);

        ArgumentCaptor<DirContextOperations> contextCaptor = ArgumentCaptor.forClass(DirContextOperations.class);
        Mockito.verify(mockLdapTemplate).modifyAttributes(contextCaptor.capture());
        assertEquals("uid=iamnotpending,ou=users,dc=georchestra,dc=org", contextCaptor.getValue().getAttributes().get("member").get().toString());
        ArgumentCaptor<Name> nameCaptor = ArgumentCaptor.forClass(Name.class);
        Mockito.verify(mockLdapTemplate).lookupContext(nameCaptor.capture());
        assertEquals("cn=csc,ou=pendingorgs", nameCaptor.getValue().toString());
    }


    @Test
    public void addingPendingUserToNotPendingOrg() throws NamingException {
        LdapTemplate mockLdapTemplate = mock(LdapTemplate.class);
        OrgsDao toTest = createToTest(mockLdapTemplate);

        Account account = new AccountImpl();
        account.setPending(true);
        account.setUid("iampending");

        Org targetOrg = new Org();
        targetOrg.setId("csc");
        targetOrg.setPending(false);

        toTest.addUser(targetOrg, account);

        ArgumentCaptor<DirContextOperations> contextCaptor = ArgumentCaptor.forClass(DirContextOperations.class);
        Mockito.verify(mockLdapTemplate).modifyAttributes(contextCaptor.capture());
        assertEquals("uid=iampending,ou=pendingusers,dc=georchestra,dc=org", contextCaptor.getValue().getAttributes().get("member").get().toString());
        ArgumentCaptor<Name> nameCaptor = ArgumentCaptor.forClass(Name.class);
        Mockito.verify(mockLdapTemplate).lookupContext(nameCaptor.capture());
        assertEquals("cn=csc,ou=orgs", nameCaptor.getValue().toString());
    }

    @Test
    public void findPendingOrNotPendingOrgs() throws NamingException {
        LdapTemplate mockLdapTemplate = mock(LdapTemplate.class);
        OrgsDao toTest = createToTest(mockLdapTemplate);
        Org pendingOrg = mock(Org.class);
        Org validOrg = mock(Org.class);

        mockOrgSearchResultDependingOnDn(mockLdapTemplate, pendingOrg, "ou=pendingorgs");
        mockOrgSearchResultDependingOnDn(mockLdapTemplate, validOrg, "ou=orgs");

        List<Org> orgs = toTest.findAll();

        assertEquals(2, orgs.size());
        assertTrue(orgs.contains(validOrg));
        assertTrue(orgs.contains(pendingOrg));

        ArgumentCaptor<AttributesMapper> attributesMapperCaptor = ArgumentCaptor.forClass(AttributesMapper.class);
        verify(mockLdapTemplate).search(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object o) {
                return o.toString().startsWith("ou=pendingorgs");
            }
        }), anyString(), attributesMapperCaptor.capture());

        Attributes attributes = mock(Attributes.class);
        Org deserializedOrg = (Org) attributesMapperCaptor.getValue().mapFromAttributes(attributes);
        assertTrue(deserializedOrg.isPending());
    }

    @Test
    public void findByCommonNameNotPendingOrg() {
        LdapTemplate mockLdapTemplate = mock(LdapTemplate.class);
        OrgsDao toTest = createToTest(mockLdapTemplate);
        Org validOrg = mock(Org.class);
        mockOrgLookupResultDependingOnDn(mockLdapTemplate, validOrg, "cn=activeorg,ou=orgs");
        mockOrgLookupResultDependingOnDn(mockLdapTemplate, null, "cn=pending,ou=pendingorgs");

        Org org = toTest.findByCommonName("activeorg");

        assertEquals(validOrg, org);
    }

    @Test
    public void findByCommonNamePendingOrg() throws NamingException {
        LdapTemplate mockLdapTemplate = mock(LdapTemplate.class);
        OrgsDao toTest = createToTest(mockLdapTemplate);
        Org pendingOrg = mock(Org.class);
        mockOrgLookupResultDependingOnDn(mockLdapTemplate, null, "cn=pending,ou=orgs");
        mockOrgLookupResultDependingOnDn(mockLdapTemplate, pendingOrg, "cn=pending,ou=pendingorgs");

        Org org = toTest.findByCommonName("pending");

        assertEquals(pendingOrg, org);

        ArgumentCaptor<ContextMapper> contextMapperCaptor = ArgumentCaptor.forClass(ContextMapper.class);
        verify(mockLdapTemplate).lookup(argThat(new ArgumentMatcher<Name>() {
            @Override
            public boolean matches(Object o) {
                return o.toString().equals("cn=pending,ou=pendingorgs");
            }
        }), contextMapperCaptor.capture());

        DirContextAdapter mockDirContext = mock(DirContextAdapter.class);
        Attributes attributes = mock(Attributes.class);
        when(mockDirContext.getAttributes()).thenReturn(attributes);
        when(mockDirContext.getDn()).thenReturn(new LdapName("cn=pending,ou=pendingorgs"));
        Org deserializedOrg = (Org) contextMapperCaptor.getValue().mapFromContext(mockDirContext);
        assertTrue(deserializedOrg.isPending());
    }

    @Test
    public void findPendingOrgForUser() throws NamingException, DataServiceException {
        LdapTemplate mockLdapTemplate = mock(LdapTemplate.class);
        OrgsDao toTest = createToTest(mockLdapTemplate);
        Org pendingOrg = mock(Org.class);
        mockOrgSearchResultDependingOnDn(mockLdapTemplate, pendingOrg, "ou=pendingorgs");
        mockOrgSearchResultDependingOnDn(mockLdapTemplate, null, "ou=orgs");
        AccountImpl userAccount = new AccountImpl();
        userAccount.setUid("dummy");

        Org org = toTest.findForUser(userAccount);

        assertEquals(org, pendingOrg);

        ArgumentCaptor<AttributesMapper> attributesMapperCaptor = ArgumentCaptor.forClass(AttributesMapper.class);
        verify(mockLdapTemplate).search(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object o) {
                return o.toString().startsWith("ou=pendingorgs");
            }
        }), anyString(), attributesMapperCaptor.capture());

        Attributes attributes = mock(Attributes.class);
        Org deserializedOrg = (Org) attributesMapperCaptor.getValue().mapFromAttributes(attributes);
        assertTrue(deserializedOrg.isPending());
    }

    @Test
    public void testUpdate() throws NamingException {
        LdapTemplate mockLdapTemplate = mock(LdapTemplate.class);
        OrgsDao toTest = createToTest(mockLdapTemplate);

        Account mockAccount = mock(Account.class);
        when(mockAccount.getUid()).thenReturn("momo");
        when(mockLdapTemplate.lookup(any(LdapName.class), any(String[].class), any(ContextMapper.class))).thenReturn(mockAccount);
        Org validOrg = mock(Org.class);
        List<String> members = Collections.singletonList("momo");

        when(validOrg.getMembers()).thenReturn(members);
        when(validOrg.getId()).thenReturn("momorg");
        DirContextAdapter mockDirContextAdapter = mock(DirContextAdapter.class);
        when(validOrg.getReference()).thenReturn(mockDirContextAdapter);
        when(mockDirContextAdapter.getDn()).thenReturn(LdapNameBuilder.newInstance("cn=momorg,ou=pendingorgs").build());

        toTest.update(validOrg);

        ArgumentCaptor<DirContextOperations> attributesCaptor = ArgumentCaptor.forClass(DirContextOperations.class);
        verify(mockLdapTemplate).modifyAttributes(attributesCaptor.capture());
        assertEquals("uid=momo,ou=users,dc=georchestra,dc=org", attributesCaptor.getValue().getAttributes().get("member").get(0));
    }

    private void mockOrgLookupResultDependingOnDn(LdapTemplate mockLdapTemplate, Org returnedOrg, String searchDn) {
        if (returnedOrg != null) {
            when(mockLdapTemplate.lookup(argThat(new ArgumentMatcher<Name>() {

                @Override
                public boolean matches(Object o) {
                    if (o == null) {
                        return false;
                    }
                    return o.toString().startsWith(searchDn);
                }
            }), any(ContextMapper.class))).thenReturn(returnedOrg);
        }
        else {
            when(mockLdapTemplate.lookup(argThat(new ArgumentMatcher<Name>() {

                @Override
                public boolean matches(Object o) {
                    if (o == null) {
                        return false;
                    }
                    return o.toString().startsWith(searchDn);
                }
            }), any(ContextMapper.class))).thenThrow(org.springframework.ldap.NameNotFoundException.class);
        }
    }

    private void mockOrgSearchResultDependingOnDn(LdapTemplate mockLdapTemplate, Org returnedOrg, String searchDn) {
        if (returnedOrg != null) {
            when(mockLdapTemplate.search(argThat(new ArgumentMatcher<String>() {

                @Override
                public boolean matches(Object o) {
                    if (o == null) {
                        return false;
                    }
                    return o.toString().startsWith(searchDn);
                }
            }), anyString(), any(AttributesMapper.class))).thenReturn(Collections.singletonList(returnedOrg));
        }
        else {
            when(mockLdapTemplate.search(argThat(new ArgumentMatcher<String>() {

                @Override
                public boolean matches(Object o) {
                    if (o == null) {
                        return false;
                    }
                    return o.toString().startsWith(searchDn);
                }
            }), anyString(), any(AttributesMapper.class))).thenThrow(org.springframework.ldap.NameNotFoundException.class);
        }
    }

    private OrgsDao createToTest(LdapTemplate mockLdapTemplate) {
        when(mockLdapTemplate.lookupContext(any(Name.class))).thenReturn(new DirContextAdapter());

        OrgsDao toTest = new OrgsDao();
        toTest.setOrgSearchBaseDN("ou=orgs");
        toTest.setPendingOrgSearchBaseDN("ou=pendingorgs");
        toTest.setBasePath("dc=georchestra,dc=org");

        toTest.setLdapTemplate(mockLdapTemplate);
        AccountDao accountDao = new AccountDaoImpl(mockLdapTemplate);
        ((AccountDaoImpl)accountDao).setUserSearchBaseDN("ou=users");
        ((AccountDaoImpl)accountDao).setPendingUserSearchBaseDN("ou=pendingusers");
        ((AccountDaoImpl)accountDao).setOrgSearchBaseDN("ou=orgs");
        ((AccountDaoImpl)accountDao).setBasePath("dc=georchestra,dc=org");
        toTest.setAccountDao(accountDao);
        return toTest;
    }

}
