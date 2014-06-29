package org.georchestra.ldapadmin.ws.backoffice.users;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import javax.naming.Name;
import javax.servlet.http.HttpServletResponse;

import org.georchestra.ldapadmin.ds.AccountDaoImpl;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.GroupDaoImpl;
import org.georchestra.ldapadmin.ds.NotFoundException;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.AccountFactory;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class UsersControllerTest {
    private LdapTemplate ldapTemplate ;
    private LdapContextSource contextSource ;

    private UsersController usersCtrl ;
    private AccountDaoImpl dao ;
    private GroupDaoImpl groupDao ;
    private UserRule userRule ;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        userRule = new UserRule();
        userRule.setListOfprotectedUsers(Arrays.asList(new String[] { "geoserver_privileged_user" }));

        ldapTemplate = Mockito.mock(LdapTemplate.class);
        contextSource = Mockito.mock(LdapContextSource.class);

        Mockito.when(contextSource.getBaseLdapPath())
            .thenReturn(new DistinguishedName("dc=georchestra,dc=org"));
        Mockito.when(ldapTemplate.getContextSource()).thenReturn(contextSource);

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

        usersCtrl = new UsersController(dao, userRule);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testFindAllException() throws Exception {
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).search((Name) Mockito.any(),
                Mockito.anyString(), (ContextMapper) Mockito.any());
        try {
            usersCtrl.findAll(request, response);
        } catch (Throwable e) {
            assertTrue(e instanceof IOException);
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void testFindByUidEmpty() throws Exception {

            usersCtrl.findByUid(request, response);

            JSONObject ret = new JSONObject(response.getContentAsString());
            assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
            assertFalse(ret.getBoolean("success"));
            assertTrue(ret.getString("error").equals("not_found_uid_empty"));

    }

    @Test
    public void testFindByUidProtected() throws Exception {
        request.setRequestURI("/ldapadmin/users/geoserver_privileged_user");

        usersCtrl.findByUid(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_CONFLICT);
        assertFalse(ret.getBoolean("success"));
        assertTrue(ret.getString("error").equals("The user is protected: geoserver_privileged_user"));
    }

    @Test
    public void testFindByUidNotFound() throws Exception {
        request.setRequestURI("/ldapadmin/users/notfounduser");
        Mockito.doThrow(NotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

        usersCtrl.findByUid(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
        assertFalse(ret.getBoolean("success"));
        assertTrue(ret.getString("error").equals("not_found"));
    }

    @Test
    public void testFindByUidDataServiceException() throws Exception {
        request.setRequestURI("/ldapadmin/users/failingUser");
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());

        try {
            usersCtrl.findByUid(request, response);
        } catch (Throwable e) {
            assertTrue(e instanceof IOException);
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void testFindByUid() throws Exception {
        request.setRequestURI("/ldapadmin/users/pmauduit");
        Account pmauduit = AccountFactory.createBrief("pmauduit", "monkey123", "Pierre", "Mauduit",
                "pmauduit@localhost", "+33123456789", "geOrchestra Project Steering Committee", "developer", "");
        Mockito.when(ldapTemplate.lookup((Name) Mockito.any(), (ContextMapper) Mockito.any())).thenReturn(pmauduit);

        usersCtrl.findByUid(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_OK);
        assertTrue(ret.getString("uid").equals("pmauduit"));
        assertTrue(ret.getString("mail").equals("pmauduit@localhost"));
        assertTrue(ret.getString("title").equals("developer"));
        assertTrue(ret.getString("sn").equals("Mauduit"));
        assertTrue(ret.getString("description").isEmpty());
        assertTrue(ret.getString("telephoneNumber").equals("+33123456789"));
        assertTrue(ret.getString("o").equals("geOrchestra Project Steering Committee"));
        assertTrue(ret.getString("givenName").equals("Pierre"));
    }

    @Test
    public void testCreateProtectedUser() throws Exception {
        JSONObject reqUsr = new JSONObject().
                put("sn", "geoserver privileged user").
                put("givenName", "geoserver").
                put("mail", "geoserver@localhost").
                put("telephoneNumber", "+331234567890").
                put("facsimileTelephoneNumber", "+33123456788").
                put("street", "Avenue des Ducs de Savoie").
                put("postalCode", "73000").
                put("l", "Chambéry").
                put("postOfficeBox", "1234").
                put("o", "GeoServer");
        request.setRequestURI("/ldapadmin/users/geoserver");
        // geoserver_privileged_user is not a valid username automatically generated
        userRule.setListOfprotectedUsers(Arrays.asList(new String[]{"geoserver_privileged_user", "ggeoserverprivilegeduser"}));
        request.setContent(reqUsr.toString().getBytes());
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any());

        usersCtrl.create(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_CONFLICT);
        assertFalse(ret.getBoolean("success"));
        assertTrue(ret.getString("error").equals("The user is protected: ggeoserverprivilegeduser"));
    }

    @Test
    public void testCreateIllegalArgumentException() throws Exception {
        JSONObject reqUsr = new JSONObject().
                put("sn", "geoserver privileged user").
                put("telephoneNumber", "+331234567890").
                put("facsimileTelephoneNumber", "+33123456788").
                put("street", "Avenue des Ducs de Savoie").
                put("postalCode", "73000").
                put("l", "Chambéry").
                put("postOfficeBox", "1234").
                put("o", "GeoServer");
        request.setRequestURI("/ldapadmin/users/geoserver");
        request.setContent(reqUsr.toString().getBytes());
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any());

        usersCtrl.create(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_CONFLICT);
        assertFalse(ret.getBoolean("success"));
        assertTrue(ret.getString("error").equals("givenName is required"));
    }

    @Test
    public void testCreateDuplicateEmailException() throws Exception {

    }

    @Test
    public void testCreateDataServiceException() throws Exception {

    }

}
