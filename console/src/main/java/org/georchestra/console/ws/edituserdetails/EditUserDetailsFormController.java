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

package org.georchestra.console.ws.edituserdetails;

import static org.georchestra.commons.security.SecurityHeaders.SEC_EXTERNAL_AUTHENTICATION;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.Setter;
import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.PasswordUtils;
import org.georchestra.console.ws.utils.Validation;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.ds.roles.Role;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.AccountImpl;
import org.georchestra.ds.users.DuplicatedEmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Support for the Edit Account user interactions.
 *
 * @author Mauricio Pazos
 */
@Controller
@SessionAttributes(types = EditUserDetailsFormBean.class)
public class EditUserDetailsFormController {

    private final RoleDao roleDao;
    private final OrgsDao orgsDao;
    private final AccountDao accountDao;

    private Validation validation;

    @Setter
    private @Value("${gdpr.allowAccountDeletion:true}") Boolean gdprAllowAccountDeletion;

    private @Value("${gdpr.displayMemberList:false}") Boolean displayMemberList;

    @Autowired
    protected LogUtils logUtils;

    @Autowired
    protected PasswordUtils passwordUtils;

    @Autowired
    public EditUserDetailsFormController(AccountDao dao, OrgsDao orgsDao, RoleDao roleDao, Validation validation) {
        this.accountDao = dao;
        this.orgsDao = orgsDao;
        this.roleDao = roleDao;
        this.validation = validation;

    }

    private static final String[] fields = { "uid", "firstName", "surname", "email", "title", "phone", "facsimile",
            "org", "description", "postalAddress" };

    @InitBinder
    public void initForm(WebDataBinder dataBinder) {

        dataBinder.setAllowedFields(fields);
    }

    /**
     * Retrieves the account data and sets the model before presenting the edit form
     * view.
     *
     * @param model
     * @return the edit form view
     * @throws IOException
     */
    @RequestMapping(value = "/account/userdetails", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String setupForm(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        try {
            String username = SecurityHeaders.decode(request.getHeader(SEC_USERNAME));
            boolean isExternalAuth = Objects.nonNull(request.getHeader(SEC_EXTERNAL_AUTHENTICATION))
                    && Boolean.parseBoolean(SecurityHeaders.decode(request.getHeader(SEC_EXTERNAL_AUTHENTICATION)));
            Account userAccount = this.accountDao.findByUID(username);
            userAccount.setIsExternalAuth(isExternalAuth);
            model.addAttribute(createForm(userAccount));
            Org org = orgsDao.findByUser(userAccount);
            model.addAttribute("org", orgToJson(org));
            model.addAttribute("isReferentOrSuperUser", isReferentOrSuperUser(userAccount));
            model.addAttribute("gdprAllowAccountDeletion", gdprAllowAccountDeletion);

            HttpSession session = request.getSession();
            for (String f : fields) {
                if (this.validation.isUserFieldRequired(f)) {
                    session.setAttribute(f + "Required", "true");
                }
            }

            return "editUserDetailsForm";

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    private boolean isReferentOrSuperUser(Account userAccount) throws DataServiceException {
        List<Role> roles = roleDao.findAllForUser(userAccount);
        return roles.stream().anyMatch(n -> (n.getName().equals("SUPERUSER") || n.getName().equals("REFERENT")));
    }

    /**
     * Creates a form based on the account data.
     *
     * @param account input data
     */
    EditUserDetailsFormBean createForm(final Account account) {

        EditUserDetailsFormBean formBean = new EditUserDetailsFormBean();

        formBean.setUid(account.getUid());
        formBean.setEmail(account.getEmail());
        formBean.setFirstName(account.getGivenName());
        formBean.setSurname(account.getSurname());
        formBean.setTitle(account.getTitle());
        formBean.setPhone(account.getPhone());
        formBean.setFacsimile(account.getFacsimile());
        formBean.setDescription(account.getDescription());
        formBean.setPostalAddress(account.getPostalAddress());
        formBean.setIsExternalAuth(account.getIsExternalAuth());
        String org = account.getOrg();
        if (!org.equals("")) {
            formBean.setOrg(orgsDao.findByCommonName(org).getName());
        }
        formBean.setIsOAuth2(account.getOAuth2Provider() != null);

        return formBean;
    }

    /**
     * Generates a new password, then an e-mail is sent to the user to inform that a
     * new password is available.
     *
     * @param formBean      Contains the user's email
     * @param resultErrors  will be updated with the list of found errors.
     * @param sessionStatus
     * @return the next view
     * @throws IOException
     */
    @RequestMapping(value = "/account/userdetails", method = RequestMethod.POST)
    public String edit(HttpServletRequest request, HttpServletResponse response, Model model,
            @ModelAttribute EditUserDetailsFormBean formBean, BindingResult resultErrors, SessionStatus sessionStatus)
            throws IOException {
        String uid = formBean.getUid();
        try {
            String username = SecurityHeaders.decode(request.getHeader(SEC_USERNAME));
            if (!username.equals(uid))
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (NullPointerException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }

        model.addAttribute("gdprAllowAccountDeletion", gdprAllowAccountDeletion);

        // Validate first name and surname
        validation.validateUserFieldWithSpecificMsg("firstName", formBean.getFirstName(), resultErrors);
        validation.validateUserFieldWithSpecificMsg("surname", formBean.getSurname(), resultErrors);

        validation.validateUserField("phone", formBean.getPhone(), resultErrors);
        validation.validateUserField("facsimile", formBean.getFacsimile(), resultErrors);
        validation.validateUserField("title", formBean.getTitle(), resultErrors);
        validation.validateUserField("description", formBean.getDescription(), resultErrors);
        validation.validateUserField("postalAddress", formBean.getPostalAddress(), resultErrors);

        if (resultErrors.hasErrors())
            return "editUserDetailsForm";

        // updates the account details
        try {

            String username = SecurityHeaders.decode(request.getHeader(SEC_USERNAME));
            Account originalAccount = this.accountDao.findByUID(username);
            Account modifiedAccount = this.accountDao.findByUID(username);
            Account account = modify(modifiedAccount, formBean);
            accountDao.update(account);

            model.addAttribute("success", true);
            model.addAttribute("pwdUtils", passwordUtils);
            Org org = orgsDao.findByUser(account);
            model.addAttribute("org", orgToJson(org));
            model.addAttribute("isReferentOrSuperUser", isReferentOrSuperUser(account));

            // create log for each account modification
            if (logUtils != null) {
                logUtils.logChanges(modifiedAccount, originalAccount);
            }
            return "editUserDetailsForm";

        } catch (DuplicatedEmailException e) {

            // right now the email cannot be edited (review requirement)
            // resultErrors.addError(new ObjectError("email", "Exist a user with this
            // e-mail"));
            return "createAccountForm";

        } catch (DataServiceException e) {

            throw new IOException(e);
        }

    }

    /**
     * Modifies the account using the values present in the formBean parameter
     *
     * @param account
     * @param formBean
     * @return modified account
     */
    private Account modify(Account account, EditUserDetailsFormBean formBean) {
        account.setGivenName(formBean.getFirstName());
        account.setSurname(formBean.getSurname());
        account.setTitle(formBean.getTitle());
        account.setPhone(formBean.getPhone());
        account.setFacsimile(formBean.getFacsimile());
        account.setDescription(formBean.getDescription());
        account.setPostalAddress(formBean.getPostalAddress());

        return account;
    }

    @ModelAttribute("editUserDetailsFormBean")
    public EditUserDetailsFormBean getEditUserDetailsFormBean() {
        return new EditUserDetailsFormBean();
    }

    private ObjectNode orgToJson(Org org) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (org == null) {
            return objectMapper.createObjectNode();
        }

        ObjectNode jsonOrg = objectMapper.valueToTree(org);
        jsonOrg.replace("members",
                displayMemberList
                        ? org.getMembers().stream().map(x -> uncheckedFindAccountByUID(x, objectMapper)).collect(
                                () -> new ArrayNode(objectMapper.getNodeFactory()),
                                (col, elem) -> col.add(elem.retain("sn", "givenName")), ArrayNode::addAll)
                        : objectMapper.createArrayNode());
        return jsonOrg;
    }

    private ObjectNode uncheckedFindAccountByUID(String uid, ObjectMapper objectMapper) {
        Account account;
        try {
            account = this.accountDao.findByUID(uid);
        } catch (Exception e) {
            account = new AccountImpl();
            account.setUid(uid);
        }
        return objectMapper.valueToTree(account);
    }
}
