package org.georchestra.console.ws.passwordrecovery;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.UserTokenDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.SessionStatus;

public class NewPasswordFormControllerTest {

    private AccountDao accountDao = Mockito.mock(AccountDao.class);
    private UserTokenDao userTokenDao = Mockito.mock(UserTokenDao.class);
    private NewPasswordFormController ctrl = new NewPasswordFormController(accountDao, userTokenDao);

    @Before
    public void setUp() throws Exception {
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

        String ret = ctrl.setupForm("test", model);
        assertTrue(ret.equals("newPasswordForm"));
    }

    @Test
    public void testSetupFormUserNotFound() throws Exception {
        Model model = Mockito.mock(Model.class);
        Mockito.doThrow(new NameNotFoundException("User not found")).when(userTokenDao).findUserByToken(Mockito.anyString());

        String ret = ctrl.setupForm("test", model);
        assertTrue(ret.equals("passwordRecoveryForm"));
    }
    @Test
    public void testSetupFormDataServiceException() throws Exception {
        Model model = Mockito.mock(Model.class);
        Mockito.doThrow(new DataServiceException("Error while accessing db")).when(userTokenDao).findUserByToken(Mockito.anyString());

        try {
            ctrl.setupForm("test", model);
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
        Mockito.doThrow(new DataServiceException("Error with DB")).when(accountDao).changePassword(Mockito.anyString(), Mockito.anyString());


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
        assertTrue(bean.toString().equals("NewPasswordFormBean [uid=1, token=token, password=password, confirmPassword=confirmed]"));
    }

}
