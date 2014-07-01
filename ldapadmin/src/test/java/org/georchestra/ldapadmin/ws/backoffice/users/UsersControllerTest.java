package org.georchestra.ldapadmin.ws.backoffice.users;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.naming.Name;
import javax.servlet.http.HttpServletResponse;

import org.georchestra.ldapadmin.ds.AccountDaoImpl;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.DuplicatedEmailException;
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
import org.springframework.ldap.core.DirContextOperations;
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
        JSONObject reqUsr = new JSONObject().
                put("sn", "geoserver privileged user").
                put("mail","tomcat@localhost").
                put("givenName", "GS Priv User").
                put("telephoneNumber", "+331234567890").
                put("facsimileTelephoneNumber", "+33123456788").
                put("street", "Avenue des Ducs de Savoie").
                put("postalCode", "73000").
                put("l", "Chambéry").
                put("postOfficeBox", "1234").
                put("o", "GeoServer");
        request.setRequestURI("/ldapadmin/users/geoserver");
        request.setContent(reqUsr.toString().getBytes());
        Mockito.doThrow(DuplicatedEmailException.class).when(ldapTemplate).lookup((Name) Mockito.any());

        usersCtrl.create(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_CONFLICT);
        assertFalse(ret.getBoolean("success"));
        assertTrue(ret.getString("error").equals("duplicated_email"));
    }

    @Test
    public void testCreateDataServiceException() throws Exception {
        JSONObject reqUsr = new JSONObject().
                put("sn", "geoserver privileged user").
                put("mail","tomcat@localhost").
                put("givenName", "GS Priv User").
                put("telephoneNumber", "+331234567890").
                put("facsimileTelephoneNumber", "+33123456788").
                put("street", "Avenue des Ducs de Savoie").
                put("postalCode", "73000").
                put("l", "Chambéry").
                put("postOfficeBox", "1234").
                put("o", "GeoServer");
        request.setRequestURI("/ldapadmin/users/geoserver");
        request.setContent(reqUsr.toString().getBytes());
        //Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any());
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any());
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());


        try {
            usersCtrl.create(request, response);
        } catch (Throwable e) {
            JSONObject ret = new JSONObject(response.getContentAsString());
            assertTrue(e instanceof IOException);
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            assertFalse(ret.getBoolean("success"));
        }
    }


    @Test
    public void createUser() throws Exception {
        JSONObject reqUsr = new JSONObject().
                put("sn", "geoserver privileged user").
                put("mail","tomcat@localhost").
                put("givenName", "GS Priv User").
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
        // TODO: Why 2 different codes checking that
        // the user exists the same way, but 2 different ways ?
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());


        Mockito.when(ldapTemplate.search((Name) Mockito.any(), Mockito.anyString(),(ContextMapper) Mockito.any()))
            .thenReturn(new ArrayList());
        Mockito.when(ldapTemplate.lookupContext(new DistinguishedName("cn=SV_USER,ou=groups")))
            .thenReturn(Mockito.mock(DirContextOperations.class));

        usersCtrl.create(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_OK);

        assertTrue(ret.getString("uid").equals("ggeoserverprivilegeduser"));
        assertTrue(ret.getString("mail").equals("tomcat@localhost"));
        assertTrue(ret.getString("sn").equals("geoserver privileged user"));
        assertTrue(ret.getString("ou").equals(""));
        assertTrue(ret.getString("facsimileTelephoneNumber").equals("+33123456788"));
        assertTrue(ret.getString("street").equals("Avenue des Ducs de Savoie"));
        assertTrue(ret.getString("o").equals("GeoServer"));
        assertTrue(ret.getString("l").equals("Chambéry"));
        assertTrue(ret.getString("givenName").equals("GS Priv User"));
        assertTrue(ret.getString("postalCode").equals("73000"));
        assertTrue(ret.getString("roomNumber").equals(""));
        assertTrue(ret.getString("telephoneNumber").equals("+331234567890"));
        assertTrue(ret.getString("physicalDeliveryOfficeName").equals(""));
        assertTrue(ret.getString("st").equals(""));
        assertTrue(ret.getString("postOfficeBox").equals("1234"));
        assertTrue(ret.getString("mobile").equals(""));

    }

    @Test
    public void testUpdateUserProtected() throws Exception {
        request.setRequestURI("/ldapadmin/users/geoserver_privileged_user");

        usersCtrl.update(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_CONFLICT);
        assertFalse(ret.getBoolean("success"));
        assertTrue(ret.getString("error").equals("The user is protected, it cannot be updated: geoserver_privileged_user"));
    }

    @Test
    public void testUpdateUserNotFound() throws Exception {
        request.setRequestURI("/ldapadmin/users/usernotfound");

        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate)
            .lookup(eq(new DistinguishedName("uid=usernotfound,ou=users")), (ContextMapper) Mockito.any());

        usersCtrl.update(request, response);

        JSONObject ret = new JSONObject(response.getContentAsString());
        assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
        assertFalse(ret.getBoolean("success"));
        assertTrue(ret.getString("error").equals("not_found"));
    }

    @Test
    public void testUpdateUserDataServiceException() throws Exception {
        request.setRequestURI("/ldapadmin/users/pmauduit");

        Mockito.doThrow(DataServiceException.class).when(ldapTemplate)
            .lookup(eq(new DistinguishedName("uid=pmauduit,ou=users")), (ContextMapper) Mockito.any());

        try {
            usersCtrl.update(request, response);
        } catch (Throwable e) {
            assertTrue(e instanceof IOException);
            assertTrue(response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void testUpdateDuplicatedEmailException() throws Exception {
        request.setRequestURI("/ldapadmin/users/pmauduit");
        JSONObject reqUsr = new JSONObject().put("mail","tomcat2@localhost");
        request.setContent(reqUsr.toString().getBytes());


        usersCtrl.update(request, response);

    }

    @Test
    public void testUpdateDataServiceExceptionWhileModifying() throws Exception {

    }


    @Test
    public void testUpdate() throws Exception {

    }

}
