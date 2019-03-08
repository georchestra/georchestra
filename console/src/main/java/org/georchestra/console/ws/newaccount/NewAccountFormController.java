/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

package org.georchestra.console.ws.newaccount;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.EmailValidator;
import org.georchestra.console.bs.Moderator;
import org.georchestra.console.bs.ReCaptchaParameters;
import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.DuplicatedEmailException;
import org.georchestra.console.ds.DuplicatedUidException;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.ds.RoleDao;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountFactory;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.dto.orgs.OrgDetail;
import org.georchestra.console.dto.orgs.OrgExt;
import org.georchestra.console.dto.Role;
import org.georchestra.console.mailservice.EmailFactory;
import org.georchestra.console.model.DelegationEntry;
import org.georchestra.console.ws.utils.PasswordUtils;
import org.georchestra.console.ws.utils.RecaptchaUtils;
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

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Manages the UI Account Form.
 *
 * @author Mauricio Pazos
 *
 */
@Controller
@SessionAttributes(types={AccountFormBean.class})
public final class NewAccountFormController {

	private static final Log LOG = LogFactory.getLog(NewAccountFormController.class.getName());

	@Autowired
	private AccountDao accountDao;

	@Autowired
	private OrgsDao orgDao;

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private EmailFactory emailFactory;

	@Autowired
	private AdvancedDelegationDao advancedDelegationDao;

	@Autowired
	protected PasswordUtils passwordUtils;

	private Moderator moderator;

	@Autowired
	protected boolean reCaptchaActivated;
	private ReCaptchaParameters reCaptchaParameters;

	private Validation validation;

	@Autowired
	public NewAccountFormController(Moderator moderatorRule,
									ReCaptchaParameters reCaptchaParameters,
									Validation validation) {
		this.moderator = moderatorRule;
		this.reCaptchaParameters = reCaptchaParameters;
		this.validation = validation;
	}

	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	public void setOrgDao(OrgsDao orgDao) {
		this.orgDao = orgDao;
	}

	public void setEmailFactory(EmailFactory emailFactory){
		this.emailFactory = emailFactory;
	}

	public void setAdvancedDelegationDao(AdvancedDelegationDao advancedDelegationDao) {
		this.advancedDelegationDao = advancedDelegationDao;
	}

	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

	@ModelAttribute("accountFormBean")
	public AccountFormBean getAccountFormBean() {
		return new AccountFormBean();
	}

	@InitBinder
	public void initForm(WebDataBinder dataBinder) {
		dataBinder.setAllowedFields(new String[]{"firstName","surname", "email", "phone",
				"org", "title", "description", "uid", "password", "confirmPassword", "createOrg", "orgName",
				"orgShortName", "orgAddress", "orgType", "orgCities", "orgDescription", "orgUrl", "orgLogo", "recaptcha_response_field"});
	}

	@RequestMapping(value="/account/new", method=RequestMethod.GET)
	public String setupForm(HttpServletRequest request, Model model) throws IOException{

		HttpSession session = request.getSession();

		populateOrgsAndOrgTypes(model);

		model.addAttribute("recaptchaActivated", this.reCaptchaActivated);

		session.setAttribute("reCaptchaPublicKey", reCaptchaParameters.getPublicKey());
		for(String f: validation.getRequiredUserFields()) {
			session.setAttribute(f + "Required", "true");
		}
		// Convert to camelcase with 'org' prefix 'shortName' --> 'orgShortName'
		for(String f: validation.getRequiredOrgFields()) {
			session.setAttribute("org" + f.substring(0, 1).toUpperCase() + f.substring(1, f.length()) + "Required", "true");
		}

		return "createAccountForm";
	}

	/**
	 * Creates a new account in ldap. If the application was configured with
	 * "moderated signup" the new account is added inside "ou=pendingusers"
	 * LDAP organizational unit, in the other case, it's inserted in the
	 * "ou=users" organization unit.
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
						 @RequestParam("orgCities") String orgCities,
						 BindingResult result,
						 SessionStatus sessionStatus,
						 Model model)
			throws IOException, SQLException {

		populateOrgsAndOrgTypes(model);

		validateFields(formBean, result);

		if(result.hasErrors()) {
			return "createAccountForm";
		}

		if(formBean.getCreateOrg()) {
			try {
				Org org = new Org();
				OrgExt orgExt = new OrgExt();
				OrgDetail orgDetail = new OrgDetail();

				// Generate textual identifier based on name
				String orgId = orgDao.generateId(formBean.getOrgShortName());
				org.setId(orgId);
				orgExt.setId(orgId);
				orgDetail.setId(orgId);

				// Store name, short name, orgType and address
				org.setName(formBean.getOrgName());
				org.setShortName(formBean.getOrgShortName());
				orgExt.setAddress(formBean.getOrgAddress());
				orgExt.setOrgType(formBean.getOrgType());
				orgExt.setDescription(formBean.getOrgDescription());
				orgDetail.setUrl(formBean.getOrgUrl());
				orgDetail.setLogo(formBean.getOrgLogo());
				// Parse and store cities
				orgCities = orgCities.trim();
				if (orgCities.length() > 0)
					org.setCities(Arrays.asList(orgCities.split("\\s*,\\s*")));

				org.setPending(moderator.moderatedSignup());
				orgExt.setPending(moderator.moderatedSignup());
				orgDetail.setPending(moderator.moderatedSignup());
				// Persist changes to LDAP server
				orgDao.insert(org);
				orgDao.insert(orgExt);
				orgDao.insert(orgDetail);

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

			account.setPending(moderator.moderatedSignup());

			String requestOriginator = request.getHeader("sec-username");
			accountDao.insert(account,  requestOriginator);
			roleDao.addUser(Role.USER, account, requestOriginator);
			if(account.getOrg().length() > 0) {
				Org org = orgDao.findByCommonName(account.getOrg());
				orgDao.addUser(org, account);
			}

			final ServletContext servletContext = request.getSession().getServletContext();

			// List of recipients for notification email
			List<String> recipients = accountDao.findByRole("SUPERUSER").stream()
					.map(x -> x.getEmail())
					.collect(Collectors.toCollection(LinkedList::new));

			// Retrieve emails of delegated admin if org is specified
			if(!formBean.getOrg().equals("-")) {
				// and a delegation is defined
				List<DelegationEntry> delegations = advancedDelegationDao.findByOrg(formBean.getOrg());

				for (DelegationEntry delegation : delegations) {
					Account delegatedAdmin = accountDao.findByUID(delegation.getUid());
					recipients.add(delegatedAdmin.getEmail());
				}
			}

			// Select email template based on moderation configuration for admin and user and send emails
			if(moderator.moderatedSignup()){
				emailFactory.sendNewAccountRequiresModerationEmail(servletContext, recipients,
						account.getCommonName(), account.getUid(), account.getEmail());
				emailFactory.sendAccountCreationInProcessEmail(servletContext, account.getEmail(),
						account.getCommonName(), account.getUid());
			} else {
				emailFactory.sendNewAccountNotificationEmail(servletContext, recipients,
						account.getCommonName(), account.getUid(), account.getEmail());
				emailFactory.sendAccountWasCreatedEmail(servletContext, account.getEmail(),
						account.getCommonName(), account.getUid());
			}
			sessionStatus.setComplete();

			return "welcomeNewUser";

		} catch (DuplicatedEmailException e) {

			result.rejectValue("email", "email.error.exist", "there is a user with this e-mail");
			return "createAccountForm";

		} catch (DuplicatedUidException e) {

			formBean.setUid(accountDao.generateUid(formBean.getUid()));
			result.rejectValue("uid", "uid.error.exist", "the uid exist");
			return "createAccountForm";

		} catch (DataServiceException|MessagingException e) {

			throw new IOException(e);
		}
	}

	private void validateFields(@ModelAttribute AccountFormBean formBean, BindingResult result) {
		// uid validation
		if (validation.validateUserFieldWithSpecificMsg("uid", formBean.getUid(), result)) {
			// A valid user identifier (uid) can only contain characters, numbers, hyphens or dot.
			// It must begin with a character.
			// keep in sync with the regexp in webapp/manager/app/templates/userForm.tpl.html
			Pattern regexp = Pattern.compile("[a-zA-Z][a-zA-Z0-9_\\.\\-]*");
			Matcher m = regexp.matcher(formBean.getUid());
			if(!m.matches())
				result.rejectValue("uid", "uid.error.invalid", "required");
		}

		// first name and surname validation
		validation.validateUserFieldWithSpecificMsg("firstName", formBean.getFirstName(), result);
		validation.validateUserFieldWithSpecificMsg("surname", formBean.getSurname(), result);

		// email validation
		if (validation.validateUserFieldWithSpecificMsg("email", formBean.getEmail(), result) && !EmailValidator.getInstance().isValid(formBean.getEmail())) {
			result.rejectValue("email", "email.error.invalidFormat", "Invalid Format");
		}

		// password validation
		passwordUtils.validate(formBean.getPassword(), formBean.getConfirmPassword(), result);

		// Check captcha
		if (reCaptchaActivated) {
			RecaptchaUtils.validate(reCaptchaParameters, formBean.getRecaptcha_response_field(), result);
		}

		// Validate remaining fields
		validation.validateUserField("phone", formBean.getPhone(), result);
		validation.validateUserField("title", formBean.getTitle(), result);
		validation.validateUserField("description", formBean.getDescription(), result);

		if(formBean.getCreateOrg() && ! result.hasErrors()){
			validation.validateOrgField("name", formBean.getOrgName(), result);
			validation.validateOrgField("shortName", formBean.getOrgShortName(), result);
			validation.validateOrgField("address", formBean.getOrgAddress(), result);
			validation.validateOrgField("type", formBean.getOrgType(), result);
		} else {
			validation.validateUserField("org", formBean.getOrg(), result);
		}
	}

	private void populateOrgsAndOrgTypes(Model model) {
		model.addAttribute("orgs", getOrgs());
		model.addAttribute("orgTypes", getOrgTypes());
	}

	private Map<String, String> getOrgTypes() {
		return Arrays.stream(orgDao.getOrgTypeValues())
				.collect(Collectors.toMap(Function.identity(), Function.identity()));
	}

	/**
	 * Create a sorted Map of organization sorted by human readable name
     */
	private Map<String, String> getOrgs() {
		return orgDao.findValidated().stream()
				.sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
				.collect(Collectors.toMap(Org::getId, Org::getName, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
	}
}
