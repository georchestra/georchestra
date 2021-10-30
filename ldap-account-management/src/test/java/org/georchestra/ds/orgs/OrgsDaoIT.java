package org.georchestra.ds.orgs;

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
public class OrgsDaoIT {
    public static @ClassRule GeorchestraLdapContainer ldap = new GeorchestraLdapContainer().withLogToStdOut();

    private @Autowired OrgsDao orgDao;
    private @Autowired LdapTemplate ldapTemplate;

    Org org;
    OrgExt orgExt;

    @Before
    public void setup() throws Exception {
        org = new Org();
        orgExt = new OrgExt();
        org.setId("torg");
        org.setName("testorg");
        org.setShortName("tEsTOrG");
        org.setOrgExt(orgExt);
        orgExt.setId("torg");
        List<String> cities = Lists.newArrayList("Paris");
        org.setCities(cities);
        orgDao.insert(org);
        orgDao.insert(orgExt);

    }

    @After
    public void cleanLdap() {
        orgDao.delete(org);
        orgDao.delete(orgExt);
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

        org = orgDao.findByCommonName("torg");
        orgExt = orgDao.findExtById("torg");

        org.setOrgExt(orgExt);
        List<String> cities = Lists.newArrayList("Marseille");
        org.setCities(cities);
        orgDao.update(org);
        assertTrue(Arrays.asList(dco.getStringAttributes("objectClass")).contains("dcObject"));
    }

}
