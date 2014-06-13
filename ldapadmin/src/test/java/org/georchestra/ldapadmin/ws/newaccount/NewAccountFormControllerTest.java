package org.georchestra.ldapadmin.ws.newaccount;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.georchestra.ldapadmin.bs.Moderator;
import org.georchestra.ldapadmin.bs.ReCaptchaParameters;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.DuplicatedEmailException;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.mailservice.EmailFactoryImpl;
import org.georchestra.ldapadmin.mailservice.MailService;
import org.georchestra.ldapadmin.ws.utils.Validation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;
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
    private HttpServletRequest request = new MockHttpServletRequest();


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

    private void configureLegitFormBean() {
        Validation v = new Validation();
        v.setRequiredFields("uid\tfirstName\temail\tpassword\n");

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
    }

    /**
     * General case : creating a user with all the
     * requirements fulfilled.
     *
     * @throws IOException
     */
    @Test
    public void testCreate() throws IOException {
        configureLegitFormBean();

        String ret = ctrl.create(request, formBean, result, status);

        assertTrue(ret.equals("welcomeNewUser"));
    }
    /**
     * General case : creating a user with all the
     * requirements fulfilled - no moderation needed.
     *
     * @throws IOException
     */
    @Test
    public void testCreateNoModeration() throws IOException {
        configureLegitFormBean();
        mod.setModeratedSignup(false);

        String ret = ctrl.create(request, formBean, result, status);

        assertTrue(ret.equals("welcomeNewUser"));
    }

    /**
     * Trying to create a new user, with errors in form.
     *
     * @throws IOException
     */
    @Test
    public void testCreateWithFormErrors() throws IOException {
        configureLegitFormBean();
        Mockito.when(result.hasErrors()).thenReturn(true);

        String ret = ctrl.create(request, formBean, result, status);

        assertTrue(ret.equals("createAccountForm"));
    }

    /**
     * Trying to create a new user, which already exists (e-mail clashes).
     *
     * @throws Exception
     */
    @Test
    public void testCreateDuplicatedEmail() throws Exception {
        configureLegitFormBean();
        Mockito.doThrow(new DuplicatedEmailException("User already exists")).when(dao).insert((Account) Mockito.any(), Mockito.anyString());

        String ret = ctrl.create(request, formBean, result, status);

        assertTrue(ret.equals("createAccountForm"));
    }
    /**
     *
     * Tests the general case of generating a user creation form.
     *
     * @throws IOException
     */

    @Test
    public void testSetupForm() throws IOException {
        configureLegitFormBean();
        Model model = Mockito.mock(Model.class);

        String ret = ctrl.setupForm(request, model);

        assertTrue(ret.equals("createAccountForm"));
    }

}
