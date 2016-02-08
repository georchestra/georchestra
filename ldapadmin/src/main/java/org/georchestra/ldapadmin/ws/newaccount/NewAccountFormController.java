/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

package org.georchestra.ldapadmin.ws.newaccount;

import java.io.IOException;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.tanesha.recaptcha.ReCaptcha;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.bs.Moderator;
import org.georchestra.ldapadmin.bs.ReCaptchaParameters;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.DuplicatedEmailException;
import org.georchestra.ldapadmin.ds.DuplicatedUidException;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.AccountFactory;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.mailservice.MailService;
import org.georchestra.ldapadmin.ws.utils.EmailUtils;
import org.georchestra.ldapadmin.ws.utils.PasswordUtils;
import org.georchestra.ldapadmin.ws.utils.RecaptchaUtils;
import org.georchestra.ldapadmin.ws.utils.UserUtils;
import org.georchestra.ldapadmin.ws.utils.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
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

/**
 * Manages the UI Account Form.
 *
 * <p>
 *
 * </p>
 *
 * @author Mauricio Pazos
 *
 */
@Controller
@SessionAttributes(types={AccountFormBean.class})
public final class NewAccountFormController {

	private static final Log LOG = LogFactory.getLog(NewAccountFormController.class.getName());

	private AccountDao accountDao;

	private MailService mailService;

	private Moderator moderator;

	private ReCaptcha reCaptcha;

	private ReCaptchaParameters reCaptchaParameters;

	private static final String[] fields = {"firstName","surname", "email", "phone", "org",
	    "title", "description", "uid", "password", "confirmPassword"};

	@Autowired
	public NewAccountFormController(AccountDao dao, MailService mailSrv, Moderator moderatorRule,
	        ReCaptcha reCaptcha, ReCaptchaParameters reCaptchaParameters) {
		this.accountDao = dao;
		this.mailService = mailSrv;
		this.moderator = moderatorRule;
		this.reCaptcha = reCaptcha;
		this.reCaptchaParameters = reCaptchaParameters;
	}

	@InitBinder
	public void initForm(WebDataBinder dataBinder) {
		dataBinder.setAllowedFields(ArrayUtils.addAll(fields,
		        new String[]{"recaptcha_challenge_field", "recaptcha_response_field"}));
	}

	@RequestMapping(value="/account/new", method=RequestMethod.GET)
	public String setupForm(HttpServletRequest request, Model model) throws IOException{

		HttpSession session = request.getSession();
		AccountFormBean formBean = new AccountFormBean();

		model.addAttribute(formBean);
		session.setAttribute("reCaptchaPublicKey", this.reCaptchaParameters.getPublicKey());
		for (String f : fields) {
			if (Validation.isFieldRequired(f)) {
				session.setAttribute(f + "Required", "true");
			}
		}
		return "createAccountForm";
	}

	/**
	 * Creates a new account in ldap. If the application was configured as "moderator singnup" the new account is added in the PENDING group,
	 * in other case, it will be inserted in the SV_USER group
	 *
	 *
	 * @param formBean
	 * @param result
	 * @param sessionStatus
	 *
	 * @return the next view
	 *
	 * @throws IOException
	 */
	@RequestMapping(value="/account/new", method=RequestMethod.POST)
	public String create(HttpServletRequest request,
						 @ModelAttribute AccountFormBean formBean,
						 BindingResult result,
						 SessionStatus sessionStatus)
			throws IOException {

		String remoteAddr = request.getRemoteAddr();

		UserUtils.validate(formBean.getUid(), formBean.getFirstName(), formBean.getSurname(), result );
		EmailUtils.validate(formBean.getEmail(), result);
		PasswordUtils.validate(formBean.getPassword(), formBean.getConfirmPassword(), result);
		new RecaptchaUtils(remoteAddr, this.reCaptcha).validate(formBean.getRecaptcha_challenge_field(), formBean.getRecaptcha_response_field(), result);
		Validation.validateField("phone", formBean.getPhone(), result);
		Validation.validateField("title", formBean.getTitle(), result);
		Validation.validateField("org", formBean.getOrg(), result);
		Validation.validateField("description", formBean.getDescription(), result);

		if(result.hasErrors()){

			return "createAccountForm";
		}

		// inserts the new account
		try {

			Account account =  AccountFactory.createBrief(
					formBean.getUid().toLowerCase(),
					formBean.getPassword(),
					formBean.getFirstName(),
					formBean.getSurname(),
					formBean.getEmail(),
					formBean.getPhone(),
					formBean.getOrg(),
					formBean.getTitle(),
					formBean.getDescription() );

			String groupID = this.moderator.moderatedSignup() ? Group.PENDING : Group.SV_USER;

			String adminUUID = null;
			try {
				adminUUID = this.accountDao.findByUID(request.getHeader("sec-username")).getUUID();
			} catch (NameNotFoundException e) {
				LOG.error("Unable to find admin/user connected, so no admin log generated when creating uid : " + formBean.getUid());
			}

			this.accountDao.insert(account, groupID, adminUUID);

			final ServletContext servletContext = request.getSession().getServletContext();
			if(this.moderator.moderatedSignup() ){

				// email to the moderator
				this.mailService.sendNewAccountRequiresModeration(servletContext, account.getUid(), account.getCommonName(), account.getEmail(), this.moderator.getModeratorEmail());

				// email to the user
				this.mailService.sendAccountCreationInProcess(servletContext, account.getUid(), account.getCommonName(), account.getEmail());
			} else {
				// email to the user
				this.mailService.sendAccountWasCreated(servletContext, account.getUid(), account.getCommonName(), account.getEmail());
			}
			sessionStatus.setComplete();

			return "welcomeNewUser";

		} catch (DuplicatedEmailException e) {

			result.rejectValue("email", "email.error.exist", "there is a user with this e-mail");
			return "createAccountForm";

		} catch (DuplicatedUidException e) {

			try {
				String proposedUid = this.accountDao.generateUid( formBean.getUid() );

				formBean.setUid(proposedUid);

				result.rejectValue("uid", "uid.error.exist", "the uid exist");

				return "createAccountForm";

			} catch (DataServiceException e1) {
				throw new IOException(e);
			}

		} catch (DataServiceException e) {

			throw new IOException(e);
		}
	}
	
	@ModelAttribute("accountFormBean")
	public AccountFormBean getAccountFormBean() {
		return new AccountFormBean();
	}
}
