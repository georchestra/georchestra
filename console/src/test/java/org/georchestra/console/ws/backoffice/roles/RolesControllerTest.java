package org.georchestra.console.ws.backoffice.roles;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;

import java.io.IOException;

import javax.naming.Name;
import javax.servlet.http.HttpServletResponse;

import org.georchestra.console.dao.AdminLogDao;
import org.georchestra.console.ds.*;
import org.georchestra.console.dto.Role;
import org.georchestra.console.dto.RoleFactory;
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

public class RolesControllerTest {

	private RolesController roleCtrl;

	private RoleDaoImpl roleDao;
	private UserRule userRule;
	private LdapTemplate ldapTemplate;
	private LdapContextSource contextSource;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

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
		roleDao.setRoleSearchBaseDN("ou=roles");
		roleDao.setUniqueNumberField("ou");
		roleDao.setUserSearchBaseDN("ou=users");
		roleDao.setLogDao(logDao);
		roleDao.setRoles(roles);

        OrgsDao orgsDao = new OrgsDao();
        orgsDao.setLdapTemplate(ldapTemplate);
        orgsDao.setOrgsSearchBaseDN("ou=orgs");
        orgsDao.setUserSearchBaseDN("ou=users");


        AccountDao accountDao = new AccountDaoImpl(ldapTemplate, roleDao, orgsDao);

		roleCtrl = new RolesController(roleDao, userRule);
		roleCtrl.setAccountDao(accountDao);

		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();

        // Set user connected through http header
        request.addHeader("sec-username", "testadmin");

	}

    @Test
    public void findAllWithException() throws Exception {
        roleDao = Mockito.mock(RoleDaoImpl.class);
        roleCtrl.setRoleDao(roleDao);

        Mockito.doThrow(Exception.class).when(roleDao).findAll();

        try {
            roleCtrl.findAll(request, response);
        } catch (Throwable e) {
            assertTrue(e instanceof IOException);
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void testFindByCNEmpty() throws Exception {
        roleCtrl.findByCN(response, "");
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testFindByCNNotFound() throws Exception {
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

        roleCtrl.findByCN(response, "NOTEXISTINGROLE");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
        assertTrue(ret.getString("error").equals("not_found"));
        assertTrue(ret.getBoolean("success") == false);
    }

    @Test
    public void testFindByCNDataServiceException() throws Exception {
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

        try {
            roleCtrl.findByCN(response, "NOTEXISTINGROLE");
        } catch (Throwable e) {
            JSONObject ret = new JSONObject(response.getContentAsString());
            assertTrue(e instanceof IOException);
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            assertTrue(ret.getBoolean("success") == false);
        }
    }

    @Test
    public void testFindByCN() throws Exception {
        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role");
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);

        roleCtrl.findByCN(response, "ADMINISTRATOR");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.get("cn").equals("ADMINISTRATOR"));
        assertTrue(ret.get("description").equals("administrator role"));
    }

    @Test
    public void testCreateDuplicateRole() throws Exception {
        request.setContent("{ \"cn\": \"MYROLE\", \"description\": \"Description for the role\" }".getBytes());

        roleCtrl.create(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getBoolean("success") == false);
        assertTrue(ret.getString("error").equals("duplicated_common_name"));
    }

    @Test
    public void testCreateDataServiceExceptionThrown() throws Exception {
        request.setContent("{ \"cn\": \"MYROLE\", \"description\": \"Description for the role\" }".getBytes());
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

    /**
     * General case for creating role.
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        request.setContent("{ \"cn\": \"MYROLE\", \"description\": \"Description for the role\" }".getBytes());
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
        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role");
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (AttributesMapper) Mockito.any());
        Name dn = LdapNameBuilder.newInstance("ou=roles").add("cn", "newName").build();
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(eq(dn), (ContextMapper) Mockito.any());

        roleCtrl.update(request, response, "ADMINISTRATOR");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getString("error").equals("not_found"));
        assertTrue(! ret.getBoolean("success"));
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);

    }

    @Test
    public void testUpdateDuplicateCN() throws Exception {
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());
        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role");
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);
        Mockito.doThrow(DuplicatedCommonNameException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (AttributesMapper) Mockito.any());
        Name dn = LdapNameBuilder.newInstance("ou=roles").add("cn", "newName").build();
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(eq(dn), (ContextMapper) Mockito.any());

        roleCtrl.update(request, response, "ADMINISTRATOR");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getString("error").equals("duplicated_common_name"));
        assertTrue(! ret.getBoolean("success"));
        assertTrue(response.getStatus() == HttpServletResponse.SC_CONFLICT);
    }

    @Test
    public void testUpdateDataServiceExceptionAtUpdate() throws Exception {
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());
        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role");
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (AttributesMapper) Mockito.any());
        Name dn = LdapNameBuilder.newInstance("ou=roles").add("cn", "newName").build();
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(eq(dn), (ContextMapper) Mockito.any());

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
        Role retAdmin = RoleFactory.create("ADMINISTRATOR", "administrator role");
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (AttributesMapper) Mockito.any())).thenReturn(-1);

        Name dn = LdapNameBuilder.newInstance("ou=roles").add("cn", "newName").build();
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(eq(dn), (ContextMapper) Mockito.any());

        roleCtrl.update(request, response, "USERS");

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_OK);
        assertTrue(ret.getString("cn").equals("newName"));
        assertTrue(ret.getString("description").equals("new Description"));

    }

	@Test
	public void testUpdateUsersNotFoundException() throws Exception {
		JSONObject toSend = new JSONObject().put("users", new JSONArray().put("testadmin").put("testuser"))
				.put("PUT", new JSONArray().put("ADMINISTRATOR")).put("DELETE", new JSONArray().put("USERS"));
		request.setContent(toSend.toString().getBytes());
		request.setRequestURI("/console/roles_users");
		DirContextOperations context = Mockito.mock(DirContextOperations.class);
		Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookupContext((Name) Mockito.any());

		roleCtrl.updateUsers(request, response);

		JSONObject ret = new JSONObject(response.getContentAsString());
		assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
		assertTrue(!ret.getBoolean("success"));
		assertTrue(ret.getString("error").equals("user_not_found"));
	}

	@Test
	public void testUpdateUsersJsonException() throws Exception {
		JSONObject toSend = new JSONObject().put("users", new JSONArray().put("testadmin").put("testuser"))
				.put("PUT", new JSONArray().put("ADMINISTRATOR")).put("DELETE", new JSONArray().put("USERS"));
		request.setContent(toSend.toString().getBytes());
		request.setRequestURI("/console/roles_users");
		// Well, this is unlikely to happen in real life, but the only thing
		// we want to test is a JSONException being caught, no matter is the
		// cause.
		Mockito.doThrow(JSONException.class).when(ldapTemplate).lookupContext((Name) Mockito.any());

		try {
			roleCtrl.updateUsers(request, response);
		} catch (Throwable e) {
			assertTrue(e instanceof IOException);
			JSONObject ret = new JSONObject(response.getContentAsString());
			assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			assertTrue(!ret.getBoolean("success"));
		}
	}

	@Test
	public void testUpdateUsersDataServiceException() throws Exception {
		JSONObject toSend = new JSONObject().put("users", new JSONArray().put("testadmin").put("testuser"))
				.put("PUT", new JSONArray().put("ADMINISTRATOR")).put("DELETE", new JSONArray().put("USERS"));
		request.setContent(toSend.toString().getBytes());
		request.setRequestURI("/console/roles_users");

		Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookupContext((Name) Mockito.any());

		try {
			roleCtrl.updateUsers(request, response);
		} catch (Throwable e) {
			assertTrue(e instanceof IOException);
			JSONObject ret = new JSONObject(response.getContentAsString());
			assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			assertTrue(!ret.getBoolean("success"));
		}
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

}
