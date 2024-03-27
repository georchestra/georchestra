package org.georchestra.console.ws.changeemail;

import org.georchestra.console.ds.UserTokenDao;
import org.georchestra.console.mailservice.EmailFactory;
import org.georchestra.console.ws.changepassword.ChangePasswordFormBean;
import org.georchestra.console.ws.changepassword.ChangePasswordFormController;
import org.georchestra.console.ws.passwordrecovery.NewPasswordFormController;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.PasswordUtils;
import org.georchestra.console.ws.utils.Validation;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.orgs.OrgsDaoImpl;
import org.georchestra.ds.roles.RoleDaoImpl;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.AccountDaoImpl;
import org.georchestra.ds.users.AccountImpl;
import org.georchestra.ds.users.UserSchema;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.SessionStatus;

import javax.naming.Name;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ChangeEmailControllerTest {

    public static final String NEW_ADDRESS = "new@address.com";
    public static final String OLD_EMAIL = "old@address.com";
    private AccountDao accountDao = Mockito.mock(AccountDao.class);
    private EmailFactory efi = Mockito.mock(EmailFactory.class);
    private UserTokenDao userTokenDao = Mockito.mock(UserTokenDao.class);
    private ChangeEmailFormController ctrlToTest;
    private HttpServletRequest request = new MockHttpServletRequest();
    private HttpServletResponse response = new MockHttpServletResponse();
    private SessionStatus status = Mockito.mock(SessionStatus.class);
    private LogUtils logUtils = Mockito.mock(LogUtils.class);

//    private ChangePasswordFormController ctrlToTest;
    private LdapTemplate ldapTemplate;
    private Model model;
    private ChangeEmailFormBean formBean;
    private BindingResult result;

    @Before
    public void setUp() {
        ldapTemplate = mock(LdapTemplate.class);

        RoleDaoImpl roleDao = new RoleDaoImpl();
        roleDao.setLdapTemplate(ldapTemplate);

        OrgsDaoImpl orgsDao = new OrgsDaoImpl();
        orgsDao.setLdapTemplate(ldapTemplate);
        orgsDao.setOrgSearchBaseDN("ou=orgs");

        AccountDaoImpl dao = new AccountDaoImpl(ldapTemplate);
        dao.setUserSearchBaseDN("ou=users");
        dao.setOrgSearchBaseDN("ou=orgs");
        dao.setOrgSearchBaseDN("ou=orgs");
        dao.setPendingUserSearchBaseDN("ou=pending");
        Validation validation = new Validation("");
        ctrlToTest = new ChangeEmailFormController(accountDao, efi, userTokenDao, validation);
//        ctrlToTest.passwordUtils = new PasswordUtils();
        ctrlToTest.setPublicUrl("https://georchestra.mydomain.org");
        ctrlToTest.setPublicContextPath("/console");
        ctrlToTest.logUtils = logUtils;

        model = mock(Model.class);
        formBean = new ChangeEmailFormBean();
        result = new MapBindingResult(new HashMap<>(), "errors");

//        ctrlToTest.logUtils = mock(LogUtils.class);

        // reset any spring security context that may have been set in previous test.
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
    }

    @Test
    public void initForm() {
        WebDataBinder dataBinder = new WebDataBinder(getClass());
        ctrlToTest.initForm(dataBinder);

        assertTrue(Arrays.asList(dataBinder.getAllowedFields()).contains("newAddress"));
    }

    @Test
    public void setupForm() throws Exception {
        prepareLegitRequest(false);
        userIsSpringSecurityAuthenticatedAndExistInLdap("me");
        String ret = ctrlToTest.setupForm(model);
        assertEquals("changeEmailForm", ret);
    }
    /*
     * @Test public void changePasswordFormInvalid() throws Exception {
     * userIsSpringSecurityAuthenticatedAndExistInLdap("pmauduit");
     * formBean.setNewAddress("monkey12");
     * when(result.hasErrors()).thenReturn(true);
     *
     * String ret = ctrlToTest.changePassword(model, formBean, result);
     *
     * assertEquals("changePasswordForm", ret); }
     */

    private void prepareLegitRequest() throws Exception {
        prepareLegitRequest(false);
    }

    private void prepareLegitRequest(boolean isPending) throws Exception {
        request = new MockHttpServletRequest();
        Account account = Mockito.mock(Account.class);
        Mockito.when(account.getUid()).thenReturn("1");
        Mockito.when(account.isPending()).thenReturn(isPending);
        Mockito.when(accountDao.findByEmail(Mockito.anyString())).thenReturn(account);
        Mockito.when(accountDao.findByUID(Mockito.anyString())).thenReturn(account);
        Mockito.when(userTokenDao.findUserByToken(Mockito.anyString())).thenReturn(NEW_ADDRESS);
    }

    @Test
    public void testSetupForm() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap("pmauduit");

        Model model = Mockito.mock(Model.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getSession()).thenReturn(Mockito.mock(HttpSession.class));

        String ret = ctrlToTest.setupForm(model);
        assertTrue(ret.equals("changeEmailForm"));
    }

    @Test
    public void changeEmailSuccess() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap("pmauduit");

        formBean.setNewAddress(NEW_ADDRESS);
        when(accountDao.findByEmail(any())).thenThrow(new NameNotFoundException(""));
        when(ldapTemplate.lookupContext((Name) any())).thenReturn(mock(DirContextOperations.class));

        String ret = ctrlToTest.changeEmail(request, formBean, result, status);

        assertEquals(false, result.hasErrors());
        assertEquals("emailWasSentForNewEmail", ret);

        ArgumentCaptor<String> tokenUid = ArgumentCaptor.forClass(String.class);
        verify(userTokenDao).insertToken(tokenUid.capture(), Mockito.anyString());
        assertEquals(NEW_ADDRESS, tokenUid.getValue());

        verify(accountDao, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void validateEmailSuccess() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap("pmauduit");

        ctrlToTest.validateEmail("token", response, status);

        assertEquals(HttpServletResponse.SC_FOUND, response.getStatus());
        assertEquals("/console/account/userdetails", response.getHeader("location"));

        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountDao).update(accountArgumentCaptor.capture());
        verify(accountArgumentCaptor.getValue()).setEmail(NEW_ADDRESS);
    }

    @Test
    public void changeEmailToAlreadyUsed() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap("pmauduit");

        formBean.setNewAddress(NEW_ADDRESS);
        Account account = Mockito.mock(Account.class);
        when(accountDao.findByEmail(any())).thenReturn(account);
        when(ldapTemplate.lookupContext((Name) any())).thenReturn(mock(DirContextOperations.class));

        String ret = ctrlToTest.changeEmail(request, formBean, result, status);

        assertEquals(true, result.hasErrors());
        assertEquals("changeEmailForm", ret);

        verifyNoMoreInteractions(userTokenDao);
        verify(accountDao, Mockito.never()).update(Mockito.any());
    }

    @Test
    public void changeEmailToWrongFormat() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap("pmauduit");

        formBean.setNewAddress("wrong_format");
        String ret = ctrlToTest.changeEmail(request, formBean, result, status);

        assertEquals(true, result.hasErrors());
        assertEquals("changeEmailForm", ret);

        verifyNoMoreInteractions(userTokenDao);
        verifyNoMoreInteractions(accountDao);
    }

    @Test
    public void testMakeChangePasswordURLOk() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap("pmauduit");
        String res = ctrlToTest.makeChangeEmailURL("https://georchestra.org", "console", "1234");
        assertEquals(res, "https://georchestra.org/console/account/validateEmail?token=1234");
    }

    private void userIsSpringSecurityAuthenticatedAndExistInLdap(String username) {
        Authentication authentication = mock(Authentication.class);
        final String password = "none";
        UserDetails user = User.builder().username(username).password(password).authorities("USER").build();
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);

        Account a = new AccountImpl();
        a.setUid(username);
        a.setPassword(password);
        a.setEmail(OLD_EMAIL);
        when(ldapTemplate.lookup(any(Name.class), eq(UserSchema.ATTR_TO_RETRIEVE),
                (AccountDaoImpl.AccountContextMapper) any())).thenReturn(a);
        // Mock for password type search in ldap.
        when(ldapTemplate.lookup(any(Name.class), eq(new String[] { "userPassword" }), (ContextMapper<String>) any()))
                .thenReturn("SSHA");
    }
}
