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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.ds.DataServiceException;

import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.dto.orgs.OrgExt;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private static final Log LOG = LogFactory.getLog(EditOrgDetailsFormController.class.getName());

    @Autowired
    protected LogUtils logUtils;

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
    @PreAuthorize("hasAnyRole('REFERENT', 'SUPERUSER')")
    public String setupForm(HttpServletRequest request, Model model) {
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
    @PreAuthorize("hasAnyRole('REFERENT', 'SUPERUSER')")
    public String edit(Model model, @ModelAttribute EditOrgDetailsFormBean formBean,
            @RequestParam(name = "logo") MultipartFile logo, BindingResult resultErrors) throws IOException {
        validation.validateOrgField("url", formBean.getUrl(), resultErrors);
        validation.validateOrgField("address", formBean.getAddress(), resultErrors);
        validation.validateOrgField("description", formBean.getDescription(), resultErrors);

        if (resultErrors.hasErrors()) {
            return "editOrgDetailsForm";
        }

        OrgExt orgExtOld = orgsDao.findExtById(formBean.getId());

        if (logUtils != null) {
            // log each attributes modifications
            logOrgExtChanges(orgExtOld, formBean, logo);
        }

        OrgExt orgExt = modifyOrgExt(orgExtOld, formBean);

        if (!logo.isEmpty()) {
            orgExt.setLogo(transformLogoFileToBase64(logo));
        }
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

    /**
     * Method to compare attributes and create log for each attributes if attribute
     * changes
     * 
     * @param orgExt   OrgExt represent organization to update
     * @param formBean EditOrgDetailsFormBean to get information about user input
     * @param logo     MultipartFile spring to treat a picture as logo
     */
    protected void logOrgExtChanges(OrgExt orgExt, EditOrgDetailsFormBean formBean, MultipartFile logo) {
        AdminLogType type = AdminLogType.ORG_ATTRIBUTE_CHANGED;
        String id = orgExt.getId();

        try {
            if (logo != null && !orgExt.getLogo().equals(transformLogoFileToBase64(logo))) {
                logUtils.createAndLogDetails(id, Org.JSON_LOGO, null, null, type);
            }
        } catch (IOException e) {
            LOG.info("Can't create log and detail for logo replacement.");
        }

        if (!orgExt.getDescription().equals(formBean.getDescription())) {
            logUtils.createAndLogDetails(id, Org.JSON_DESCRIPTION, orgExt.getDescription(), formBean.getDescription(),
                    type);
        }

        if (!orgExt.getUrl().equals(formBean.getUrl())) {
            logUtils.createAndLogDetails(id, Org.JSON_URL, orgExt.getUrl(), formBean.getUrl(), type);
        }

        if (!orgExt.getAddress().equals(formBean.getAddress())) {
            logUtils.createAndLogDetails(id, OrgExt.JSON_ADDRESS, orgExt.getAddress(), formBean.getAddress(), type);
        }
    }

}