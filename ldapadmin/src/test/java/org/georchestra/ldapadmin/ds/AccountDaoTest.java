package org.georchestra.ldapadmin.ds;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.HashMap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.ldap.LdapName;

import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.AccountFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.AuthenticationSource;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;


public class AccountDaoTest {

    private AccountDao us;
    private GroupDaoImpl groupDao;
    private LdapContextSource contextSource;
    private Account adminAccount;

    @Before
    public void setUp() throws Exception {
        assumeTrue(System.getProperty("ldapadmin.test.openldap.ldapurl") != null
                && System.getProperty("ldapadmin.test.openldap.basedn") != null
                && System.getProperty("ldapadmin.test.openldap.binddn") != null
                && System.getProperty("ldapadmin.test.openldap.password") != null);

        String ldapUrl = System.getProperty("ldapadmin.test.openldap.ldapurl");
        String baseDn = System.getProperty("ldapadmin.test.openldap.basedn");
        String ldapAdminDn = System.getProperty("ldapadmin.test.openldap.binddn");
        String ldapAdminDnPw = System.getProperty("ldapadmin.test.openldap.password");

        contextSource = new LdapContextSource();
        contextSource.setBase(baseDn);
        contextSource.setUrl(ldapUrl);
        contextSource.setBaseEnvironmentProperties(new HashMap<String, Object>());
        contextSource.setUserDn(ldapAdminDn);
        contextSource.setPassword(ldapAdminDnPw);
        contextSource.setAnonymousReadOnly(true);
        contextSource.setCacheEnvironmentProperties(false);
        AuthenticationSource authsrc =  Mockito.mock(AuthenticationSource.class);
        Mockito.when(authsrc.getPrincipal()).thenReturn(ldapAdminDn);
        Mockito.when(authsrc.getCredentials()).thenReturn(ldapAdminDnPw);
        contextSource.setAuthenticationSource(authsrc);

        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        groupDao = new GroupDaoImpl();
        groupDao.setLdapTemplate(ldapTemplate);
        groupDao.setGroupSearchBaseDN("ou=roles");
        groupDao.setUniqueNumberField("ou");
        groupDao.setUserSearchBaseDN("ou=users");

        OrgsDao orgsDao = new OrgsDao();
        orgsDao.setLdapTemplate(ldapTemplate);
        orgsDao.setOrgsSearchBaseDN("ou=orgs");
        orgsDao.setUserSearchBaseDN("ou=users");


        us = new AccountDaoImpl(ldapTemplate, groupDao, orgsDao);
        ((AccountDaoImpl) us).setUserSearchBaseDN("ou=users");

        this.adminAccount = AccountFactory.createBrief("testadmin", "monkey123", "Test", "ADmin",
                "postmastrer@localhost", "+33123456789", "admin", "");
    }

    @Test
    public void testBlankFields_issues_1086_1096() throws Exception {
        Account testadminAc  = us.findByUID("testadmin");
        us.update(testadminAc, this.adminAccount.getUid());
        
        Attributes attrs = contextSource.getReadWriteContext().getAttributes(new LdapName("uid=testadmin,ou=users"));
            
        boolean hasStillUserPassword = attrs.get("userPassword") != null;

        us.update(testadminAc, "testadmin");

        assertTrue("No userPassword found for testadmin, expected one", hasStillUserPassword);
    }

    @Test
    public void testUpdateAcountAccount() throws Exception {
        Account testadminAc  = us.findByUID("testadmin");
        String oldUid = testadminAc.getUid();

        Account newTestAdminAc = AccountFactory.create(testadminAc);
        assertTrue(newTestAdminAc.getUid().equals(testadminAc.getUid()));

        newTestAdminAc.setUid("testadminblah");

        us.update(testadminAc, newTestAdminAc, this.adminAccount.getUid());

        Attributes attrs = contextSource.getReadWriteContext().getAttributes(new LdapName("uid=testadminblah,ou=users"));
        Object o = attrs.get("uid");
        boolean correctlyrenamed = ((BasicAttribute) o).get(0).toString().equals("testadminblah");
        boolean encounteredNamingEx = false;
        try {
            Attributes oldAttrs = contextSource.getReadWriteContext().getAttributes(new LdapName("uid=testadmin,ou=users"));
        } catch (NamingException e) {
            encounteredNamingEx = true;
        }


        // restoring testadmin in its initial state
        us.update(newTestAdminAc, testadminAc, this.adminAccount.getUid());

        assertTrue("Was able to find testadmin back (found some attributes), none expected", encounteredNamingEx);
        assertTrue("Wrong uid encountered (found " + o.toString() + " instead of testadminblah", correctlyrenamed);

    }
 
}
