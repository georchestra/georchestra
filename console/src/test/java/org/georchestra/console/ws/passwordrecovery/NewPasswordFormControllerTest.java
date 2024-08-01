package org.georchestra.console.ws.passwordrecovery;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.georchestra.console.ds.UserTokenDao;
import org.georchestra.console.ws.utils.PasswordUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.AccountDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class NewPasswordFormControllerTest {

    private AccountDao accountDao = Mockito.mock(AccountDao.class);
    private UserTokenDao userTokenDao = Mockito.mock(UserTokenDao.class);
    private NewPasswordFormController ctrl = new NewPasswordFormController(accountDao, userTokenDao);

    @Before
    public void setUp() throws Exception {
        ctrl.passwordUtils = new PasswordUtils();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testInitForm() {
        WebDataBinder dataBinder = new WebDataBinder(null);
        ctrl.initForm(dataBinder);

        List<String> ret = Arrays.asList(dataBinder.getAllowedFields());
        assertTrue(ret.contains("password"));
        assertTrue(ret.contains("confirmPassword"));
    }

    @Test
    public void testSetupForm() throws IOException {

        Model model = Mockito.mock(Model.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getSession()).thenReturn(Mockito.mock(HttpSession.class));

        String ret = ctrl.setupForm("test", model, request);
        assertTrue(ret.equals("newPasswordForm"));
    }

    @Test
    public void testSetupFormUserTokenNotFound() throws Exception {
        Model model = Mockito.mock(Model.class);
        Mockito.when(userTokenDao.findUidWithoutAdditionalInfo(Mockito.anyString()))
                .thenThrow(NameNotFoundException.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getSession()).thenReturn(Mockito.mock(HttpSession.class));

        String ret = ctrl.setupForm("test", model, request);
        assertTrue(ret.equals("redirect:/account/passwordRecovery"));
    }

    @Test
    public void testSetupFormDataServiceException() throws Exception {
        Model model = Mockito.mock(Model.class);
        Mockito.doThrow(new DataServiceException("Error while accessing db")).when(userTokenDao)
                .findUidWithoutAdditionalInfo(Mockito.anyString());
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getSession()).thenReturn(Mockito.mock(HttpSession.class));

        try {
            ctrl.setupForm("test", model, request);
        } catch (Throwable e) {
            assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void testNewPassword() throws IOException {
        NewPasswordFormBean formBean = new NewPasswordFormBean();
        BindingResult result = Mockito.mock(BindingResult.class);
        SessionStatus sessionStatus = Mockito.mock(SessionStatus.class);

        formBean.setConfirmPassword("confirm");
        formBean.setPassword("password");

        String ret = ctrl.newPassword(formBean, result, sessionStatus);
        assertTrue(ret.equals("passwordUpdated"));
    }

    @Test
    public void testNewPasswordWithErrors() throws IOException {
        NewPasswordFormBean formBean = new NewPasswordFormBean();
        BindingResult result = Mockito.mock(BindingResult.class);
        SessionStatus sessionStatus = Mockito.mock(SessionStatus.class);
        Mockito.when(result.hasErrors()).thenReturn(true);

        formBean.setConfirmPassword("confirm");
        formBean.setPassword("password");

        String ret = ctrl.newPassword(formBean, result, sessionStatus);
        assertTrue(ret.equals("newPasswordForm"));
    }

    @Test
    public void testNewPasswordDataServiceException() throws Exception {
        NewPasswordFormBean formBean = new NewPasswordFormBean();
        BindingResult result = Mockito.mock(BindingResult.class);
        SessionStatus sessionStatus = Mockito.mock(SessionStatus.class);
        Mockito.doThrow(new DataServiceException("Error with DB")).when(accountDao).changePassword(Mockito.anyString(),
                Mockito.anyString());

        formBean.setConfirmPassword("confirm");
        formBean.setPassword("password");

        try {
            ctrl.newPassword(formBean, result, sessionStatus);
        } catch (Throwable e) {
            assertTrue(e instanceof IOException);
        }
    }

    @Test
    public void testNewPasswordFormBean() {
        NewPasswordFormBean bean = new NewPasswordFormBean();
        bean.setConfirmPassword("confirmed");
        bean.setPassword("password");
        bean.setToken("token");
        bean.setUid("1");

        assertTrue(bean.getConfirmPassword().equals("confirmed"));
        assertTrue(bean.getPassword().equals("password"));
        assertTrue(bean.getToken().equals("token"));
        assertTrue(bean.getUid().equals("1"));
        assertTrue(bean.toString()
                .equals("NewPasswordFormBean [uid=1, token=token, password=password, confirmPassword=confirmed]"));
    }

}
