package org.georchestra.console.ws.backoffice;


import org.apache.commons.logging.LogFactory;
import org.georchestra.console.ds.AccountDaoImpl;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.ds.RoleDaoImpl;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountFactory;
import org.georchestra.console.dto.Role;
import org.georchestra.console.dto.RoleFactory;
import org.georchestra.console.dto.UserSchema;
import org.georchestra.console.ws.backoffice.users.UserRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.Mockito;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.PresentFilter;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.eq;

public class UsersRolesControllerTest {

    private AccountDaoImpl dao;
    private RoleDaoImpl roleDao;
    private OrgsDao orgDao;
    private UserRule userRule;
    private LdapTemplate ldapTemplate;
    private LdapContextSource contextSource;


    private static String ENV_ACTIVATED = "console.test.openldap.activated";
    private static String ENV_BINDDN = "console.test.openldap.binddn";
    private static String ENV_PASSWORD = "console.test.openldap.password";
    private static String ENV_LDAPURL = "console.test.openldap.ldapurl";
    private static String ENV_BASEDN = "console.test.openldap.basedn";

    private String basedn ;
    private boolean realLdapActivated ;


    @Rule
    public ErrorCollector collector = new ErrorCollector();

    private void setUpRealLdap() {
        final String bindDn = System.getProperty(ENV_BINDDN);
        final String password = System.getProperty(ENV_PASSWORD);
        String ldapurl = System.getProperty(ENV_LDAPURL);
        basedn = System.getProperty(ENV_BASEDN);

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
        contextSource
                .setBaseEnvironmentProperties(new HashMap<String, Object>());

        // Setting the following property to true on huge directories (PIGMA)
        // can lead to org.springframework.ldap.SizeLimitExceededException being
        // thrown. By default, OpenLDAP on debian limits the number of objects
        // returned for non privileged users to 500 objects.
        contextSource.setAnonymousReadOnly(false);

        contextSource.setCacheEnvironmentProperties(false);

        ldapTemplate = new LdapTemplate(contextSource);
    }
    private void setUpMockedObjects() {

        ldapTemplate = Mockito.mock(LdapTemplate.class);
        contextSource = Mockito.mock(LdapContextSource.class);

        // fake account list
        List<Account> fakeAccountList = new ArrayList<Account>();
        fakeAccountList.add(AccountFactory.createFull("testadmin", "testadmin", "testadmin", "administrator",
                "psc@georchestra.org", "administrator", "+331234567890", "admin",
                "48 avenue du lac du Bourget", "73000", "registeredAddress", "BP 352", "Le-Bourget-du-Lac",
                "avenue du lac du Bourget", "Savoie-Technolac", "+331234567899", "geodata administration",
                "Undisclosed", "+336123457890", "42", "Rhone-Alpes", null, null));

        fakeAccountList.add(AccountFactory.createFull("testuser", "testuser", "testuser", "regular user",
                "psc@georchestra.org", "user", "+331234567890", "user",
                "48 avenue du lac du Bourget", "73000", "registeredAddress", "BP 352", "Le-Bourget-du-Lac",
                "avenue du lac du Bourget", "Savoie-Technolac", "+331234567899", "Peon",
                "Undisclosed", "+336123457890", "42", "Rhone-Alpes", null, null));
        Account tempAccount = AccountFactory.createFull("testadminTmp", "testadminTmp", "testadminTmp", "administrator",
                "psc@georchestra.org", "administrator", "+331234567890", "admin",
                "48 avenue du lac du Bourget", "73000", "registeredAddress", "BP 352", "Le-Bourget-du-Lac",
                "avenue du lac du Bourget", "Savoie-Technolac", "+331234567899", "geodata administration",
                "Undisclosed", "+336123457890", "42", "Rhone-Alpes", null, null);
        fakeAccountList.add(tempAccount);


        // fake role List
        List<Role> fakeRoleList = new ArrayList<Role>();
        Role adminGrp = RoleFactory.create("ADMINISTRATOR", "roles of the administrators", false);
        adminGrp.setUserList(Arrays.asList(new String[] {"testadmin"}));

        Role userGrp = RoleFactory.create("USER", "regular users", false);
        userGrp.setUserList(Arrays.asList(new String[] {"testuser"}));

        fakeRoleList.add(adminGrp);
        fakeRoleList.add(userGrp);

        EqualsFilter accountFilter = new EqualsFilter("objectClass", "person");
        EqualsFilter roleFilter = new EqualsFilter("objectClass", "groupOfMembers");

        // Fake findAll() on AccountDao
        Mockito.when(ldapTemplate.search(Mockito.any(DistinguishedName.class), eq(accountFilter.encode()),
                Mockito.any(SearchControls.class),  Mockito.any(ContextMapper.class))).thenReturn(fakeAccountList);

        // Fake findAll() on RoleDao
        Mockito.when(ldapTemplate.search(Mockito.any(DistinguishedName.class), eq(roleFilter.encode()),
                Mockito.any(ContextMapper.class))).thenReturn(fakeRoleList);

        // Fake temporary account
        List<Account> fakeTempAccountList = new ArrayList<Account>();
        fakeTempAccountList.add(tempAccount);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "shadowAccount"));
        filter.and(new EqualsFilter("objectClass", "inetOrgPerson"));
        filter.and(new EqualsFilter("objectClass", "organizationalPerson"));
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new PresentFilter("shadowExpire"));

        SearchControls sc = new SearchControls();
        sc.setReturningAttributes(UserSchema.ATTR_TO_RETRIEVE);
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);


        Mockito.when(ldapTemplate.search(Mockito.any(DistinguishedName.class), eq(filter.encode()), Mockito.any(SearchControls.class),
                Mockito.any(ContextMapper.class))).thenReturn(fakeTempAccountList);

    }

    @Before
    public void setUp() throws Exception {
        realLdapActivated = "true".equalsIgnoreCase(System.getProperty(ENV_ACTIVATED));

        if (realLdapActivated) {
            // if a LDAP is available locally and the environment
            // is correctly configured, then uses it, instead of mocks.
            //
            // To configure the env, add the following to maven / jvm
            // properties, e.g.:
            //
            // $ mvn clean test -Dtest=UsersRolesControllerTest                        \
            //        -Dconsole.test.openldap.activated=true                          \
            //        -Dconsole.test.openldap.binddn="cn=admin,dc=georchestra,dc=org" \
            //        -Dconsole.test.openldap.password=secret                         \
            //        -Dconsole.test.openldap.ldapurl="ldap://localhost:389"          \
            //        -Dconsole.test.openldap.basedn="dc=georchestra,dc=org"
            //
            // This will activate the test onto a "real" OpenLDAP server,
            // giving some hints about the possible inconsistencies related
            // to your OpenLDAP geOrchestra tree.
            setUpRealLdap();
        } else {
            setUpMockedObjects();
        }
        userRule = new UserRule();
        userRule.setListOfprotectedUsers(new String[] { "geoserver_privileged_user" });


        // Configures roleDao
        roleDao = new RoleDaoImpl();
        roleDao.setLdapTemplate(ldapTemplate);
        roleDao.setRoleSearchBaseDN("ou=roles");
        roleDao.setUserSearchBaseDN("ou=users");

        OrgsDao orgsDao = new OrgsDao();
        orgsDao.setLdapTemplate(ldapTemplate);
        orgsDao.setOrgSearchBaseDN("ou=orgs");
        orgsDao.setUserSearchBaseDN("ou=users");

        // configures AccountDao
        dao = new AccountDaoImpl(ldapTemplate, roleDao, orgsDao);
        dao.setUserSearchBaseDN("ou=users");
        dao.setRoleDao(roleDao);
      
    }

    private final String TEST_ROLE_NAME = "LDAPADMIN_TESTSUITE_SAMPLE_ROLE" ;

    /**
     * This test is related to reveal issue #650
     * https://github.com/georchestra/georchestra/issues/650
     */
    @Test
    public final void testModifyRole_issue650() throws Exception {
        // This test needs to run onto a real LDAP
        assumeTrue(realLdapActivated);

        // first, ensures the following role does not exist
        try {
            roleDao.delete(TEST_ROLE_NAME);
        } catch (NameNotFoundException e) {
            LogFactory.getLog(this.getClass()).info(TEST_ROLE_NAME + " does not exist in the LDAP tree, it is safe to create it");
        }
        // Then creates it
        Role testGrp = RoleFactory.create(TEST_ROLE_NAME, "sample role", false);
        testGrp.addUser("uid=testadmin,ou=users," + basedn);
        testGrp.addUser("uid=testuser,ou=users," + basedn);
        roleDao.insert(testGrp);

        // Retrieves the saved role
        Role retrievedGrp = roleDao.findByCommonName(TEST_ROLE_NAME);

        collector.checkThat("Role should have contained 2 users", retrievedGrp.getUserList().size(), equalTo(2));

        // Then modifies the role desc
        retrievedGrp.setDescription("sample role with updated desc");
        roleDao.update(TEST_ROLE_NAME, retrievedGrp);

        Role retrievedGrp2 = roleDao.findByCommonName(TEST_ROLE_NAME);

        // should still contain 2 users
        collector.checkThat("Role should have contained 2 users", retrievedGrp.getUserList().size(), equalTo(2));

        // Actually deletes the role
        roleDao.delete(TEST_ROLE_NAME);

    }
}
