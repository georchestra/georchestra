package org.georchestra.console.ws.backoffice;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.eq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.georchestra.console.ds.AccountDaoImpl;
import org.georchestra.console.ds.GroupDaoImpl;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountFactory;
import org.georchestra.console.dto.Group;
import org.georchestra.console.dto.GroupFactory;
import org.georchestra.console.dto.UserSchema;
import org.georchestra.console.ws.backoffice.groups.GroupsController;
import org.georchestra.console.ws.backoffice.users.UserRule;
import org.georchestra.console.ws.backoffice.users.UsersController;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

import javax.naming.directory.SearchControls;

public class UsersGroupsControllerTest {

    private UsersController userCtrl;
    private GroupsController groupCtrl;

    private AccountDaoImpl dao;
    private GroupDaoImpl groupDao;
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


        // fake group List
        List<Group> fakeGroupList = new ArrayList<Group>();
        Group adminGrp = GroupFactory.create("ADMINISTRATOR", "groups of the administrators");
        adminGrp.setUserList(Arrays.asList(new String[] {"testadmin"}));

        Group userGrp = GroupFactory.create("USER", "regular users");
        userGrp.setUserList(Arrays.asList(new String[] {"testuser"}));

        fakeGroupList.add(adminGrp);
        fakeGroupList.add(userGrp);

        EqualsFilter accountFilter = new EqualsFilter("objectClass", "person");
        EqualsFilter groupFilter = new EqualsFilter("objectClass", "groupOfMembers");

        // Fake findAll() on AccountDao
        Mockito.when(ldapTemplate.search(Mockito.any(DistinguishedName.class), eq(accountFilter.encode()),
                Mockito.any(SearchControls.class),  Mockito.any(ContextMapper.class))).thenReturn(fakeAccountList);

        // Fake findAll() on GroupDao
        Mockito.when(ldapTemplate.search(Mockito.any(DistinguishedName.class), eq(groupFilter.encode()),
                Mockito.any(ContextMapper.class))).thenReturn(fakeGroupList);

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
            // $ mvn clean test -Dtest=UsersGroupsControllerTest                        \
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


        // Configures groupDao
        groupDao = new GroupDaoImpl();
        groupDao.setLdapTemplate(ldapTemplate);
        groupDao.setGroupSearchBaseDN("ou=roles");
        groupDao.setUniqueNumberField("ou");
        groupDao.setUserSearchBaseDN("ou=users");

        OrgsDao orgsDao = new OrgsDao();
        orgsDao.setLdapTemplate(ldapTemplate);
        orgsDao.setOrgsSearchBaseDN("ou=orgs");
        orgsDao.setUserSearchBaseDN("ou=users");

        // configures AccountDao
        dao = new AccountDaoImpl(ldapTemplate, groupDao, orgsDao);
        dao.setUniqueNumberField("employeeNumber");
        dao.setUserSearchBaseDN("ou=users");
        dao.setGroupDao(groupDao);

        userCtrl = new UsersController(dao, userRule);
        userCtrl.setOrgDao(orgsDao);
        groupCtrl = new GroupsController(groupDao, userRule);
        groupCtrl.setAccountDao(dao);
      
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
            for (int j = 0; j < curUsrs.length(); ++j) {
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
            collector.checkThat(user + " is not in the expected users, but is member of groups",
                    encounteredUsers.contains(user), Matchers.equalTo(true));
        }
        // Every users should be affected to at least one group
        for (String user : encounteredUsers) {
            collector.checkThat(user + " is not in any groups",
                    encounteredUsersInGroups.contains(user), Matchers.equalTo(true));
        }

    }

    private final String TEST_GROUP_NAME = "LDAPADMIN_TESTSUITE_SAMPLE_GROUP" ;

    /**
     * This test is related to reveal issue #650
     * https://github.com/georchestra/georchestra/issues/650
     */
    @Test
    public final void testModifyGroup_issue650() throws Exception {
        // This test needs to run onto a real LDAP
        assumeTrue(realLdapActivated);

        // first, ensures the following group does not exist
        try {
            groupDao.delete(TEST_GROUP_NAME);
        } catch (NameNotFoundException e) {
            LogFactory.getLog(this.getClass()).info(TEST_GROUP_NAME + " does not exist in the LDAP tree, it is safe to create it");
        }
        // Then creates it
        Group testGrp = GroupFactory.create(TEST_GROUP_NAME, "sample group");
        testGrp.addUser("uid=testadmin,ou=users," + basedn);
        testGrp.addUser("uid=testuser,ou=users," + basedn);
        groupDao.insert(testGrp);

        // Retrieves the saved group
        Group retrievedGrp = groupDao.findByCommonName(TEST_GROUP_NAME);

        collector.checkThat("Group should have contained 2 users", retrievedGrp.getUserList().size(), equalTo(2));

        // Then modifies the group desc
        retrievedGrp.setDescription("sample group with updated desc");
        groupDao.update(TEST_GROUP_NAME, retrievedGrp);

        Group retrievedGrp2 = groupDao.findByCommonName(TEST_GROUP_NAME);

        // should still contain 2 users
        collector.checkThat("Group should have contained 2 users", retrievedGrp.getUserList().size(), equalTo(2));

        // Actually deletes the group
        groupDao.delete(TEST_GROUP_NAME);

    }
}
