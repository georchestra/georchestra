package org.georchestra.console.ws.passwordrecovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.georchestra.console.ReCaptchaV2;
import org.georchestra.console.bs.ReCaptchaParameters;
import org.georchestra.console.ds.UserTokenDao;
import org.georchestra.console.mailservice.EmailFactory;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.SessionStatus;

public class PasswordRecoveryFormControllerTest {

    private PasswordRecoveryFormController ctrl;
    private AccountDao dao = Mockito.mock(AccountDao.class);
    private RoleDao gdao = Mockito.mock(RoleDao.class);
    private EmailFactory efi = Mockito.mock(EmailFactory.class);
    private ReCaptchaV2 rec = Mockito.mock(ReCaptchaV2.class);
    private ReCaptchaParameters rep = new ReCaptchaParameters();
    private UserTokenDao utd = Mockito.mock(UserTokenDao.class);
    private Model model = Mockito.mock(Model.class);
    private HttpServletRequest request = new MockHttpServletRequest();
    private PasswordRecoveryFormBean formBean = Mockito.mock(PasswordRecoveryFormBean.class);
    private BindingResult result = Mockito.mock(BindingResult.class);
    private SessionStatus status = Mockito.mock(SessionStatus.class);
    private LogUtils mockLogUtils = Mockito.mock(LogUtils.class);

    @Before
    public void setUp() throws Exception {
        ctrl = new PasswordRecoveryFormController(dao, gdao, efi, utd, rep);
        ctrl.setPublicUrl("https://georchestra.mydomain.org");
        ctrl.setPublicContextPath("/console");
        ctrl.logUtils = mockLogUtils;
    }

    @After
    public void tearDown() throws Exception {
    }

    private void prepareLegitRequest() throws Exception {
        prepareLegitRequest(false);
    }

    private void prepareLegitRequest(boolean isPending) throws Exception {
        request = new MockHttpServletRequest();
        Mockito.when(formBean.getRecaptcha_response_field()).thenReturn("valid");
        Account account = Mockito.mock(Account.class);
        Mockito.when(account.getUid()).thenReturn("1");
        Mockito.when(account.isPending()).thenReturn(isPending);
        Mockito.when(dao.findByEmail(Mockito.anyString())).thenReturn(account);
        Mockito.when(utd.exist(Mockito.anyString())).thenReturn(true);
    }

    @Test
    public void testInitForm() {
        WebDataBinder bind = new WebDataBinder(null);

        ctrl.initForm(bind);

        List<String> expectedFields = Arrays.asList(bind.getAllowedFields());

        assertTrue(expectedFields.contains("email"));
        assertTrue(expectedFields.contains("recaptcha_response_field"));
    }

    @Test
    public void testSetupForm() throws Exception {
        prepareLegitRequest();

        String ret = ctrl.setupForm(request, "test@localhost.com", model);

        assertEquals("passwordRecoveryForm", ret);
    }

    @Test
    public void testGenerateTokenWithDataServiceException() throws Exception {
        prepareLegitRequest();
        Mockito.when(utd.exist(Mockito.anyString())).thenReturn(false);

        Mockito.doThrow(DataServiceException.class).when(utd).insertToken(Mockito.anyString(), Mockito.anyString());

        try {
            ctrl.generateToken(request, formBean, result, status);
        } catch (Throwable e) {
            assertTrue(e instanceof IOException);
        }

    }

    @Test
    public void testGenerateTokenWithUserNotFound() throws Exception {
        prepareLegitRequest();
        Mockito.when(utd.exist(Mockito.anyString())).thenReturn(false);
        Mockito.doThrow(NameNotFoundException.class).when(dao).findByEmail(Mockito.anyString());

        String ret = ctrl.generateToken(request, formBean, result, status);
        assertEquals("emailWasSentForPasswordChange", ret);
    }

    @Test
    public void testGenerateTokenBadEmail() throws Exception {
        prepareLegitRequest();
        Mockito.when(result.hasErrors()).thenReturn(true);

        String ret = ctrl.generateToken(request, formBean, result, status);

        assertEquals("passwordRecoveryForm", ret);
    }

    @Test
    public void testGenerateToken() throws Exception {
        prepareLegitRequest();

        String ret = ctrl.generateToken(request, formBean, result, status);
        assertEquals("emailWasSentForPasswordChange", ret);
    }

    @Test
    public void testBadCaptchaGenerateToken() throws Exception {
        prepareLegitRequest();
        Mockito.when(result.hasErrors()).thenReturn(true);
        Mockito.when(rec.isValid(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        String ret = ctrl.generateToken(request, formBean, result, status);

        assertEquals("passwordRecoveryForm", ret);
    }

    /**
     * Test the underlying bean (PasswordRecoveryFormBean).
     *
     */
    @Test
    public void testPasswordRecoveryFormBean() {
        PasswordRecoveryFormBean b = new PasswordRecoveryFormBean();

        b.setEmail("test@localhost.com");
        b.setRecaptcha_response_field("valid");

        assertEquals("test@localhost.com", b.getEmail());
        assertEquals("PasswordRecoveryFormBean [email=test@localhost.com, recaptcha_response_field=valid]",
                b.toString());

    }

    /**
     * test for recovery password when user is a pending user
     *
     * @throws Exception
     */
    @Test
    public void testPasswordRecoveryWithPendingUser() throws Exception {
        prepareLegitRequest(true);
        Mockito.when(result.hasErrors()).thenReturn(false);
        String ret = ctrl.generateToken(request, formBean, result, status);

        assertEquals("emailWasSentForPasswordChange", ret);
    }

    @Test
    public void testMakeChangePasswordURLOk() {
        String res = ctrl.makeChangePasswordURL("https://georchestra.org", "console", "1234");
        assertEquals(res, "https://georchestra.org/console/account/newPassword?token=1234");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakeChangePasswordURLWronglyformated() {
        ctrl.makeChangePasswordURL("pompom", "https://blabla.com", "https://pompom");
    }
}
