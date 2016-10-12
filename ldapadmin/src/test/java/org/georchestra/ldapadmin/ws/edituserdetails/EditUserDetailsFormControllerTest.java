package org.georchestra.ldapadmin.ws.edituserdetails;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.OrgsDao;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.AccountFactory;
import org.georchestra.ldapadmin.dto.Org;
import org.georchestra.ldapadmin.ws.utils.Validation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.SessionStatus;


public class EditUserDetailsFormControllerTest {

    private EditUserDetailsFormController ctrl;

    private AccountDao dao = Mockito.mock(AccountDao.class);
    private OrgsDao orgsDao = Mockito.mock(OrgsDao.class);

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockHttpServletResponse response = new MockHttpServletResponse();

    private EditUserDetailsFormBean formBean = new EditUserDetailsFormBean();
    private BindingResult resultErrors = Mockito.mock(BindingResult.class);

    private SessionStatus sessionStatus = Mockito.mock(SessionStatus.class);

    private Model model = Mockito.mock(Model.class);
    private Account mtesterAccount;

    @Before
    public void setUp() throws Exception {
        ctrl = new EditUserDetailsFormController(dao, orgsDao, new Validation());
        formBean.setDescription("description");
        formBean.setEmail("email");
        formBean.setFacsimile("+331234567890");
        formBean.setFirstName("testFirst");
        formBean.setOrg("geOrchestra testing LLC");
        formBean.setPhone("+331234567891");
        formBean.setPostalAddress("48 Avenue du Lac du Bourget. 73377 Le Bourget-du-Lac");
        formBean.setSurname("misterTest");
        formBean.setTitle("test engineer");
        formBean.setUid("mtester");

        // Mock mtester user
        this.mtesterAccount = AccountFactory.createBrief("mtester",
                "12345",
                "testFirst",
                "misterTest",
                "email",
                "+331234567891",
                "test engineer",
                "description");
        mtesterAccount.setOrg("georTest");

        Mockito.when(dao.findByUID(Mockito.eq("mtester"))).thenReturn(mtesterAccount);

        Org org = new Org();
        org.setId("georTest");
        org.setName("geOrchestra testing LLC");

        Mockito.when(this.orgsDao.findByCommonName(Mockito.eq("georTest"))).thenReturn(org);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testEditUserDetailsFormController() {

    }

    @Test
    public void testInitForm() throws Exception {
        WebDataBinder dataBinder = new WebDataBinder(null);

        ctrl.initForm(dataBinder);

        List<String> ret = Arrays.asList(dataBinder.getAllowedFields());
        assertTrue(ret.contains("uid"));
        assertTrue(ret.contains("firstName"));
        assertTrue(ret.contains("surname"));
        assertTrue(ret.contains("email"));
        assertTrue(ret.contains("title"));
        assertTrue(ret.contains("phone"));
        assertTrue(ret.contains("facsimile"));
        assertTrue(ret.contains("org"));
        assertTrue(ret.contains("description"));
        assertTrue(ret.contains("postalAddress"));
    }

    @Test
    public void testSetupFormWithoutSecurityProxyHeaders() throws Exception {
        Account mockedAccount = Mockito.mock(Account.class);
        Mockito.when(dao.findByUID(Mockito.anyString())).thenReturn(mockedAccount);

        String ret = ctrl.setupForm(request, response, model);

        assertTrue(ret == null);
    }

    @Test
    public void testSetupForm() throws Exception {
        request.addHeader("sec-username", "mtester");
        Mockito.when(dao.findByUID(Mockito.anyString())).thenReturn(this.mtesterAccount);

        String ret = ctrl.setupForm(request, response, model);

        assertTrue(ret.equals("editUserDetailsForm"));
    }

    /**
     * Testing the general case on EditUserDetail controller,
     * general case.
     *
     * @throws Exception
     */
    @Test
    public void testEdit() throws Exception {
       request.addHeader("sec-username", "mtester");
       String ret = ctrl.edit(request, response, model, formBean, resultErrors, sessionStatus);
       assertTrue(ret.equals("editUserDetailsForm"));
    }

    /**
     * Tests the underlying form bean class (EditUserDetailsFormBean).
     */
    @Test
    public void testEditUserDetailsFormBean() {

        assertTrue(formBean.getUid().equals("mtester"));
        assertTrue(formBean.getDescription().equals("description"));
        assertTrue(formBean.getSurname().equals("misterTest"));
        assertTrue(formBean.getFirstName().equals("testFirst"));
        assertTrue(formBean.getEmail().equals("email"));
        assertTrue(formBean.getTitle().equals("test engineer"));
        assertTrue(formBean.getPhone().equals("+331234567891"));
        assertTrue(formBean.getFacsimile().equals("+331234567890"));
        assertTrue(formBean.getOrg().equals("geOrchestra testing LLC"));
        assertTrue(formBean.getPostalAddress().equals("48 Avenue du Lac du Bourget. 73377 Le Bourget-du-Lac"));

        assertTrue(formBean.toString().equals("EditUserDetailsFormBean [uid=mtester, surname=misterTest, "
                + "givenName=testFirst, email=email, title=test engineer, phone=+331234567891, facsimile=+331234567890, "
                + "org=geOrchestra testing LLC, description=description, postalAddress=48 Avenue du Lac du Bourget. 73377 "
                + "Le Bourget-du-Lac]"));

    }



}
