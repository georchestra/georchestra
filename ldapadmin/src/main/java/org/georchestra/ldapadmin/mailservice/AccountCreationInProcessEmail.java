/**
 * 
 */
package org.georchestra.ldapadmin.mailservice;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;

/**
 * 
 * @author Mauricio Pazos
 *
 */
class AccountCreationInProcessEmail extends Email {

	private static final Log LOG = LogFactory.getLog(NewAccountRequiresModerationEmail.class.getName());
	private ServletContext servletContext;

	public AccountCreationInProcessEmail(
			String[] recipients, 
			String emailSubject,
			String smtpHost, 
			int smtpPort, 
			String emailHtml,
			String replyTo, 
			String from,
			String bodyEncoding, 
			String subjectEncoding, 
			String[] languages, 
			String fileBodyTemplate,
			ServletContext servletContext,
			GeorchestraConfiguration georConfig) {
	
		super(recipients, emailSubject, smtpHost, smtpPort, emailHtml, replyTo, from,
				bodyEncoding, subjectEncoding, languages, fileBodyTemplate, georConfig);
		
		this.servletContext = servletContext;

	}
	
	public void sendMsg(final String userName, final String uid ) throws AddressException, MessagingException {

		LOG.debug("New account user in progress. User ID: " + uid ); 
		
		String body = writeNewAccountMail(uid, userName);

		super.sendMsg(body);
	}
	
	@Override
    protected String toAbsolutePath(String fileTemplate) {

    	return this.servletContext.getRealPath(fileTemplate);
    }

	private String writeNewAccountMail(String uid, String name) {

		String body = getBodyTemplate();
		
		body = body.replace("{name}", name);
		body = body.replace("{uid}", uid);
		
		if(LOG.isDebugEnabled() ){
			
			LOG.debug("built email: "+ body);
		}

		return body;
	}

}
