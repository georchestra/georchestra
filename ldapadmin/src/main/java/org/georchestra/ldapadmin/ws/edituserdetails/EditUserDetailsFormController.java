/**
 *
 */
package org.georchestra.ldapadmin.ws.edituserdetails;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.DuplicatedEmailException;
import org.georchestra.ldapadmin.dto.Account;
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

	private static final String[] fields = {"uid", "firstName", "surname", "email", "title", "phone", "facsimile", "org", "description", "postalAddress"};

	@InitBinder
	public void initForm( WebDataBinder dataBinder) {

		dataBinder.setAllowedFields(fields);
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
	public String setupForm(HttpServletRequest request, HttpServletResponse response,  Model model) throws IOException{

		if(request.getHeader("sec-username") == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}

		try {

			this.accountBackup = this.accountDao.findByUID(request.getHeader("sec-username"));

			HttpSession session = request.getSession();
			EditUserDetailsFormBean formBean = createForm(this.accountBackup);

			model.addAttribute(formBean);
			for (String f : fields) {
				if (Validation.isFieldRequired(f)) {
					session.setAttribute(f + "Required", "true");
				}
			}

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
		formBean.setPostalAddress(account.getPostalAddress());

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
						HttpServletResponse response,
						Model model,
						@ModelAttribute EditUserDetailsFormBean formBean,
						BindingResult resultErrors,
						SessionStatus sessionStatus)
						throws IOException {
		String uid = formBean.getUid();
		try {
			if(!request.getHeader("sec-username").equals(uid)){
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		} catch (NullPointerException e) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}

		UserUtils.validate( formBean.getFirstName(), formBean.getSurname(), resultErrors );
		Validation.validateField("phone", formBean.getPhone(), resultErrors);
		Validation.validateField("facsimile", formBean.getFacsimile(), resultErrors);
		Validation.validateField("title", formBean.getTitle(), resultErrors);
		Validation.validateField("org", formBean.getOrg(), resultErrors);
		Validation.validateField("description", formBean.getDescription(), resultErrors);
		Validation.validateField("postalAddress", formBean.getPostalAddress(), resultErrors);

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
		account.setPostalAddress(formBean.getPostalAddress());

		return account;
	}

	/**
	 * Setter only meant for testing purposes.
	 *
	 */
	public void setAccountBackup(Account a) {
	    this.accountBackup = a;
	}
	
	@ModelAttribute("editUserDetailsFormBean")
	public EditUserDetailsFormBean getEditUserDetailsFormBean() {
		return new EditUserDetailsFormBean();
	}
}
