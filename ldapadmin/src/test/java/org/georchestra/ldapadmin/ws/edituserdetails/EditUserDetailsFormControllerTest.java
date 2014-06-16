package org.georchestra.ldapadmin.ws.edituserdetails;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.dto.Account;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;


public class EditUserDetailsFormControllerTest {

    private EditUserDetailsFormController ctrl;

    private AccountDao dao = Mockito.mock(AccountDao.class);

    @Before
    public void setUp() throws Exception {
        ctrl = new EditUserDetailsFormController(dao);
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
    public void testSetupForm() throws Exception {
        Model model = Mockito.mock(Model.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Account mockedAccount = Mockito.mock(Account.class);
        Mockito.when(dao.findByUID(Mockito.anyString())).thenReturn(mockedAccount);

        String ret = ctrl.setupForm(request, response, model);

        assertTrue(ret.equals("editUserDetailsForm"));
    }

    @Test
    public void testEdit() {
        fail("Not yet implemented");
    }

    /**
     * Tests the underlying form bean class (EditUserDetailsFormBean)
     */
    @Test
    public void testEditUserDetailsFormBean() {
        EditUserDetailsFormBean tested = new EditUserDetailsFormBean();

        tested.setDescription("description");
        tested.setEmail("email");
        tested.setFacsimile("+331234567890");
        tested.setFirstName("testFirst");
        tested.setOrg("geOrchestra testing LLC");
        tested.setPhone("+331234567891");
        tested.setPostalAddress("48 Avenue du Lac du Bourget. 73377 Le Bourget-du-Lac");
        tested.setSurname("misterTest");
        tested.setTitle("test engineer");
        tested.setUid("mtester");

        assertTrue(tested.getUid().equals("mtester"));
        assertTrue(tested.getDescription().equals("description"));
        assertTrue(tested.getSurname().equals("misterTest"));
        assertTrue(tested.getFirstName().equals("testFirst"));
        assertTrue(tested.getEmail().equals("email"));
        assertTrue(tested.getTitle().equals("test engineer"));
        assertTrue(tested.getPhone().equals("+331234567891"));
        assertTrue(tested.getFacsimile().equals("+331234567890"));
        assertTrue(tested.getOrg().equals("geOrchestra testing LLC"));
        assertTrue(tested.getPostalAddress().equals("48 Avenue du Lac du Bourget. 73377 Le Bourget-du-Lac"));


        assertTrue(tested.toString().equals("EditUserDetailsFormBean [uid=mtester, surname=misterTest, "
                + "givenName=testFirst, email=email, title=test engineer, phone=+331234567891, facsimile=+331234567890, "
                + "org=geOrchestra testing LLC, description=description, postalAddress=48 Avenue du Lac du Bourget. 73377 "
                + "Le Bourget-du-Lac]"));

    }



}
