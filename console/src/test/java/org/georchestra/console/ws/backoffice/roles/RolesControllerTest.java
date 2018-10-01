package org.georchestra.console.ws.backoffice.roles;

import org.georchestra.console.dao.AdminLogDao;
import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.AccountDaoImpl;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.ds.RoleDaoImpl;
import org.georchestra.console.dto.Role;
import org.georchestra.console.dto.RoleFactory;
import org.georchestra.console.model.DelegationEntry;
import org.georchestra.console.ws.backoffice.users.UserRule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.naming.Name;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;

public class RolesControllerTest {

	private RolesController roleCtrl;

	private RoleDaoImpl roleDao;
	private UserRule userRule;
	private LdapTemplate ldapTemplate;
	private LdapContextSource contextSource;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	private String roleSearchBaseDN = "ou=roles";

    @Before
	public void setUp() throws Exception {
		ldapTemplate = Mockito.mock(LdapTemplate.class);
		contextSource = Mockito.mock(LdapContextSource.class);
        AdminLogDao logDao = Mockito.mock(AdminLogDao.class);

		Mockito.when(contextSource.getBaseLdapPath()).thenReturn(new DistinguishedName("dc=georchestra,dc=org"));

		Mockito.when(ldapTemplate.getContextSource()).thenReturn(contextSource);

		userRule = new UserRule();
		userRule.setListOfprotectedUsers(new String[] { "geoserver_privileged_user" });

		RoleProtected roles = new RoleProtected();
		roles.setListOfprotectedRoles(new String[] { "ADMINISTRATOR", "USER", "GN_.*", "MOD_.*" });

		// AdminLogDao logDao = new Adm

		// Configures roleDao
		roleDao = new RoleDaoImpl();
		roleDao.setLdapTemplate(ldapTemplate);
		roleDao.setRoleSearchBaseDN(this.roleSearchBaseDN);
		roleDao.setUserSearchBaseDN("ou=users");
		roleDao.setLogDao(logDao);
		roleDao.setRoles(roles);

        OrgsDao orgsDao = new OrgsDao();
        orgsDao.setLdapTemplate(ldapTemplate);
        orgsDao.setOrgSearchBaseDN("ou=orgs");
        orgsDao.setUserSearchBaseDN("ou=users");


        AccountDao accountDao = new AccountDaoImpl(ldapTemplate, roleDao, orgsDao);

		roleCtrl = new RolesController(roleDao, userRule);
		roleCtrl.setAccountDao(accountDao);

		DelegationDao delegationDao = Mockito.mock(DelegationDao.class);
        DelegationEntry resTestuser = new DelegationEntry();
        resTestuser.setUid("testuser");
        resTestuser.setOrgs(new String[]{"psc", "cra"});
        resTestuser.setRoles(new String[]{"GN_REVIEWER", "GN_EDITOR"});
		Mockito.when(delegationDao.findOne(Mockito.eq("testuser"))).thenReturn(resTestuser);
		roleCtrl.setDelegationDao(delegationDao);

        AdvancedDelegationDao advancedDelegationDao = Mockito.mock(AdvancedDelegationDao.class);
        Set<String> usersUnderDelegation = new HashSet<String>();
        usersUnderDelegation.add("testeditor");
        usersUnderDelegation.add("testreviewer");

        Mockito.when(advancedDelegationDao.findUsersUnderDelegation(Mockito.eq("testuser"))).thenReturn(usersUnderDelegation);
        roleCtrl.setAdvancedDelegationDao(advancedDelegationDao);

		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();

        // Set user connected through spring security
        List<GrantedAuthority> role = new LinkedList<GrantedAuthority>();
        role.add(new SimpleGrantedAuthority("ROLE_SUPERUSER"));
        Authentication auth = new PreAuthenticatedAuthenticationToken("testadmin",
                null,
                role);
        SecurityContextHolder.getContext().setAuthentication(auth);
	}

    @Test(expected = Exception.class)
    public void findAllWithException() throws Exception {
        roleDao = Mockito.mock(RoleDaoImpl.class);
        roleCtrl.setRoleDao(roleDao);
        Mockito.doThrow(Exception.class).when(roleDao).findAll();
        roleCtrl.findAll();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByCNEmpty() throws Exception {
        roleCtrl.findByCN("");
    }

    @Test(expected = DataServiceException.class)
    public void testFindByCNDataServiceException() throws Exception {
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());
        roleCtrl.findByCN("NOTEXISTINGROLE");
    }

    @Test
    public void testFindByCN() throws Exception {
        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role", false);
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);

        Role res = roleCtrl.findByCN("ADMINISTRATOR");
        assertEquals(res, retAdmin);
    }

    @Test
    public void testCreateDuplicateRole() throws Exception {

        Name ldapFilter = LdapNameBuilder.newInstance(this.roleSearchBaseDN).add("cn", "MYROLE").build();
        Role myRole = RoleFactory.create("MYROLE", "test role", false);
        Mockito.when(ldapTemplate.lookup((Name) Mockito.eq(ldapFilter), (ContextMapper) Mockito.any())).thenReturn(myRole);

        request.setContent("{ \"cn\": \"MYROLE\", \"description\": \"Description for the role\", \"isFavorite\": false }".getBytes());

        roleCtrl.create(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getBoolean("success") == false);
        assertTrue(ret.getString("error").equals("duplicated_common_name"));
    }

    @Test
    public void testCreateDataServiceExceptionThrown() throws Exception {
        request.setContent("{ \"cn\": \"MYROLE\", \"description\": \"Description for the role\", \"isFavorite\": false }".getBytes());
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

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
        String content = "{ \"cn\": \"" + roleName + "\", \"description\": \"" + roleDescription + "\", \"isFavorite\": false }";
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
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        request.setContent("{ \"cn\": \"MYROLE\", \"description\": \"Description for the role\", \"isFavorite\": false }".getBytes());
        // ensures the mocked object does not return an already existing role
        // Raising NotFoundException instead
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

        roleCtrl.create(request, response);

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
		roleCtrl.delete(response, "ADMINISTRATOR");

		JSONObject ret = new JSONObject(response.getContentAsString());
		assertTrue(response.getStatus() == HttpServletResponse.SC_BAD_REQUEST);
		assertTrue(ret.getBoolean("success") == false);
	}

	@Test
	public void testDeleteSV() throws Exception {
		roleCtrl.delete(response, "GN_123USERS");

		JSONObject ret = new JSONObject(response.getContentAsString());
		assertTrue(response.getStatus() == HttpServletResponse.SC_BAD_REQUEST);
		assertTrue(ret.getBoolean("success") == false);
	}

	@Test
    public void testDeleteException() throws Exception {
        Mockito.doThrow(Exception.class).when(ldapTemplate).unbind((Name) Mockito.any(), Mockito.anyBoolean());

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

        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

        roleCtrl.update(request, response, "ADMINISTRATOR");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
        assertTrue(ret.getBoolean("success") == false);
        assertTrue(ret.getString("error").equals("not_found"));
    }

    @Test
    public void testUpdateDataServiceExceptionAtLookup() throws Exception {
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());

        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

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
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (AttributesMapper) Mockito.any());
        Name dn = LdapNameBuilder.newInstance("ou=roles").add("cn", "newName").build();
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(eq(dn), (ContextMapper) Mockito.any());

        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookupContext((Name) Mockito.any());
        roleCtrl.update(request, response, "ADMINISTRATOR");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getString("error").equals("not_found"));
        assertTrue(! ret.getBoolean("success"));
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);

    }

    @Test
    public void testUpdateDuplicateCN() throws Exception {
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());

        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role", false);
        Role retNewName = RoleFactory.create("newName", "new Description", false);
        Name adminDn = LdapNameBuilder.newInstance("ou=roles").add("cn", "ADMINISTRATOR").build();
        Name newNameDn = LdapNameBuilder.newInstance("ou=roles").add("cn", "newName").build();
        Mockito.when(ldapTemplate.lookup(eq(adminDn), (ContextMapper) Mockito.any())).thenReturn(retAdmin);
        Mockito.when(ldapTemplate.lookup(eq(newNameDn), (ContextMapper) Mockito.any())).thenReturn(retNewName);

        DirContextOperations context = Mockito.mock(DirContextOperations.class);
        Mockito.when(ldapTemplate.lookupContext((Name) Mockito.any())).thenReturn(context);

        roleCtrl.update(request, response, "ADMINISTRATOR");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getString("error").equals("duplicated_common_name"));
        assertTrue(! ret.getBoolean("success"));
        assertTrue(response.getStatus() == HttpServletResponse.SC_CONFLICT);
    }

    @Test
    public void testUpdateDataServiceExceptionAtUpdate() throws Exception {
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());
        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role", false);
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (AttributesMapper) Mockito.any());
        Name dn = LdapNameBuilder.newInstance("ou=roles").add("cn", "newName").build();
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(eq(dn), (ContextMapper) Mockito.any());

        DirContextOperations context = Mockito.mock(DirContextOperations.class);
        Mockito.when(ldapTemplate.lookupContext((Name) Mockito.any())).thenReturn(context);

        try {
            roleCtrl.update(request, response, "ADMINISTRATOR");
        } catch (Throwable e) {
            JSONObject ret = new JSONObject(response.getContentAsString());
            assertTrue(e instanceof IOException);
            assertTrue(! ret.getBoolean("success"));
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());
        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role", false);
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (AttributesMapper) Mockito.any())).thenReturn(-1);

        Name dn = LdapNameBuilder.newInstance("ou=roles").add("cn", "newName").build();
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(eq(dn), (ContextMapper) Mockito.any());

        DirContextOperations context = Mockito.mock(DirContextOperations.class);
        Mockito.when(ldapTemplate.lookupContext((Name) Mockito.any())).thenReturn(context);

        roleCtrl.update(request, response, "USERS");

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
		DirContextOperations context = Mockito.mock(DirContextOperations.class);
		Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookupContext((Name) Mockito.any());

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
		request.setContent(toSend.toString().getBytes());
		request.setRequestURI("/console/roles_users");

		Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookupContext((Name) Mockito.any());

        roleCtrl.updateUsers(request, response);
	}

	@Test
	public void testUpdateUsers() throws Exception {
		JSONObject toSend = new JSONObject().put("users", new JSONArray().put("testadmin").put("testuser"))
				.put("PUT", new JSONArray().put("ADMINISTRATOR")).put("DELETE", new JSONArray().put("USERS"));
		request.setContent(toSend.toString().getBytes());
		request.setRequestURI("/console/roles_users");
		DirContextOperations context = Mockito.mock(DirContextOperations.class);
		Mockito.when(ldapTemplate.lookupContext((Name) Mockito.any())).thenReturn(context);

		roleCtrl.updateUsers(request, response);

		JSONObject ret = new JSONObject(response.getContentAsString());
		assertTrue(response.getStatus() == HttpServletResponse.SC_OK);
		assertTrue(ret.getBoolean("success"));
	}

	@Test
    public void testCheckAuthorizationOK(){
        roleCtrl.checkAuthorization("testuser",
                Arrays.asList(new String[]{"testeditor", "testreviewer"}),
                Arrays.asList(new String[]{"GN_REVIEWER"}),
                Arrays.asList(new String[]{"GN_EDITOR"}));
    }

    @Test(expected = AccessDeniedException.class)
    public void testCheckAuthorizationIllegalUser(){
        roleCtrl.checkAuthorization("testuser",
                Arrays.asList(new String[]{"testuser", "testreviewer"}),
                Arrays.asList(new String[]{"GN_REVIEWER"}),
                Arrays.asList(new String[]{"GN_EDITOR"}));
    }

    @Test(expected = AccessDeniedException.class)
    public void testCheckAuthorizationIllegalRolePut(){
        roleCtrl.checkAuthorization("testuser",
                Arrays.asList(new String[]{"testeditor", "testreviewer"}),
                Arrays.asList(new String[]{"GN_ADMIN"}),
                Arrays.asList(new String[]{"GN_EDITOR"}));
    }

    @Test(expected = AccessDeniedException.class)
    public void testCheckAuthorizationIllegalRoleDelete(){
        roleCtrl.checkAuthorization("testuser",
                Arrays.asList(new String[]{"testeditor", "testreviewer"}),
                Arrays.asList(new String[]{"GN_REVIEWER"}),
                Arrays.asList(new String[]{"GN_ADMIN"}));
    }
}
