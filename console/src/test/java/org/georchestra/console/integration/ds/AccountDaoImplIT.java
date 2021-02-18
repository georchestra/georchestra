package org.georchestra.console.integration.ds;

import org.georchestra.console.ds.AccountDaoImpl;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.DuplicatedEmailException;
import org.georchestra.console.ds.DuplicatedUidException;
import org.georchestra.console.ds.RoleDaoImpl;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountFactory;
import org.georchestra.console.integration.IntegrationTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class AccountDaoImplIT {
    public @Rule @Autowired IntegrationTestSupport support;
    @Autowired
    AccountDaoImpl accountDao;
    @Autowired
    RoleDaoImpl roleDao;
    @Autowired
    LdapTemplate ldapTemplate;

    Account account;

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
        ArrayList<String> oc = Arrays.stream(dco.getStringAttributes("objectClass"))
                .collect(Collectors.toCollection(ArrayList::new));
        // Adding a random (but valid, we're dealing with real ldap server) objectClass
        oc.add("dcObject");
        dco.setAttributeValues("objectClass", oc.toArray());
        dco.setAttributeValue("dc", "RandomValue");
        ldapTemplate.modifyAttributes(dco);
        dco = ldapTemplate.lookupContext("uid=userforittest,ou=users");
        account.setDescription("This is a new desc");
        accountDao.update(account, "test");
        assertTrue(Arrays.stream(dco.getStringAttributes("objectClass"))
                .collect(Collectors.toCollection(ArrayList::new)).contains("dcObject"));
    }
}
