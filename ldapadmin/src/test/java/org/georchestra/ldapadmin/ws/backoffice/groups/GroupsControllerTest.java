package org.georchestra.ldapadmin.ws.backoffice.groups;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;

import java.io.IOException;

import javax.naming.Name;
import javax.servlet.http.HttpServletResponse;

import org.georchestra.ldapadmin.dao.AdminLogDao;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.AccountDaoImpl;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.DuplicatedCommonNameException;
import org.georchestra.ldapadmin.ds.GroupDaoImpl;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.GroupFactory;
import org.georchestra.ldapadmin.ws.backoffice.users.UserRule;
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

public class GroupsControllerTest {

	private GroupsController groupCtrl;

	private AccountDaoImpl dao;
	private GroupDaoImpl groupDao;
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

		GroupProtected groups = new GroupProtected();
		groups.setListOfprotectedGroups(new String[] { "ADMINISTRATOR", "USER", "GN_.*", "MOD_.*" });

		// AdminLogDao logDao = new Adm

		// Configures groupDao
		groupDao = new GroupDaoImpl();
		groupDao.setLdapTemplate(ldapTemplate);
		groupDao.setGroupSearchBaseDN("ou=roles");
		groupDao.setUniqueNumberField("ou");
		groupDao.setUserSearchBaseDN("ou=users");
		groupDao.setLogDao(logDao);
		groupDao.setGroups(groups);

		AccountDao accountDao = new AccountDaoImpl(ldapTemplate, groupDao);

		groupCtrl = new GroupsController(groupDao, userRule);
		groupCtrl.setAccountDao(accountDao);

		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();

        // Set user connected through http header
        request.addHeader("sec-username", "testadmin");

	}

    @Test
    public void findAllWithException() throws Exception {
        groupDao = Mockito.mock(GroupDaoImpl.class);
        groupCtrl.setGroupDao(groupDao);

        Mockito.doThrow(Exception.class).when(groupDao).findAll();

        try {
            groupCtrl.findAll(request, response);
        } catch (Throwable e) {
            assertTrue(e instanceof IOException);
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void testFindByCNEmpty() throws Exception {
        groupCtrl.findByCN(request, response);
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testFindByCNNotFound() throws Exception {
        request.setRequestURI("/ldapadmin/groups/NOTEXISTINGGROUP");
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

        groupCtrl.findByCN(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
        assertTrue(ret.getString("error").equals("not_found"));
        assertTrue(ret.getBoolean("success") == false);
    }

    @Test
    public void testFindByCNDataServiceException() throws Exception {
        request.setRequestURI("/ldapadmin/groups/NOTEXISTINGGROUP");
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

        try {
            groupCtrl.findByCN(request, response);
        } catch (Throwable e) {
            JSONObject ret = new JSONObject(response.getContentAsString());
            assertTrue(e instanceof IOException);
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            assertTrue(ret.getBoolean("success") == false);
        }
    }

    @Test
    public void testFindByCN() throws Exception {
        request.setRequestURI("/ldapadmin/groups/ADMINISTRATOR");
        Group retAdmin = GroupFactory.create("ADMINISTRATOR", "administrator group");
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);

        groupCtrl.findByCN(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.get("cn").equals("ADMINISTRATOR"));
        assertTrue(ret.get("description").equals("administrator group"));
    }

    @Test
    public void testCreateDuplicateGroup() throws Exception {
        request.setContent("{ \"cn\": \"MYGROUP\", \"description\": \"Description for the group\" }".getBytes());

        groupCtrl.create(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getBoolean("success") == false);
        assertTrue(ret.getString("error").equals("duplicated_common_name"));
    }

    @Test
    public void testCreateDataServiceExceptionThrown() throws Exception {
        request.setContent("{ \"cn\": \"MYGROUP\", \"description\": \"Description for the group\" }".getBytes());
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

        try {
            groupCtrl.create(request, response);
        } catch (Throwable e) {
            JSONObject ret = new JSONObject(response.getContentAsString());
            assertTrue(e instanceof IOException);
            assertTrue(ret.getBoolean("success") == false);
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * General case for creating group.
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        request.setContent("{ \"cn\": \"MYGROUP\", \"description\": \"Description for the group\" }".getBytes());
        // ensures the mocked object does not return an already existing group
        // Raising NotFoundException instead
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

        groupCtrl.create(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getJSONArray("users").length() == 0);
        assertTrue(ret.getString("cn").equals("MYGROUP"));
        assertTrue(ret.getString("description").equals("Description for the group"));
        assertTrue(response.getStatus() == HttpServletResponse.SC_OK);
    }

	@Test
	public void testDelete() throws Exception {
		request.setRequestURI("/groups/USERS");

		groupCtrl.delete(request, response);

		JSONObject ret = new JSONObject(response.getContentAsString());
		assertTrue(response.getStatus() == HttpServletResponse.SC_OK);
		assertTrue(ret.getBoolean("success") == true);
	}

	@Test
	public void testDeleteADMIN() throws Exception {
		request.setRequestURI("/groups/ADMINISTRATOR");

		groupCtrl.delete(request, response);

		JSONObject ret = new JSONObject(response.getContentAsString());
		assertTrue(response.getStatus() == HttpServletResponse.SC_BAD_REQUEST);
		assertTrue(ret.getBoolean("success") == false);
	}

	@Test
	public void testDeleteSV() throws Exception {
		request.setRequestURI("/groups/GN_123USERS");

		groupCtrl.delete(request, response);

		JSONObject ret = new JSONObject(response.getContentAsString());
		assertTrue(response.getStatus() == HttpServletResponse.SC_BAD_REQUEST);
		assertTrue(ret.getBoolean("success") == false);
	}

	@Test
    public void testDeleteException() throws Exception {
        request.setRequestURI("/groups/ADMINISTRATOR");
        Mockito.doThrow(Exception.class).when(ldapTemplate).unbind((Name) Mockito.any(), Mockito.anyBoolean());

        try {
            groupCtrl.delete(request, response);
        } catch (Throwable e) {
            JSONObject ret = new JSONObject(response.getContentAsString());
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            assertTrue(ret.getBoolean("success") == false);
            assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        request.setRequestURI("/ldapadmin/groups/ADMINISTRATOR");
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());

        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

        groupCtrl.update(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
        assertTrue(ret.getBoolean("success") == false);
        assertTrue(ret.getString("error").equals("not_found"));
    }

    @Test
    public void testUpdateDataServiceExceptionAtLookup() throws Exception {
        request.setRequestURI("/ldapadmin/groups/ADMINISTRATOR");
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());

        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

        try {
            groupCtrl.update(request, response);
        } catch (Throwable e) {
            assertTrue(e instanceof IOException);
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void testUpdateNotFoundAtModification() throws Exception {
        request.setRequestURI("/ldapadmin/groups/ADMINISTRATOR");
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());
        Group retAdmin = GroupFactory.create("ADMINISTRATOR", "administrator group");
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (AttributesMapper) Mockito.any());
        Name dn = LdapNameBuilder.newInstance("ou=roles").add("cn", "newName").build();
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(eq(dn), (ContextMapper) Mockito.any());

        groupCtrl.update(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getString("error").equals("not_found"));
        assertTrue(! ret.getBoolean("success"));
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);

    }

    @Test
    public void testUpdateDuplicateCN() throws Exception {
        request.setRequestURI("/ldapadmin/groups/ADMINISTRATOR");
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());
        Group retAdmin = GroupFactory.create("ADMINISTRATOR", "administrator group");
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);
        Mockito.doThrow(DuplicatedCommonNameException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (AttributesMapper) Mockito.any());
        Name dn = LdapNameBuilder.newInstance("ou=roles").add("cn", "newName").build();
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(eq(dn), (ContextMapper) Mockito.any());

        groupCtrl.update(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(ret.getString("error").equals("duplicated_common_name"));
        assertTrue(! ret.getBoolean("success"));
        assertTrue(response.getStatus() == HttpServletResponse.SC_CONFLICT);
    }

    @Test
    public void testUpdateDataServiceExceptionAtUpdate() throws Exception {
        request.setRequestURI("/ldapadmin/groups/ADMINISTRATOR");
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());
        Group retAdmin = GroupFactory.create("ADMINISTRATOR", "administrator group");
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (AttributesMapper) Mockito.any());
        Name dn = LdapNameBuilder.newInstance("ou=roles").add("cn", "newName").build();
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(eq(dn), (ContextMapper) Mockito.any());

        try {
            groupCtrl.update(request, response);
        } catch (Throwable e) {
            JSONObject ret = new JSONObject(response.getContentAsString());
            assertTrue(e instanceof IOException);
            assertTrue(! ret.getBoolean("success"));
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        request.setRequestURI("/ldapadmin/groups/USERS");
        request.setContent(" { \"cn\": \"newName\", \"description\": \"new Description\" } ".getBytes());
        Group retAdmin = GroupFactory.create("ADMINISTRATOR", "administrator group");
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(retAdmin);
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (AttributesMapper) Mockito.any())).thenReturn(-1);

        Name dn = LdapNameBuilder.newInstance("ou=roles").add("cn", "newName").build();
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(eq(dn), (ContextMapper) Mockito.any());

        groupCtrl.update(request, response);

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
		request.setRequestURI("/ldapadmin/groups_users");
		DirContextOperations context = Mockito.mock(DirContextOperations.class);
		Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookupContext((Name) Mockito.any());

		groupCtrl.updateUsers(request, response);

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
		request.setRequestURI("/ldapadmin/groups_users");
		// Well, this is unlikely to happen in real life, but the only thing
		// we want to test is a JSONException being caught, no matter is the
		// cause.
		Mockito.doThrow(JSONException.class).when(ldapTemplate).lookupContext((Name) Mockito.any());

		try {
			groupCtrl.updateUsers(request, response);
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
		request.setRequestURI("/ldapadmin/groups_users");

		Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookupContext((Name) Mockito.any());

		try {
			groupCtrl.updateUsers(request, response);
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
		request.setRequestURI("/ldapadmin/groups_users");
		DirContextOperations context = Mockito.mock(DirContextOperations.class);
		Mockito.when(ldapTemplate.lookupContext((Name) Mockito.any())).thenReturn(context);

		groupCtrl.updateUsers(request, response);

		JSONObject ret = new JSONObject(response.getContentAsString());
		assertTrue(response.getStatus() == HttpServletResponse.SC_OK);
		assertTrue(ret.getBoolean("success"));
	}

}
