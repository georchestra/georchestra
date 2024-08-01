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

package org.georchestra.console.ws.changepassword;

import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.PasswordUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.PasswordType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;

import static org.georchestra.commons.security.SecurityHeaders.SEC_EXTERNAL_AUTHENTICATION;

/**
 * This controller is responsible of manage the user interactions required for
 * changing the user account's password.
 * <p>
 * This controller is associated to the changePasswordForm.jsp view and
 * {@link ChangePasswordFormBean}.
 * </p>
 *
 * @author Mauricio Pazos
 */
@Controller
@SessionAttributes(types = ChangePasswordFormBean.class)
public class ChangePasswordFormController {

    private final AccountDao accountDao;

    @Autowired
    protected PasswordUtils passwordUtils;

    @Autowired
    protected LogUtils logUtils;

    @Autowired
    public ChangePasswordFormController(AccountDao dao) {
        this.accountDao = dao;
    }

    @InitBinder
    public void initForm(WebDataBinder dataBinder) {
        dataBinder.setAllowedFields("password", "confirmPassword");
    }

    /**
     * Initializes the {@link ChangePasswordFormBean} with the uid provided as
     * parameter. The changePasswordForm view is provided as result of this method.
     *
     * @param model
     *
     * @return changePasswordForm view to display
     *
     * @throws DataServiceException
     */
    @RequestMapping(value = "/account/changePassword", method = RequestMethod.GET)
    public String setupForm(HttpServletRequest request, HttpServletResponse response, Model model)
            throws DataServiceException {
        Optional<String> uid = getUsername();
        if (uid.isPresent()) {
            boolean isExternalAuth = Objects.nonNull(request.getHeader(SEC_EXTERNAL_AUTHENTICATION))
                    && Boolean.parseBoolean(SecurityHeaders.decode(request.getHeader(SEC_EXTERNAL_AUTHENTICATION)));
            if (isUserAuthenticatedBySASL(uid.get()) || isExternalAuth) {
                return "userManagedBySASL";
            }

            ChangePasswordFormBean formBean = new ChangePasswordFormBean();
            model.addAttribute(formBean);
            model.addAttribute("pwdUtils", passwordUtils);
            return "changePasswordForm";
        }
        return "forbidden";
    }

    /**
     * Changes the password in the ldap store.
     *
     * @param model
     * @param formBean
     * @param result
     *
     * @return the next view
     *
     * @throws DataServiceException
     */
    @RequestMapping(value = "/account/changePassword", method = RequestMethod.POST)
    public String changePassword(Model model, @ModelAttribute ChangePasswordFormBean formBean, BindingResult result)
            throws DataServiceException {
        Optional<String> username = getUsername();
        if (username.isPresent()) {
            String uid = username.get();
            if (isUserAuthenticatedBySASL(uid)) {
                return "userManagedBySASL";
            }

            passwordUtils.validate(formBean.getPassword(), formBean.getConfirmPassword(), result);
            model.addAttribute("pwdUtils", passwordUtils);
            if (result.hasErrors()) {
                return "changePasswordForm";
            }

            // change the user's password
            String password = formBean.getPassword();
            this.accountDao.changePassword(uid, password);
            model.addAttribute("success", true);

            // log that password was changed for this user
            logUtils.createLog(uid, AdminLogType.USER_PASSWORD_CHANGED, null);

            return "changePasswordForm";
        }
        return "forbidden";
    }

    @ModelAttribute("changePasswordFormBean")
    public ChangePasswordFormBean getChangePasswordFormBean() {
        return new ChangePasswordFormBean();
    }

    private Optional<String> getUsername() {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return Optional.of(user.getUsername());
        } catch (NullPointerException ex) {
            return Optional.empty();
        }
    }

    private boolean checkPermission(String uid) {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return user.getUsername().equals(uid);
        } catch (NullPointerException ex) {
            return false;
        }
    }

    private boolean isUserAuthenticatedBySASL(String uid) throws DataServiceException {
        // check if the user is managed by an external service. if so, then forbid
        // access to change password.
        Account d = this.accountDao.findByUID(uid);
        return d.getPasswordType() == PasswordType.SASL;
    }

}
