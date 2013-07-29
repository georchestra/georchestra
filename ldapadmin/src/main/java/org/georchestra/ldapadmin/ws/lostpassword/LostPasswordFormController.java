/**
 * 
 */
package org.georchestra.ldapadmin.ws.lostpassword;

import java.io.IOException;
import java.util.UUID;

import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.NotFoundException;
import org.georchestra.ldapadmin.ds.UserTokenDao;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.mailservice.MailService;
import org.georchestra.ldapadmin.ws.utils.EmailUtils;
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
@SessionAttributes(types=LostPasswordFormBean.class)
public class LostPasswordFormController  {
	
	// collaborations 
	private AccountDao accountDao;
	private MailService mailService;
	private UserTokenDao userTokenDao;
	
	// properties
	private String ApplicationUrl =""; // TODO configure
	
	@Autowired
	public LostPasswordFormController( AccountDao dao, MailService mailSrv, UserTokenDao userTokenDao){
		this.accountDao = dao;
		this.mailService = mailSrv;
		this.userTokenDao = userTokenDao;
	}
	
	@InitBinder
	public void initForm( WebDataBinder dataBinder) {
		
		dataBinder.setAllowedFields(new String[]{"email"});
	}
	
	@RequestMapping(value="/public/accounts/lostPassword", method=RequestMethod.GET)
	public String setupForm(Model model) throws IOException{

		LostPasswordFormBean formBean = new LostPasswordFormBean();
		
		model.addAttribute(formBean);
		
		return "lostPasswordForm";
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
	@RequestMapping(value="/public/accounts/lostPassword", method=RequestMethod.POST)
	public String generateToken(
						@ModelAttribute LostPasswordFormBean formBean, 
						BindingResult resultErrors, 
						SessionStatus sessionStatus) 
						throws IOException {
		
		EmailUtils.validate(formBean.getEmail(), resultErrors);

		
		if(resultErrors.hasErrors()){
			
			return "lostPasswordForm";
		}
		
		try {
			// Finds the user using the email as key, if it exists a new token is generated to include in the unique http URL.
			Account account = this.accountDao.findByEmail(formBean.getEmail());
			
			String token = UUID.randomUUID().toString();
			
			// if there is a previous token it is removed
			if( this.userTokenDao.exist(account.getUid()) ) {
				this.userTokenDao.delete(account.getUid());
			}
			
			this.userTokenDao.insertToken(account.getUid(), token);
			
			String url = makeChangePasswordURL(token);

			this.mailService.sendChangePassowrdURL(account.getUid(), account.getCommonName(), url, account.getEmail());
			
			sessionStatus.setComplete();
			
			return "emailWasSentForm";
			
		} catch (DataServiceException e) {
			
			throw new IOException(e);
			
		} catch (NotFoundException e) {
			
			resultErrors.rejectValue("email", "mailNoExist", "There is not a user with the provided email.");
			
			return "lostPasswordForm";
			
		} 
	}

	/**
	 * Create the URL to change the password based on the provided token  
	 * @param token
	 * 
	 * @return a new URL to change password
	 */
	private String makeChangePasswordURL(final String token) {

		StringBuilder strBuilder = new StringBuilder(this.ApplicationUrl);
		strBuilder.append( "/public/accounts/newPassword?token=").append(token);
		
		return strBuilder.toString();
	}
}
