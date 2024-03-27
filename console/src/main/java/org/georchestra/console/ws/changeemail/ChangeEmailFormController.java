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

package org.georchestra.console.ws.changeemail;

import org.apache.commons.validator.routines.EmailValidator;
import org.georchestra.console.ds.UserTokenDao;
import org.georchestra.console.mailservice.EmailFactory;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.Validation;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.DuplicatedEmailException;
import org.georchestra.ds.users.PasswordType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.NameNotFoundException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.util.UriComponentsBuilder;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * This controller is responsible of manage the user interactions required for
 * changing the user account's password.
 * <p>
 * This controller is associated to the changePasswordForm.jsp view and
 * {@link ChangeEmailFormBean}.
 * </p>
 *
 * @author Mauricio Pazos
 */
@Controller
@SessionAttributes(types = ChangeEmailFormBean.class)
public class ChangeEmailFormController {

    private final AccountDao accountDao;
    private EmailFactory emailFactory;
    private final UserTokenDao userTokenDao;
    private Validation validation;

    @Autowired
    protected LogUtils logUtils;

    @Value("${publicContextPath:/console}")
    private String publicContextPath;

    @Value("https://${domainName}")
    private String publicUrl;

    @Autowired
    public ChangeEmailFormController(AccountDao accountDao, EmailFactory emailFactory, UserTokenDao userTokenDao,
            Validation validation) {
        this.accountDao = accountDao;
        this.emailFactory = emailFactory;
        this.userTokenDao = userTokenDao;
        this.validation = validation;
    }

    @InitBinder
    public void initForm(WebDataBinder dataBinder) {
        dataBinder.setAllowedFields("newAddress");
    }

    /**
     * Initializes the {@link ChangeEmailFormBean} with the email address provided
     * as parameter. The {@link ChangeEmailFormBean} view is provided as result of
     * this method.
     *
     * @param model
     *
     * @return changeEmailForm view to display
     *
     * @throws DataServiceException
     */
    @RequestMapping(value = "/account/changeEmail", method = RequestMethod.GET)
    public String setupForm(Model model) throws DataServiceException {
        Account account = getAccount();
        if (isUserAuthenticatedBySASL(account)) {
            return "userManagedBySASL";
        }

        ChangeEmailFormBean formBean = new ChangeEmailFormBean();
        model.addAttribute(formBean);
        return "changeEmailForm";
    }

    /**
     * Changes the password in the ldap store.
     *
     * @param formBean
     *
     * @return the next view
     *
     * @throws DataServiceException
     */
    @RequestMapping(value = "/account/changeEmail", method = RequestMethod.POST)
    public String changeEmail(HttpServletRequest request, @ModelAttribute ChangeEmailFormBean formBean,
            BindingResult result, SessionStatus sessionStatus) throws DataServiceException, IOException {

        // email validation
        if (validation.validateUserFieldWithSpecificMsg("newAddress", formBean.getNewAddress(), result)
                && !EmailValidator.getInstance().isValid(formBean.getNewAddress())) {
            result.rejectValue("newAddress", "email.error.invalidFormat", "Invalid Format");
            return "changeEmailForm";
        }

        Account account = getAccount();
        String uid = account.getUid();
        String newAddress = formBean.getNewAddress();
        if (isUserAuthenticatedBySASL(account)) {
            return "userManagedBySASL";
        }

        try {
            this.accountDao.findByEmail(newAddress);
            result.rejectValue("newAddress", "email.error.exist",
                    new String[] { String.format("%s%s", publicContextPath, "/account/changeEmail") },
                    "there is a user with this e-mail");
            return "changeEmailForm";
        } catch (NameNotFoundException e) {
        }

        String token = UUID.randomUUID().toString();

        if (this.userTokenDao.exist(newAddress)) {
            this.userTokenDao.delete(newAddress);
        }

        this.userTokenDao.insertToken(newAddress, token);

        String url = makeChangeEmailURL(publicUrl, publicContextPath, token);

        try {
            ServletContext servletContext = request.getSession().getServletContext();
            this.emailFactory.sendChangeEmailAddressEmail(servletContext, newAddress, account.getCommonName(),
                    account.getUid(), url);
            sessionStatus.setComplete();

        } catch (MessagingException e) {
            throw new IOException(e);
        }

        logUtils.createLog(account.getUid(), AdminLogType.EMAIL_CHANGE_EMAIL_SENT, "");

        return "emailWasSentForNewEmail";
    }

    /**
     * Changes the new email address
     *
     * @param token
     * @param sessionStatus
     *
     * @return the next view
     *
     * @throws IOException
     */
    @RequestMapping(value = "/account/validateEmail", method = RequestMethod.GET)
    public void validateEmail(@RequestParam(name = "token", required = false) String token,
            HttpServletResponse response, SessionStatus sessionStatus) throws IOException {
        try {
            final String newAddress = this.userTokenDao.findUserByToken(token);
            Account account = getAccount();

            account.setEmail(newAddress);
            this.accountDao.update(account);

            this.userTokenDao.delete(newAddress);

            sessionStatus.setComplete();

        } catch (DataServiceException e) {
            throw new IOException(e);
        } catch (DuplicatedEmailException e) {
        }

        response.sendRedirect(
                UriComponentsBuilder.fromPath(publicContextPath).path("/account/userdetails").toUriString());
    }

    @ModelAttribute("changeEmailFormBean")
    public ChangeEmailFormBean getChangePasswordFormBean() {
        return new ChangeEmailFormBean();
    }

    private Account getAccount() throws DataServiceException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return this.accountDao.findByUID(user.getUsername());
    }

    private boolean isUserAuthenticatedBySASL(Account account) throws DataServiceException {
        // check if the user is managed by an external service. if so, then forbid
        // access to change password.
        return account.getPasswordType() == PasswordType.SASL;
    }

    /**
     * Create the URL to change the email address based on the provided token
     *
     * @return a new URL to change email address
     */
    protected String makeChangeEmailURL(final String publicUrl, final String contextPath, final String token) {
        String url = UriComponentsBuilder.fromHttpUrl(publicUrl).path(contextPath).path("/account/validateEmail")
                .query("token={token}").buildAndExpand(token).toUriString();

//        if (logUtils.isDebugEnabled()) {
//            logUtils.debug("generated url:" + url);
//        }

        return url;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public void setPublicContextPath(String publicContextPath) {
        this.publicContextPath = publicContextPath;
    }
}
