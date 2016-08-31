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
import org.georchestra.ldapadmin.ds.OrgsDao;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.AccountFactory;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.dto.Org;
import org.georchestra.ldapadmin.dto.OrgExt;
import org.georchestra.ldapadmin.mailservice.MailService;
import org.georchestra.ldapadmin.ws.utils.EmailUtils;
import org.georchestra.ldapadmin.ws.utils.PasswordUtils;
import org.georchestra.ldapadmin.ws.utils.RecaptchaUtils;
import org.georchestra.ldapadmin.ws.utils.UserUtils;
import org.georchestra.ldapadmin.ws.utils.Validation;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

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
	private OrgsDao orgDao;

	private MailService mailService;

	private Moderator moderator;

	private ReCaptcha reCaptcha;

	private ReCaptchaParameters reCaptchaParameters;

	private static final String[] fields = {"firstName","surname", "email", "phone", "org", "title", "description",
			"uid", "password", "confirmPassword", "createOrg", "orgName", "orgShortName", "orgAddress", "orgType"};

	@Autowired
	public NewAccountFormController(AccountDao dao, OrgsDao orgDao, MailService mailSrv, Moderator moderatorRule,
									ReCaptcha reCaptcha, ReCaptchaParameters reCaptchaParameters) {
		this.accountDao = dao;
		this.orgDao = orgDao;
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

		// Populate orgs droplist
		model.addAttribute("orgs", this.getOrgs());
		// Populate org type droplist
		model.addAttribute("orgTypes", this.getOrgTypes());

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
	 * in other case, it will be inserted in the USER group
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
						 SessionStatus sessionStatus,
						 Model model)
			throws IOException {

		// Populate orgs droplist
		model.addAttribute("orgs", this.getOrgs());

		// Populate org type droplist
		model.addAttribute("orgTypes", this.getOrgTypes());

		UserUtils.validate(formBean.getUid(), formBean.getFirstName(), formBean.getSurname(), result );
		EmailUtils.validate(formBean.getEmail(), result);
		PasswordUtils.validate(formBean.getPassword(), formBean.getConfirmPassword(), result);
		new RecaptchaUtils(request.getRemoteAddr(), this.reCaptcha)
				.validate(formBean.getRecaptcha_challenge_field(), formBean.getRecaptcha_response_field(), result);
		Validation.validateField("phone", formBean.getPhone(), result);
		Validation.validateField("title", formBean.getTitle(), result);
		Validation.validateField("org", formBean.getOrg(), result);
		Validation.validateField("description", formBean.getDescription(), result);

		if(result.hasErrors())
			return "createAccountForm";

		// Create org if needed
		if("true".equals(formBean.getCreateOrg())){
			try {

				Org org = new Org();
				OrgExt orgExt = new OrgExt();

				// Generate textual identifier based on name
				String orgId = this.orgDao.generateId(formBean.getOrgName());
				org.setId(orgId);
				orgExt.setId(orgId);

				// Generate numeric identifier
				orgExt.setNumericId(this.orgDao.generateNumericId());

				// Store name, short name, orgType and address
				org.setName(formBean.getOrgName());
				org.setShortName(formBean.getOrgShortName());
				orgExt.setAddress(formBean.getOrgAddress());
				orgExt.setOrgType(formBean.getOrgType());

				// Set default value
				org.setStatus("PENDING");

				// Persist changes to LDAP server
				this.orgDao.insert(org);
				this.orgDao.insert(orgExt);

				// Set real org identifier in form
				formBean.setOrg(orgId);

			} catch (Exception e) {
				LOG.error(e.getMessage());
				throw new IOException(e);
			}
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
					formBean.getTitle(),
					formBean.getDescription() );

			if(!formBean.getOrg().equals("-"))
				account.setOrg(formBean.getOrg());

			String groupID = this.moderator.moderatedSignup() ? Group.PENDING : Group.USER;

			this.accountDao.insert(account, groupID, request.getHeader("sec-username"));

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


	public Map<String, String> getOrgTypes() {
		Map<String, String> orgTypes = new LinkedHashMap<String, String>();
		for(String orgType: this.orgDao.getOrgTypeValues())
			orgTypes.put(orgType, orgType);
		return orgTypes;
	}

	/**
	 * Create a sorted Map of organization sorted by human readable name
	 * @return
     */
	public Map<String, String> getOrgs() {
		List<Org> orgs = this.orgDao.findValidated();
		Collections.sort(orgs, new Comparator<Org>() {
			@Override
			public int compare(Org o1, Org o2){
				return  o1.getName().compareTo(o2.getName());
			}
		});

		Map<String, String> orgsName = new LinkedHashMap<String, String>();
		for(Org org : orgs)
			orgsName.put(org.getId(), org.getName());

		return orgsName;
	}
}
