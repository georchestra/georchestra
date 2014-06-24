package org.georchestra.ldapadmin.ws.backoffice;

import static org.junit.Assume.assumeTrue;

import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.AccountDaoImpl;
import org.georchestra.ldapadmin.ds.GroupDaoImpl;
import org.georchestra.ldapadmin.ws.backoffice.groups.GroupsController;
import org.georchestra.ldapadmin.ws.backoffice.users.UserRule;
import org.georchestra.ldapadmin.ws.backoffice.users.UsersController;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.ldap.authentication.SpringSecurityAuthenticationSource;

public class UsersGroupsControllerTest {

    private UsersController userCtrl;
    private GroupsController groupCtrl;

    private AccountDao dao;
    private GroupDaoImpl groupDao;
    private UserRule userRule;
    private LdapTemplate ldapTemplate;
    private LdapContextSource contextSource;
    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockHttpServletResponse response = new MockHttpServletResponse();

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
        groupDao = new GroupDaoImpl();

        contextSource = new LdapContextSource();
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

        ldapTemplate = new LdapTemplate();
        ldapTemplate.setContextSource(contextSource);

        // Configures groupDao
        groupDao.setLdapTemplate(ldapTemplate);
        groupDao.setGroupSearchBaseDN("ou=groups");
        groupDao.setUniqueNumberField("ou");
        groupDao.setUserSearchBaseDN("ou=users");

        // configures AccountDao
        dao = new AccountDaoImpl(ldapTemplate, groupDao);

        userCtrl = new UsersController(dao, userRule);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testUsersGroupController() throws Exception {
        contextSource.getReadOnlyContext();
        userCtrl.findAll(request, response);
        JSONObject userJson = new JSONObject(response.getContentAsString());

        groupCtrl.findAll(request, response);
        JSONObject groupJson = new JSONObject(response.getContentAsString());



    }
}
