/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

import static org.georchestra.commons.security.SecurityHeaders.SEC_ORG;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.Validation;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
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

@Controller
@SessionAttributes(types = EditOrgDetailsFormBean.class)
public class EditOrgDetailsFormController {
    private OrgsDao orgsDao;
    private Validation validation;
    private static final String[] FIELDS = { "id", "url", "description", "logo", "name", "address", "mail",
            "orgUniqueId" };

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
        Org org = this.orgsDao.findByCommonName(SecurityHeaders.decode(request.getHeader(SEC_ORG)));
        model.addAttribute(createForm(org));
        model.addAttribute("name", org.getName());
        model.addAttribute("id", org.getId());
        model.addAttribute("logo", org.getLogo());
        model.addAttribute("mail", org.getMail());
        model.addAttribute("orgUniqueId", org.getOrgUniqueId());
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
        validation.validateOrgField("orgUniqueId", formBean.getOrgUniqueId(), resultErrors);
        // TODO validate mail address for the organization ?

        if (resultErrors.hasErrors()) {
            return "editOrgDetailsForm";
        }

        Org orgOrigin = orgsDao.findByCommonName(formBean.getId());
        final Org orgOriginClone = orgOrigin.clone();

        orgOrigin.setDescription(formBean.getDescription());
        orgOrigin.setUrl(formBean.getUrl());
        orgOrigin.setAddress(formBean.getAddress());
        orgOrigin.setOrgUniqueId(formBean.getOrgUniqueId());

        if (!logo.isEmpty()) {
            orgOrigin.setLogo(transformLogoFileToBase64(logo));
        }

        orgOrigin.setMail(formBean.getMail());

        orgsDao.update(orgOrigin);
        model.addAttribute("success", true);

        // log each attributes modifications
        logOrgDetailsChanges(orgOriginClone, formBean, logo);

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
        formBean.setAddress(org.getAddress());
        formBean.setOrgType(org.getOrgType());
        formBean.setMail(org.getMail());
        formBean.setOrgUniqueId(org.getOrgUniqueId());
        return formBean;
    }

    /**
     * Method to compare attributes and create log for each attributes if attribute
     * changes
     *
     * @param org      organization to update
     * @param formBean EditOrgDetailsFormBean to get information about user input
     * @param logo     MultipartFile spring to treat a picture as logo
     */
    protected void logOrgDetailsChanges(Org org, EditOrgDetailsFormBean formBean, MultipartFile logo) {
        AdminLogType type = AdminLogType.ORG_ATTRIBUTE_CHANGED;
        String id = org.getId();

        try {
            if (logo != null && org.getLogo() != null && !org.getLogo().equals(transformLogoFileToBase64(logo))) {
                logUtils.createAndLogDetails(id, Org.JSON_LOGO, null, null, type);
            }
        } catch (IOException e) {
            LOG.info("Can't create admin log and detail for logo replacement.", e);
        }

        if (!org.getDescription().equals(formBean.getDescription())) {
            logUtils.createAndLogDetails(id, Org.JSON_DESCRIPTION, org.getDescription(), formBean.getDescription(),
                    type);
        }

        if (!org.getUrl().equals(formBean.getUrl())) {
            logUtils.createAndLogDetails(id, Org.JSON_URL, org.getUrl(), formBean.getUrl(), type);
        }

        if (!org.getAddress().equals(formBean.getAddress())) {
            logUtils.createAndLogDetails(id, Org.JSON_ADDRESS, org.getAddress(), formBean.getAddress(), type);
        }
        if (StringUtils.isNotEmpty(org.getMail()) && !org.getMail().equals(formBean.getMail())) {
            logUtils.createAndLogDetails(id, Org.JSON_MAIL, org.getMail(), formBean.getMail(), type);
        }

        if (!org.getOrgUniqueId().equals(formBean.getOrgUniqueId())) {
            logUtils.createAndLogDetails(id, Org.JSON_ORG_UNIQ_ID, org.getOrgUniqueId(), formBean.getAddress(), type);
        }

    }

}