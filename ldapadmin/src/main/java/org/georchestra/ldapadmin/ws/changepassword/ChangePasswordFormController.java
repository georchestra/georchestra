/**
 * 
 */
package org.georchestra.ldapadmin.ws.changepassword;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.UserTokenDao;
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

/**
 * This controller is responsible of manage the user interactions required for changing the user account's password.
 * <p>
 * This controller is associated to the changePasswordForm.jsp view and {@link ChangePasswordFormBean}. 
 * </p>
 * 
 * @author Mauricio Pazos
 */
@Controller
@SessionAttributes(types=ChangePasswordFormBean.class)
public class ChangePasswordFormController {
	
	private static final Log LOG = LogFactory.getLog(ChangePasswordFormController.class.getName());
	
	private AccountDao accountDao;
	
	
	@Autowired
	public ChangePasswordFormController( AccountDao dao){
		this.accountDao = dao;
	}
	
	@InitBinder
	public void initForm( WebDataBinder dataBinder) {
		
		dataBinder.setAllowedFields(new String[]{"password", "confirmPassword"});
	}
	
	/**
	 * Initializes the {@link ChangePasswordFormBean} with the uid provided as parameter. 
	 * The changePasswordForm view is provided as result of this method.  
	 * 
	 * @param uid	user id
	 * @param model 
	 * 
	 * @return changePasswordForm view to display 
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value="/public/accounts/changePassword", method=RequestMethod.GET)
	public String setupForm(@RequestParam("uid") String uid, Model model) throws IOException{
		
		ChangePasswordFormBean formBean = new ChangePasswordFormBean();
		
		formBean.setUid(uid);
		
		model.addAttribute(formBean);
		
		return "changePasswordForm";
	}
	
	/**
	 * Changes the password in the ldap store.
	 * 
	 * @param formBean
	 * @param result
	 * @param sessionStatus
	 * 
	 * @return the next view
	 * 
	 * @throws IOException 
	 */
	@RequestMapping(value="/public/accounts/changePassword", method=RequestMethod.POST)
	public String changePassword(
						@ModelAttribute ChangePasswordFormBean formBean, 
						BindingResult result, 
						SessionStatus sessionStatus) 
						throws IOException {
		
		new ChangePasswordFormValidator().validate(formBean, result);
		
		if(result.hasErrors()){
			
			return "changePasswordForm";
		}

		// change the user's password
		try {

			String uid = formBean.getUid();
			String  password = formBean.getPassword();
			
			this.accountDao.changePassword(uid, password);
			
			sessionStatus.setComplete();
			
			return "redirect:/public/accounts/userdetails?uid=" + uid;			
			
		} catch (DataServiceException e) {
			
			throw new IOException(e);
			
		} 
	}
	
	

}
