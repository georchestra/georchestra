package org.georchestra.console.ws.backoffice.users;

import org.georchestra.console.dao.AdminLogDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.ds.AccountDaoImpl;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.DuplicatedEmailException;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.ds.RoleDaoImpl;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountFactory;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.dto.UserSchema;
import org.georchestra.console.model.DelegationEntry;
import org.georchestra.console.ws.backoffice.roles.RoleProtected;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
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
import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

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
    private DelegationDao mockDelegationDao;

    @Before
    public void setUp() {
        userRule = new UserRule();
        userRule.setListOfprotectedUsers(new String[] { "geoserver_privileged_user" });

        ldapTemplate = mock(LdapTemplate.class);
        contextSource = mock(LdapContextSource.class);
        roles = mock(RoleProtected.class);
        AdminLogDao logDao = mock(AdminLogDao.class);

        when(contextSource.getBaseLdapPath())
            .thenReturn(new DistinguishedName("dc=georchestra,dc=org"));
        when(ldapTemplate.getContextSource()).thenReturn(contextSource);
        when(roles.isProtected(Mockito.eq("USER"))).thenReturn(true);

        // Configures roleDao
        roleDao = new RoleDaoImpl();
        roleDao.setLdapTemplate(ldapTemplate);
        roleDao.setRoles(this.roles);
        roleDao.setLogDao(logDao);
        roleDao.setRoleSearchBaseDN("ou=roles");
        roleDao.setBasePath("dc=georchestra,dc=org");

        OrgsDao orgsDao = new OrgsDao();
        orgsDao.setLdapTemplate(ldapTemplate);
        orgsDao.setOrgSearchBaseDN("ou=orgs");
        orgsDao.setOrgSearchBaseDN("ou=orgs");
        orgsDao.setBasePath("dc=georchestra,dc=org");

        // configures AccountDao
        dao = new AccountDaoImpl(ldapTemplate);
        dao.setUserSearchBaseDN("ou=users");
        dao.setPendingUserSearchBaseDN("ou=pendingusers");
        dao.setOrgSearchBaseDN("ou=orgs");
        dao.setRoleSearchBaseDN("ou=roles");
        dao.setBasePath("dc=georchestra,dc=org");
        dao.setLogDao(logDao);
        roleDao.setAccountDao(dao);
        orgsDao.setAccountDao(dao);
        usersCtrl = new UsersController(dao, userRule);
        usersCtrl.setOrgDao(orgsDao);
        mockDelegationDao = mock(DelegationDao.class);
        usersCtrl.setDelegationDao(mockDelegationDao);
        usersCtrl.setRoleDao(roleDao);

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
        doThrow(DataServiceException.class).when(ldapTemplate)
                .search(any(Name.class), anyString(), any(SearchControls.class), any(ContextMapper.class));
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
        doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(any(Name.class), any(ContextMapper.class));
        usersCtrl.findByUid("notfounduser");
    }

    @Test(expected = NameNotFoundException.class)
    public void testFindByUidDataServiceException() throws Exception {
        doThrow(DataServiceException.class).when(ldapTemplate).lookup(any(Name.class), any(ContextMapper.class));
        usersCtrl.findByUid("failingUser");
    }

    @Test
    public void testFindByUid() throws Exception {
        Account pmauduit = AccountFactory.createBrief("pmauduit", "monkey123", "Pierre", "Mauduit",
                "pmauduit@localhost", "+33123456789", "developer", "");

        when(ldapTemplate.lookup(any(LdapName.class), eq(UserSchema.ATTR_TO_RETRIEVE), any(ContextMapper.class))).thenReturn(pmauduit);
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
        doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(any(Name.class));

        usersCtrl.create(request);
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
        doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(any(Name.class));
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
        doThrow(DuplicatedEmailException.class).when(ldapTemplate).lookup(any(Name.class));
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
        doThrow(NameNotFoundException.class).when(ldapTemplate).lookup(any(Name.class));
        when(ldapTemplate.search(any(Name.class), anyString(), any(ContextMapper.class)))
            .thenReturn(new ArrayList<>());
        when(ldapTemplate.lookupContext(LdapNameBuilder.newInstance("cn=USER,ou=roles").build()))
            .thenReturn(mock(DirContextOperations.class));

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
        assertEquals(res.isPending(), false);
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdateUserProtected() throws Exception {
        usersCtrl.update("geoserver_privileged_user", request);
    }

	@Test(expected = NameNotFoundException.class)
	public void testUpdateUserNotFound() throws Exception {
		doThrow(NameNotFoundException.class).when(ldapTemplate)
				.lookup(argThat(getMatcherFor("uid=usernotfound,ou=users")), any(ContextMapper.class));
		usersCtrl.update("usernotfound", request);
	}

    @Test(expected = DataServiceException.class)
    public void testUpdateUserDataServiceException() throws Exception {
        doThrow(DataServiceException.class).when(ldapTemplate)
                .lookup(argThat(getMatcherFor("uid=pmauduit,ou=users")), any(String[].class), any(ContextMapper.class));
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
        Mockito.doReturn(fakedAccount).when(ldapTemplate).lookup(any(Name.class), any(ContextMapper.class));
        // Returns the same account when searching it back
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "inetOrgPerson"));
        filter.and(new EqualsFilter("objectClass", "organizationalPerson"));
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new EqualsFilter("mail", "tomcat2@localhost"));

        List<Account> listFakedAccount = new ArrayList<Account>();
        listFakedAccount.add(fakedAccount2);
        Mockito.doReturn(listFakedAccount).when(ldapTemplate).search(argThat(getMatcherFor("ou=users")),
                eq(filter.encode()), any(SearchControls.class), any(ContextMapper.class));

        Mockito.doReturn(fakedAccount).when(ldapTemplate).lookup(any(Name.class),
                eq(UserSchema.ATTR_TO_RETRIEVE), any(ContextMapper.class));

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
        Mockito.doReturn(fakedAccount).when(ldapTemplate).lookup(any(Name.class), eq(UserSchema.ATTR_TO_RETRIEVE), any(ContextMapper.class));
        doThrow(DataServiceException.class).when(ldapTemplate).search(argThat(getMatcherFor("ou=users")),
                eq(mFilter), any(SearchControls.class), any(ContextMapper.class));

        usersCtrl.update("pmauduit", request);

    }

    @Test(expected = JSONException.class)
    public void testUpdateBadJSON() throws Exception {
        request.setContent("{[this is ] } not valid JSON obviously ....".getBytes());
        when(ldapTemplate.lookup(any(Name.class), any(String[].class), any(ContextMapper.class))).thenReturn(
              AccountFactory.createBrief("pmauduit", "monkey123", "Pierre", "Mauduit",
              "pmt@c2c.com", "+123", "developer", "developer"));

        usersCtrl.update("pmauduit", request);
    }

    @Test
    public void testUpdate() throws Exception {
        Mockito.reset(ldapTemplate);
        Mockito.reset(mockDelegationDao);

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
                .put("givenName", "newPierre")
                .put("pending", "true")
                .put("org", "new_org")
                .put("uid", "pMaUdUiT");

        request.setContent(reqUsr.toString().getBytes());

        Account initialState = AccountFactory.createBrief("pmauduit",
                "monkey123",
                "Pierre",
                "pmauduit",
                "pmauduit@georchestra.org",
                "+33123456789",
                "developer & sysadmin",
                "dev&ops");
        initialState.setPending(false);
        initialState.setOrg("psc");

        Mockito.doReturn(initialState).when(ldapTemplate).lookup(any(Name.class), any(String[].class), any(ContextMapper.class));
        // Returns the same account when searching it back
        String mFilter = "(&(objectClass=inetOrgPerson)(objectClass=organizationalPerson)"
                + "(objectClass=person)(mail=tomcat2@localhost))";
        List<Account> listFakedAccount = new ArrayList<Account>();
        listFakedAccount.add(initialState);
        Mockito.doReturn(listFakedAccount).when(ldapTemplate).search(argThat(getMatcherFor("ou=users")),
                eq(mFilter), any(SearchControls.class), any(ContextMapper.class));
        DirContextOperations mockDirCtxForPsc = mock(DirContextOperations.class);
        Mockito.doReturn(mock(DirContextOperations.class)).when(ldapTemplate).lookupContext(argThat(new ArgumentMatcher<Name>() {
            @Override
            public boolean matches(Object o) {
                return o.toString().startsWith("uid=pMaUdUiT,ou=pendingusers");
            }
        }));
        Mockito.doReturn(mockDirCtxForPsc).when(ldapTemplate).lookupContext(argThat(new ArgumentMatcher<Name>() {
            @Override
            public boolean matches(Object o) {
                return o.toString().startsWith("cn=psc");
            }
        }));
        DirContextOperations mockDirCtxForNewOrg = mock(DirContextOperations.class);
        Mockito.doReturn(mockDirCtxForNewOrg).when(ldapTemplate).lookupContext(argThat(new ArgumentMatcher<Name>() {
            @Override
            public boolean matches(Object o) {
                return o.toString().startsWith("cn=new_org");
            }
        }));

        Org initialOrg = new Org();
        initialOrg.setId("psc");
        Org newOrg = new Org();
        newOrg.setId("new_org");

        Mockito.doReturn(initialOrg).when(ldapTemplate).lookup(argThat(new ArgumentMatcher<Name>() {
            @Override
            public boolean matches(Object o) {
                return o.toString().startsWith("cn=psc,ou=orgs");
            }
        }), any(ContextMapper.class));
        Mockito.doReturn(newOrg).when(ldapTemplate).lookup(argThat(new ArgumentMatcher<Name>() {
            @Override
            public boolean matches(Object o) {
                return o.toString().startsWith("cn=new_org,ou=orgs");
            }
        }), any(ContextMapper.class));

        DelegationEntry toBeModified =  new DelegationEntry();
        toBeModified.setUid("dummy");
        Mockito.when(mockDelegationDao.findOne("pmauduit")).thenReturn(toBeModified);

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
        assertEquals("pMaUdUiT", ret.getUid());
        assertEquals(true, ret.isPending());
        assertEquals("new_org", ret.getOrg());

        ArgumentCaptor<String> delDnCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockDirCtxForPsc).removeAttributeValue(anyString(), delDnCaptor.capture());
        ArgumentCaptor<String> addDnCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockDirCtxForNewOrg).addAttributeValue(anyString(), addDnCaptor.capture(), eq(false));

        assertEquals("uid=pmauduit,ou=users,dc=georchestra,dc=org", delDnCaptor.getValue());
        assertEquals("uid=pmauduit,ou=users,dc=georchestra,dc=org", addDnCaptor.getValue());

        Mockito.verify(mockDelegationDao).delete(toBeModified);
        Mockito.verify(mockDelegationDao).save(toBeModified);
        assertEquals("pMaUdUiT", toBeModified.getUid());
    }

    @Test
    public void updateUidChangeButNoAssociatedDelegation() throws Exception {
        Mockito.reset(ldapTemplate);
        Mockito.reset(mockDelegationDao);

        JSONObject reqUsr = new JSONObject()
                .put("sn","newPmauduit")
                .put("uid", "pMaUdUiT")
                .put("org", "psc");

        request.setContent(reqUsr.toString().getBytes());

        Account initialState = AccountFactory.createBrief("pmauduit",
                "monkey123",
                "Pierre",
                "pmauduit",
                "pmauduit@georchestra.org",
                "+33123456789",
                "developer & sysadmin",
                "dev&ops");
        initialState.setPending(false);
        initialState.setOrg("psc");

        Mockito.doReturn(initialState).when(ldapTemplate).lookup(any(Name.class), any(String[].class), any(ContextMapper.class));

        Mockito.doReturn(mock(DirContextOperations.class)).when(ldapTemplate).lookupContext(argThat(new ArgumentMatcher<Name>() {
            @Override
            public boolean matches(Object o) {
                return o.toString().startsWith("uid=pMaUdUiT,ou=users");
            }
        }));


        Mockito.when(mockDelegationDao.findOne("pmauduit")).thenReturn(null);

        usersCtrl.update("pmauduit", request);

        Mockito.verify(mockDelegationDao, never()).delete(anyString());
        Mockito.verify(mockDelegationDao, never()).save(any(DelegationEntry.class));
    }


    @Test
    public void testUpdateEmptyTelephoneNumber() throws Exception {
        Mockito.reset(mockDelegationDao);

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
            lookup(any(Name.class), eq(UserSchema.ATTR_TO_RETRIEVE), any(ContextMapper.class));
        // Returns the same account when searching it back
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "inetOrgPerson"));
        filter.and(new EqualsFilter("objectClass", "organizationalPerson"));
        filter.and(new EqualsFilter("objectClass", "person"));
        filter.and(new EqualsFilter("mail", "tomcat2@localhost"));


        List<Account> listFakedAccount = new ArrayList<Account>();
        listFakedAccount.add(fakedAccount);
        Mockito.doReturn(listFakedAccount).when(ldapTemplate).search(argThat(getMatcherFor("ou=users")),
                eq(filter.encode()), any(SearchControls.class), any(ContextMapper.class));
        Mockito.doReturn(mock(DirContextOperations.class)).
            when(ldapTemplate).lookupContext(any(Name.class));

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

        Mockito.verify(mockDelegationDao, never()).findOne(anyString());
        Mockito.verify(mockDelegationDao, never()).delete(anyString());
        Mockito.verify(mockDelegationDao, never()).save(any(DelegationEntry.class));

    }

    @Test(expected = AccessDeniedException.class)
    public void testDeleteUserProtected() throws Exception {
        Mockito.reset(mockDelegationDao);

        mockLookup("geoserver_privileged_user", false);

        DelegationEntry toBeDeleted =  new DelegationEntry();
        Mockito.when(mockDelegationDao.findOne("geoserver_privileged_user")).thenReturn(toBeDeleted);

        usersCtrl.delete("geoserver_privileged_user", request, response);

        Mockito.verify(mockDelegationDao).delete(toBeDeleted);
        Mockito.verify(mockDelegationDao, never()).save(any(DelegationEntry.class));

    }

    @Test(expected = DataServiceException.class)
    public void testDeleteDataServiceExDataServiceExceptionceptionCaught() throws Exception {
        mockLookup("pmauduit", false);
        doThrow(DataServiceException.class).when(ldapTemplate).unbind(any(Name.class), eq(true));
        usersCtrl.delete("pmauduit", request, response);
    }

    @Test(expected = NameNotFoundException.class)
    public void testDeleteNotFoundExceptionCaught() throws Exception {
        mockLookup("pmauduitnotfound", false);
        doThrow(NameNotFoundException.class).when(ldapTemplate).unbind(any(Name.class), eq(true));
        usersCtrl.delete("pmauduitnotfound", request, response);
    }

    @Test
    public void testResquestProducesDelete() throws Exception {
        mockLookup("pmaudui", false);
        usersCtrl.delete("pmaudui", request, response);
    }

    private void mockLookup(String uuid, boolean pending) {
        Account mockAccount = mock(Account.class);
        when(mockAccount.isPending()).thenReturn(pending);
        when(mockAccount.getUid()).thenReturn(uuid);
        when(ldapTemplate.lookup(any(Name.class), anyObject(), any(AccountDaoImpl.AccountContextMapper.class))).thenReturn(mockAccount);
    }

    private ArgumentMatcher<LdapName> getMatcherFor(final String dn) {
        return new ArgumentMatcher<LdapName>() {
            @Override
            public boolean matches(Object o) {
                return ((LdapName) o).toString().equalsIgnoreCase(dn);
            }
        };
    }
}
