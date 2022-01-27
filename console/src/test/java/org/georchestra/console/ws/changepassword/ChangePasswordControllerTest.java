package org.georchestra.console.ws.changepassword;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Name;

import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.PasswordUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.orgs.OrgsDaoImpl;
import org.georchestra.ds.roles.RoleDaoImpl;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDaoImpl;
import org.georchestra.ds.users.AccountImpl;
import org.georchestra.ds.users.UserSchema;
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

        OrgsDaoImpl orgsDao = new OrgsDaoImpl();
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

    @Test
    public void setupForm() throws Exception {
        userIsSpringSecurityAuthenticatedAndExistInLdap("me");
        String ret = ctrlToTest.setupForm(model);
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
        tested.setConfirmPassword("monkey123");
        tested.setUid("1");
        tested.setPassword("monkey123");

        assertEquals("1", tested.getUid());
        assertEquals("monkey123", tested.getPassword());
        assertEquals("monkey123", tested.getConfirmPassword());
        assertEquals("ChangePasswordFormBean [uid=1, confirmPassword=monkey123, password=monkey123]",
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
