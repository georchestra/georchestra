package org.georchestra.console.ws.changepassword;

import org.georchestra.console.ds.AccountDaoImpl;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.ds.RoleDaoImpl;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountImpl;
import org.georchestra.console.dto.UserSchema;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.PasswordUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;

import javax.naming.Name;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangePasswordControllerTest {

    private ChangePasswordFormController ctrlToTest;
    private LdapTemplate ldapTemplate;
    private Model model;
    private ChangePasswordFormBean formBean;
    private BindingResult result;

    @Before
    public void setUp() {
        ldapTemplate = mock(LdapTemplate.class);

        RoleDaoImpl roleDao = new RoleDaoImpl();
        roleDao.setLdapTemplate(ldapTemplate);

        OrgsDao orgsDao = new OrgsDao();
        orgsDao.setLdapTemplate(ldapTemplate);
        orgsDao.setOrgSearchBaseDN("ou=orgs");

        AccountDaoImpl dao = new AccountDaoImpl(ldapTemplate);
        dao.setUserSearchBaseDN("ou=users");
        dao.setOrgSearchBaseDN("ou=orgs");
        dao.setOrgSearchBaseDN("ou=orgs");
        dao.setPendingUserSearchBaseDN("ou=pending");
        ctrlToTest = new ChangePasswordFormController(dao);
        ctrlToTest.passwordUtils = new PasswordUtils();

        model = mock(Model.class);
        formBean = new ChangePasswordFormBean();
        result = mock(BindingResult.class);

        ctrlToTest.logUtils = mock(LogUtils.class);

        // reset any spring security context that may have been set in previous test.
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
    }

    @Test
    public void initForm() {
        WebDataBinder dataBinder = new WebDataBinder(getClass());
        ctrlToTest.initForm(dataBinder);

        assertTrue(Arrays.asList(dataBinder.getAllowedFields()).contains("password"));
        assertTrue(Arrays.asList(dataBinder.getAllowedFields()).contains("confirmPassword"));
    }

    public void setupFormForbidden() throws Exception {
        String ret = ctrlToTest.setupForm("notme", model);
        assertEquals("forbidden", ret);
    }

    @Test
    public void setupWrongUid() throws DataServiceException {
        userIsSpringSecurityAuthenticatedAndExistInLdap("me");
        String ret = ctrlToTest.setupForm("notme", model);
        assertEquals("forbidden", ret);
    }

    @Test
    public void setupForm() throws Exception {
        userIsSpringSecurityAuthenticatedAndExistInLdap("me");
        String ret = ctrlToTest.setupForm("me", model);
        assertEquals("changePasswordForm", ret);
    }

    @Test
    public void changePasswordFormInvalid() throws Exception {
        userIsSpringSecurityAuthenticatedAndExistInLdap("pmauduit");
        formBean.setUid("pmauduit");
        formBean.setPassword("monkey12");
        formBean.setConfirmPassword("monkey123");
        when(result.hasErrors()).thenReturn(true);

        String ret = ctrlToTest.changePassword(model, formBean, result);

        assertEquals("changePasswordForm", ret);
    }

    @Test
    public void changePasswordSuccess() throws Exception {
        userIsSpringSecurityAuthenticatedAndExistInLdap("pmauduit");
        formBean.setUid("pmauduit");
        formBean.setPassword("monkey123");
        formBean.setConfirmPassword("monkey123");
        when(result.hasErrors()).thenReturn(false);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("success", true);
        when(model.asMap()).thenReturn(map);
        when(ldapTemplate.lookupContext((Name) any())).thenReturn(mock(DirContextOperations.class));

        String ret = ctrlToTest.changePassword(model, formBean, result);

        assertEquals("changePasswordForm", ret);
        assertTrue((Boolean) model.asMap().get("success"));
    }

    @Test(expected = DataServiceException.class)
    public void changePasswordDataServiceException() throws Exception {
        userIsSpringSecurityAuthenticatedAndExistInLdap("pmauduit");
        formBean.setUid("pmauduit");
        formBean.setPassword("monkey123");
        formBean.setConfirmPassword("monkey123");
        when(result.hasErrors()).thenReturn(false);
        Mockito.doThrow(DataServiceException.class).when(ldapTemplate).lookupContext((Name) any());

        ctrlToTest.changePassword(model, formBean, result);
    }

    @Test
    public void changePasswordUidMismatch() throws Exception {
        formBean.setUid("pmauduit1");
        userIsSpringSecurityAuthenticatedAndExistInLdap("pmauduit");

        String ret = ctrlToTest.changePassword(model, formBean, result);

        assertNull(ret);
    }

    @Test
    public void changePasswordNotAuthenticated() throws Exception {
        formBean.setUid("pmauduit");

        String ret = ctrlToTest.changePassword(model, formBean, result);

        assertNull(ret);
    }

    @Test
    public void changePasswordFormBean() {
        ChangePasswordFormBean tested = new ChangePasswordFormBean();
        tested.setConfirmPassword("monkey123x");
        tested.setUid("1");
        tested.setPassword("monkey123");

        assertEquals("1", tested.getUid());
        assertEquals("monkey123", tested.getPassword());
        assertEquals("monkey123x", tested.getConfirmPassword());
        assertEquals("ChangePasswordFormBean [uid=1, confirmPassword=monkey123x, password=monkey123]",
                tested.toString());
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
        when(ldapTemplate.lookup(any(Name.class), eq(UserSchema.ATTR_TO_RETRIEVE),
                (AccountDaoImpl.AccountContextMapper) any())).thenReturn(a);
        // Mock for password type search in ldap.
        when(ldapTemplate.lookup(any(Name.class), eq(new String[] { "userPassword" }), (ContextMapper<String>) any()))
                .thenReturn("SSHA");
    }
}
