package org.georchestra.ldapadmin.ws.backoffice.users;

import static org.junit.Assume.assumeTrue;

import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.AccountDaoImpl;
import org.georchestra.ldapadmin.ds.GroupDaoImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class UsersControllerTest {

    private UsersController ctrl;
    private AccountDao dao;
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
        String bindDn = System.getProperty(ENV_BINDDN);
        String password = System.getProperty(ENV_PASSWORD);
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

        ldapTemplate = new LdapTemplate();
        ldapTemplate.setContextSource(contextSource);

        // Configures groupDao
        groupDao.setLdapTemplate(ldapTemplate);
        groupDao.setGroupSearchBaseDN("ou=groups");
        groupDao.setUniqueNumberField("ou");
        groupDao.setUserSearchBaseDN("ou=users");

        // configures AccountDao
        dao = new AccountDaoImpl(ldapTemplate, groupDao);

        ctrl = new UsersController(dao, userRule);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testUsersController() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ctrl.findAll(request, response);
    }
}
