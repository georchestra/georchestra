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

package org.georchestra.console.ws.passwordrecovery;

import java.io.IOException;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.bs.ReCaptchaParameters;
import org.georchestra.console.ds.UserTokenDao;
import org.georchestra.console.mailservice.EmailFactory;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.RecaptchaUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.PasswordType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.NameNotFoundException;
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

/**
 * Manage the user interactions required to implement the lost password
 * workflow:
 * <p>
 * <ul>
 *
 * <li>Present a form in order to ask for the user's mail.</li>
 *
 * <li>If the given email matches one of the LDAP users, an email is sent to
 * this user with a unique http URL to reset his password.</li>
 *
 * <li>As result of this interaction the view EmailSentForm.jsp is
 * presented</li>
 * </ul>
 * </p>
 *
 * @author Mauricio Pazos
 */
@Controller
@SessionAttributes(types = PasswordRecoveryFormBean.class)
public class PasswordRecoveryFormController {

    protected static final Log LOG = LogFactory.getLog(PasswordRecoveryFormController.class.getName());

    // collaborations
    private final AccountDao accountDao;
    private final RoleDao roleDao;
    private EmailFactory emailFactory;
    private final UserTokenDao userTokenDao;
    private final ReCaptchaParameters reCaptchaParameters;

    @Autowired
    private boolean reCaptchaActivated;

    @Autowired
    protected LogUtils logUtils;

    @Value("${publicContextPath:/console}")
    private String publicContextPath;

    @Value("https://${domainName}")
    private String publicUrl;

    @Autowired
    public PasswordRecoveryFormController(AccountDao dao, RoleDao gDao, EmailFactory emailFactory,
            UserTokenDao userTokenDao, ReCaptchaParameters reCaptchaParameters) {
        this.accountDao = dao;
        this.roleDao = gDao;
        this.emailFactory = emailFactory;
        this.userTokenDao = userTokenDao;
        this.reCaptchaParameters = reCaptchaParameters;
    }

    @InitBinder
    public void initForm(WebDataBinder dataBinder) {

        dataBinder.setAllowedFields("email", "recaptcha_response_field");
    }

    @RequestMapping(value = "/account/passwordRecovery", method = RequestMethod.GET)
    public String setupForm(HttpServletRequest request, @RequestParam(value = "email", required = false) String email,
            Model model) throws DataServiceException {

        if (email != null) {
            PasswordType pt = getPasswordType(email);
            if (pt == PasswordType.SASL) {
                return "userManagedBySASL";
            }
        }
        HttpSession session = request.getSession();

        String errmsg = (String) session.getAttribute("errmsg");
        if ("bad.token".equals(errmsg)) {
            session.removeAttribute("errmsg");
            model.addAttribute("badtoken", true);
        }

        PasswordRecoveryFormBean formBean = new PasswordRecoveryFormBean();
        formBean.setEmail(email);

        model.addAttribute(formBean);
        model.addAttribute("recaptchaActivated", this.reCaptchaActivated);
        session.setAttribute("reCaptchaPublicKey", this.reCaptchaParameters.getPublicKey());

        return "passwordRecoveryForm";
    }

    /**
     * Generates a new unique http URL based on a token, then an e-mail is sent to
     * the user with instruction to change his password.
     *
     *
     * @param formBean      Contains the user's email
     * @param resultErrors  will be updated with the list of found errors.
     * @param sessionStatus
     *
     * @return the next view
     *
     * @throws IOException
     */
    @RequestMapping(value = "/account/passwordRecovery", method = RequestMethod.POST)
    public String generateToken(HttpServletRequest request, @ModelAttribute PasswordRecoveryFormBean formBean,
            BindingResult resultErrors, SessionStatus sessionStatus) throws IOException {

        if (reCaptchaActivated) {
            RecaptchaUtils.validate(reCaptchaParameters, formBean.getRecaptcha_response_field(), resultErrors);
        }
        if (resultErrors.hasErrors()) {
            return "passwordRecoveryForm";
        }

        try {
            Account account = this.accountDao.findByEmail(formBean.getEmail());
            // Finds the user using the email as key, if it exists a new token is generated
            // to include in the unique http URL.

            if (account.isPending()) {
                throw new NameNotFoundException("User is pending");
            }

            ServletContext servletContext = request.getSession().getServletContext();
            if ((account.getOAuth2Provider() == null) && !account.getIsExternalAuth()) {
                String token = UUID.randomUUID().toString();

                // if there is a previous token it is removed
                if (this.userTokenDao.exist(account.getUid())) {
                    this.userTokenDao.delete(account.getUid());
                }

                this.userTokenDao.insertToken(account.getUid(), token);

                String url = makeChangePasswordURL(publicUrl, publicContextPath, token);

                this.emailFactory.sendChangePasswordEmail(servletContext, account.getEmail(), account.getCommonName(),
                        account.getUid(), url);
                sessionStatus.setComplete();

                // log role deleted
                logUtils.createLog(account.getUid(), AdminLogType.EMAIL_RECOVERY_SENT, "");
            } else {
                this.emailFactory.sendChangePasswordOAuth2Email(servletContext, account.getEmail(),
                        account.getCommonName());
            }
        } catch (DataServiceException | MessagingException e) {
            throw new IOException(e);
        } catch (NameNotFoundException e) {
        }

        return "emailWasSentForPasswordChange";
    }

    /**
     * Create the URL to change the password based on the provided token
     *
     * @return a new URL to change password
     */
    protected String makeChangePasswordURL(final String publicUrl, final String contextPath, final String token) {
        String url = UriComponentsBuilder.fromHttpUrl(publicUrl).path(contextPath).path("/account/newPassword")
                .query("token={token}").buildAndExpand(token).toUriString();

        if (LOG.isDebugEnabled()) {
            LOG.debug("generated url:" + url);
        }

        return url;
    }

    @ModelAttribute("passwordRecoveryFormBean")
    public PasswordRecoveryFormBean getPasswordRecoveryFormBean() {
        return new PasswordRecoveryFormBean();
    }

    public void setEmailFactory(EmailFactory emailFactory) {
        this.emailFactory = emailFactory;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public void setPublicContextPath(String publicContextPath) {
        this.publicContextPath = publicContextPath;
    }

    private PasswordType getPasswordType(String email) throws DataServiceException {
        Account a = this.accountDao.findByEmail(email);
        return a.getPasswordType();
    }
}
