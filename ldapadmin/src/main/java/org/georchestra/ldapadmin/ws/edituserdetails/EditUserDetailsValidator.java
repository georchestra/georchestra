/**
 * 
 */
package org.georchestra.ldapadmin.ws.edituserdetails;

import org.georchestra.ldapadmin.ws.utils.UserUtils;
import org.springframework.validation.BindingResult;

/**
 * @author Mauricio Pazos
 *
 */
public class EditUserDetailsValidator {

	public void validate(EditUserDetailsFormBean form, BindingResult errors) {
		
		UserUtils.validate( form.getFirstName(), form.getSurname(), errors ); 
		
	}

}
