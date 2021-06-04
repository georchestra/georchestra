package org.georchestra.console.integration.ds;

import org.apache.log4j.Logger;
import org.georchestra.console.ds.AccountDaoImpl;
import org.georchestra.console.ds.RoleDaoImpl;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountFactory;
import org.georchestra.console.dto.Role;
import org.georchestra.console.dto.RoleFactory;
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

import javax.naming.Name;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class RoleDaoImplIT {
    private static Logger LOGGER = Logger.getLogger(org.georchestra.console.integration.RolesIT.class);
    private static DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    public @Rule @Autowired IntegrationTestSupport support;
    @Autowired
    AccountDaoImpl accountDao;
    @Autowired
    RoleDaoImpl roleDao;
    @Autowired
    LdapTemplate ldapTemplate;

    Account account;
    Role role;

    @Before
    public void setUp() throws Exception {
        account = AccountFactory.createBrief("userforrolestest", "monkey123", "userforrolestest", "userforrolestest123",
                "userforrolestest@localhost", "+33123456789", "UnknownPomPom", "");
        accountDao.insert(account, "test");
        role = RoleFactory.create("TEST_ROLE", "sample role", false);
        roleDao.insert(role);
    }

    @After
    public void cleanLdap() throws Exception {
        accountDao.delete(account, "test");
        roleDao.delete("TEST_ROLE");
    }

    /**
     * Georchestra use to keep only objectClass that _it_ needed. This test add a
     * new objectClass that is _not_ needed by georchestra and look if add/update
     * method destroys it.
     * 
     * @throws Exception should not raise exception.
     */
    @Test
    public void testObjectClassMapping() throws Exception {
        // Adding another objectClass to the role "TEST_ROLE", created in @Before
        Name roleDn = roleDao.buildRoleDn("TEST_ROLE");
        DirContextOperations dco = ldapTemplate.lookupContext(roleDn);
        List<String> oc = Arrays.asList(dco.getStringAttributes("objectClass"));
        oc.add("uidObject");
        dco.setAttributeValue("uid", "1234");
        dco.setAttributeValues("objectClass", oc.toArray());
        // Persisting everything
        ldapTemplate.modifyAttributes(dco);
        // create a new object. Otherwise it uses cache anc create a new
        dco = ldapTemplate.lookupContext(roleDn);
        role.setDescription("New Description");
        roleDao.update("TEST_ROLE", role);
        assertTrue(Arrays.stream(dco.getStringAttributes("objectClass"))
                .collect(Collectors.toCollection(ArrayList::new)).contains("uidObject"));
    }

}
