/**
 * 
 */
package org.georchestra.ldapadmin.ws.newaccount;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;

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
@SessionAttributes(value={"reCaptchaPublicKey"},types={AccountFormBean.class})
public final class NewAccountFormController {
	
	private AccountDao accountDao;
	
	private MailService mailService;

	private Moderator moderator;

	private ReCaptcha reCaptcha; 

	private ReCaptchaParameters reCaptchaParameters;

	@Autowired
	public NewAccountFormController( AccountDao dao, MailService mailSrv , Moderator moderatorRule, ReCaptcha reCaptcha, ReCaptchaParameters reCaptchaParameters){
		this.accountDao = dao;
		this.mailService = mailSrv;
		this.moderator = moderatorRule;
		this.reCaptcha = reCaptcha;
		this.reCaptchaParameters = reCaptchaParameters;
	}
	
	@InitBinder
	public void initForm( WebDataBinder dataBinder) {
		
		dataBinder.setAllowedFields(new String[]{"firstName","surname", "email", "phone", "org", "description", "uid", "password", "confirmPassword", "role", "recaptcha_challenge_field", "recaptcha_response_field"});
	}
	
	@RequestMapping(value="/account/new", method=RequestMethod.GET)
	public String setupForm(Model model) throws IOException{

		AccountFormBean formBean = new AccountFormBean();
		
		model.addAttribute(formBean);
		model.addAttribute("reCaptchaPublicKey", this.reCaptchaParameters.getPublicKey());
		
		return "createAccountForm";
	}
	
	/**
	 * Creates a new account in ldap. If the application was configured as "moderator singnup" the new account is added in the PENDING_USERS group, 
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
	public String create(
						HttpServletRequest request,
						@ModelAttribute AccountFormBean formBean,
						BindingResult result, 
						SessionStatus sessionStatus) 
						throws IOException {

		String remoteAddr = request.getRemoteAddr();

		UserUtils.validate(formBean.getUid(), formBean.getFirstName(), formBean.getSurname(), result );
		EmailUtils.validate(formBean.getEmail(), result);
		PasswordUtils.validate(formBean.getPassword(), formBean.getConfirmPassword(), result);
		new RecaptchaUtils(remoteAddr, this.reCaptcha).validate(formBean.getRecaptcha_challenge_field(), formBean.getRecaptcha_response_field(), result);

		if(result.hasErrors()){
			
			return "createAccountForm";
		}
		
		// inserts the new account 
		try {
			
			Account account =  AccountFactory.createBrief(
					formBean.getUid(),
					formBean.getPassword(),
					formBean.getFirstName(),
					formBean.getSurname(),
					formBean.getEmail(),
					formBean.getPhone(),
					formBean.getOrg(),
					formBean.getDescription() );

			String groupID = this.moderator.moderatedSignup() ? Group.PENDING_USERS : Group.SV_USER; 
			
			this.accountDao.insert(account, groupID);

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
}
