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
 * Manages the change password email. This mail is send when a user has lost his password.
 *  
 * @author Mauricio Pazos
 *
 */
class ChangePasswordEmail extends Email {

	private static final Log LOG = LogFactory.getLog(ChangePasswordEmail.class.getName());
	
	private ServletContext servletContext;
	
	public ChangePasswordEmail(
			String[] recipients, 
			String emailSubject,
			String smtpHost, 
			int smtpPort, 
			String replyTo, 
			String from,
			String bodyEncoding, 
			String subjectEncoding, 
			String[] languages, 
			String fileTemplate, 
			ServletContext servletContext) {

		super(recipients, emailSubject, smtpHost, smtpPort, replyTo, from,
				bodyEncoding, subjectEncoding, languages, fileTemplate);

		this.servletContext = servletContext;
	}

	public void sendMsg( final String userName, final String uid, final String url) throws AddressException, MessagingException {

		if(LOG.isDebugEnabled() ){
			
			LOG.debug("send change password email to user "+ userName+ " - uid: ." + uid  );
		}
		
		String body = writeNewPasswordMail(userName, url);
		
		super.sendMsg(body);
	}
	
	@Override
    protected String toAbsoltuPath(String fileTemplate) {

    	return this.servletContext.getRealPath(fileTemplate);
    }
	
	private String writeNewPasswordMail(final String userName, final String url) {
		
		final String body = getBodyTemplate();
		
		body.replace("{name}", userName);
		body.replace("{url}", url);
		
		if(LOG.isDebugEnabled() ){
			
			LOG.debug("built email: "+ body);
		}
		
		return body;
	}

}
