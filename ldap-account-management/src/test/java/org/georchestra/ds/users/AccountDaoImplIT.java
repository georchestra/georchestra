package org.georchestra.ds.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.ldap.LdapName;

import org.georchestra.testcontainers.ldap.GeorchestraLdapContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Lists;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "classpath:testApplicationContext.xml" })
public class AccountDaoImplIT {

    public static @ClassRule GeorchestraLdapContainer ldap = new GeorchestraLdapContainer().withLogToStdOut();

    private @Autowired AccountDaoImpl accountDao;
    private @Autowired LdapTemplate ldapTemplate;

    private Account account;

    @Before
    public void setup() throws Exception {
        account = AccountFactory.createBrief("userforittest", "monkey123", "userforrolestest", "userforrolestest123",
                "userforrolestest@localhost", "+33123456789", "UnknownPomPom", "");
        accountDao.insert(account);
    }

    @After
    public void cleanLdap() {
        accountDao.delete(account);
    }

    @Test
    public void testObjectClassContextMapper() throws Exception {
        DirContextOperations dco = ldapTemplate.lookupContext("uid=userforittest,ou=users");
        List<String> oc = Lists.newArrayList(dco.getStringAttributes("objectClass"));
        // Adding a random (but valid, we're dealing with real ldap server) objectClass
        oc.add("dcObject");
        dco.setAttributeValues("objectClass", oc.toArray());
        dco.setAttributeValue("dc", "RandomValue");
        ldapTemplate.modifyAttributes(dco);
        dco = ldapTemplate.lookupContext("uid=userforittest,ou=users");
        account.setDescription("This is a new desc");
        accountDao.update(account);
        assertTrue(Arrays.asList(dco.getStringAttributes("objectClass")).contains("dcObject"));
    }

    @Test
    public void testBlankFields_issues_1086_1096() throws Exception {
        Account testadminAc = accountDao.findByUID("testadmin");
        accountDao.update(testadminAc);

        ContextSource contextSource = ldapTemplate.getContextSource();
        Attributes attrs = contextSource.getReadWriteContext().getAttributes(new LdapName("uid=testadmin,ou=users"));

        boolean hasStillUserPassword = attrs.get("userPassword") != null;

        accountDao.update(testadminAc);

        assertTrue("No userPassword found for testadmin, expected one", hasStillUserPassword);
    }

    @Test
    public void testUpdateAcountAccount() throws Exception {
        Account testadminAc = accountDao.findByUID("testadmin");

        Account newTestAdminAc = AccountFactory.create(testadminAc);
        assertEquals(newTestAdminAc.getUid(), testadminAc.getUid());

        newTestAdminAc.setUid("testadminblah");

        accountDao.update(testadminAc, newTestAdminAc);

        ContextSource contextSource = ldapTemplate.getContextSource();
        Attributes attrs = contextSource.getReadWriteContext()
                .getAttributes(new LdapName("uid=testadminblah,ou=users"));
        Object o = attrs.get("uid");
        boolean correctlyrenamed = ((BasicAttribute) o).get(0).toString().equals("testadminblah");
        boolean encounteredNamingEx = false;
        try {
            Attributes oldAttrs = contextSource.getReadWriteContext()
                    .getAttributes(new LdapName("uid=testadmin,ou=users"));
        } catch (NamingException e) {
            encounteredNamingEx = true;
        }

        // restoring testadmin in its initial state
        accountDao.update(newTestAdminAc, testadminAc);

        assertTrue("Was able to find testadmin back (found some attributes), none expected", encounteredNamingEx);
        assertTrue("Wrong uid encountered (found " + o.toString() + " instead of testadminblah", correctlyrenamed);

    }

    @Test
    public void pendingUserExists() throws Exception {
        assertTrue(accountDao.exist("testpendinguser"));
    }

    @Test
    public void findPendingUser() throws Exception {
        Account testpending = accountDao.findByUID("testpendinguser");

        assertEquals("testpendinguser", testpending.getUid());
        assertTrue(testpending.isPending());
    }

    @Test
    public void getPasswordType() throws Exception {
        Account testpending = accountDao.findByUID("testadmin");
        assertEquals(PasswordType.SHA, testpending.getPasswordType());
    }

    @Test
    public void getPasswordTypePendingUser() throws Exception {
        Account testpending = accountDao.findByUID("testpendinguser");
        assertEquals(PasswordType.SHA, testpending.getPasswordType());
    }
}
