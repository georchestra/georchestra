package org.georchestra.console.ws.backoffice.users;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.model.DelegationEntry;
import org.georchestra.console.ws.backoffice.users.GDPRAccountWorker.DeletedAccountSummary;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.ds.roles.Role;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.roles.RoleProtected;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.AccountFactory;
import org.georchestra.ds.users.DuplicatedEmailException;
import org.georchestra.ds.users.DuplicatedUidException;
import org.georchestra.ds.users.UserRule;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class UsersControllerTest {

    private UsersController usersCtrl;
    private AccountDao dao;
    private RoleDao roleDao;
    private OrgsDao orgsDao;

    private UserRule userRule;
    private RoleProtected roles;
    private GDPRAccountWorker mockGDPR;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private DelegationDao mockDelegationDao;
    private AdvancedDelegationDao mockAdvancedDelegationDao;
    private LogUtils mockLogUtils;

    @Before
    public void setUp() throws DataServiceException {
        dao = mock(AccountDao.class);
        roleDao = mock(RoleDao.class);
        orgsDao = mock(OrgsDao.class);

        userRule = new UserRule();
        userRule.setListOfprotectedUsers(new String[] { "geoserver_privileged_user" });

        roles = mock(RoleProtected.class);
        when(roles.isProtected(Mockito.eq("USER"))).thenReturn(true);

        mockLogUtils = mock(LogUtils.class);

        usersCtrl = new UsersController(dao, userRule);
        usersCtrl.setOrgDao(orgsDao);
        usersCtrl.setRoleDao(roleDao);

        mockDelegationDao = mock(DelegationDao.class);
        mockAdvancedDelegationDao = mock(AdvancedDelegationDao.class);
        usersCtrl.setDelegationDao(mockDelegationDao);
        usersCtrl.setAdvancedDelegationDao(mockAdvancedDelegationDao);

        usersCtrl.logUtils = mockLogUtils;

        mockGDPR = Mockito.mock(GDPRAccountWorker.class);
        when(mockGDPR.deleteAccountRecords(any(Account.class))).thenReturn(DeletedAccountSummary.builder().build());
        usersCtrl.setGdprInfoWorker(mockGDPR);
        usersCtrl.setGdprAllowAccountDeletion(true);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

//      // Set user connected through spring security
        List<GrantedAuthority> role = new LinkedList<GrantedAuthority>();
        role.add(new SimpleGrantedAuthority("ROLE_SUPERUSER"));
        Authentication auth = new PreAuthenticatedAuthenticationToken("testadmin", null, role);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test(expected = DataServiceException.class)
    public void testFindAllException() throws DataServiceException {
        doThrow(DataServiceException.class).when(dao).findFilterBy(any());
        usersCtrl.findAll();
    }

    @Test(expected = NameNotFoundException.class)
    public void testFindByUidEmpty() throws Exception {
        doThrow(NameNotFoundException.class).when(dao).findByUID(eq("nonexistentuser"));
        usersCtrl.findByUid("nonexistentuser");
    }

    @Test(expected = AccessDeniedException.class)
    public void testFindByUidProtected() throws Exception {
        usersCtrl.findByUid("geoserver_privileged_user");
    }

    @Test(expected = NameNotFoundException.class)
    public void testFindByUidNotFound() throws Exception {
        doThrow(NameNotFoundException.class).when(dao).findByUID(eq("notfounduser"));
        usersCtrl.findByUid("notfounduser");
    }

    @Test(expected = DataServiceException.class)
    public void testFindByUidDataServiceException() throws Exception {
        doThrow(DataServiceException.class).when(dao).findByUID(eq("failingUser"));
        usersCtrl.findByUid("failingUser");
    }

    @Test
    public void testFindByUid() throws Exception {
        Account pmauduit = AccountFactory.createBrief("pmauduit", "monkey123", "Pierre", "Mauduit",
                "pmauduit@localhost", "+33123456789", "developer", "");
        mockLookup(pmauduit);

        Account res = usersCtrl.findByUid("pmauduit");
        assertEquals(pmauduit, res);
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreateProtectedUser() throws Exception {
        JSONObject reqUsr = new JSONObject().put("sn", "geoserver privileged user").put("givenName", "geoserver")
                .put("mail", "geoserver@localhost").put("telephoneNumber", "+331234567890")
                .put("facsimileTelephoneNumber", "+33123456788").put("street", "Avenue des Ducs de Savoie")
                .put("postalCode", "73000").put("l", "Chambéry").put("postOfficeBox", "1234").put("o", "GeoServer");
        request.setRequestURI("/console/users/geoserver");
        // geoserver_privileged_user is not a valid username automatically generated
        userRule.setListOfprotectedUsers(new String[] { "geoserver_privileged_user", "ggeoserverprivilegeduser" });
        request.setContent(reqUsr.toString().getBytes());

        usersCtrl.create(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIllegalArgumentException_FirstNameIsRequired() throws Exception {
        JSONObject reqUsr = new JSONObject().put("sn", "geoserver privileged user")
                .put("telephoneNumber", "+331234567890").put("facsimileTelephoneNumber", "+33123456788")
                .put("street", "Avenue des Ducs de Savoie").put("postalCode", "73000").put("l", "Chambéry")
                .put("postOfficeBox", "1234").put("o", "GeoServer");
        request.setRequestURI("/console/users/geoserver");
        request.setContent(reqUsr.toString().getBytes());

        usersCtrl.create(request);
    }

    @Test(expected = DuplicatedEmailException.class)
    public void testCreateDuplicateEmailException() throws Exception {
        JSONObject reqUsr = new JSONObject().put("sn", "geoserver privileged user").put("mail", "tomcat@localhost")
                .put("givenName", "GS Priv User").put("telephoneNumber", "+331234567890")
                .put("facsimileTelephoneNumber", "+33123456788").put("street", "Avenue des Ducs de Savoie")
                .put("postalCode", "73000").put("l", "Chambéry").put("postOfficeBox", "1234").put("o", "GeoServer");
        request.setRequestURI("/console/users/geoserver");
        request.setContent(reqUsr.toString().getBytes());

        doThrow(DuplicatedEmailException.class).when(dao).insert(any());
        usersCtrl.create(request);
    }

    @Test
    @Ignore("not implemented")
    public void testCreateSaslUser() {
        JSONObject reqUsr = new JSONObject().put("sn", "geoserver privileged user").put("mail", "tomcat@localhost")
                .put("givenName", "GS Priv User").put("telephoneNumber", "+331234567890")
                .put("facsimileTelephoneNumber", "+33123456788").put("street", "Avenue des Ducs de Savoie")
                .put("postalCode", "73000").put("l", "Chambéry").put("postOfficeBox", "1234").put("o", "GeoServer");
    }

    @Test
    public void createUser() throws Exception {
        JSONObject reqUsr = new JSONObject().put("sn", "geoserver privileged user").put("mail", "tomcat@localhost")
                .put("givenName", "GS Priv User").put("telephoneNumber", "+331234567890")
                .put("facsimileTelephoneNumber", "+33123456788").put("street", "Avenue des Ducs de Savoie")
                .put("postalCode", "73000").put("l", "Chambéry").put("postOfficeBox", "1234")
                .put("privacyPolicyAgreementDate", "2019-03-12");
        request.setRequestURI("/console/users/geoserver");
        request.setContent(reqUsr.toString().getBytes());

        Account res = usersCtrl.create(request);
        assertNotNull(res);

        verify(dao).insert(notNull(Account.class));

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
        assertFalse(res.isPending());
        assertEquals(res.getPrivacyPolicyAgreementDate(), LocalDate.of(2019, 3, 12));
    }

    @Test
    public void createUserWithOrgDefaultRoles() throws DataServiceException, DuplicatedUidException, IOException, DuplicatedEmailException {
        Role roleA = mock(Role.class);
        when(roleA.getName()).thenReturn("rolea");
        when(roleA.getOrgList()).thenReturn(List.of("testorg"));
        Role roleB = mock(Role.class);
        when(roleB.getName()).thenReturn("roleb");
        when(roleB.getOrgList()).thenReturn(List.of("testorg"));
        Org org = mock(Org.class);
        when(org.getName()).thenReturn("testorg");
        when(orgsDao.findByOrgUniqueId("testorg")).thenReturn(org);
        when(roleDao.findAllForOrg(eq(org))).thenReturn(List.of(roleA, roleB));
        JSONObject reqUsr = new JSONObject().put("sn", "user with role from org").put("mail", "tomcat@localhost")
                .put("org", "testorg")
                .put("givenName", "GS Priv User").put("telephoneNumber", "+331234567890")
                .put("facsimileTelephoneNumber", "+33123456788").put("street", "Avenue des Ducs de Savoie")
                .put("postalCode", "73000").put("l", "Chambéry").put("postOfficeBox", "1234")
                .put("privacyPolicyAgreementDate", "2019-03-12");
        request.setRequestURI("/console/users/geoserver");
        request.setContent(reqUsr.toString().getBytes());

        usersCtrl.create(request);
        Class<List<Account>> accountListClass = (Class<List<Account>>)(Class)List.class;
        Class<List<String>> stringListClass = (Class<List<String>>)(Class)List.class;
        ArgumentCaptor<List<Account>> accountArgumentCaptor = ArgumentCaptor.forClass(accountListClass);
        ArgumentCaptor<List<String>> roleArgumentCaptor = ArgumentCaptor.forClass(stringListClass);
        verify(roleDao).addUsersInRoles(roleArgumentCaptor.capture(), accountArgumentCaptor.capture());

        assertThat(roleArgumentCaptor.getValue(), containsInAnyOrder("rolea", "roleb"));
        assertEquals("guserwithrolefromorg", accountArgumentCaptor.getValue().get(0).getUid());
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdateUserProtected() throws Exception {
        usersCtrl.update("geoserver_privileged_user", request);
    }

    @Test(expected = NameNotFoundException.class)
    public void testUpdateUserNotFound() throws Exception {
        doThrow(NameNotFoundException.class).when(dao).findByUID(eq("usernotfound"));
        usersCtrl.update("usernotfound", request);
    }

    @Test(expected = DataServiceException.class)
    public void testUpdateUserDataServiceException() throws Exception {
        doThrow(DataServiceException.class).when(dao).findByUID(eq("pmauduit"));
        usersCtrl.update("pmauduit", request);
    }

    @Test(expected = DuplicatedEmailException.class)
    public void testUpdateDuplicatedEmailException() throws Exception {

        final String duplicateEmail = "tomcat2@localhost";

        request.setContent(new JSONObject().put("mail", duplicateEmail).toString().getBytes());

        Account originalAccount = AccountFactory.createBrief("pmauduit", "monkey123", "Pierre", "pmauduit",
                "pmauduit@georchestra.org", "+33123456789", "developer & sysadmin", "dev&ops");
        originalAccount.setOrg("psc");
        mockLookup(originalAccount);

        Account expectedModifiedAccount = AccountFactory.create(originalAccount);
        // the clone method above does not clone pwd
        originalAccount.setPassword(null);
        expectedModifiedAccount.setPassword(null);

        assertEquals(originalAccount, expectedModifiedAccount);

        expectedModifiedAccount.setEmail(duplicateEmail);

        doThrow(DuplicatedEmailException.class).when(dao).update(eq(originalAccount), eq(expectedModifiedAccount));
        usersCtrl.update("pmauduit", request);
    }

    @Test(expected = DataServiceException.class)
    public void testUpdateDataServiceExceptionWhileModifying() throws Exception {
        JSONObject reqUsr = new JSONObject().put("mail", "tomcat2@localhost");
        request.setContent(reqUsr.toString().getBytes());
        Account fakedAccount = AccountFactory.createBrief("pmauduit", "monkey123", "Pierre", "pmauduit",
                "pmauduit@georchestra.org", "+33123456789", "developer & sysadmin", "dev&ops");
        mockLookup(fakedAccount);

        doThrow(DataServiceException.class).when(dao).update(eq(fakedAccount), any());
        usersCtrl.update("pmauduit", request);

    }

    @Test(expected = JSONException.class)
    public void testUpdateBadJSON() throws Exception {
        request.setContent("{[this is ] } not valid JSON obviously ....".getBytes());

        mockLookup("pmauduit", false);

        usersCtrl.update("pmauduit", request);
    }

    @Test
    public void testUpdate() throws Exception {
        Mockito.reset(mockDelegationDao);

        JSONObject reqUsr = new JSONObject()//
                .put("sn", "newPmauduit")//
                .put("org", "new_org")//
                .put("postalAddress", "newAddress")//
                .put("postOfficeBox", "newPOBox")//
                .put("postalCode", "73000").put("street", "newStreet")//
                .put("l", "newLocality") // locality
                .put("telephoneNumber", "+33987654321")//
                .put("facsimileTelephoneNumber", "+339182736745")//
                .put("title", "CEO")//
                .put("description", "CEO geOrchestra Corporation")//
                .put("givenName", "newPierre")//
                .put("pending", "true")//
                .put("uid", "pMaUdUiT")//
                .put("privacyPolicyAgreementDate", "");

        request.setContent(reqUsr.toString().getBytes());

        Account initialState = AccountFactory.createBrief("pmauduit", "monkey123", "Pierre", "pmauduit",
                "pmauduit@georchestra.org", "+33123456789", "developer & sysadmin", "dev&ops");
        initialState.setPending(false);
        initialState.setOrg("psc");
        mockLookup(initialState);

        when(dao.hasUserDnChanged(eq(initialState), any())).thenReturn(true);
        when(dao.hasUserLoginChanged(eq(initialState), any())).thenReturn(true);

        Org initialOrg = new Org();
        initialOrg.setId("psc");
        when(orgsDao.findByCommonName(initialOrg.getId())).thenReturn(initialOrg);

        Org newOrg = new Org();
        newOrg.setId("new_org");
        when(orgsDao.findByCommonName(newOrg.getId())).thenReturn(newOrg);

        DelegationEntry toBeModified = new DelegationEntry();
        toBeModified.setUid("dummy");
        when(mockDelegationDao.findFirstByUid("pmauduit")).thenReturn(toBeModified);

        Account ret = usersCtrl.update("pmauduit", request);

        verify(orgsDao).unlinkUser(eq(initialState));
        verify(dao).update(eq(initialState), eq(ret));
        verify(orgsDao).linkUser(eq(ret));
        verify(roleDao).modifyUser(eq(initialState), eq(ret));
        verify(mockDelegationDao).delete(toBeModified);
        verify(mockDelegationDao).save(toBeModified);
        assertEquals("pMaUdUiT", toBeModified.getUid());

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
        assertTrue(ret.isPending());
        assertEquals("new_org", ret.getOrg());
        assertNull(ret.getPrivacyPolicyAgreementDate());
    }

    @Test
    public void updateUidChangeButNoAssociatedDelegation() throws Exception {
        Mockito.reset(mockDelegationDao);

        JSONObject reqUsr = new JSONObject().put("sn", "newPmauduit").put("uid", "pMaUdUiT").put("org", "psc");

        request.setContent(reqUsr.toString().getBytes());

        Account initialState = AccountFactory.createBrief("pmauduit", "monkey123", "Pierre", "pmauduit",
                "pmauduit@georchestra.org", "+33123456789", "developer & sysadmin", "dev&ops");
        initialState.setPending(false);
        initialState.setOrg("psc");
        mockLookup(initialState);

        when(mockDelegationDao.findFirstByUid("pmauduit")).thenReturn(null);

        usersCtrl.update("pmauduit", request);

        verify(mockDelegationDao, never()).deleteById(anyString());
        verify(mockDelegationDao, never()).save(any(DelegationEntry.class));
    }

    @Test
    public void testUpdateEmptyTelephoneNumber() throws Exception {
        Mockito.reset(mockDelegationDao);

        JSONObject reqUsr = new JSONObject().put("sn", "newPmauduit").put("postalAddress", "newAddress")
                .put("postOfficeBox", "newPOBox").put("postalCode", "73000").put("street", "newStreet")
                .put("l", "newLocality") // locality
                .put("telephoneNumber", "").put("facsimileTelephoneNumber", "+339182736745").put("title", "CEO")
                .put("description", "CEO geOrchestra Corporation").put("givenName", "newPierre");
        request.setContent(reqUsr.toString().getBytes());

        Account fakedAccount = AccountFactory.createBrief("pmauduit", "monkey123", "Pierre", "pmauduit",
                "pmauduit@georchestra.org", "+33123456789", "developer & sysadmin", "dev&ops");
        mockLookup(fakedAccount);

        Account ret = usersCtrl.update("pmauduit", request);

        verify(dao).update(eq(fakedAccount), eq(ret));

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

        verify(mockDelegationDao, never()).findFirstByUid(anyString());
        verify(mockDelegationDao, never()).deleteById(anyString());
        verify(mockDelegationDao, never()).save(any(DelegationEntry.class));
    }

    @Test(expected = AccessDeniedException.class)
    public void testDeleteUserProtected() throws Exception {
        Mockito.reset(mockDelegationDao);

        mockLookup("geoserver_privileged_user", false);

        DelegationEntry toBeDeleted = new DelegationEntry();
        when(mockDelegationDao.findFirstByUid("geoserver_privileged_user")).thenReturn(toBeDeleted);

        usersCtrl.delete("geoserver_privileged_user", request, response);

        verify(mockDelegationDao).delete(toBeDeleted);
        verify(mockDelegationDao, never()).save(any(DelegationEntry.class));

    }

    @Test(expected = DataServiceException.class)
    public void testDeleteDataServiceExDataServiceExceptionceptionCaught() throws Exception {
        Account acc = mockLookup("pmauduit", false);
        doThrow(DataServiceException.class).when(dao).delete(eq(acc));
        usersCtrl.delete("pmauduit", request, response);
    }

    @Test(expected = NameNotFoundException.class)
    public void testDeleteNotFoundExceptionCaught() throws Exception {
        Account acc = mockLookup("pmauduitnotfound", false);
        doThrow(NameNotFoundException.class).when(dao).delete(eq(acc));
        usersCtrl.delete("pmauduitnotfound", request, response);
    }

    @Test
    public void testResquestProducesDelete() throws Exception {
        mockLookup("pmaudui", false);
        usersCtrl.delete("pmaudui", request, response);
    }

    @Test
    public void testGetProfileAnonymous() throws Exception {
        // reset any spring security context that may have been set in previous test.
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());

        // Set user connected through spring security
        List<GrantedAuthority> role = new LinkedList<GrantedAuthority>();
        Authentication auth = new PreAuthenticatedAuthenticationToken("testadmin", null, role);
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockLookup("testadmin", false);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String ret = usersCtrl.myProfile(request);

        JSONObject parsed = new JSONObject(ret);
        assertTrue(parsed.getJSONArray("roles").isEmpty());
    }

    @Test
    public void testGetProfileEmptyRole() throws Exception {
        // reset any spring security context that may have been set in previous test.
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());

        MockHttpServletRequest request = new MockHttpServletRequest();

        // Set user connected through spring security
        List<GrantedAuthority> role = new LinkedList<GrantedAuthority>();
        Authentication auth = new PreAuthenticatedAuthenticationToken("testadmin", null, role);
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockLookup("testadmin", false);

        String ret = usersCtrl.myProfile(request);

        JSONObject parsed = new JSONObject(ret);
        assertEquals(parsed.get("uid"), "testadmin");
        assertTrue(parsed.getJSONArray("roles").isEmpty());
    }

    @Test
    public void testGetProfile() throws Exception {
        mockLookup("testadmin", false);
        String ret = usersCtrl.myProfile(request);

        JSONObject parsed = new JSONObject(ret);
        assertEquals(parsed.get("uid"), "testadmin");
        assertTrue(
                parsed.getJSONArray("roles").length() == 1 && parsed.getJSONArray("roles").get(0).equals("SUPERUSER"));
    }

    public void testGDPRDisabled() throws DataServiceException {
        usersCtrl.setGdprAllowAccountDeletion(false);
        usersCtrl.deleteCurrentUserAndGDPRData(response);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    private Account mockLookup(String uuid, boolean pending) throws NameNotFoundException, DataServiceException {
        Account mockAccount = mock(Account.class);
        when(mockAccount.isPending()).thenReturn(pending);
        when(mockAccount.getUid()).thenReturn(uuid);
        mockLookup(mockAccount);
        return mockAccount;
    }

    private void mockLookup(Account mockAccount) throws DataServiceException {
        when(dao.findByUID(eq(mockAccount.getUid()))).thenReturn(mockAccount);
    }
}
