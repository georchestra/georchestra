package org.georchestra.ldapadmin.ws.newaccount;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.georchestra.ldapadmin.bs.Moderator;
import org.georchestra.ldapadmin.bs.ReCaptchaParameters;
import org.georchestra.ldapadmin.ds.*;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.AccountFactory;

import org.georchestra.ldapadmin.mailservice.EmailFactoryImpl;
import org.georchestra.ldapadmin.mailservice.MailService;
import org.georchestra.ldapadmin.ws.utils.Validation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapRdn;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.SessionStatus;

public class NewAccountFormControllerTest {

    // Mocked objects needed by the controller default constructor
    private NewAccountFormController ctrl ;
    private AccountDao dao = Mockito.mock(AccountDao.class);
    private OrgsDao org = Mockito.mock(OrgsDao.class);
    private EmailFactoryImpl efi = Mockito.mock(EmailFactoryImpl.class);
    private MailService srv = new MailService(efi);
    private Moderator  mod = new Moderator();
    private ReCaptcha  rec = Mockito.mock(ReCaptcha.class);
    private ReCaptchaParameters rep = new ReCaptchaParameters();
    private ReCaptchaResponse rer = Mockito.mock(ReCaptchaResponse.class);
    private MockHttpServletRequest request = new MockHttpServletRequest();
    private LdapTemplate ldapTemplate = Mockito.mock(LdapTemplate.class);
    private Model UiModel = Mockito.mock(Model.class);


    private Account adminAccount;


    AccountFormBean formBean = Mockito.mock(AccountFormBean.class);
    BindingResult result = Mockito.mock(BindingResult.class);
    SessionStatus status = Mockito.mock(SessionStatus.class);

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


    @Before
    public void setUp() throws Exception {
        ctrl = new NewAccountFormController(dao, org, srv, mod, rec, rep, new Validation());

        // Mock admin account
        DistinguishedName dn = new DistinguishedName();
        dn.add(new LdapRdn("ou=users"));
        dn.add("uid", "testadmin");
        this.adminAccount =  AccountFactory.createBrief("testadmin", "monkey123", "Test", "ADmin",
                "postmastrer@localhost", "+33123456789", "admin", "");
        this.request.addHeader("sec-username", "testadmin"); // Set user connected through http header
        try {
            Mockito.when(this.dao.findByUID(eq("testadmin"))).thenReturn(this.adminAccount);
        } catch (DataServiceException e) {
            assertTrue(false);
        } catch (NameNotFoundException e) {
            assertTrue(false);
        }

        String[] orgTypes = {"Association", "Company", "Non-governmental organization"};
        Mockito.when(this.org.getOrgTypeValues()).thenReturn(orgTypes);
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
        configureLegitFormBean();

        String ret = ctrl.create(request, formBean, result, status, UiModel);

        assertTrue(ret.equals("welcomeNewUser"));
    }

    /**
     * Tests the initform method.
     */
    @Test
    public void testInitForm() {
        WebDataBinder bind = new WebDataBinder(null);

        ctrl.initForm(bind);
        List<String> expectedFields = Arrays.asList(bind.getAllowedFields());

        assertTrue(expectedFields.contains("firstName"));
        assertTrue(expectedFields.contains("surname"));
        assertTrue(expectedFields.contains("email"));
        assertTrue(expectedFields.contains("phone"));
        assertTrue(expectedFields.contains("org"));
        assertTrue(expectedFields.contains("title"));
        assertTrue(expectedFields.contains("description"));
        assertTrue(expectedFields.contains("uid"));
        assertTrue(expectedFields.contains("password"));
        assertTrue(expectedFields.contains("confirmPassword"));
        assertTrue(expectedFields.contains("recaptcha_challenge_field"));
        assertTrue(expectedFields.contains("recaptcha_response_field"));
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

        String ret = ctrl.create(request, formBean, result, status, UiModel);

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

        String ret = ctrl.create(request, formBean, result, status, UiModel);

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
        Mockito.doThrow(new DuplicatedEmailException("User already exists")).
            when(dao).insert((Account) Mockito.any(), Mockito.anyString(), Mockito.anyString());

        String ret = ctrl.create(request, formBean, result, status, UiModel);

        // The user must be redirected to the Create Account form
        assertTrue(ret.equals("createAccountForm"));
    }

    @Test
    public void testCreateUserWithError() throws Exception {
        configureLegitFormBean();
        Mockito.doThrow(new DataServiceException("Something went wrong when dealing with LDAP")).
            when(dao).insert((Account) Mockito.any(), Mockito.anyString(), Mockito.anyString());

        try {
            ctrl.create(request, formBean, result, status, UiModel);
        } catch (Throwable e) {
            assertTrue (e instanceof IOException);
        }
    }

    /**
     * Trying to create a new user, which already exists (UID clashes).
     *
     * @throws Exception
     */
    @Test
    public void testCreateDuplicatedUid() throws Exception {
        configureLegitFormBean();
        Mockito.doThrow(new DuplicatedUidException("User ID already exists")).
            when(dao).insert((Account) Mockito.any(), Mockito.anyString(), Mockito.anyString());

        String ret = ctrl.create(request, formBean, result, status, UiModel);

        // The user must be redirected to the Create Account form
        assertTrue(ret.equals("createAccountForm"));

        // Same scenario, but unable to generate a new UID
        Mockito.doThrow(new DataServiceException("something has messed up")).
            when(dao).generateUid(Mockito.anyString());
        try {
            ctrl.create(request, formBean, result, status, UiModel);
        } catch (Throwable e) {
            assertTrue (e instanceof IOException);
        }

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

    /**
     * Tests the AccountFormBean object creation.
     *
     * The usefulness of this test is questionable, since the underlying tested
     * classes does pretty trivial things. This is just to ensure a good coverage.
     */
    @Test
    public void testAccountFormBean() {
        AccountFormBean t = new AccountFormBean();
        t.setConfirmPassword("test");
        t.setDescription("testing account");
        // TODO only tested client-side ? related to
        // https://github.com/georchestra/georchestra/issues/682
        t.setEmail("test@localhost.com");
        t.setFirstName("Test");
        t.setOrg("geOrchestra");
        t.setPassword("monkey123");
        t.setPhone("+331234567890");
        t.setRecaptcha_challenge_field("good");
        t.setRecaptcha_response_field("wrong");
        t.setSurname("testmaster");
        t.setTitle("software engineer");
        t.setUid("123123-21465456-3434");

        assertTrue(t.getConfirmPassword().equals("test"));
        assertTrue(t.getDescription().equals("testing account"));
        assertTrue(t.getEmail().equals("test@localhost.com"));
        assertTrue(t.getFirstName().equals("Test"));
        assertTrue(t.getOrg().equals("geOrchestra"));
        assertTrue(t.getPassword().equals("monkey123"));
        assertTrue(t.getPhone().equals("+331234567890"));
        assertTrue(t.getRecaptcha_challenge_field().equals("good"));
        assertTrue(t.getRecaptcha_response_field().equals("wrong"));
        assertTrue(t.getSurname().equals("testmaster"));
        assertTrue(t.getTitle().equals("software engineer"));
        assertTrue(t.getUid().equals("123123-21465456-3434"));
        assertTrue(t.toString().equals("AccountFormBean [uid=123123-21465456-3434, "
                + "firstName=Test, surname=testmaster, org=geOrchestra, "
                + "title=software engineer, email=test@localhost.com, "
                + "phone=+331234567890, description=testing account, "
                + "password=monkey123, confirmPassword=test, recaptcha_challenge_field=good, "
                + "recaptcha_response_field=wrong]"));
    }
}
