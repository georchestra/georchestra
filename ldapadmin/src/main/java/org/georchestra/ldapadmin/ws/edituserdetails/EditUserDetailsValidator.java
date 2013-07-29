/**
 * 
 */
package org.georchestra.ldapadmin.ws.edituserdetails;

import org.georchestra.ldapadmin.ws.utils.UserNameUtils;
import org.springframework.validation.BindingResult;

/**
 * @author Mauricio Pazos
 *
 */
public class EditUserDetailsValidator {

	public void validate(EditUserDetailsFormBean form, BindingResult errors) {
		
		UserNameUtils.validate( form.getFirstName(), form.getSurname(), errors ); 
		
	}

}
