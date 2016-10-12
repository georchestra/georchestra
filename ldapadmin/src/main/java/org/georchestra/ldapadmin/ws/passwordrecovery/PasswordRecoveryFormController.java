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

package org.georchestra.ldapadmin.ws.passwordrecovery;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.Configuration;
import org.georchestra.ldapadmin.bs.ReCaptchaParameters;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.GroupDao;
import org.georchestra.ldapadmin.ds.UserTokenDao;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.dto.Group;
import org.georchestra.ldapadmin.mailservice.MailService;
import org.georchestra.ldapadmin.ws.utils.RecaptchaUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import net.tanesha.recaptcha.ReCaptcha;

/**
 * Manage the user interactions required to implement the lost password workflow: 
 * <p>
 * <ul>
 * 
 * <li>Present a form in order to ask for the user's mail.</li>
 * 
 * <li>If the given email matches one of the LDAP users, an email is sent to this user with a unique http URL to reset his password.</li>
 * 
 * <li>As result of this interaction the view EmailSentForm.jsp is presented</li>
 * </ul>
 * </p>
 * 
 * @author Mauricio Pazos
 */
@Controller
@SessionAttributes(types=PasswordRecoveryFormBean.class)
public class PasswordRecoveryFormController  {
	
	protected static final Log LOG = LogFactory.getLog(PasswordRecoveryFormController.class.getName());
	
	// collaborations 
	private AccountDao accountDao;
	private GroupDao groupDao;
	private MailService mailService;
	private UserTokenDao userTokenDao;
	private Configuration config;
	private ReCaptcha reCaptcha;
	private ReCaptchaParameters reCaptchaParameters;
	

	@Autowired
	public PasswordRecoveryFormController( AccountDao dao,GroupDao gDao, MailService mailSrv, UserTokenDao userTokenDao,
			Configuration cfg, ReCaptcha reCaptcha, ReCaptchaParameters reCaptchaParameters){
		this.accountDao = dao;
		this.groupDao = gDao;
		this.mailService = mailSrv;
		this.userTokenDao = userTokenDao;
		this.config = cfg;
		this.reCaptcha = reCaptcha;
		this.reCaptchaParameters = reCaptchaParameters;
		
	}
	
	@InitBinder
	public void initForm( WebDataBinder dataBinder) {
		
		dataBinder.setAllowedFields(new String[]{"email", "recaptcha_challenge_field", "recaptcha_response_field"});
	}
	
	@RequestMapping(value="/account/passwordRecovery", method=RequestMethod.GET)
	public String setupForm(HttpServletRequest request, @RequestParam(value="email", required=false) String email, Model model) throws IOException{

		HttpSession session = request.getSession();
		PasswordRecoveryFormBean formBean = new PasswordRecoveryFormBean();
		formBean.setEmail(email);
		
		model.addAttribute(formBean);
		session.setAttribute("reCaptchaPublicKey", this.reCaptchaParameters.getPublicKey());
		
		return "passwordRecoveryForm";
	}
	
	/**
	 * Generates a new unique http URL based on a token, then an e-mail is sent to the user with instruction to change his password.
	 * 
	 * 
	 * @param formBean		Contains the user's email
	 * @param resultErrors 	will be updated with the list of found errors. 
	 * @param sessionStatus	
	 * 
	 * @return the next view
	 * 
	 * @throws IOException 
	 */
	@RequestMapping(value="/account/passwordRecovery", method=RequestMethod.POST)
	public String generateToken(
						HttpServletRequest request,
						@ModelAttribute PasswordRecoveryFormBean formBean, 
						BindingResult resultErrors, 
						SessionStatus sessionStatus) 
						throws IOException {

		String remoteAddr = request.getRemoteAddr();
		new RecaptchaUtils(remoteAddr, this.reCaptcha).validate(formBean.getRecaptcha_challenge_field(), formBean.getRecaptcha_response_field(), resultErrors);
		if(resultErrors.hasErrors()){
			return "passwordRecoveryForm";
		}
		
		try {
			Account account = this.accountDao.findByEmail(formBean.getEmail());
			List<Group> group = this.groupDao.findAllForUser(account.getUid());
			// Finds the user using the email as key, if it exists a new token is generated to include in the unique http URL.
			

			for (Group g : group) {
				if (g.getName().equals(Group.PENDING)) {
					throw new NameNotFoundException("User in PENDING group");
				}
			}
			
			String token = UUID.randomUUID().toString();

		
			
			// if there is a previous token it is removed
			if( this.userTokenDao.exist(account.getUid()) ) {
				this.userTokenDao.delete(account.getUid());
			}
			
			this.userTokenDao.insertToken(account.getUid(), token);
			
			String contextPath = this.config.getPublicContextPath();
			String url = makeChangePasswordURL(request.getServerName(), request.getServerPort(), contextPath, token);

			ServletContext servletContext = request.getSession().getServletContext();
			this.mailService.sendChangePasswordURL(servletContext, account.getUid(), account.getCommonName(), url, account.getEmail());
			
			sessionStatus.setComplete();
			
			return "emailWasSent";
			
		} catch (DataServiceException e) {
			
			throw new IOException(e);
			
		} catch (NameNotFoundException e) {
			
			resultErrors.rejectValue("email", "email.error.notFound", "No user found for this email.");
			
			return "passwordRecoveryForm";
			
		} 
	}


	/**
	 * Create the URL to change the password based on the provided token
	 * 
	 * @return a new URL to change password
	 */
	private String makeChangePasswordURL(final String host, final int port, final String contextPath, final String token) {

		StringBuilder strBuilder = new StringBuilder("");
		if ((port == 80) || (port == 443)) {
			strBuilder.append("https://").append(host);
		} else {
			strBuilder.append("http://").append(host).append(":").append(port);
		}
		strBuilder.append(contextPath);
		strBuilder.append( "/account/newPassword?token=").append(token);
		
		String url = strBuilder.toString();
		
		if(LOG.isDebugEnabled()){
			LOG.debug("generated url:" + url);
		}
		
		return url;
	}
	
	@ModelAttribute("passwordRecoveryFormBean")
	public PasswordRecoveryFormBean getPasswordRecoveryFormBean() {
		return new PasswordRecoveryFormBean();
	}
}
