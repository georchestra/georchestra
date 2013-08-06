/**
 * 
 */
package org.georchestra.ldapadmin.mailservice;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.lib.mailservice.Email;

/**
 * Email to inform that a new account was created. 
 * 
 * @author Mauricio Pazos
 *
 */
class AccountWasCreatedEmail extends Email {

	
	private static final Log LOG = LogFactory.getLog(NewAccountRequiresSignupEmail.class.getName());
	private ServletContext servletContext;

	public AccountWasCreatedEmail(
			String[] recipients, 
			String emailSubject,
			String smtpHost, 
			int smtpPort, 
			String replyTo, 
			String from,
			String bodyEncoding, 
			String subjectEncoding, 
			String[] languages, 
			String fileBodyTemplate, ServletContext servletContext) {
	
		super(recipients, emailSubject, smtpHost, smtpPort, replyTo, from,
				bodyEncoding, subjectEncoding, languages, fileBodyTemplate);
		
		this.servletContext = servletContext;

	}
	
	public void sendMsg(final String userName, final String uid ) throws AddressException, MessagingException {

		LOG.debug("New account was created. User Name:"+ userName + "User ID: " + uid );
		
		String body = writeNewAccoutnMail(uid, userName);

		super.sendMsg(body);
	}
	
	@Override
    protected String toAbsoltuPath(String fileTemplate) {

    	return this.servletContext.getRealPath(fileTemplate);
    }

	private String writeNewAccoutnMail(String uid, String name) {

		final String body = this.servletContext.getRealPath(getBodyTemplate());
		
		body.replace("{name}", name);
		body.replace("{uid}", uid);
		
		LOG.debug("built email: "+ body);

		return body;
	}
	
}
