package org.georchestra.console.integration.dao;

import org.apache.commons.lang3.RandomStringUtils;
import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.integration.ConsoleIntegrationTest;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.DuplicatedCommonNameException;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.ds.roles.Role;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.roles.RoleFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class RoleDaoIT extends ConsoleIntegrationTest {

    private @Autowired RoleDao roleDao;

    private @Autowired OrgsDao orgsDao;

    @Test
    public void orgWithRole() throws DuplicatedCommonNameException, DataServiceException {
        String roleName = "IT_ROLE_" + RandomStringUtils.randomAlphabetic(8).toUpperCase();
        Role role = RoleFactory.create(roleName, "sample role", false);
        roleDao.insert(role);
        String orgName = "IT_ORG_" + RandomStringUtils.randomAlphabetic(8).toUpperCase();
        Org org = new Org();
        org.setId(orgName);
        orgsDao.insert(org);

        roleDao.addOrgsInRoles(Arrays.asList(roleName), Arrays.asList(org));

        Role actualRole = roleDao.findByCommonName(roleName);
        assertEquals(1, actualRole.getOrgList().size());
        assertEquals(0, actualRole.getUserList().size());

    }
}
