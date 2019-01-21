package org.georchestra.console.ds;

import org.georchestra.console.dao.AdminLogDao;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.ldap.LdapName;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;


public class AccountDaoTest {

    private AccountDao toTest;
    private RoleDaoImpl roleDao;
    private LdapContextSource contextSource;
    private Account adminAccount;

    @Before
    public void setUp() throws Exception {
        assumeTrue(System.getProperty("console.test.openldap.ldapurl") != null
                && System.getProperty("console.test.openldap.basedn") != null
                && System.getProperty("console.test.openldap.binddn") != null
                && System.getProperty("console.test.openldap.password") != null);

        String ldapUrl = System.getProperty("console.test.openldap.ldapurl");
        String baseDn = System.getProperty("console.test.openldap.basedn");
        String ldapAdminDn = System.getProperty("console.test.openldap.binddn");
        String ldapAdminDnPw = System.getProperty("console.test.openldap.password");

//        String ldapUrl = "ldap://127.0.0.1:389";
//        String baseDn = "dc=georchestra,dc=org";
//        String ldapAdminDn = "cn=admin,dc=georchestra,dc=org";
//        String ldapAdminDnPw = "";

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

        roleDao = new RoleDaoImpl();
        roleDao.setLdapTemplate(ldapTemplate);
        roleDao.setRoleSearchBaseDN("ou=roles");

        OrgsDao orgsDao = new OrgsDao();
        orgsDao.setLdapTemplate(ldapTemplate);
        orgsDao.setOrgSearchBaseDN("ou=orgs");


        toTest = new AccountDaoImpl(ldapTemplate);
        ((AccountDaoImpl) toTest).setUserSearchBaseDN("ou=users");
        ((AccountDaoImpl) toTest).setPendingUserSearchBaseDN("ou=pendingusers");
        ((AccountDaoImpl) toTest).setOrgSearchBaseDN("ou=orgs");
        ((AccountDaoImpl) toTest).setPendingOrgSearchBaseDN("ou=pendingorgs");
        ((AccountDaoImpl) toTest).setRoleSearchBaseDN("ou=roles");
        ((AccountDaoImpl) toTest).init();

        ((AccountDaoImpl) toTest).setLogDao(Mockito.mock(AdminLogDao.class));

        this.adminAccount = AccountFactory.createBrief("testadmin", "monkey123", "Test", "ADmin",
                "postmastrer@localhost", "+33123456789", "admin", "");
    }

    @Test
    public void testBlankFields_issues_1086_1096() throws Exception {
        Account testadminAc  = toTest.findByUID("testadmin");
        toTest.update(testadminAc, this.adminAccount.getUid());
        
        Attributes attrs = contextSource.getReadWriteContext().getAttributes(new LdapName("uid=testadmin,ou=users"));
            
        boolean hasStillUserPassword = attrs.get("userPassword") != null;

        toTest.update(testadminAc, "testadmin");

        assertTrue("No userPassword found for testadmin, expected one", hasStillUserPassword);
    }

    @Test
    public void testUpdateAcountAccount() throws Exception {
        Account testadminAc  = toTest.findByUID("testadmin");

        Account newTestAdminAc = AccountFactory.create(testadminAc);
        assertTrue(newTestAdminAc.getUid().equals(testadminAc.getUid()));

        newTestAdminAc.setUid("testadminblah");

        toTest.update(testadminAc, newTestAdminAc, this.adminAccount.getUid());

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
        toTest.update(newTestAdminAc, testadminAc, this.adminAccount.getUid());

        assertTrue("Was able to find testadmin back (found some attributes), none expected", encounteredNamingEx);
        assertTrue("Wrong uid encountered (found " + o.toString() + " instead of testadminblah", correctlyrenamed);

    }

    @Test
    public void pendingUserExists() throws Exception {
        assertTrue( toTest.exist("testpendinguser"));
    }

    @Test
    public void findPendingUser() throws Exception {
        Account testpending  = toTest.findByUID("testpendinguser");

        assertTrue("testpendinguser".equals(testpending.getUid()));
        assertTrue(testpending.isPending());
    }
}
