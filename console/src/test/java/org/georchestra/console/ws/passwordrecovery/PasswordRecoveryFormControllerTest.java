package org.georchestra.console.ws.passwordrecovery;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.georchestra.console.Configuration;
import org.georchestra.console.ReCaptchaV2;
import org.georchestra.console.bs.ReCaptchaParameters;
import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.RoleDao;
import org.georchestra.console.ds.UserTokenDao;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.Role;
import org.georchestra.console.dto.RoleFactory;
import org.georchestra.console.mailservice.EmailFactoryImpl;
import org.georchestra.console.mailservice.MailService;
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

    private PasswordRecoveryFormController ctrl ;
    private AccountDao dao = Mockito.mock(AccountDao.class);
    private RoleDao gdao = Mockito.mock(RoleDao.class);
    private EmailFactoryImpl efi = Mockito.mock(EmailFactoryImpl.class);
    private MailService srv = new MailService(efi);
    private ReCaptchaV2 rec = Mockito.mock(ReCaptchaV2.class);
    private ReCaptchaParameters rep = new ReCaptchaParameters();
    private UserTokenDao utd = Mockito.mock(UserTokenDao.class);
    private Configuration cfg = new Configuration();
    private Model model = Mockito.mock(Model.class);
    private HttpServletRequest request = new MockHttpServletRequest();
    PasswordRecoveryFormBean formBean = Mockito.mock(PasswordRecoveryFormBean.class);
    BindingResult result = Mockito.mock(BindingResult.class);
    SessionStatus status = Mockito.mock(SessionStatus.class);

    @Before
    public void setUp() throws Exception {
        ctrl = new PasswordRecoveryFormController(dao,gdao, srv, utd, cfg, rep);
    }

    @After
    public void tearDown() throws Exception {
    }

    private void prepareLegitRequest() throws Exception {
        request = new MockHttpServletRequest();
        Mockito.when(formBean.getRecaptcha_response_field()).thenReturn("valid");
        Account account = Mockito.mock(Account.class);
        Mockito.when(account.getUid()).thenReturn("1");
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

        String ret = ctrl.setupForm(request , "test@localhost.com", model);

        assertTrue(ret.equals("passwordRecoveryForm"));
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
        assertTrue(ret.equals("passwordRecoveryForm"));
    }


    @Test
    public void testGenerateTokenBadEmail() throws Exception {
        prepareLegitRequest();
        Mockito.when(result.hasErrors()).thenReturn(true);

        String ret = ctrl.generateToken(request, formBean, result, status);

        assertTrue(ret.equals("passwordRecoveryForm"));
    }

    @Test
    public void testGenerateToken() throws Exception {
        prepareLegitRequest();

        String ret = ctrl.generateToken(request, formBean, result, status);
        assertTrue(ret.equals("emailWasSent"));
    }

    @Test
    public void testBadCaptchaGenerateToken() throws Exception {
        prepareLegitRequest();
        Mockito.when(result.hasErrors()).thenReturn(true);
        Mockito.when(rec.isValid(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(false);

        String ret = ctrl.generateToken(request, formBean, result, status);

        assertTrue(ret.equals("passwordRecoveryForm"));
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

        assertTrue(b.getEmail().equals("test@localhost.com"));
        assertTrue(b.toString().equals("PasswordRecoveryFormBean [email=test@localhost.com, "
                + "recaptcha_response_field=valid]"));

    }
    /**
     * test for recovery password when user is a PENDING USER
     * @throws Exception
    */
    @Test
    public void testPasswordRecoveryWithPendingUser() throws Exception {
        prepareLegitRequest();
        Mockito.when(result.hasErrors()).thenReturn(false);
        ArrayList<Role> pendingUsersRoleList = new ArrayList();
        pendingUsersRoleList.add(RoleFactory.create(Role.PENDING, "roles of pending users", false));
        Mockito.when(gdao.findAllForUser(Mockito.anyString())).thenReturn(pendingUsersRoleList);
        String ret = ctrl.generateToken(request, formBean, result, status);

        assertTrue(ret.equals("passwordRecoveryForm"));
        for (Role g : pendingUsersRoleList){
        assertTrue(g.getName().equals(Role.PENDING));

        }
    }
}
