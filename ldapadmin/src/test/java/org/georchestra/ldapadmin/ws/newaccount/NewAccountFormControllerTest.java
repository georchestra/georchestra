package org.georchestra.ldapadmin.ws.newaccount;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.georchestra.ldapadmin.bs.Moderator;
import org.georchestra.ldapadmin.bs.ReCaptchaParameters;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.mailservice.EmailFactoryImpl;
import org.georchestra.ldapadmin.mailservice.MailService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.support.SessionStatus;

public class NewAccountFormControllerTest {

    // Mocked objects needed by the controller default constructor
    private NewAccountFormController ctrl ;
    private AccountDao dao = Mockito.mock(AccountDao.class);
    private EmailFactoryImpl efi = Mockito.mock(EmailFactoryImpl.class);
    private MailService srv = new MailService(efi);
    private Moderator  mod = new Moderator();
    private ReCaptcha  rec = Mockito.mock(ReCaptcha.class);
    private ReCaptchaParameters rep = new ReCaptchaParameters();
    private ReCaptchaResponse rer = Mockito.mock(ReCaptchaResponse.class);

    AccountFormBean formBean = Mockito.mock(AccountFormBean.class);
    BindingResult result = Mockito.mock(BindingResult.class);
    SessionStatus status = Mockito.mock(SessionStatus.class);

    @Before
    public void setUp() throws Exception {
        ctrl = new NewAccountFormController(dao, srv, mod, rec, rep);
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * General case : creating a user with all the
     * requirements fulfilled.
     *
     * @throws IOException
     */
    @Test
    public void testCreate() throws IOException {
        HttpServletRequest request = new MockHttpServletRequest();


        Mockito.when(formBean.getUid()).thenReturn("1");
        Mockito.when(formBean.getFirstName()).thenReturn("test");
        Mockito.when(formBean.getSurname()).thenReturn("test");
        Mockito.when(formBean.getEmail()).thenReturn("test@localhost.com");
        Mockito.when(formBean.getPassword()).thenReturn("abc1234");
        Mockito.when(formBean.getConfirmPassword()).thenReturn("abc1234");
        Mockito.when(formBean.getRecaptcha_challenge_field()).thenReturn("abc1234");
        Mockito.when(formBean.getRecaptcha_response_field()).thenReturn("abc1234");
        Mockito.when(formBean.getPhone()).thenReturn("+331234567890");
        Mockito.when(formBean.getTitle()).thenReturn("+331234567890");
        Mockito.when(formBean.getOrg()).thenReturn("geOrchestra testing team");
        Mockito.when(formBean.getDescription()).thenReturn("Bot Unit Testing");

        Mockito.when(rec.checkAnswer(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(rer);
        Mockito.when(rer.isValid()).thenReturn(true);

        String ret = ctrl.create(request, formBean, result, status);
        assert (ret.equals("welcomeNewUser"));
    }

}
