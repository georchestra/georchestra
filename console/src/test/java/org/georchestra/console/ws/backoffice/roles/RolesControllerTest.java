package org.georchestra.console.ws.backoffice.roles;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.model.DelegationEntry;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.DuplicatedCommonNameException;
import org.georchestra.ds.roles.Role;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.roles.RoleFactory;
import org.georchestra.ds.roles.RoleProtected;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.AccountImpl;
import org.georchestra.ds.users.UserRule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class RolesControllerTest {

    private RolesController roleCtrl;

    private RoleDao roleDao;
    private AccountDao accountDao;

    private UserRule userRule;
    private LdapContextSource contextSource;
    private LogUtils mockLogUtils;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private String roleSearchBaseDN = "ou=roles";

    @Before
    public void setUp() throws Exception {
        contextSource = mock(LdapContextSource.class);
        mockLogUtils = mock(LogUtils.class);
        roleDao = mock(RoleDao.class);
        accountDao = mock(AccountDao.class);

        userRule = new UserRule();
        userRule.setListOfprotectedUsers(new String[] { "geoserver_privileged_user" });

        RoleProtected roles = new RoleProtected();
        roles.setListOfprotectedRoles(new String[] { "ADMINISTRATOR", "USER", "GN_.*", "MOD_.*" });

        roleCtrl = new RolesController(roleDao, userRule);
        roleCtrl.setAccountDao(accountDao);

        DelegationDao delegationDao = mock(DelegationDao.class);
        DelegationEntry resTestuser = new DelegationEntry();
        resTestuser.setUid("testuser");
        resTestuser.setOrgs(new String[] { "psc", "cra" });
        resTestuser.setRoles(new String[] { "GN_REVIEWER", "GN_EDITOR" });
        when(delegationDao.findFirstByUid(eq("testuser"))).thenReturn(resTestuser);
        roleCtrl.setDelegationDao(delegationDao);

        AdvancedDelegationDao advancedDelegationDao = mock(AdvancedDelegationDao.class);
        Set<String> usersUnderDelegation = new HashSet<String>();
        usersUnderDelegation.add("testeditor");
        usersUnderDelegation.add("testreviewer");

        when(advancedDelegationDao.findUsersUnderDelegation(eq("testuser"))).thenReturn(usersUnderDelegation);
        roleCtrl.setAdvancedDelegationDao(advancedDelegationDao);

        roleCtrl.logUtils = mockLogUtils;

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        // reset any spring security context that may have been set in previous test.
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());

        // Set user connected through spring security
        List<GrantedAuthority> role = new LinkedList<GrantedAuthority>();
        role.add(new SimpleGrantedAuthority("ROLE_SUPERUSER"));
        Authentication auth = new PreAuthenticatedAuthenticationToken("testadmin", null, role);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test(expected = DataServiceException.class)
    public void findAllWithException() throws Exception {
        doThrow(DataServiceException.class).when(roleDao).findAll();
        roleCtrl.findAll();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByCNEmpty() throws Exception {
        roleCtrl.findByCN("");
    }

    @Test(expected = DataServiceException.class)
    public void testFindByCNDataServiceException() throws Exception {
        doThrow(DataServiceException.class).when(roleDao).findByCommonName(anyString());
        roleCtrl.findByCN("NOTEXISTINGROLE");
    }

    @Test
    public void testFindByCN() throws Exception {
        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role", false);
        when(roleDao.findByCommonName(eq("ADMINISTRATOR"))).thenReturn(retAdmin);
        Role res = roleCtrl.findByCN("ADMINISTRATOR");
        assertEquals(res, retAdmin);
    }

    @Test
    public void testFindByCNTemp() throws Exception {
        Role retTemp = RoleFactory.create("TEMPORARY", "Virtual role that contains all temporary users", false);
        when(roleDao.findByCommonName(eq("TEMPORARY"))).thenReturn(retTemp);
        Role res = roleCtrl.findByCN("TEMPORARY");
        assertEquals(res, retTemp);
    }

    @Test
    public void testFindByCNExpired() throws Exception {
        Role retExpired = RoleFactory.create("EXPIRED", "Virtual role that contains all expired users", false);
        when(roleDao.findByCommonName(eq("EXPIRED"))).thenReturn(retExpired);
        Role res = roleCtrl.findByCN("EXPIRED");
        assertEquals(res, retExpired);
    }

    @Test
    public void testCreateDuplicateRole() throws Exception {
        Role myRole = RoleFactory.create("MYROLE", "test role", false);
        when(roleDao.findByCommonName(eq("MYROLE"))).thenReturn(myRole);

        request.setContent(
                "{ \"cn\": \"MYROLE\", \"description\": \"Description for the role\", \"isFavorite\": false }"
                        .getBytes());

        doThrow(DuplicatedCommonNameException.class).when(roleDao).insert(any());
        roleCtrl.create(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getBoolean("success") == false);
        assertTrue(ret.getString("error").equals("duplicated_common_name"));
    }

    @Test
    public void testCreateDataServiceExceptionThrown() throws Exception {
        request.setContent(
                "{ \"cn\": \"MYROLE\", \"description\": \"Description for the role\", \"isFavorite\": false }"
                        .getBytes());
        doThrow(DataServiceException.class).when(roleDao).findByCommonName(eq("MYROLE"));

        try {
            roleCtrl.create(request, response);
        } catch (Throwable e) {
            JSONObject ret = new JSONObject(response.getContentAsString());
            assertTrue(e instanceof IOException);
            assertTrue(ret.getBoolean("success") == false);
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void testCreateLowerCase() throws IOException, JSONException {
        String roleName = "test_lower";
        String roleDescription = "lower case role";
        String content = "{ \"cn\": \"" + roleName + "\", \"description\": \"" + roleDescription
                + "\", \"isFavorite\": false }";
        request.setContent(content.getBytes());

        roleCtrl.create(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertEquals(ret.getString("cn"), roleName.toUpperCase());
        assertEquals(ret.getString("description"), roleDescription);

    }

    @Test
    public void testIllegalCharacter() throws IOException, JSONException {
        String roleName = "tést_lower";
        String roleDescription = "test illegal character";
        String content = "{ \"cn\": \"" + roleName + "\", \"description\": \"" + roleDescription + "\" }";
        request.setContent(content.getBytes());

        roleCtrl.create(request, response);
        assertEquals(response.getStatus(), HttpServletResponse.SC_CONFLICT);
        JSONObject ret = new JSONObject(response.getContentAsString());
        assertFalse(ret.getBoolean("success"));
        assertEquals(ret.getString("error"), "illegal_character");
    }

    /**
     * General case for creating role.
     *
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        request.setContent(
                "{ \"cn\": \"MYROLE\", \"description\": \"Description for the role\", \"isFavorite\": false }"
                        .getBytes());

        roleCtrl.create(request, response);

        Role expected = RoleFactory.create("MYROLE", "Description for the role", false);
        verify(roleDao).insert(eq(expected));

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getJSONArray("users").length() == 0);
        assertTrue(ret.getString("cn").equals("MYROLE"));
        assertTrue(ret.getString("description").equals("Description for the role"));
        assertTrue(response.getStatus() == HttpServletResponse.SC_OK);
    }

    @Test
    public void testDelete() throws Exception {
        roleCtrl.delete(response, "USERS");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_OK);
        assertTrue(ret.getBoolean("success") == true);
    }

    @Test
    public void testDeleteADMIN() throws Exception {
        doThrow(new DataServiceException("Role ADMINISTRATOR is a protected role")).when(roleDao)
                .delete(eq("ADMINISTRATOR"));

        roleCtrl.delete(response, "ADMINISTRATOR");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(ret.getBoolean("success") == false);
    }

    @Test
    public void testDeleteSV() throws Exception {
        doThrow(DataServiceException.class).when(roleDao).delete("GN_123USERS");

        roleCtrl.delete(response, "GN_123USERS");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(ret.getBoolean("success") == false);
    }

    @Test
    public void testDeleteException() throws Exception {

        doThrow(Exception.class).when(roleDao).delete(eq("ADMINISTRATOR"));

        try {
            roleCtrl.delete(response, "ADMINISTRATOR");
        } catch (Throwable e) {
            JSONObject ret = new JSONObject(response.getContentAsString());
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            assertTrue(ret.getBoolean("success") == false);
            assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());

        doThrow(NameNotFoundException.class).when(roleDao).findByCommonName("ADMINISTRATOR");

        roleCtrl.update(request, response, "ADMINISTRATOR");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
        assertTrue(ret.getBoolean("success") == false);
        assertTrue(ret.getString("error").equals("not_found"));
    }

    @Test
    public void testUpdateDataServiceExceptionAtLookup() throws Exception {
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());

        doThrow(DataServiceException.class).when(roleDao).findByCommonName(eq("ADMINISTRATOR"));

        try {
            roleCtrl.update(request, response, "ADMINISTRATOR");
        } catch (Throwable e) {
            assertTrue(e instanceof IOException);
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void testUpdateNotFoundAtModification() throws Exception {
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());
        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role", false);

        when(roleDao.findByCommonName(eq("ADMINISTRATOR"))).thenReturn(retAdmin);

        doThrow(NameNotFoundException.class).when(roleDao).update(eq("ADMINISTRATOR"), same(retAdmin));

        roleCtrl.update(request, response, "ADMINISTRATOR");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getString("error").equals("not_found"));
        assertTrue(!ret.getBoolean("success"));
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);

    }

    @Test
    public void testUpdateDuplicateCN() throws Exception {
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());

        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role", false);
        Role retNewName = RoleFactory.create("newName", "new Description", false);

        when(roleDao.findByCommonName(eq(retAdmin.getName()))).thenReturn(retAdmin);
        when(roleDao.findByCommonName(eq(retNewName.getName()))).thenReturn(retNewName);

        doThrow(DuplicatedCommonNameException.class).when(roleDao).update(eq("ADMINISTRATOR"), any());
        roleCtrl.update(request, response, "ADMINISTRATOR");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getString("error").equals("duplicated_common_name"));
        assertTrue(!ret.getBoolean("success"));
        assertTrue(response.getStatus() == HttpServletResponse.SC_CONFLICT);
    }

    @Test
    public void testUpdateDataServiceExceptionAtUpdate() throws Exception {
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());
        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role", false);
        when(roleDao.findByCommonName(eq(retAdmin.getName()))).thenReturn(retAdmin);

        doThrow(DataServiceException.class).when(roleDao).update(eq("ADMINISTRATOR"), any());

        try {
            roleCtrl.update(request, response, "ADMINISTRATOR");
        } catch (Throwable e) {
            JSONObject ret = new JSONObject(response.getContentAsString());
            assertTrue(e instanceof IOException);
            assertTrue(!ret.getBoolean("success"));
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());
        Role retUsers = RoleFactory.create("USERS", "USERS role", false);

        when(roleDao.findByCommonName(eq(retUsers.getName()))).thenReturn(retUsers);

        roleCtrl.update(request, response, "USERS");

        Role expected = RoleFactory.create("newName", "new Description", false);
        verify(roleDao).update(eq("USERS"), eq(expected));

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_OK);
        assertTrue(ret.getString("cn").equals("newName"));
        assertTrue(ret.getString("description").equals("new Description"));

    }

    @Test(expected = NameNotFoundException.class)
    public void testUpdateUsersNotFoundException() throws Exception {
        JSONObject toSend = new JSONObject().put("users", new JSONArray().put("testadmin").put("testuser"))
                .put("PUT", new JSONArray().put("ADMINISTRATOR")).put("DELETE", new JSONArray().put("USERS"));
        request.setContent(toSend.toString().getBytes());
        request.setRequestURI("/console/roles_users");

        doThrow(NameNotFoundException.class).when(accountDao).findByUID(anyString());

        roleCtrl.updateUsers(request, response);
    }

    @Test(expected = JSONException.class)
    public void testUpdateUsersJsonException() throws Exception {
        JSONObject toSend = new JSONObject().put("users", new JSONArray().put("testadmin").put("testuser"))
                .put("PUT", new JSONArray().put("ADMINISTRATOR")).put("DELETE", new JSONArray().put("USERS"));
        // Remove first char of json string so json is not parsable
        request.setContent(toSend.toString().substring(1).getBytes());
        request.setRequestURI("/console/roles_users");

        roleCtrl.updateUsers(request, response);
    }

    @Test(expected = DataServiceException.class)
    public void testUpdateUsersDataServiceException() throws Exception {
        JSONObject toSend = new JSONObject().put("users", new JSONArray().put("testadmin").put("testuser"))
                .put("PUT", new JSONArray().put("ADMINISTRATOR")).put("DELETE", new JSONArray().put("USERS"));

        mockAccountLookup("testadmin");
        mockAccountLookup("testuser");

        request.setContent(toSend.toString().getBytes());
        request.setRequestURI("/console/roles_users");

        doThrow(DataServiceException.class).when(roleDao).addUsersInRoles(any(), any());

        roleCtrl.updateUsers(request, response);
    }

    @Test
    public void testUpdateUsers() throws Exception {
        JSONObject toSend = new JSONObject().put("users", new JSONArray().put("testadmin").put("testuser"))
                .put("PUT", new JSONArray().put("ADMINISTRATOR")).put("DELETE", new JSONArray().put("USERS"));

        Account testadmin = mockAccountLookup("testadmin");
        Account testuser = mockAccountLookup("testuser");

        request.setContent(toSend.toString().getBytes());
        request.setRequestURI("/console/roles_users");

        Role myRole = RoleFactory.create("USERS", "USERS role", false);
        when(roleDao.findByCommonName(eq(myRole.getName()))).thenReturn(myRole);

        roleCtrl.updateUsers(request, response);

        List<Account> accounts = newArrayList(testadmin, testuser);

        verify(roleDao).addUsersInRoles(eq(singletonList("ADMINISTRATOR")), eq(accounts));
        verify(roleDao).deleteUsersInRoles(eq(singletonList("USERS")), eq(accounts));

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_OK);
        assertTrue(ret.getBoolean("success"));
    }

    private Account mockAccountLookup(String uuid) throws NameNotFoundException, DataServiceException {
        Account account = new AccountImpl();
        account.setUid(uuid);
        when(accountDao.findByUID(eq(uuid))).thenReturn(account);
        return account;
    }

    @Test
    public void testCheckAuthorizationOK() {
        roleCtrl.checkAuthorization("testuser", Arrays.asList(new String[] { "testeditor", "testreviewer" }),
                Arrays.asList(new String[] { "GN_REVIEWER" }), Arrays.asList(new String[] { "GN_EDITOR" }));
    }

    @Test(expected = AccessDeniedException.class)
    public void testCheckAuthorizationIllegalUser() {
        roleCtrl.checkAuthorization("testuser", Arrays.asList(new String[] { "testuser", "testreviewer" }),
                Arrays.asList(new String[] { "GN_REVIEWER" }), Arrays.asList(new String[] { "GN_EDITOR" }));
    }

    @Test(expected = AccessDeniedException.class)
    public void testCheckAuthorizationIllegalRolePut() {
        roleCtrl.checkAuthorization("testuser", Arrays.asList(new String[] { "testeditor", "testreviewer" }),
                Arrays.asList(new String[] { "GN_ADMIN" }), Arrays.asList(new String[] { "GN_EDITOR" }));
    }

    @Test(expected = AccessDeniedException.class)
    public void testCheckAuthorizationIllegalRoleDelete() {
        roleCtrl.checkAuthorization("testuser", Arrays.asList(new String[] { "testeditor", "testreviewer" }),
                Arrays.asList(new String[] { "GN_REVIEWER" }), Arrays.asList(new String[] { "GN_ADMIN" }));
    }
}
