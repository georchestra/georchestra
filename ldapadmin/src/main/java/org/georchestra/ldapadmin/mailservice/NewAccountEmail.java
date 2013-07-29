/**
 * 
 */
package org.georchestra.ldapadmin.mailservice;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.lib.mailservice.Email;

/**
 * This mail is sent to the moderator when a new account is created.
 * 
 * @author Mauricio Pazos
 *
 */
class NewAccountEmail extends Email {

	private static final Log LOG = LogFactory.getLog(NewAccountEmail.class.getName());

	public NewAccountEmail(
			String[] recipients, 
			String emailSubject,
			String smtpHost, 
			int smtpPort, 
			String replyTo, 
			String from,
			String bodyEncoding, 
			String subjectEncoding, 
			String[] languages, 
			String fileBodyTemplate) {
	
		super(recipients, emailSubject, smtpHost, smtpPort, replyTo, from,
				bodyEncoding, subjectEncoding, languages, fileBodyTemplate);
	}
	
	public void sendMsg(final String userName, final String uid ) throws AddressException, MessagingException {

		LOG.debug("New account user in the pending group. User ID: " + uid );
		
		String body = writeNewAccoutnMail(uid, userName);

		super.sendMsg(body);
	}
	

	private String writeNewAccoutnMail(String uid, String name) {

		final String body = getBodyTemplate();
		
		body.replace("{name}", name);
		body.replace("{uid}", uid);
		
		LOG.debug("built email: "+ body);

		return body;
	}
	
	

}
