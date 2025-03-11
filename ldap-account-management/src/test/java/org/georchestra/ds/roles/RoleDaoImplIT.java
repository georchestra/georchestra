package org.georchestra.ds.roles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.naming.Name;

import org.apache.commons.lang3.RandomStringUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.DuplicatedCommonNameException;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDaoImpl;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDaoImpl;
import org.georchestra.ds.users.AccountFactory;
import org.georchestra.ds.users.AccountImpl;
import org.georchestra.ds.users.DuplicatedEmailException;
import org.georchestra.ds.users.DuplicatedUidException;
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
public class RoleDaoImplIT {
    public static @ClassRule GeorchestraLdapContainer ldap = new GeorchestraLdapContainer().withLogToStdOut();

    private @Autowired AccountDaoImpl accountDao;
    private @Autowired RoleDaoImpl roleDao;
    private @Autowired OrgsDaoImpl orgsDao;
    private @Autowired LdapTemplate ldapTemplate;

    private Account account;
    private Role role;

    @Before
    public void setUp() throws Exception {
        account = AccountFactory.createBrief("userforrolestest", "monkey123", "userforrolestest", "userforrolestest123",
                "userforrolestest@localhost", "+33123456789", "UnknownPomPom", "");
        accountDao.insert(account);
        role = RoleFactory.create("TEST_ROLE", "sample role", false);
        roleDao.insert(role);
    }

    @After
    public void cleanLdap() throws Exception {
        accountDao.delete(account);
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
        List<String> oc = Lists.newArrayList(dco.getStringAttributes("objectClass"));
        oc.add("uidObject");
        dco.setAttributeValue("uid", "1234");
        dco.setAttributeValues("objectClass", oc.toArray());
        // Persisting everything
        ldapTemplate.modifyAttributes(dco);
        // create a new object. Otherwise it uses cache anc create a new
        dco = ldapTemplate.lookupContext(roleDn);
        role.setDescription("New Description");
        roleDao.update("TEST_ROLE", role);
        roleDao.addUser("TEST_ROLE", account);

        ArrayList<String> objectClasses = Arrays.stream(dco.getStringAttributes("objectClass"))
                .collect(Collectors.toCollection(ArrayList::new));
        assertTrue(objectClasses.contains("uidObject"));
        assertTrue(objectClasses.contains("top"));
        assertTrue(objectClasses.contains("groupOfMembers"));
    }

    @Test
    public void makeOneOrgMemberOfOneRole() throws DuplicatedCommonNameException, DataServiceException {
        String roleName = createRole();
        Org org = createOrg();

        roleDao.addOrg(roleName, org);

        Role actualRole = roleDao.findByCommonName(roleName);
        assertEquals(1, actualRole.getOrgList().size());
        assertEquals(0, actualRole.getUserList().size());
    }

    @Test
    public void deleteOneOrgMemberOfOneRole() throws DuplicatedCommonNameException, DataServiceException {
        String roleName = createRole();
        Org orgA = createOrg();
        Org orgB = createOrg();
        roleDao.addOrg(roleName, orgA);
        roleDao.addOrg(roleName, orgB);

        roleDao.deleteOrg(roleName, orgA);

        Role actualRole = roleDao.findByCommonName(roleName);
        assertTrue(actualRole.getOrgList().get(0).contains(orgB.getId()));
        assertEquals(1, actualRole.getOrgList().size());
    }

    @Test
    public void makeOrgsMembersOfRolesNominal() throws DuplicatedCommonNameException, DataServiceException {
        String roleName = createRole();
        Org org = createOrg();

        roleDao.addOrgsInRoles(Arrays.asList(roleName), Arrays.asList(org));

        Role actualRole = roleDao.findByCommonName(roleName);
        assertEquals(1, actualRole.getOrgList().size());
        assertEquals(0, actualRole.getUserList().size());
    }

    @Test
    public void deleteOrgsMembersFromRolesNominal() throws DuplicatedCommonNameException, DataServiceException {
        String roleName = createRole();
        Org org = createOrg();
        roleDao.addOrgsInRoles(Arrays.asList(roleName), Arrays.asList(org));

        roleDao.deleteOrgsInRoles(Arrays.asList(roleName), Arrays.asList(org));

        Role actualRole = roleDao.findByCommonName(roleName);
        assertEquals(0, actualRole.getOrgList().size());
    }

    @Test
    public void findAllForOrg() throws DuplicatedCommonNameException, DataServiceException {
        String roleNameA = createRole();
        String roleNameB = createRole();
        Org org = createOrg();
        roleDao.addOrg(roleNameA, org);
        roleDao.addOrg(roleNameB, org);

        List<String> roleNames = roleDao.findAllForOrg(org).stream().map(Role::getName).collect(Collectors.toList());

        assertThat(roleNames, containsInAnyOrder(roleNameA, roleNameB));
    }

    @Test
    public void findAll() throws DuplicatedCommonNameException, DataServiceException, DuplicatedUidException,
            DuplicatedEmailException {
        String roleNameA = createRole();
        String roleNameB = createRole();
        Org org = createOrg();
        Account account = createAccount();
        roleDao.addOrg(roleNameA, org);
        roleDao.addUser(roleNameA, account);
        roleDao.addUser(roleNameB, account);

        List<Role> role = roleDao.findAll();

        Role roleA = role.stream().filter(r -> r.getName().equals(roleNameA)).findFirst().get();
        Role roleB = role.stream().filter(r -> r.getName().equals(roleNameB)).findFirst().get();
        assertEquals(1, roleA.getOrgList().size());
        assertEquals(1, roleA.getUserList().size());
        assertEquals(0, roleB.getOrgList().size());
        assertEquals(1, roleB.getUserList().size());
    }

    private Org createOrg() {
        String orgName = "IT_ORG_" + RandomStringUtils.randomAlphabetic(8).toUpperCase();
        Org org = new Org();
        org.setId(orgName);
        orgsDao.insert(org);
        return org;
    }

    private Account createAccount() throws DataServiceException, DuplicatedUidException, DuplicatedEmailException {
        String accountName = "IT_ACCOUNT_" + RandomStringUtils.randomAlphabetic(8).toUpperCase();
        Account account = AccountFactory.createBrief(accountName, "123", "fname", "sname",
                String.format("%s@localhost", accountName), "+33123456789", "title", "");
        accountDao.insert(account);
        return account;
    }

    private String createRole() throws DataServiceException, DuplicatedCommonNameException {
        String roleName = "IT_ROLE_" + RandomStringUtils.randomAlphabetic(8).toUpperCase();
        Role role = RoleFactory.create(roleName, "sample role", false);
        roleDao.insert(role);
        return roleName;
    }
}
