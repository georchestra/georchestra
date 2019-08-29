/*
 * Copyright (C) 2009-2019 by the geOrchestra PSC
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
package org.georchestra.console.ws.editorgdetails;

import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.dto.orgs.OrgExt;
import org.georchestra.console.ws.utils.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;

@Controller
@SessionAttributes(types = EditOrgDetailsFormBean.class)
public class EditOrgDetailsFormController {
    private OrgsDao orgsDao;
    private Validation validation;
    private static final String[] FIELDS = { "id", "url", "description", "logo", "name", "address" };

    @Autowired
    public EditOrgDetailsFormController(OrgsDao orgsDao, Validation validation) {
        this.orgsDao = orgsDao;
        this.validation = validation;
    }


    @InitBinder
    public void initForm(WebDataBinder dataBinder) {
        dataBinder.setAllowedFields(FIELDS);
    }

    @ModelAttribute("editOrgDetailsFormBean")
    public EditOrgDetailsFormBean getEditOrgDetailsFormBean() {
        return new EditOrgDetailsFormBean();
    }

    /**
     * Retrieves the org data and sets the model before presenting the edit form
     * view.
     *
     * @param model
     * @return the edit form view
     * @throws IOException
     */
    @RequestMapping(value = "/account/orgdetails", method = RequestMethod.GET)
    public String setupForm(HttpServletRequest request, HttpServletResponse response, Model model)
            throws IOException, DataServiceException {

        if (request.getHeader("sec-username") == null && request.getHeader("sec-org") == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        Org org = this.orgsDao.findByCommonName(request.getHeader("sec-org"));
        OrgExt orgExt = this.orgsDao.findExtById(org.getId());
        org.setOrgExt(orgExt);

        model.addAttribute(createForm(org));
        model.addAttribute("name", org.getName());
        model.addAttribute("id", org.getId());
        model.addAttribute("logo", org.getLogo());
        HttpSession session = request.getSession();
        for (String f : FIELDS) {
            if (validation.isOrgFieldRequired(f)) {
                session.setAttribute(f + "Required", "true");
            }
        }
        return "editOrgDetailsForm";
    }

    @RequestMapping(value = "/account/orgdetails", method = RequestMethod.POST)
    public String edit(HttpServletRequest request, HttpServletResponse response, Model model,
            @ModelAttribute EditOrgDetailsFormBean formBean, @RequestParam(name = "logo") MultipartFile logo,
            BindingResult resultErrors, SessionStatus sessionStatus) throws IOException {
        if (request.getHeader("sec-username") == null && request.getHeader("sec-org") == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        validation.validateOrgField("url", formBean.getUrl(), resultErrors);
        validation.validateOrgField("address", formBean.getAddress(), resultErrors);
        validation.validateOrgField("description", formBean.getAddress(), resultErrors);

        if (resultErrors.hasErrors()) {
            return "editOrgDetailsForm";
        }

        OrgExt orgExt = modifyOrgExt(orgsDao.findExtById(formBean.getId()), formBean);
        orgExt.setLogo(transformLogoFileToBase64(logo));
        orgsDao.update(orgExt);
        model.addAttribute("success", true);

        return "redirect:/account/userdetails";

    }

    private String transformLogoFileToBase64(MultipartFile logo) throws IOException {
        byte[] base64Encoded = Base64.getMimeEncoder().encode(logo.getBytes());
        return new String(base64Encoded);
    }

    private EditOrgDetailsFormBean createForm(Org org) {
        EditOrgDetailsFormBean formBean = new EditOrgDetailsFormBean();
        formBean.setId(org.getId());
        formBean.setName(org.getName());
        formBean.setShortName(org.getShortName());
        formBean.setDescription(org.getDescription());
        formBean.setUrl(org.getUrl());
        formBean.setAddress(org.getOrgAddress());
        formBean.setOrgType(org.getOrgType());
        return formBean;

    }

    private OrgExt modifyOrgExt(OrgExt orgExt, EditOrgDetailsFormBean formBean) {
        orgExt.setDescription(formBean.getDescription());
        orgExt.setUrl(formBean.getUrl());
        orgExt.setAddress(formBean.getAddress());
        return orgExt;
    }

}