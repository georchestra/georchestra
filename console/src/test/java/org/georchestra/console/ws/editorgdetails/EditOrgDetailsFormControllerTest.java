package org.georchestra.console.ws.editorgdetails;

import static org.georchestra.commons.security.SecurityHeaders.SEC_ORG;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.Validation;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.ds.roles.Role;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.roles.RoleFactory;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.AccountImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.ByteStreams;

public class EditOrgDetailsFormControllerTest {
    private EditOrgDetailsFormController ctrl;
    private OrgsDao orgsDao = Mockito.mock(OrgsDao.class);
    private RoleDao rolesDao = Mockito.mock(RoleDao.class);
    private AccountDao accountDao = Mockito.mock(AccountDao.class);
    private LogUtils mockLogUtils = Mockito.mock(LogUtils.class);

    private MockHttpServletRequest request = new MockHttpServletRequest();

    private EditOrgDetailsFormBean formBean = new EditOrgDetailsFormBean();

    private Model model = Mockito.mock(Model.class);

    @Before
    public void setUp() throws Exception {
        ctrl = new EditOrgDetailsFormController(orgsDao, new Validation(""));
        ctrl.logUtils = mockLogUtils;

        formBean.setDescription("description");
        formBean.setName("geOrchestra testing LLC");
        formBean.setAddress("48 Avenue du Lac du Bourget. 73377 Le Bourget-du-Lac");
        formBean.setUrl("https://georchestra.org");
        formBean.setId("georTest");

        Org org = new Org();
        org.setId("georTest");
        org.setName("geOrchestra testing LLC");
        org.setOrgType("Non profit");
        org.setAddress("fake address");
        org.setUrl("https://georchestra.org");
        org.setDescription("A test desc");

        Mockito.when(this.orgsDao.findByCommonName(Mockito.eq("georTest"))).thenReturn(org);
        Mockito.when(this.orgsDao.findByCommonName(Mockito.eq(""))).thenReturn(null);

        AccountImpl acc_with_referent = new AccountImpl();
        acc_with_referent.setUid("user_with_referent");
        acc_with_referent.setOrg("georTest");
        AccountImpl acc_with_superuser = new AccountImpl();
        acc_with_superuser.setUid("user_with_superuser");
        acc_with_superuser.setOrg("georTest");
        Mockito.when(this.accountDao.findByUID(Mockito.eq("user_with_referent"))).thenReturn(acc_with_referent);
        Mockito.when(this.accountDao.findByUID(Mockito.eq("user_with_superuser"))).thenReturn(acc_with_superuser);

        Role referentRole = RoleFactory.create("REFERENT", "referent", true);
        referentRole.addUser("user_with_referent");
        Role superUserRole = RoleFactory.create("SUPERUSER", "superuser", true);
        Mockito.when(this.rolesDao.findByCommonName("REFERENT")).thenReturn(referentRole);
        Mockito.when(this.rolesDao.findByCommonName("SUPERUSER")).thenReturn(superUserRole);
    }

    @Test
    public void testInitForm() {
        WebDataBinder dataBinder = new WebDataBinder(null);

        ctrl.initForm(dataBinder);

        List<String> ret = Arrays.asList(dataBinder.getAllowedFields());
        assertTrue(ret.contains("id"));
    }

    @Test
    public void testSetupFormChangeUrl() throws IOException {
        request.addHeader(SEC_ORG, "georTest");
        request.addHeader(SEC_USERNAME, "georTest");
        request.addHeader(SEC_ROLES, "REFERENT;THE_ADMIN");
        BindingResult resultErrors = new MapBindingResult(new HashMap<>(), "errors");
        ctrl = new EditOrgDetailsFormController(orgsDao, new Validation(""));
        ctrl.logUtils = mockLogUtils;
        try (InputStream is = getClass().getResourceAsStream("/georchestra_logo.png")) {
            MultipartFile logo = new MockMultipartFile("image", is);
            formBean.setUrl("https://newurl.com");
            ctrl.edit(model, formBean, logo, resultErrors);
        }
        assertEquals("https://newurl.com", orgsDao.findByCommonName("georTest").getUrl());
    }

    @Test
    public void testSetupFormWithImage() throws IOException {
        request.addHeader(SEC_USERNAME, "georTest");
        request.addHeader(SEC_ORG, "georTest");
        request.addHeader(SEC_ROLES, "MOMO;REFERENT");
        BindingResult resultErrors = new MapBindingResult(new HashMap<>(), "errors");
        ctrl = new EditOrgDetailsFormController(orgsDao, new Validation(""));
        ctrl.logUtils = mockLogUtils;
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        try (InputStream image1 = getClass().getResourceAsStream("/georchestra_logo.png");
                InputStream image2 = getClass().getResourceAsStream("/georchestra_logo.png")) {
            OutputStream out = Base64.getMimeEncoder().wrap(encoded);
            ByteStreams.copy(image1, out);
            String base64Image = new String(encoded.toByteArray());

            MultipartFile logo = new MockMultipartFile("image", image2);
            ctrl.edit(model, formBean, logo, resultErrors);

            assertEquals(base64Image, orgsDao.findByCommonName("georTest").getLogo());
        }
    }

}
