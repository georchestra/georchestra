package org.georchestra.ldapadmin.ws.backoffice;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.georchestra.ldapadmin.ds.AccountDaoImpl;
import org.georchestra.ldapadmin.ds.GroupDaoImpl;
import org.georchestra.ldapadmin.ws.backoffice.groups.GroupsController;
import org.georchestra.ldapadmin.ws.backoffice.users.UserRule;
import org.georchestra.ldapadmin.ws.backoffice.users.UsersController;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

public class UsersGroupsControllerTest {

    private UsersController userCtrl;
    private GroupsController groupCtrl;

    private AccountDaoImpl dao;
    private GroupDaoImpl groupDao;
    private UserRule userRule;
    private LdapTemplate ldapTemplate;
    private LdapContextSource contextSource;


    private boolean testSuiteActivated = false;

    private static String ENV_ACTIVATED = "ldapadmin.test.openldap.activated";
    private static String ENV_BINDDN = "ldapadmin.test.openldap.binddn";
    private static String ENV_PASSWORD = "ldapadmin.test.openldap.password";
    private static String ENV_LDAPURL = "ldapadmin.test.openldap.ldapurl";
    private static String ENV_BASEDN = "ldapadmin.test.openldap.basedn";

    @Before
    public void setUp() throws Exception {
        testSuiteActivated = "true".equalsIgnoreCase(System.getProperty(ENV_ACTIVATED));
        final String bindDn = System.getProperty(ENV_BINDDN);
        final String password = System.getProperty(ENV_PASSWORD);
        String ldapurl = System.getProperty(ENV_LDAPURL);
        String basedn = System.getProperty(ENV_BASEDN);

        // if a LDAP is available locally, then uses it (or disables the test
        // suite, it surely isn't a good idea to launch destructive LDAP queries
        // on production).

        assumeTrue(testSuiteActivated);

        userRule = new UserRule();
        userRule.setListOfprotectedUsers(Arrays.asList(new String[]{"extractorapp_privileged_admin"}));


        contextSource = new DefaultSpringSecurityContextSource(ldapurl + basedn);
        contextSource.setBase(basedn);
        contextSource.setUrl(ldapurl);
        contextSource.setUserDn(bindDn);
        contextSource.setPassword(password);
        contextSource.setAuthenticationSource(new AuthenticationSource() {
            @Override
            public String getPrincipal() {
                   return bindDn;
            }
            @Override
            public String getCredentials() {
                return password;
            }
        });
        contextSource.setBaseEnvironmentProperties(new HashMap<String,Object>());
        contextSource.setAnonymousReadOnly(false);
        contextSource.setCacheEnvironmentProperties(false);

        ldapTemplate = new LdapTemplate(contextSource);

        // Configures groupDao
        groupDao = new GroupDaoImpl();
        groupDao.setLdapTemplate(ldapTemplate);
        groupDao.setGroupSearchBaseDN("ou=groups");
        groupDao.setUniqueNumberField("ou");
        groupDao.setUserSearchBaseDN("ou=users");

        // configures AccountDao
        dao = new AccountDaoImpl(ldapTemplate, groupDao);
        dao.setUniqueNumberField("employeeNumber");
        dao.setUserSearchBaseDN("ou=users");
        dao.setGroupDao(groupDao);

        userCtrl = new UsersController(dao, userRule);
        groupCtrl = new GroupsController(groupDao, userRule);

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public final void testUsersGroupController() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        userCtrl.findAll(request, response);

        JSONArray userJson = new JSONArray(response.getContentAsString());

        // reinitialize objects before reusing on the 2nd controller
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        groupCtrl.findAll(request, response);
        JSONArray groupJson = new JSONArray(response.getContentAsString());


        // Parses the output of groups controller
        Set<String> encounteredUsersInGroups = new HashSet<String>();
        for (int i = 0; i < groupJson.length(); ++i) {
            JSONObject curGrp = (JSONObject) groupJson.get(i);
            JSONArray curUsrs = (JSONArray) curGrp.get("users");
            for (int j = 0; j < curUsrs.length() ; ++j) {
                encounteredUsersInGroups.add((String) curUsrs.get(j));
            }
        }

        // Parses the output of users controller
        Set<String> encounteredUsers = new HashSet<String>();
        for (int i = 0; i < userJson.length(); ++i) {
            JSONObject currentUser = (JSONObject) userJson.get(i);
            encounteredUsers.add(currentUser.getString("uid"));
        }

        // Actually test

        // Every users in groups should exist
        for (String user : encounteredUsersInGroups) {
            assertTrue(user + " is not in the expected users", encounteredUsers.contains(user));
        }
        // Every users should be affected to at least one group
        for (String user : encounteredUsers) {
            assertTrue(user + " does not belong to any group", encounteredUsers.contains(user));
        }
    }
}
