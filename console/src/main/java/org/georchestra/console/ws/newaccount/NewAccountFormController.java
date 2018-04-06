/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

import net.tanesha.recaptcha.ReCaptcha;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.EmailValidator;
import org.georchestra.console.bs.Moderator;
import org.georchestra.console.bs.ReCaptchaParameters;
import org.georchestra.console.dao.Delegation2Dao;
import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.ds.DuplicatedEmailException;
import org.georchestra.console.ds.DuplicatedUidException;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountFactory;
import org.georchestra.console.dto.Role;
import org.georchestra.console.dto.Org;
import org.georchestra.console.dto.OrgExt;
import org.georchestra.console.mailservice.MailService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private Validation validation;

	@Autowired
	private Delegation2Dao delegation2Dao;

	@Autowired
	public NewAccountFormController(AccountDao dao, OrgsDao orgDao, Delegation2Dao delegation2Dao,
									MailService mailSrv, Moderator moderatorRule, ReCaptcha reCaptcha,
									ReCaptchaParameters reCaptchaParameters,
									Validation validation) {
		this.accountDao = dao;
		this.orgDao = orgDao;
		this.delegation2Dao = delegation2Dao;
		this.mailService = mailSrv;
		this.moderator = moderatorRule;
		this.reCaptcha = reCaptcha;
		this.reCaptchaParameters = reCaptchaParameters;
		this.validation = validation;
	}

	@InitBinder
	public void initForm(WebDataBinder dataBinder) {
		dataBinder.setAllowedFields(ArrayUtils.addAll(new String[]{"firstName","surname", "email", "phone",
				"org", "title", "description", "uid", "password", "confirmPassword", "createOrg", "orgName",
				"orgShortName", "orgAddress", "orgType", "orgCities"},
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
		for(String f: this.validation.getRequiredUserFields())
			session.setAttribute(f + "Required", "true");

		// Convert to camelcase with 'org' prefix 'shortName' --> 'orgShortName'
		for(String f: this.validation.getRequiredOrgFields())
			session.setAttribute("org" + f.substring(0, 1).toUpperCase() + f.substring(1, f.length()) + "Required",
					"true");

		return "createAccountForm";
	}

	/**
	 * Creates a new account in ldap. If the application was configured as "moderator singnup" the new account is added in the PENDING role,
	 * in other case, it will be inserted in the USER role
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

		// Populate orgs droplist
		model.addAttribute("orgs", this.getOrgs());

		// Populate org type droplist
		model.addAttribute("orgTypes", this.getOrgTypes());

		// uid validation
		if(!this.validation.validateUserField("uid", formBean.getUid())){
			result.rejectValue("uid", "uid.error.required", "required");
		} else {
			// A valid user identifier (uid) can only contain characters, numbers, hyphens or dot.
			// It must begin with a character.
			Pattern regexp = Pattern.compile("[a-zA-Z][a-zA-Z0-9.-]*");
			Matcher m = regexp.matcher(formBean.getUid());
			if(!m.matches())
				result.rejectValue("uid", "uid.error.invalid", "required");
		}

		// first name and surname validation
		if(!this.validation.validateUserField("firstName", formBean.getFirstName()))
			result.rejectValue("firstName", "firstName.error.required", "required");
		if(!this.validation.validateUserField("surname", formBean.getSurname()))
			result.rejectValue("surname", "surname.error.required", "required");

		// email validation
		if (!this.validation.validateUserField("email", formBean.getEmail()))
			result.rejectValue("email", "email.error.required", "required");
		else if (!EmailValidator.getInstance().isValid(formBean.getEmail()))
			result.rejectValue("email", "email.error.invalidFormat", "Invalid Format");

		// password validation
		PasswordUtils.validate(formBean.getPassword(), formBean.getConfirmPassword(), result);

		// Check captcha
		new RecaptchaUtils(request.getRemoteAddr(), this.reCaptcha)
				.validate(formBean.getRecaptcha_challenge_field(), formBean.getRecaptcha_response_field(), result);

		// Validate remaining fields
		this.validation.validateUserField("phone", formBean.getPhone(), result);
		this.validation.validateUserField("title", formBean.getTitle(), result);
		this.validation.validateUserField("description", formBean.getDescription(), result);

		// Create org if needed
		if(formBean.getCreateOrg() && ! result.hasErrors()){
			try {

				// Check required fields
				this.validation.validateOrgField("name", formBean.getOrgName(), result);
				this.validation.validateOrgField("shortName", formBean.getOrgShortName(), result);
				this.validation.validateOrgField("address", formBean.getOrgAddress(), result);
				this.validation.validateOrgField("type", formBean.getOrgType(), result);

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

				// Parse and store cities
				orgCities = orgCities.trim();
				if(orgCities.length() > 0)
					org.setCities(Arrays.asList(orgCities.split("\\s*,\\s*")));

				// Set default value
				org.setStatus(Org.STATUS_PENDING);

				// Persist changes to LDAP server
				if(!result.hasErrors()){
					this.orgDao.insert(org);
					this.orgDao.insert(orgExt);

					// Set real org identifier in form
					formBean.setOrg(orgId);
				}

			} catch (Exception e) {
				LOG.error(e.getMessage());
				throw new IOException(e);
			}
		} else {
			this.validation.validateUserField("org", formBean.getOrg(), result);
		}

		if(result.hasErrors())
			return "createAccountForm";


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

			String roleID = this.moderator.moderatedSignup() ? Role.PENDING : Role.USER;

			this.accountDao.insert(account, roleID, request.getHeader("sec-username"));

			final ServletContext servletContext = request.getSession().getServletContext();

			// email to the moderator
			this.mailService.sendNewAccountRequiresModeration(servletContext, account.getUid(), account.getCommonName(), account.getEmail(), this.moderator.getModeratorEmail());

			// email to delegated admin if org is specified
			if(!formBean.getOrg().equals("-")) {
				// and a delegation is defined
				List<DelegationEntry> delegations = this.delegation2Dao.findByOrg(formBean.getOrg());
				for(DelegationEntry delegation: delegations){
					Account delegatedAdmin = this.accountDao.findByUID(delegation.getUid());
					this.mailService.sendNewAccountRequiresModeration(servletContext, account.getUid(), account.getCommonName(), account.getEmail(), delegatedAdmin.getEmail());
				}
			}

			// email to the user
			if(this.moderator.moderatedSignup()){
				this.mailService.sendAccountCreationInProcess(servletContext, account.getUid(), account.getCommonName(), account.getEmail());
			} else {
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
