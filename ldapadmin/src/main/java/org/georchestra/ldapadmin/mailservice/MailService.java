/**
 * 
 */
package org.georchestra.ldapadmin.mailservice;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Facade of mail service.
 * 
 * @author Mauricio Pazos
 *
 */
public final class MailService {
	
	protected static final Log LOG = LogFactory.getLog(MailService.class.getName());

	private EmailFactoryImpl emailFactory;

	
	@Autowired
	public MailService(EmailFactoryImpl emailFactory) {
		this.emailFactory = emailFactory;
	}


	public void sendNewAccount(ServletContext servletContext, final String uid, final String userName, final String moderatorEmail) {

		try {
			NewAccountEmail email = this.emailFactory.createNewAccountEmail(servletContext,  new String[]{moderatorEmail});
			
			email.sendMsg(userName, uid);
		
		} catch (Exception e) {
			
			LOG.error(e);
		} 
	}

	

	/**
	 * Sent an email to the user whit the unique URL required to change his password.
	 * @param servletContext 
	 * @param servletContext 
	 * 
	 * @param uid user id
	 * @param commonName user full name
	 * @param url 	url where the user can change his password
	 * @param userEmail user email
	 * 
	 */
	public void sendChangePassowrdURL(ServletContext servletContext, final String uid, final String commonName, final String url, final String userEmail) {
		
		if(LOG.isDebugEnabled()){
			LOG.debug("uid: "+uid+ "- commonName" + commonName + " - url: " + url + " - email: " + userEmail);
		}

		try{
			
			ChangePasswordEmail email = this.emailFactory.createChangePasswordEmail(servletContext, new String[]{userEmail});
			
			email.sendMsg(commonName, uid, url);
			
		} catch (Exception e) {
			
			LOG.error(e);
		} 
		
	}

}
