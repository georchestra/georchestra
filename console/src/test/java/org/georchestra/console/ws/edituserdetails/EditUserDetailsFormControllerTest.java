/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.console.ws.edituserdetails;

import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.Validation;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.AccountFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.SessionStatus;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class EditUserDetailsFormControllerTest {

    private EditUserDetailsFormController ctrl;

    private AccountDao dao = Mockito.mock(AccountDao.class);
    private OrgsDao orgsDao = Mockito.mock(OrgsDao.class);
    private RoleDao roleDao = Mockito.mock(RoleDao.class);

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockHttpServletResponse response = new MockHttpServletResponse();

    private EditUserDetailsFormBean formBean = new EditUserDetailsFormBean();
    private BindingResult resultErrors = Mockito.mock(BindingResult.class);

    private SessionStatus sessionStatus = Mockito.mock(SessionStatus.class);

    private Model model = Mockito.mock(Model.class);
    private LogUtils mockLogUtils = Mockito.mock(LogUtils.class);
    private Account mtesterAccount;
    private Account mtesterAccountNoOrg;

    @Before
    public void setUp() throws Exception {
        ctrl = new EditUserDetailsFormController(dao, orgsDao, roleDao, new Validation(""));
        ctrl.logUtils = mockLogUtils;

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
        formBean.setIsOAuth2(false);

        // Mock mtester user
        this.mtesterAccount = AccountFactory.createBrief("mtester", "12345", "testFirst", "misterTest", "email",
                "+331234567891", "test engineer", "description");
        mtesterAccount.setOrg("georTest");
        Mockito.when(dao.findByUID(Mockito.eq("mtester"))).thenReturn(mtesterAccount);

        this.mtesterAccountNoOrg = AccountFactory.createBrief("mtesterNoOrg", "12345", "testFirst", "misterTest",
                "email", "+331234567891", "test engineer", "description");
        Mockito.when(dao.findByUID(Mockito.eq("mtesterNoOrg"))).thenReturn(mtesterAccountNoOrg);

        Org org = new Org();
        org.setId("georTest");
        org.setName("geOrchestra testing LLC");

        Mockito.when(this.orgsDao.findByCommonName(Mockito.eq("georTest"))).thenReturn(org);
        Mockito.when(this.orgsDao.findByCommonName(Mockito.eq(""))).thenReturn(null);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testEditUserDetailsFormController() {

    }

    @Test
    public void testInitForm() {
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
        request.addHeader(SEC_USERNAME, "mtester");
        Mockito.when(dao.findByUID(Mockito.anyString())).thenReturn(this.mtesterAccount);

        String ret = ctrl.setupForm(request, response, model);
        assertEquals("editUserDetailsForm", ret);
    }

    /**
     * Testing the general case on EditUserDetail controller, general case.
     */
    @Test
    public void testEdit() throws Exception {
        String incredibleDesc = "The incredible, marvelous and wonderfull company which tests georchestra";
        request.addHeader(SEC_USERNAME, "mtester");
        Org org = new Org();
        org.setId("georTest");
        org.setName("geOrchestra testing LLC");
        org.setDescription(incredibleDesc);
        Mockito.when(this.orgsDao.findByUser(Mockito.any(Account.class))).thenReturn(org);

        String ret = ctrl.edit(request, response, model, formBean, resultErrors, sessionStatus);

        ArgumentCaptor<Boolean> refOrSuCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<ObjectNode> orgWithExtCaptor = ArgumentCaptor.forClass(ObjectNode.class);
        verify(model).addAttribute(Mockito.eq("isReferentOrSuperUser"), refOrSuCaptor.capture());
        verify(model).addAttribute(Mockito.eq("org"), orgWithExtCaptor.capture());
        assertEquals("editUserDetailsForm", ret);
        assertNotNull("expected a isReferentOrSuperUser in the model, null returned", refOrSuCaptor.getValue());
        ObjectNode node = orgWithExtCaptor.getValue();
        String desc = node.get("description").asText();
        assertEquals("Description unexpected, missing OrgExt attributes ?", desc, incredibleDesc);
    }

    @Test
    public void testSetupFormNoOrg() throws Exception {
        request.addHeader(SEC_USERNAME, "mtesterNoOrg");
        Mockito.when(dao.findByUID(Mockito.anyString())).thenReturn(this.mtesterAccountNoOrg);

        String ret = ctrl.setupForm(request, response, model);

        assertEquals("editUserDetailsForm", ret);
    }

    @Test
    public void testEditNoOrg() throws Exception {
        request.addHeader(SEC_USERNAME, "mtesterNoOrg");
        Mockito.when(dao.findByUID(Mockito.anyString())).thenReturn(this.mtesterAccountNoOrg);
        String ret = ctrl.edit(request, response, model, formBean, resultErrors, sessionStatus);

        assertEquals("editUserDetailsForm", ret);
    }

    /**
     * Tests the underlying form bean class (EditUserDetailsFormBean).
     */
    @Test
    public void testEditUserDetailsFormBean() {

        assertEquals("mtester", formBean.getUid());
        assertEquals("description", formBean.getDescription());
        assertEquals("misterTest", formBean.getSurname());
        assertEquals("testFirst", formBean.getFirstName());
        assertEquals("email", formBean.getEmail());
        assertEquals("test engineer", formBean.getTitle());
        assertEquals("+331234567891", formBean.getPhone());
        assertEquals("+331234567890", formBean.getFacsimile());
        assertEquals("geOrchestra testing LLC", formBean.getOrg());
        assertEquals("48 Avenue du Lac du Bourget. 73377 Le Bourget-du-Lac", formBean.getPostalAddress());

        assertEquals(formBean.toString(), "EditUserDetailsFormBean [uid=mtester, surname=misterTest, "
                + "givenName=testFirst, email=email, title=test engineer, phone=+331234567891, facsimile=+331234567890, "
                + "org=geOrchestra testing LLC, description=description, postalAddress=48 Avenue du Lac du Bourget. 73377 "
                + "Le Bourget-du-Lac]");

    }

    @Test
    public void testEditUserMissingRequiredField() throws IOException {
        request.addHeader(SEC_USERNAME, "mtester");
        EditUserDetailsFormBean formBeanWithMissingField = new EditUserDetailsFormBean();
        BindingResult resultErrors = new MapBindingResult(new HashMap<>(), "errors");
        ctrl = new EditUserDetailsFormController(dao, orgsDao, roleDao,
                new Validation("firstName,surname,org,orgType"));

        ctrl.edit(request, response, model, formBeanWithMissingField, resultErrors, sessionStatus);

        assertEquals("required", resultErrors.getFieldError("firstName").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("surname").getDefaultMessage());
        assertEquals(2, resultErrors.getFieldErrorCount());
    }

    @Test
    public void specialValidators() throws IOException {
        request.addHeader(SEC_USERNAME, "mtester");
        EditUserDetailsFormBean formBeanWithMissingField = new EditUserDetailsFormBean();
        BindingResult resultErrors = new MapBindingResult(new HashMap<>(), "errors");
        ctrl = new EditUserDetailsFormController(dao, orgsDao, roleDao,
                new Validation("phone,facsimile,title,description,postalAddress"));

        ctrl.edit(request, response, model, formBeanWithMissingField, resultErrors, sessionStatus);

        assertEquals("required", resultErrors.getFieldError("phone").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("facsimile").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("title").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("description").getDefaultMessage());
        assertEquals("required", resultErrors.getFieldError("postalAddress").getDefaultMessage());
    }

    @Test
    public void testCreateForm() {
        ctrl = new EditUserDetailsFormController(dao, orgsDao, roleDao,
                new Validation("phone,facsimile,title,description,postalAddress"));
        EditUserDetailsFormBean editUserDetailsFormBean = ctrl.createForm(mtesterAccount);
        assertEquals("email", editUserDetailsFormBean.getEmail());
        assertEquals("mtester", editUserDetailsFormBean.getUid());
        assertEquals("geOrchestra testing LLC", editUserDetailsFormBean.getOrg());

    }
}
