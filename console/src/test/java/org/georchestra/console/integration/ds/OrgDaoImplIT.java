package org.georchestra.console.integration.ds;

import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.dto.orgs.OrgExt;
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
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class OrgDaoImplIT {
    public @Rule @Autowired IntegrationTestSupport support;
    @Autowired
    OrgsDao orgDao;
    @Autowired
    LdapTemplate ldapTemplate;

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
        ArrayList<String> cities = new ArrayList<String>() {
            {
                add("Paris");
            }
        };
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
        List<String> oc = Arrays.asList(dco.getStringAttributes("objectClass"));
        // Adding a random (but valid, we're dealing with real ldap server) objectClass
        oc.add("dcObject");
        dco.setAttributeValues("objectClass", oc.toArray());
        dco.setAttributeValue("dc", "RandomValue");
        ldapTemplate.modifyAttributes(dco);
        dco = ldapTemplate.lookupContext("cn=torg,ou=orgs");

        org = orgDao.findByCommonName("torg");
        orgExt = orgDao.findExtById("torg");

        org.setOrgExt(orgExt);
        ArrayList<String> cities = new ArrayList<String>() {
            {
                add("Marseille");
            }
        };
        org.setCities(cities);
        orgDao.update(org);
        assertTrue(Arrays.asList(dco.getStringAttributes("objectClass")).contains("dcObject"));
    }

}
