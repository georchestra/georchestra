/**
 * 
 */
package org.georchestra.ldapadmin.ws.edituserdetails;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.DuplicatedEmailException;
import org.georchestra.ldapadmin.dto.Account;
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
 * Support for the Edit Account user interactions.
 * 
 * @author Mauricio Pazos
 *
 */
@Controller
@SessionAttributes(types=EditUserDetailsFormBean.class)
public class EditUserDetailsFormController {

	private AccountDao accountDao;
	
	private Account accountBackup;
	
	
	@Autowired
	public EditUserDetailsFormController( AccountDao dao){
		this.accountDao = dao;
	}
	
	@InitBinder
	public void initForm( WebDataBinder dataBinder) {
		
		dataBinder.setAllowedFields(new String[]{"uid", "firstName", "surname", "email", "title", "phone", "facsimile", "org", "description", "postalAddress", "postalCode",  "registeredAddress", "postOfficeBox", "physicalDeliveryOfficeName"});
	}
	
	
	/**
	 * Retrieves the account data and sets the model before presenting the edit form view.
	 * 
	 * @param model
	 * 
	 * @return the edit form view
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value="/account/userdetails", method=RequestMethod.GET)
	public String setupForm(HttpServletRequest request,  Model model) throws IOException{
		
		if(request.getHeader("sec-username") == null){
			return "forbidden";
		}

		try {
			
			this.accountBackup = this.accountDao.findByUID(request.getHeader("sec-username"));
			
			EditUserDetailsFormBean formBean = createForm(this.accountBackup);

			model.addAttribute(formBean);
			
			return "editUserDetailsForm";
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		} 
	}
	

	/**
	 * Creates a form based on the account data.
	 * 
	 * @param account input data 
	 * @param formBean (out)
	 */
	private EditUserDetailsFormBean createForm(final Account account) {

		EditUserDetailsFormBean formBean = new EditUserDetailsFormBean();
		
		formBean.setUid(account.getUid());
		formBean.setEmail(account.getEmail());
		
		formBean.setFirstName(account.getGivenName());
		formBean.setSurname(account.getSurname());
		formBean.setTitle(account.getTitle());

		formBean.setPhone(account.getPhone());
		formBean.setFacsimile(account.getFacsimile());
		formBean.setOrg(account.getOrg());
		formBean.setDescription(account.getDescription());
		formBean.setPhysicalDeliveryOfficeName(account.getPhysicalDeliveryOfficeName());
		formBean.setPostalAddress(account.getPostalAddress());
		formBean.setPostalCode(account.getPostalCode());
		formBean.setPostOfficeBox(account.getPostOfficeBox());
		formBean.setRegisteredAddress(account.getRegisteredAddress());

		return formBean;
	}

	/**
	 * Generates a new password, then an e-mail is sent to the user to inform that a new password is available.
	 * 
	 * @param formBean		Contains the user's email
	 * @param resultErrors 	will be updated with the list of found errors. 
	 * @param sessionStatus	
	 * 
	 * @return the next view
	 * 
	 * @throws IOException 
	 */
	@RequestMapping(value="/account/userdetails", method=RequestMethod.POST)
	public String edit(
						HttpServletRequest request,
						Model model,
						@ModelAttribute EditUserDetailsFormBean formBean, 
						BindingResult resultErrors,
						SessionStatus sessionStatus) 
						throws IOException {
		String uid = formBean.getUid();
		try {
			if(!request.getHeader("sec-username").equals(uid)){
				return "forbidden";
			}
		} catch (NullPointerException e) {
			return "forbidden";
		}

		new EditUserDetailsValidator().validate(formBean, resultErrors);
		
		if(resultErrors.hasErrors()){
			
			return "editUserDetailsForm";
		}

		// updates the account details 
		try {
			
			Account account = modify(this.accountBackup, formBean);
			
			this.accountDao.update(account);
			
			model.addAttribute("success", true);

			return "editUserDetailsForm";
			
		} catch (DuplicatedEmailException e) {

			// right now the email cannot be edited (review requirement)
			//resultErrors.addError(new ObjectError("email", "Exist a user with this e-mail"));
			return "createAccountForm";
			
		} catch (DataServiceException e) {
			
			throw new IOException(e);
		} 
		
		
	}

	/**
	 * Modifies the account using the values present in the formBean parameter
	 *  
	 * @param account
	 * @param formBean
	 * 
	 * @return modified account
	 */
	private Account modify(
			Account account,
			EditUserDetailsFormBean formBean) {

		account.setGivenName( formBean.getFirstName() );
		account.setSurname(formBean.getSurname());
		account.setTitle( formBean.getTitle() );
		account.setPhone(formBean.getPhone());
		account.setFacsimile(formBean.getFacsimile());
		account.setOrg(formBean.getOrg());
		account.setDescription(formBean.getDescription());
		account.setPhysicalDeliveryOfficeName(formBean.getPhysicalDeliveryOfficeName());
		account.setPostalAddress(formBean.getPostalAddress());
		account.setPostalCode( formBean.getPostalCode() );
		account.setPostOfficeBox( formBean.getPostOfficeBox() );
		account.setRegisteredAddress( formBean.getRegisteredAddress() );
		
		return account;
	}

	
	
}
