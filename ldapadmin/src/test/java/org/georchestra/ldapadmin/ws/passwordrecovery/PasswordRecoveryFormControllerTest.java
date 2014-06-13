package org.georchestra.ldapadmin.ws.passwordrecovery;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;

import org.georchestra.ldapadmin.Configuration;
import org.georchestra.ldapadmin.bs.ReCaptchaParameters;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.UserTokenDao;
import org.georchestra.ldapadmin.mailservice.EmailFactoryImpl;
import org.georchestra.ldapadmin.mailservice.MailService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.SessionStatus;

public class PasswordRecoveryFormControllerTest {

    private PasswordRecoveryFormController ctrl ;
    private AccountDao dao = Mockito.mock(AccountDao.class);
    private EmailFactoryImpl efi = Mockito.mock(EmailFactoryImpl.class);
    private MailService srv = new MailService(efi);
    private ReCaptchaParameters rep = new ReCaptchaParameters();
    private UserTokenDao utd = Mockito.mock(UserTokenDao.class);
    private Configuration cfg = new Configuration();
    private ReCaptcha rec = Mockito.mock(ReCaptcha.class);

    PasswordRecoveryFormBean formBean = Mockito.mock(PasswordRecoveryFormBean.class);
    BindingResult result = Mockito.mock(BindingResult.class);
    SessionStatus status = Mockito.mock(SessionStatus.class);

    @Before
    public void setUp() throws Exception {
        ctrl = new PasswordRecoveryFormController(dao, srv, utd, cfg, rec, rep);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPasswordRecoveryFormController() {

    }

    @Test
    public void testInitForm() {
        WebDataBinder bind = new WebDataBinder(null);

        ctrl.initForm(bind);

        List<String> expectedFields = Arrays.asList(bind.getAllowedFields());

        assertTrue(expectedFields.contains("email"));
        assertTrue(expectedFields.contains("recaptcha_challenge_field"));
        assertTrue(expectedFields.contains("recaptcha_response_field"));
    }

    @Test
    public void testSetupForm() throws IOException {
        Model model = Mockito.mock(Model.class);
        HttpServletRequest request = new MockHttpServletRequest();

        String ret = ctrl.setupForm(request , "test@localhost.com", model);

        assertTrue(ret.equals("passwordRecoveryForm"));
    }

    @Test
    public void testGenerateTokenBadEmail() throws IOException {
        HttpServletRequest request = new MockHttpServletRequest();
        Mockito.when(result.hasErrors()).thenReturn(true);

        String ret = ctrl.generateToken(request, formBean, result, status);

        assertTrue(ret.equals("passwordRecoveryForm"));
    }

    @Test
    public void testGenerateToken() throws IOException {
        HttpServletRequest request = new MockHttpServletRequest();

        ctrl.generateToken(request, formBean, result, status);

    }


}
