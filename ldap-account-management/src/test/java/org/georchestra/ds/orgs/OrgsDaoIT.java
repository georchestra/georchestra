package org.georchestra.ds.orgs;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.AccountImpl;
import org.georchestra.testcontainers.ldap.GeorchestraLdapContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Lists;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "classpath:testApplicationContext.xml" })
public class OrgsDaoIT {
    public static @ClassRule GeorchestraLdapContainer ldap = new GeorchestraLdapContainer().withLogToStdOut();

    private @Autowired OrgsDao dao;
    private @Autowired AccountDao accountDao;
    private @Autowired LdapTemplate ldapTemplate;

    Org org;
    OrgExt orgExt;

    Org pendingOrg;

    @Before
    public void setup() throws Exception {
        org = new Org();
        orgExt = new OrgExt();
        org.setId("torg");
        org.setName("testorg");
        org.setShortName("tEsTOrG");
        org.setOrgExt(orgExt);
        orgExt.setId(org.getId());
        org.setCities(Lists.newArrayList("Paris"));
        dao.insert(org);
        dao.insert(orgExt);

        pendingOrg = new Org();
        pendingOrg.setId("testPendingOrg");
        pendingOrg.setShortName("PO");
        pendingOrg.setName("Pending Org");
        pendingOrg.setPending(true);
        dao.insert(pendingOrg);
    }

    @After
    public void cleanLdap() {
        dao.delete(org);
        dao.delete(orgExt);
        dao.delete(pendingOrg);
    }

    @Test
    public void testObjectClassContextMapper() throws Exception {
        DirContextOperations dco = ldapTemplate.lookupContext("cn=torg,ou=orgs");
        List<String> oc = Lists.newArrayList(dco.getStringAttributes("objectClass"));
        // Adding a random (but valid, we're dealing with real ldap server) objectClass
        oc.add("dcObject");
        dco.setAttributeValues("objectClass", oc.toArray());
        dco.setAttributeValue("dc", "RandomValue");
        ldapTemplate.modifyAttributes(dco);
        dco = ldapTemplate.lookupContext("cn=torg,ou=orgs");

        org = dao.findByCommonName("torg");
        orgExt = dao.findExtById("torg");

        org.setOrgExt(orgExt);
        List<String> cities = Lists.newArrayList("Marseille");
        org.setCities(cities);
        dao.update(org);
        assertTrue(Arrays.asList(dco.getStringAttributes("objectClass")).contains("dcObject"));
    }

    @Test
    public void addUserToPendingOrg() throws Exception {
        Account account = new AccountImpl();
        account.setOrg(pendingOrg.getId());

        account.setPending(false);
        account.setUid("iamnotpending");
        account.setGivenName("John");
        account.setSurname("Doe");
        account.setEmail("john.doe@test.com");
        account.setCommonName("John Doe");

        accountDao.insert(account);

        dao.linkUser(account);

        Org org = dao.findByCommonName(pendingOrg.getId());
        assertEquals(Collections.singletonList(account.getUid()), org.getMembers());

        DirContextOperations dco = ldapTemplate.lookupContext(format("cn=%s,ou=pendingorgs", org.getId()));
        Attributes atts = dco.getAttributes();
        Attribute member = atts.get("member");
        assertEquals("uid=iamnotpending,ou=users,dc=georchestra,dc=org", member.get());
    }

    @Test
    public void addPendingUserToNotPendingOrg() throws Exception {
        Org notPendingOrg = this.org;
        assertFalse(notPendingOrg.isPending());

        Account pendingAccount = new AccountImpl();
        pendingAccount.setPending(true);
        pendingAccount.setUid("iampending");
        pendingAccount.setOrg(notPendingOrg.getId());
        pendingAccount.setGivenName("John");
        pendingAccount.setSurname("Doe");
        pendingAccount.setEmail("john.doe.pending@test.com");
        pendingAccount.setCommonName("John Doe");

        accountDao.insert(pendingAccount);

        dao.linkUser(pendingAccount);

        Org org = dao.findByCommonName(notPendingOrg.getId());
        assertEquals(Collections.singletonList(pendingAccount.getUid()), org.getMembers());

        DirContextOperations dco = ldapTemplate.lookupContext(format("cn=%s,ou=orgs", org.getId()));
        Attributes atts = dco.getAttributes();
        Attribute member = atts.get("member");
        assertEquals("uid=iampending,ou=pendingusers,dc=georchestra,dc=org", member.get());
    }

    @Test
    public void findPendingOrNotPendingOrgs() throws NamingException {
        assertFalse(this.org.isPending());
        assertTrue(this.pendingOrg.isPending());

        List<Org> orgs = dao.findAll();

        org.setOrgExt(null);// findAll does not return exts
        assertTrue(orgs.contains(org));
        assertTrue(orgs.contains(pendingOrg));
    }

    @Test
    public void findByCommonNameNotPendingOrg() {
        Org expected = this.org;
        expected.setOrgExt(null);
        Org found = dao.findByCommonName(expected.getId());
        assertEquals(expected, found);
    }

    @Test
    public void findByCommonNameWithExt() {
        Org expected = this.org;
        Org found = dao.findByCommonNameWithExt(expected.getId());
        assertEquals(org, found);
    }

    @Test
    public void findByCommonNamePendingOrg() throws NamingException {
        Org found = dao.findByCommonName(this.pendingOrg.getId());

        assertEquals(pendingOrg, found);
    }

    @Test
    public void testUpdate() throws Exception {
        Account account = new AccountImpl();
        account.setOrg(org.getId());
        account.setUid("momo");
        account.setGivenName("John");
        account.setSurname("Doe");
        account.setEmail("momo@test.com");
        account.setCommonName("John Doe");
        accountDao.insert(account);

        org = dao.findByCommonName(org.getId());
        org.getMembers().add(account.getUid());
        dao.update(org);

        Org updated = dao.findByCommonName(org.getId());
        assertEquals(org, updated);
        assertEquals(Collections.singletonList(account.getUid()), updated.getMembers());
    }

    @Test
    public void testOrgAttributeMapperCities() throws NamingException {
        Attributes orgToDeserialize = new BasicAttributes();
        orgToDeserialize.put("description", "1,2,3");
        AttributesMapper<Org> toTest = dao.getExtension(new Org()).getAttributeMapper(true);

        Org org = toTest.mapFromAttributes(orgToDeserialize);

        assertTrue("Expected 3 cities", org.getCities().size() == 3);
    }

    @Test
    public void testOrgAttributeMapperCitiesMultipleDescAttributes() throws NamingException {
        Attributes orgToDeserialize = new BasicAttributes();
        Attribute desc = new BasicAttribute("description");
        desc.add("1,2,3");
        desc.add("4,5,6");
        orgToDeserialize.put(desc);
        AttributesMapper<Org> toTest = dao.getExtension(new Org()).getAttributeMapper(true);

        Org org = toTest.mapFromAttributes(orgToDeserialize);
        assertTrue("Expected 6 cities", org.getCities().size() == 6);
    }
}
