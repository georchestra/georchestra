package org.georchestra.console.ws.backoffice.users;

import org.georchestra.console.dao.AdminLogDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.ds.AccountDaoImpl;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.DuplicatedEmailException;
import org.georchestra.console.ds.RoleDaoImpl;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountFactory;
import org.georchestra.console.dto.UserSchema;
import org.georchestra.console.ws.backoffice.roles.RoleProtected;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
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
import javax.naming.directory.SearchControls;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;

public class UsersControllerTest {
    private LdapTemplate ldapTemplate;
    private LdapContextSource contextSource;

    private UsersController usersCtrl;
    private AccountDaoImpl dao;
    private RoleDaoImpl roleDao;
    private UserRule userRule;
    private RoleProtected roles;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        userRule = new UserRule();
        userRule.setListOfprotectedUsers(new String[] { "geoserver_privileged_user" });

        ldapTemplate = Mockito.mock(LdapTemplate.class);
        contextSource = Mockito.mock(LdapContextSource.class);
        roles = Mockito.mock(RoleProtected.class);
        AdminLogDao logDao = Mockito.mock(AdminLogDao.class);

        Mockito.when(contextSource.getBaseLdapPath())
            .thenReturn(new DistinguishedName("dc=georchestra,dc=org"));
        Mockito.when(ldapTemplate.getContextSource()).thenReturn(contextSource);
        Mockito.when(roles.isProtected(Mockito.eq("USER"))).thenReturn(true);

        // Configures roleDao
        roleDao = new RoleDaoImpl();
        roleDao.setLdapTemplate(ldapTemplate);
        roleDao.setRoleSearchBaseDN("ou=roles");
        roleDao.setUniqueNumberField("ou");
        roleDao.setUserSearchBaseDN("ou=users");
        roleDao.setRoles(this.roles);
        roleDao.setLogDao(logDao);

        OrgsDao orgsDao = new OrgsDao();
        orgsDao.setLdapTemplate(ldapTemplate);
        orgsDao.setOrgSearchBaseDN("ou=orgs");
        orgsDao.setUserSearchBaseDN("ou=users");


        // configures AccountDao
        dao = new AccountDaoImpl(ldapTemplate, roleDao, orgsDao);
        dao.setUniqueNumberField("employeeNumber");
        dao.setUserSearchBaseDN("ou=users");
        dao.setRoleDao(roleDao);
        dao.setLogDao(logDao);

        usersCtrl = new UsersController(dao, userRule);
        usersCtrl.setOrgDao(orgsDao);
        usersCtrl.setDelegationDao(Mockito.mock(DelegationDao.class));

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

    @Test(expected = DataServiceException.class)
    public void testFindAllException() throws DataServiceException {
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate)
                .search(any(DistinguishedName.class), anyString(), any(SearchControls.class), any(ContextMapper.class));
        usersCtrl.findAll();
    }

	@Test(expected = NameNotFoundException.class)
	public void testFindByUidEmpty() throws Exception {
		usersCtrl.findByUid("nonexistentuser");
    }

    @Test(expected = AccessDeniedException.class)
    public void testFindByUidProtected() throws Exception {
        usersCtrl.findByUid("geoserver_privileged_user");
    }

    @Test(expected = NameNotFoundException.class)
    public void testFindByUidNotFound() throws Exception {
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());
        usersCtrl.findByUid("notfounduser");
    }

    @Test(expected = NameNotFoundException.class)
    public void testFindByUidDataServiceException() throws Exception {
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());
        usersCtrl.findByUid("failingUser");
    }

    @Test
    public void testFindByUid() throws Exception {
        Account pmauduit = AccountFactory.createBrief("pmauduit", "monkey123", "Pierre", "Mauduit",
                "pmauduit@localhost", "+33123456789", "developer", "");

        Mockito.when(ldapTemplate.lookup(any(DistinguishedName.class), eq(UserSchema.ATTR_TO_RETRIEVE), any(ContextMapper.class))).thenReturn(pmauduit);
        Account res = usersCtrl.findByUid("pmauduit");
        assertEquals(pmauduit, res);
    }

    @Test(expected = AccessDeniedException.class)
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
        request.setRequestURI("/console/users/geoserver");
        // geoserver_privileged_user is not a valid username automatically generated
        userRule.setListOfprotectedUsers(new String[]{"geoserver_privileged_user", "ggeoserverprivilegeduser"});
        request.setContent(reqUsr.toString().getBytes());
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any());

        Account res = usersCtrl.create(request);
    }

    @Test(expected = IllegalArgumentException.class)
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
        request.setRequestURI("/console/users/geoserver");
        request.setContent(reqUsr.toString().getBytes());
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any());
        usersCtrl.create(request);
    }

    @Test(expected = DuplicatedEmailException.class)
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
        request.setRequestURI("/console/users/geoserver");
        request.setContent(reqUsr.toString().getBytes());
        Mockito.doThrow(DuplicatedEmailException.class).when(ldapTemplate).lookup((Name) Mockito.any());
        usersCtrl.create(request);
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
                put("postOfficeBox", "1234");
        request.setRequestURI("/console/users/geoserver");
        request.setContent(reqUsr.toString().getBytes());
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).lookup((Name) Mockito.any());
        Mockito.when(ldapTemplate.search((Name) Mockito.any(), Mockito.anyString(),(ContextMapper) Mockito.any()))
            .thenReturn(new ArrayList<Object>());
        Mockito.when(ldapTemplate.lookupContext(LdapNameBuilder.newInstance("cn=USER,ou=roles").build()))
            .thenReturn(Mockito.mock(DirContextOperations.class));

        Account res = usersCtrl.create(request);

        assertEquals(res.getUid(), "ggeoserverprivilegeduser");
        assertEquals(res.getEmail(), "tomcat@localhost");
        assertEquals(res.getSurname(), "geoserver privileged user");
        assertEquals(res.getFacsimile(), "+33123456788");
        assertEquals(res.getStreet(), "Avenue des Ducs de Savoie");
        assertEquals(res.getLocality(), "Chambéry");
        assertEquals(res.getGivenName(), "GS Priv User");
        assertEquals(res.getPostalCode(), "73000");
        assertEquals(res.getRoomNumber(), "");
        assertEquals(res.getPhone(), "+331234567890");
        assertEquals(res.getPhysicalDeliveryOfficeName(), "");
        assertEquals(res.getStateOrProvince(), "");
        assertEquals(res.getPostOfficeBox(), "1234");
        assertEquals(res.getMobile(), "");
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdateUserProtected() throws Exception {
        usersCtrl.update("geoserver_privileged_user", request);
    }

	@Test(expected = NameNotFoundException.class)
	public void testUpdateUserNotFound() throws Exception {
		Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate)
				.lookup(eq(new DistinguishedName("uid=usernotfound,ou=users")), Mockito.any(ContextMapper.class));
		usersCtrl.update("usernotfound", request);
	}

    @Test(expected = DataServiceException.class)
    public void testUpdateUserDataServiceException() throws Exception {
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate)
                .lookup(eq(new DistinguishedName("uid=pmauduit,ou=users")), any(String[].class), any(ContextMapper.class));
        usersCtrl.update("pmauduit", request);
    }

    @Test(expected = DuplicatedEmailException.class)
    public void testUpdateDuplicatedEmailException() throws Exception {
        JSONObject reqUsr = new JSONObject().put("mail","tomcat2@localhost");
        request.setContent(reqUsr.toString().getBytes());
        Account fakedAccount = AccountFactory.createBrief("pmauduit", "monkey123", "Pierre",
                "pmauduit", "pmauduit@georchestra.org", "+33123456789",
                "developer & sysadmin", "dev&ops");
        Account fakedAccount2 = AccountFactory.createBrief("pmauduit2", "monkey123", "Pierre",
                "pmauduit", "tomcat2@localhost", "+33123456789",
                "developer & sysadmin", "dev&ops");
        Mockito.doReturn(fakedAccount).when(ldapTemplate).lookup((Name) Mockito.any(), (ContextMapper) Mockito.any());
        // Returns the same account when searching it back
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "inetOrgPerson"));
        filter.and(new EqualsFilter("objectClass", "organizationalPerson"));
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new EqualsFilter("mail", "tomcat2@localhost"));

        List<Account> listFakedAccount = new ArrayList<Account>();
        listFakedAccount.add(fakedAccount2);
        Mockito.doReturn(listFakedAccount).when(ldapTemplate).search(eq(DistinguishedName.EMPTY_PATH),
                eq(filter.encode()), Mockito.any(SearchControls.class), (ContextMapper) Mockito.any());

        Mockito.doReturn(fakedAccount).when(ldapTemplate).lookup(Mockito.any(DistinguishedName.class),
                eq(UserSchema.ATTR_TO_RETRIEVE), (ContextMapper) Mockito.any());

        usersCtrl.update("pmauduit", request);
    }

    @Test(expected = DataServiceException.class)
    public void testUpdateDataServiceExceptionWhileModifying() throws Exception {
        JSONObject reqUsr = new JSONObject().put("mail","tomcat2@localhost");
        request.setContent(reqUsr.toString().getBytes());
        Account fakedAccount = AccountFactory.createBrief("pmauduit", "monkey123", "Pierre",
                "pmauduit", "pmauduit@georchestra.org", "+33123456789",
                "developer & sysadmin", "dev&ops");
        String mFilter = "(&(objectClass=inetOrgPerson)(objectClass=organizationalPerson)"
                + "(objectClass=person)(mail=tomcat2@localhost))";
        Mockito.doReturn(fakedAccount).when(ldapTemplate).lookup((Name) Mockito.any(), eq(UserSchema.ATTR_TO_RETRIEVE), (ContextMapper) Mockito.any());
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).search(eq(DistinguishedName.EMPTY_PATH),
                eq(mFilter),(SearchControls) Mockito.any(), (ContextMapper) Mockito.any());

        usersCtrl.update("pmauduit", request);

    }

    @Test(expected = JSONException.class)
    public void testUpdateBadJSON() throws Exception {
        request.setContent("{[this is ] } not valid JSON obviously ....".getBytes());
        Mockito.when(ldapTemplate.lookup(any(Name.class), any(String[].class), any(ContextMapper.class))).thenReturn(
              AccountFactory.createBrief("pmauduit", "monkey123", "Pierre", "Mauduit",
              "pmt@c2c.com", "+123", "developer", "developer"));

        usersCtrl.update("pmauduit", request);
    }

    @Test
    public void testUpdate() throws Exception {
        JSONObject reqUsr = new JSONObject().put("sn","newPmauduit")
                .put("postalAddress", "newAddress")
                .put("postOfficeBox", "newPOBox")
                .put("postalCode", "73000")
                .put("street", "newStreet")
                .put("l", "newLocality") // locality
                .put("telephoneNumber", "+33987654321")
                .put("facsimileTelephoneNumber", "+339182736745")
                .put("title", "CEO")
                .put("description", "CEO geOrchestra Corporation")
                .put("givenName", "newPierre");

        request.setContent(reqUsr.toString().getBytes());
        Account fakedAccount = AccountFactory.createBrief("pmauduit",
                "monkey123",
                "Pierre",
                "pmauduit",
                "pmauduit@georchestra.org",
                "+33123456789",
                "developer & sysadmin",
                "dev&ops");
        Mockito.doReturn(fakedAccount).when(ldapTemplate).lookup((Name) Mockito.any(), (String[]) Mockito.any(), (ContextMapper) Mockito.any());
        // Returns the same account when searching it back
        String mFilter = "(&(objectClass=inetOrgPerson)(objectClass=organizationalPerson)"
                + "(objectClass=person)(mail=tomcat2@localhost))";
        List<Account> listFakedAccount = new ArrayList<Account>();
        listFakedAccount.add(fakedAccount);
        Mockito.doReturn(listFakedAccount).when(ldapTemplate).search(eq(DistinguishedName.EMPTY_PATH),
                eq(mFilter), (SearchControls) Mockito.any(), (ContextMapper) Mockito.any());
        Mockito.doReturn(Mockito.mock(DirContextOperations.class)).when(ldapTemplate).lookupContext((Name) Mockito.any());

        Account ret = usersCtrl.update("pmauduit", request);

        // Add missing param in request
        assertEquals("newPmauduit", ret.getSurname());
        assertEquals("newAddress", ret.getPostalAddress());
        assertEquals("newPOBox", ret.getPostOfficeBox());
        assertEquals("73000", ret.getPostalCode());
        assertEquals("newStreet", ret.getStreet());
        assertEquals("newLocality", ret.getLocality());
        assertEquals("+33987654321", ret.getPhone());
        assertEquals("+339182736745", ret.getFacsimile());
        assertEquals("CEO", ret.getTitle());
        assertEquals("CEO geOrchestra Corporation", ret.getDescription());
        assertEquals("newPierre", ret.getGivenName());
        assertEquals("pmauduit@georchestra.org", ret.getEmail());
        assertEquals("newPierre newPmauduit", ret.getCommonName());
        assertEquals("pmauduit", ret.getUid());
        assertEquals("", ret.getOrg());

    }

    @Test
    public void testUpdateEmptyTelephoneNumber() throws Exception {
        JSONObject reqUsr = new JSONObject().put("sn","newPmauduit")
                .put("postalAddress", "newAddress")
                .put("postOfficeBox", "newPOBox")
                .put("postalCode", "73000")
                .put("street", "newStreet")
                .put("l", "newLocality") // locality
                .put("telephoneNumber", "")
                .put("facsimileTelephoneNumber", "+339182736745")
                .put("title", "CEO")
                .put("description", "CEO geOrchestra Corporation")
                .put("givenName", "newPierre");
        request.setContent(reqUsr.toString().getBytes());
        Account fakedAccount = AccountFactory.createBrief("pmauduit", "monkey123", "Pierre",
                "pmauduit", "pmauduit@georchestra.org", "+33123456789",
                "developer & sysadmin", "dev&ops");
        Mockito.doReturn(fakedAccount).when(ldapTemplate).
            lookup((Name) Mockito.any(), eq(UserSchema.ATTR_TO_RETRIEVE), (ContextMapper) Mockito.any());
        // Returns the same account when searching it back
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "inetOrgPerson"));
        filter.and(new EqualsFilter("objectClass", "organizationalPerson"));
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new EqualsFilter("mail", "tomcat2@localhost"));


        List<Account> listFakedAccount = new ArrayList<Account>();
        listFakedAccount.add(fakedAccount);
        Mockito.doReturn(listFakedAccount).when(ldapTemplate).search(eq(DistinguishedName.EMPTY_PATH),
                eq(filter.encode()), Mockito.any(SearchControls.class), (ContextMapper) Mockito.any());
        Mockito.doReturn(Mockito.mock(DirContextOperations.class)).
            when(ldapTemplate).lookupContext((Name) Mockito.any());

        Account ret = usersCtrl.update("pmauduit", request);

        assertEquals("", ret.getPhone());
        assertEquals("newPmauduit", ret.getSurname());
        assertEquals("newAddress", ret.getPostalAddress());
        assertEquals("newPOBox", ret.getPostOfficeBox());
        assertEquals("73000", ret.getPostalCode());
        assertEquals("newStreet", ret.getStreet());
        assertEquals("newLocality", ret.getLocality());
        assertEquals("", ret.getPhone());
        assertEquals("+339182736745", ret.getFacsimile());
        assertEquals("CEO", ret.getTitle());
        assertEquals("CEO geOrchestra Corporation", ret.getDescription());
        assertEquals("newPierre", ret.getGivenName());
        assertEquals("pmauduit@georchestra.org", ret.getEmail());
        assertEquals("newPierre newPmauduit", ret.getCommonName());
        assertEquals("pmauduit", ret.getUid());
        assertEquals("", ret.getOrg());
    }

    @Test(expected = AccessDeniedException.class)
    public void testDeleteUserProtected() throws Exception {
        usersCtrl.delete("geoserver_privileged_user", request, response);
    }

    @Test(expected = DataServiceException.class)
    public void testDeleteDataServiceExDataServiceExceptionceptionCaught() throws Exception {
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).unbind((Name) Mockito.any(), eq(true));
        usersCtrl.delete("pmauduit", request, response);
    }

    @Test(expected = NameNotFoundException.class)
    public void testDeleteNotFoundExceptionCaught() throws Exception {
        Mockito.doThrow(NameNotFoundException.class).when(ldapTemplate).unbind((Name) Mockito.any(), eq(true));
        usersCtrl.delete("pmauduitnotfound", request, response);
    }

    @Test
    public void testResquestProducesDelete() throws Exception {
        usersCtrl.delete("pmaudui", request, response);
    }

}
