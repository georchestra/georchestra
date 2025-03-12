package org.georchestra.console.ws.changeemail;

import org.georchestra.console.ds.UserTokenDao;
import org.georchestra.console.mailservice.EmailFactory;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.Validation;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.AccountDaoImpl;
import org.georchestra.ds.users.AccountImpl;
import org.georchestra.ds.users.UserSchema;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ChangeEmailControllerTest {

    public static final String NEW_EMAIL = "new@address.com";
    public static final String OLD_EMAIL = "old@address.com";
    public static final String ACCOUNT_UID = "pmauduit";
    private AccountDao accountDao = mock(AccountDao.class);
    private EmailFactory efi = mock(EmailFactory.class);
    private UserTokenDao userTokenDao = mock(UserTokenDao.class);
    private ChangeEmailFormController ctrlToTest;
    private HttpServletRequest request = new MockHttpServletRequest();
    private HttpServletResponse response = new MockHttpServletResponse();
    private SessionStatus status = mock(SessionStatus.class);
    private LogUtils logUtils = mock(LogUtils.class);

    private LdapTemplate ldapTemplate;
    private Model model;
    private ChangeEmailFormBean formBean;
    private BindingResult result;

    @Before
    public void setUp() {
        ldapTemplate = mock(LdapTemplate.class);

        Validation validation = new Validation("");
        ctrlToTest = new ChangeEmailFormController(accountDao, efi, userTokenDao, validation);
        ctrlToTest.setPublicUrl("https://georchestra.mydomain.org");
        ctrlToTest.setPublicContextPath("/console");
        ctrlToTest.logUtils = logUtils;

        model = mock(Model.class);
        formBean = new ChangeEmailFormBean();
        result = new MapBindingResult(new HashMap<>(), "errors");

        // reset any spring security context that may have been set in previous test.
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
    }

    @Test
    public void initForm() {
        WebDataBinder dataBinder = new WebDataBinder(getClass());
        ctrlToTest.initForm(dataBinder);

        assertTrue(Arrays.asList(dataBinder.getAllowedFields()).contains("newEmail"));
    }

    @Test
    public void setupForm() throws Exception {
        prepareLegitRequest(false);
        userIsSpringSecurityAuthenticatedAndExistInLdap("me");
        String ret = ctrlToTest.setupForm(model);
        assertEquals("changeEmailForm", ret);
    }

    private void prepareLegitRequest() throws Exception {
        prepareLegitRequest(false);
    }

    private void prepareLegitRequest(boolean isPending) throws Exception {
        request = new MockHttpServletRequest();
        Account account = mock(Account.class);
        when(account.getUid()).thenReturn(ACCOUNT_UID);
        when(account.isPending()).thenReturn(isPending);
        when(accountDao.findByEmail(anyString())).thenReturn(account);
        when(accountDao.findByUID(anyString())).thenReturn(account);
        when(userTokenDao.findAdditionalInfo(anyString(), anyString())).thenReturn(NEW_EMAIL);
    }

    @Test
    public void testSetupForm() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap(ACCOUNT_UID);

        Model model = mock(Model.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class));

        String ret = ctrlToTest.setupForm(model);
        assertTrue(ret.equals("changeEmailForm"));
    }

    @Test
    public void changeEmailSuccess() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap(ACCOUNT_UID);

        formBean.setNewEmail(NEW_EMAIL);
        when(accountDao.findByEmail(any())).thenThrow(new NameNotFoundException(""));
        when(ldapTemplate.lookupContext((Name) any())).thenReturn(mock(DirContextOperations.class));

        String ret = ctrlToTest.changeEmail(request, formBean, result, status);

        assertEquals(false, result.hasErrors());
        assertEquals("emailWasSentForEmailChange", ret);

        ArgumentCaptor<String> tokenUid = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> tokenInfo = ArgumentCaptor.forClass(String.class);
        verify(userTokenDao).insertToken(tokenUid.capture(), anyString(), tokenInfo.capture());
        assertEquals(ACCOUNT_UID, tokenUid.getValue());
        assertEquals(NEW_EMAIL, tokenInfo.getValue());

        verify(accountDao, never()).update(any());
    }

    @Test
    public void changeEmailToAlreadyUsed() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap(ACCOUNT_UID);

        formBean.setNewEmail(NEW_EMAIL);
        Account account = mock(Account.class);
        when(accountDao.findByEmail(any())).thenReturn(account);
        when(ldapTemplate.lookupContext((Name) any())).thenReturn(mock(DirContextOperations.class));

        String ret = ctrlToTest.changeEmail(request, formBean, result, status);

        assertEquals(true, result.hasErrors());
        assertEquals("changeEmailForm", ret);

        verifyNoMoreInteractions(userTokenDao);
        verify(accountDao, never()).update(any());
    }

    @Test
    public void changeEmailToWrongFormat() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap(ACCOUNT_UID);

        formBean.setNewEmail("wrong_format");
        String ret = ctrlToTest.changeEmail(request, formBean, result, status);

        assertEquals(true, result.hasErrors());
        assertEquals("changeEmailForm", ret);

        verifyNoMoreInteractions(userTokenDao);
        verifyNoMoreInteractions(accountDao);
    }

    @Test
    public void validateEmailSuccess() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap(ACCOUNT_UID);

        ctrlToTest.validateEmail("token", response, status);

        assertEquals(HttpServletResponse.SC_FOUND, response.getStatus());
        assertEquals("/console/account/userdetails", response.getHeader("location"));

        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountDao).update(accountArgumentCaptor.capture());
        verify(accountArgumentCaptor.getValue()).setEmail(NEW_EMAIL);
    }

    @Test
    public void validateEmailWrongToken() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap(ACCOUNT_UID);
        when(userTokenDao.findAdditionalInfo(anyString(), anyString())).thenThrow(new NameNotFoundException(""));

        ctrlToTest.validateEmail("token", response, status);

        assertEquals(HttpServletResponse.SC_FOUND, response.getStatus());
        assertEquals("/console/account/userdetails", response.getHeader("location"));

        verify(accountDao, never()).update(any());
    }

    @Test
    public void testMakeChangePasswordURLOk() throws Exception {
        prepareLegitRequest();
        userIsSpringSecurityAuthenticatedAndExistInLdap(ACCOUNT_UID);
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
