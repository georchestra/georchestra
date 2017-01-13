package org.georchestra.ldapadmin.ws.passwordrecovery;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.georchestra.ldapadmin.Configuration;
import org.georchestra.ldapadmin.bs.ReCaptchaParameters;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.GroupDao;
import org.georchestra.ldapadmin.ds.UserTokenDao;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.GroupFactory;
import org.georchestra.ldapadmin.mailservice.EmailFactoryImpl;
import org.georchestra.ldapadmin.mailservice.MailService;
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

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;

public class PasswordRecoveryFormControllerTest {

    private PasswordRecoveryFormController ctrl ;
    private AccountDao dao = Mockito.mock(AccountDao.class);
    private GroupDao gdao = Mockito.mock(GroupDao.class);
    private EmailFactoryImpl efi = Mockito.mock(EmailFactoryImpl.class);
    private MailService srv = new MailService(efi);
    private ReCaptchaParameters rep = new ReCaptchaParameters();
    private UserTokenDao utd = Mockito.mock(UserTokenDao.class);
    private Configuration cfg = new Configuration();
    private ReCaptcha rec = Mockito.mock(ReCaptcha.class);
    private ReCaptchaResponse rer = Mockito.mock(ReCaptchaResponse.class);
    private Model model = Mockito.mock(Model.class);
    private HttpServletRequest request = new MockHttpServletRequest();
    PasswordRecoveryFormBean formBean = Mockito.mock(PasswordRecoveryFormBean.class);
    BindingResult result = Mockito.mock(BindingResult.class);
    SessionStatus status = Mockito.mock(SessionStatus.class);

    @Before
    public void setUp() throws Exception {
        ctrl = new PasswordRecoveryFormController(dao,gdao, srv, utd, cfg, rec, rep);
    }

    @After
    public void tearDown() throws Exception {
    }

    private void prepareLegitRequest() throws Exception {
        request = new MockHttpServletRequest();
        Mockito.when(formBean.getRecaptcha_challenge_field()).thenReturn("valid");
        Mockito.when(formBean.getRecaptcha_response_field()).thenReturn("valid");
        Mockito.when(rer.isValid()).thenReturn(true);
        Mockito.when(rec.checkAnswer(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(rer);
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
        assertTrue(expectedFields.contains("recaptcha_challenge_field"));
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
        Mockito.when(rec.checkAnswer(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(rer);

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
        b.setRecaptcha_challenge_field("valid");
        b.setRecaptcha_response_field("valid");

        assertTrue(b.getRecaptcha_challenge_field().equals(b.getRecaptcha_response_field()));
        assertTrue(b.getEmail().equals("test@localhost.com"));
        assertTrue(b.toString().equals("PasswordRecoveryFormBean [email=test@localhost.com, "
                + "recaptcha_challenge_field=valid, recaptcha_response_field=valid]"));

    }
    /**
     * test for recovery password when user is a PENDING USER
     * @throws Exception 
    */ 
    @Test
    public void testPasswordRecoveryWithPendingUser() throws Exception {
        prepareLegitRequest();
        Mockito.when(result.hasErrors()).thenReturn(false);
        Mockito.when(rec.checkAnswer(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(rer);
        ArrayList<Group> pendingUsersGroupList = new ArrayList();
        
        pendingUsersGroupList.add(GroupFactory.create(Group.PENDING, "groups of pending users"));
        Mockito.when(gdao.findAllForUser(Mockito.anyString())).thenReturn(pendingUsersGroupList);
        String ret = ctrl.generateToken(request, formBean, result, status);
        
        assertTrue(ret.equals("passwordRecoveryForm"));
        for (Group g : pendingUsersGroupList){
        assertTrue(g.getName().equals(Group.PENDING));

        }
    } 
}
