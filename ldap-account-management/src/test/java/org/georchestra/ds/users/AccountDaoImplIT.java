package org.georchestra.ds.users;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.georchestra.testcontainers.ldap.GeorchestraLdapContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
        accountDao.insert(account, "test");
    }

    @After
    public void cleanLdap() {
        accountDao.delete(account, "test");
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
        accountDao.update(account, "test");
        assertTrue(Arrays.asList(dco.getStringAttributes("objectClass")).contains("dcObject"));
    }
}
