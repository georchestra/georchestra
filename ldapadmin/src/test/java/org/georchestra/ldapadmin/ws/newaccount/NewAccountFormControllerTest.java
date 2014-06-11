package org.georchestra.ldapadmin.ws.newaccount;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;

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


    @Before
    public void setUp() throws Exception {
        ctrl = new NewAccountFormController(dao, srv, mod, rec, rep);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreate() throws IOException {

        HttpServletRequest request = new MockHttpServletRequest();
        AccountFormBean formBean = Mockito.mock(AccountFormBean.class);
        BindingResult result = Mockito.mock(BindingResult.class);
        SessionStatus status = Mockito.mock(SessionStatus.class);
        // To be continued ...
        try {
            ctrl.create(request, formBean, result, status);
        } catch (Throwable e) {
            assertTrue(e instanceof NullPointerException);
        }

    }

}
