package org.georchestra.console.ws.editorgdetails;

import com.google.common.io.ByteStreams;
import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.ds.RoleDao;
import org.georchestra.console.dto.AccountImpl;
import org.georchestra.console.dto.Role;
import org.georchestra.console.dto.RoleFactory;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.dto.orgs.OrgExt;
import org.georchestra.console.ws.utils.Validation;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EditOrgDetailsFormControllerTest {
    private EditOrgDetailsFormController ctrl;
    private OrgsDao orgsDao = Mockito.mock(OrgsDao.class);
    private RoleDao rolesDao = Mockito.mock(RoleDao.class);
    private AccountDao accountDao = Mockito.mock(AccountDao.class);

    private MockHttpServletRequest request = new MockHttpServletRequest();

    private EditOrgDetailsFormBean formBean = new EditOrgDetailsFormBean();

    private Model model = Mockito.mock(Model.class);

    @Before
    public void setUp() throws Exception {
        ctrl = new EditOrgDetailsFormController(orgsDao, new Validation(""));
        formBean.setDescription("description");
        formBean.setName("geOrchestra testing LLC");
        formBean.setAddress("48 Avenue du Lac du Bourget. 73377 Le Bourget-du-Lac");
        formBean.setUrl("https://georchestra.org");
        formBean.setId("georTest");

        OrgExt orgExt = new OrgExt();
        orgExt.setOrgType("Non profit");
        orgExt.setAddress("fake address");
        orgExt.setUrl("https://georchestra.org");
        orgExt.setDescription("A test desc");
        orgExt.setId("georTest");
        Org org = new Org();
        org.setId("georTest");
        org.setName("geOrchestra testing LLC");
        org.setOrgExt(orgExt);

        Mockito.when(this.orgsDao.findByCommonName(Mockito.eq("georTest"))).thenReturn(org);
        Mockito.when(this.orgsDao.findExtById(Mockito.eq("georTest"))).thenReturn(orgExt);
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
        request.addHeader("sec-org", "georTest");
        request.addHeader("sec-username", "georTest");
        request.addHeader("sec-role", "REFERENT,THE_ADMIN");
        BindingResult resultErrors = new MapBindingResult(new HashMap<>(), "errors");
        ctrl = new EditOrgDetailsFormController(orgsDao, new Validation(""));
        try (InputStream is = getClass().getResourceAsStream("/georchestra_logo.png")) {
            MultipartFile logo = new MockMultipartFile("image", is);
            formBean.setUrl("https://newurl.com");
            ctrl.edit(model, formBean, logo, resultErrors);
        }
        assertEquals("https://newurl.com", orgsDao.findExtById("georTest").getUrl());
    }

    @Test
    public void testSetupFormWithImage() throws IOException {
        request.addHeader("sec-username", "georTest");
        request.addHeader("sec-org", "georTest");
        request.addHeader("sec-role", "MOMO,REFERENT");
        BindingResult resultErrors = new MapBindingResult(new HashMap<>(), "errors");
        ctrl = new EditOrgDetailsFormController(orgsDao, new Validation(""));
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        try (InputStream image1 = getClass().getResourceAsStream("/georchestra_logo.png");
                InputStream image2 = getClass().getResourceAsStream("/georchestra_logo.png")) {
            OutputStream out = Base64.getMimeEncoder().wrap(encoded);
            ByteStreams.copy(image1, out);
            String base64Image = new String(encoded.toByteArray());

            MultipartFile logo = new MockMultipartFile("image", image2);
            ctrl.edit(model, formBean, logo, resultErrors);

            assertEquals(base64Image, orgsDao.findExtById("georTest").getLogo());
        }
    }

}
