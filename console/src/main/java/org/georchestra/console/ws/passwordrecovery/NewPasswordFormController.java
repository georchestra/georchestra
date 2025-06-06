/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.console.ws.passwordrecovery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.ds.UserTokenDao;
import org.georchestra.console.ws.utils.PasswordUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.AccountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This controller implements the interactions required to ask for a new
 * password based on a token provide.
 *
 * @author Mauricio Pazos
 *
 */
@Controller
@SessionAttributes(types = NewPasswordFormBean.class)
public class NewPasswordFormController {

    private static final Log LOG = LogFactory.getLog(NewPasswordFormController.class.getName());

    @Autowired
    protected PasswordUtils passwordUtils;

    private final AccountDao accountDao;
    private final UserTokenDao userTokenDao;

    @Autowired
    private boolean reCaptchaActivated;

    public NewPasswordFormController(AccountDao accountDao, UserTokenDao userTokenDao) {
        this.accountDao = accountDao;
        this.userTokenDao = userTokenDao;
    }

    @InitBinder
    public void initForm(WebDataBinder dataBinder) {
        dataBinder.setAllowedFields("password", "confirmPassword");
    }

    /**
     * Search the user associated to the provided token, then initialize the
     * {@link NewPasswordFormBean}. If the token is not valid (it didn't exist in
     * the system registry) the PasswordRecoveryForm is presented to offer a new
     * chance to the user.
     *
     * @param token the token was generated by the
     *              {@link PasswordRecoveryFormController}}
     * @param model
     *
     * @return newPasswordForm or passwordRecoveryForm
     *
     * @throws IOException
     */
    @GetMapping("/account/newPassword")
    public String setupForm(@RequestParam(required = false) String token, Model model, HttpServletRequest request)
            throws IOException {
        model.addAttribute("recaptchaActivated", reCaptchaActivated);
        try {
            final String uid = this.userTokenDao.findUidWithoutAdditionalInfo(token);

            NewPasswordFormBean formBean = new NewPasswordFormBean();

            formBean.setToken(token);
            formBean.setUid(uid);

            model.addAttribute(formBean);
            model.addAttribute("pwdUtils", passwordUtils);

            return "newPasswordForm";

        } catch (NameNotFoundException e) {
            model.asMap().clear();
            request.getSession().setAttribute("errmsg", "bad.token");
            return "redirect:/account/passwordRecovery";
        } catch (DataServiceException e) {

            LOG.error("cannot insert the setup the passwordRecoveryForm. " + e.getMessage());

            throw new IOException(e);
        }

    }

    /**
     * Registers the new password, if it is valid.
     *
     * @param formBean
     * @param result
     * @param sessionStatus
     *
     * @return the next view
     *
     * @throws IOException
     */
    @PostMapping("/account/newPassword")
    public String newPassword(@ModelAttribute NewPasswordFormBean formBean, BindingResult result,
            SessionStatus sessionStatus) throws IOException {

        passwordUtils.validate(formBean.getPassword(), formBean.getConfirmPassword(), result);

        if (result.hasErrors()) {

            return "newPasswordForm";
        }

        // changes the user's password and removes the token
        try {

            String uid = formBean.getUid();
            String password = formBean.getPassword();

            this.accountDao.changePassword(uid, password);

            this.userTokenDao.delete(uid);

            sessionStatus.setComplete();

            return "passwordUpdated";

        } catch (DataServiceException e) {
            LOG.error("cannot set the the new password. " + e.getMessage());

            throw new IOException(e);

        }
    }

    @ModelAttribute("newPasswordFormBean")
    public NewPasswordFormBean getNewPasswordFormBean() {
        return new NewPasswordFormBean();
    }
}
