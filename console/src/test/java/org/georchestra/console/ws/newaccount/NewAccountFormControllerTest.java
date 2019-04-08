package org.georchestra.console.ws.newaccount;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.validator.routines.EmailValidator;
import org.georchestra.console.ReCaptchaV2;
import org.georchestra.console.bs.ReCaptchaParameters;
import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.DuplicatedEmailException;
import org.georchestra.console.ds.DuplicatedUidException;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.ds.RoleDao;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountFactory;
import org.georchestra.console.dto.Org;
import org.georchestra.console.dto.OrgExt;
import org.georchestra.console.mailservice.EmailFactory;
import org.georchestra.console.ws.utils.PasswordUtils;
import org.georchestra.console.ws.utils.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapRdn;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.SessionStatus;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.net.ssl.*"})
@PrepareForTest({ReCaptchaV2.class, URL.class, DataOutputStream.class})
public class NewAccountFormControllerTest {

    // Mocked objects needed by the controller default constructor
    private NewAccountFormController toTest;
    private AccountDao mockAccountDao = mock(AccountDao.class);
    private OrgsDao mockOrgDao = mock(OrgsDao.class);
    private AdvancedDelegationDao advancedDelegationDao = mock(AdvancedDelegationDao.class);
    private EmailFactory efi = mock(EmailFactory.class);
    private ReCaptchaV2 rec = mock(ReCaptchaV2.class);
    private ReCaptchaParameters rep = new ReCaptchaParameters();
    private MockHttpServletRequest request = new MockHttpServletRequest();
    private Model UiModel = mock(Model.class);
    private AccountFormBean formBean = mock(AccountFormBean.class);
    private BindingResult mockedValidationReports = mock(BindingResult.class);
    private SessionStatus status = mock(SessionStatus.class);


    @Before
    public void setUp() {

    	rep.setVerifyUrl("https://localhost");
    	rep.setPrivateKey("privateKey");

        toTest = createToTest("");
        toTest.setRoleDao(mock(RoleDao.class));

        // Mock admin account
        DistinguishedName dn = new DistinguishedName();
        dn.add(new LdapRdn("ou=users"));
        dn.add("uid", "testadmin");
        Account adminAccount =  AccountFactory.createBrief("testadmin", "monkey123", "Test", "ADmin",
                "postmastrer@localhost", "+33123456789", "admin", "");
        request.addHeader("sec-username", "testadmin"); // Set user connected through http header
        try {
            when(mockAccountDao.findByUID(eq("testadmin"))).thenReturn(adminAccount);
        } catch (DataServiceException | NameNotFoundException e) {
            assertTrue(false);
        }

        String[] orgTypes = {"Association", "Company", "Non-governmental organization"};
        when(mockOrgDao.getOrgTypeValues()).thenReturn(orgTypes);
    }

    @Test
    public void initForm() {
        WebDataBinder bind = new WebDataBinder(null);

        toTest.initForm(bind);

        List<String> allowedField = Arrays.asList(bind.getAllowedFields());
        assertTrue(allowedField.contains("firstName"));
        assertTrue(allowedField.contains("surname"));
        assertTrue(allowedField.contains("email"));
        assertTrue(allowedField.contains("phone"));
        assertTrue(allowedField.contains("org"));
        assertTrue(allowedField.contains("title"));
        assertTrue(allowedField.contains("description"));
        assertTrue(allowedField.contains("uid"));
        assertTrue(allowedField.contains("password"));
        assertTrue(allowedField.contains("confirmPassword"));
        assertTrue(allowedField.contains("recaptcha_response_field"));
        assertTrue(allowedField.contains("privacyPolicyAgreed"));
    }

    @Test
    public void nominalCreate() throws IOException, SQLException, DuplicatedEmailException, DataServiceException, DuplicatedUidException {
        configureFormBean();

        String ret = toTest.create(request, formBean, "", mockedValidationReports, status, UiModel);

        assertTrue(ret.equals("welcomeNewUser"));
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(mockAccountDao).insert(accountCaptor.capture(), anyString());
        assertEquals(true, accountCaptor.getValue().isPending());
    }

    /**
     * General case : creating a user with all the
     * requirements fulfilled - no moderation needed.
     */
    @Test
    public void createNoModeration() throws IOException, SQLException, DuplicatedEmailException, DataServiceException, DuplicatedUidException {
        configureFormBean();
        toTest.setModeratedSignup(false);

        String ret = toTest.create(request, formBean, "", mockedValidationReports, status, UiModel);

        assertTrue(ret.equals("welcomeNewUser"));
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(mockAccountDao).insert(accountCaptor.capture(), anyString());
        assertEquals(false, accountCaptor.getValue().isPending());
    }

    @Test
    public void nominalCreateWithNewOrg() throws IOException, SQLException, DuplicatedEmailException, DataServiceException, DuplicatedUidException {
        configureFormBean();
        when(formBean.getCreateOrg()).thenReturn(true);

        String ret = toTest.create(request, formBean, "", mockedValidationReports, status, UiModel);

        assertTrue(ret.equals("welcomeNewUser"));
        ArgumentCaptor<Org> orgCaptor = ArgumentCaptor.forClass(Org.class);
        verify(mockOrgDao).insert(orgCaptor.capture());
        assertTrue(orgCaptor.getValue().isPending());
        verify(mockOrgDao).insert(any(OrgExt.class));

    }

    @Test
    public void nominalCreateWithNewOrgModeration() throws IOException, SQLException, DuplicatedEmailException, DataServiceException, DuplicatedUidException {
        configureFormBean();
        when(formBean.getCreateOrg()).thenReturn(true);
        toTest.setModeratedSignup(false);

        String ret = toTest.create(request, formBean, "", mockedValidationReports, status, UiModel);

        assertTrue(ret.equals("welcomeNewUser"));
        ArgumentCaptor<Org> orgCaptor = ArgumentCaptor.forClass(Org.class);
        verify(mockOrgDao).insert(orgCaptor.capture());
        assertFalse(orgCaptor.getValue().isPending());
        verify(mockOrgDao).insert(any(OrgExt.class));
    }

    /**
     * Trying to create a new user, with errors in form.
     */
    @Test
    public void createWithFormErrors() throws IOException, SQLException {
        configureFormBean();
        when(mockedValidationReports.hasErrors()).thenReturn(true);

        String ret = toTest.create(request, formBean, "", mockedValidationReports, status, UiModel);

        assertTrue(ret.equals("createAccountForm"));
    }

    /**
     * Trying to create a new user, which already exists (e-mail clashes).
     */
    @Test
    public void createDuplicatedEmail() throws Exception {
        configureFormBean();
        Mockito.doThrow(new DuplicatedEmailException("User already exists")).
            when(mockAccountDao).insert((Account) any(), anyString());

        String ret = toTest.create(request, formBean, "", mockedValidationReports, status, UiModel);

        // The user must be redirected to the Create Account form
        assertTrue(ret.equals("createAccountForm"));
    }

    @Test
    public void createUserWithError() throws Exception {
        configureFormBean();
        Mockito.doThrow(new DataServiceException("Something went wrong when dealing with LDAP")).
            when(mockAccountDao).insert((Account) any(), anyString());

        try {
            toTest.create(request, formBean, "", mockedValidationReports, status, UiModel);
        } catch (Throwable e) {
            assertTrue (e instanceof IOException);
        }
    }

    /**
     * Trying to create a new user, which already exists (UID clashes).
     */
    @Test
    public void createDuplicatedUid() throws Exception {
        configureFormBean();
        Mockito.doThrow(new DuplicatedUidException("User ID already exists")).
            when(mockAccountDao).insert((Account) any(), anyString());

        String ret = toTest.create(request, formBean, "", mockedValidationReports, status, UiModel);

        // The user must be redirected to the Create Account form
        assertTrue(ret.equals("createAccountForm"));
    }

    /**
     *
     * Tests the general case of generating a user creation form.
     */

    @Test
    public void setupForm() throws IOException {
        configureFormBean();
        Model model = mock(Model.class);

        String ret = toTest.setupForm(request, model);

        assertTrue(ret.equals("createAccountForm"));
    }

    /**
     * Tests the AccountFormBean object creation.
     *
     * The usefulness of this test is questionable, since the underlying tested
     * classes does pretty trivial things. This is just to ensure a good coverage.
     */
    @Test
    public void accountFormBean() {
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
        t.setRecaptcha_response_field("wrong");
        t.setSurname("testmaster");
        t.setTitle("software engineer");
        t.setUid("123123-21465456-3434");
        t.setPrivacyPolicyAgreed(false);

        assertTrue(t.getConfirmPassword().equals("test"));
        assertTrue(t.getDescription().equals("testing account"));
        assertTrue(t.getEmail().equals("test@localhost.com"));
        assertTrue(t.getFirstName().equals("Test"));
        assertTrue(t.getOrg().equals("geOrchestra"));
        assertTrue(t.getPassword().equals("monkey123"));
        assertTrue(t.getPhone().equals("+331234567890"));
        assertTrue(t.getRecaptcha_response_field().equals("wrong"));
        assertTrue(t.getSurname().equals("testmaster"));
        assertTrue(t.getTitle().equals("software engineer"));
        assertTrue(t.getUid().equals("123123-21465456-3434"));
        assertEquals("AccountFormBean [uid=123123-21465456-3434, "
                + "firstName=Test, surname=testmaster, org=geOrchestra, "
                + "title=software engineer, email=test@localhost.com, "
                + "phone=+331234567890, description=testing account, "
                + "password=monkey123, confirmPassword=test, "
                + "privacyPolicyAgreed=false, "
                + "recaptcha_response_field=wrong]", t.toString());
    }

    /**
     * Tests email address with new domains like .bzh or .alsace
     */
    @Test
    public void newDomains() {
        EmailValidator validator = EmailValidator.getInstance();
        // Test valid email address
        assertTrue(validator.isValid("mael@bretagne.bzh"));
        assertTrue(validator.isValid("romain@champagne.alsace"));
        assertTrue(validator.isValid("test@domain.com"));

        // Test invalid email address
        assertFalse(validator.isValid("test@domain.no-existent"));
        assertFalse(validator.isValid("test@domain.qsdfgryzeh"));
    }

    @Test
    public void requiredFieldsUndefined() throws IOException, SQLException {
        NewAccountFormController toTest = createToTest("firstName,surname,org,orgType");
        toTest.reCaptchaActivated = true;
        toTest.privacyPolicyAgreementActivated = true;
        AccountFormBean formBean = new AccountFormBean();
        formBean.setPassword("");
        formBean.setConfirmPassword("");
        BindingResult resultErrors = new MapBindingResult(new HashMap<>(), "errors");

        toTest.create(request, formBean, "", resultErrors, status, UiModel);

        assertEquals("required", resultErrors.getFieldError("firstName").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("surname").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("email").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("uid").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("recaptcha_response_field").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("org").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("password").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("confirmPassword").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("privacyPolicyAgreed").getDefaultMessage());

        assertEquals(9, resultErrors.getFieldErrorCount());
    }

    @Test
    public void specialValidators() throws IOException, SQLException {
        NewAccountFormController toTest = createToTest("firstName,surname,org,orgType,phone,title,description");
        toTest.privacyPolicyAgreementActivated = true;
        AccountFormBean formBean = new AccountFormBean();
        formBean.setUid("I am no compliant !!!!");
        formBean.setEmail("I am no compliant !!!!");
        formBean.setPassword("Pré$ident");
        formBean.setConfirmPassword("lapinmalin");
        formBean.setPrivacyPolicyAgreed(false);
        BindingResult resultErrors = new MapBindingResult(new HashMap<>(), "errors");

        toTest.create(request, formBean, "", resultErrors, status, UiModel);

        assertEquals("uid.error.invalid", resultErrors.getFieldError("uid").getCode());
        assertEquals("email.error.invalidFormat", resultErrors.getFieldError("email").getCode());
        assertEquals("confirmPassword.error.pwdNotEquals", resultErrors.getFieldError("confirmPassword").getCode());
        assertEquals("required", resultErrors.getFieldError("phone").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("title").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("description").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("privacyPolicyAgreed").getDefaultMessage());
    }

    @Test
    public void orgValidators() throws Exception {
        NewAccountFormController toTest = createToTest("firstName,surname,org,orgType,orgShortName,orgAddress");
        AccountFormBean formBean = new AccountFormBean();
        formBean.setCreateOrg(true);
        formBean.setFirstName("Test");
        formBean.setSurname("testmaster");
        formBean.setEmail("test@localhost.com");
        formBean.setUid("a123123-21465456-3434");
        formBean.setConfirmPassword("testtest");
        formBean.setPassword("testtest");
        formBean.setRecaptcha_response_field("success");
        formBean.setPrivacyPolicyAgreed(true);
        mockRecaptchaSucess();
        BindingResult resultErrors = new MapBindingResult(new HashMap<>(), "errors");

        toTest.create(request, formBean, "", resultErrors, status, UiModel);

        assertEquals("required", resultErrors.getFieldError("orgName").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("orgType").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("orgShortName").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("orgAddress").getDefaultMessage());
        assertEquals(4, resultErrors.getFieldErrorCount());
    }

    private void configureFormBean() {
        when(formBean.getUid()).thenReturn("1");
        when(formBean.getFirstName()).thenReturn("test");
        when(formBean.getSurname()).thenReturn("test");
        when(formBean.getEmail()).thenReturn("test@localhost.com");
        when(formBean.getPassword()).thenReturn("abc1234");
        when(formBean.getConfirmPassword()).thenReturn("abc1234");
        when(formBean.getRecaptcha_response_field()).thenReturn("abc1234");
        when(formBean.getPhone()).thenReturn("+331234567890");
        when(formBean.getTitle()).thenReturn("+331234567890");
        when(formBean.getOrg()).thenReturn("geOrchestra testing team");
        when(formBean.getDescription()).thenReturn("Bot Unit Testing");
        when(formBean.getPrivacyPolicyAgreed()).thenReturn(true);

        when(rec.isValid(anyString(), anyString(), anyString())).thenReturn(true);
    }

    private NewAccountFormController createToTest(String requiredFields) {
        Validation validation = new Validation(requiredFields);
        PasswordUtils passwordUtils = new PasswordUtils();
        passwordUtils.setValidation(validation);

        NewAccountFormController toTest = new NewAccountFormController(rep, validation);
        toTest.setAccountDao(mockAccountDao);
        toTest.setOrgDao(mockOrgDao);
        toTest.setAdvancedDelegationDao(advancedDelegationDao);
        toTest.setEmailFactory(efi);
        toTest.passwordUtils = passwordUtils;
        return toTest;
    }

    private void mockRecaptchaSucess() throws Exception {
        HttpsURLConnection hucMock = mock(HttpsURLConnection.class);
        BufferedInputStream successInputStream = new BufferedInputStream(new ReaderInputStream(new StringReader("{\"success\": true}")));
        when(hucMock.getInputStream()).thenReturn(successInputStream);
        URL urlMock = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(urlMock);
        when(urlMock.openConnection()).thenReturn(hucMock);
        DataOutputStream dosMock = PowerMockito.mock(DataOutputStream.class);
        PowerMockito.whenNew(DataOutputStream.class).withArguments(any(OutputStream.class)).thenReturn(dosMock);
    }
}
