/**
 * 
 */
package org.georchestra.ldapadmin.mailservice;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
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
	private GeorchestraConfiguration georchestraConfiguration;
	
	@Autowired
	public MailService(EmailFactoryImpl emailFactory) {
		this.emailFactory = emailFactory;
	}


	public void sendNewAccountRequiresModeration(ServletContext servletContext, final String uid, final String userName, final String userEmail, final String moderatorEmail) {

		try {
			NewAccountRequiresModerationEmail email = this.emailFactory.createNewAccountRequiresModerationEmail(servletContext, userEmail, new String[]{moderatorEmail});
			
			email.sendMsg(userName, uid);
		
		} catch (Exception e) {
			
			LOG.error(e);
		} 
	}


	public void sendAccountCreationInProcess(
			final ServletContext servletContext,
			final String uid, final String commonName, final String userEmail) {

		if(LOG.isDebugEnabled()){
			LOG.debug("uid: "+uid+ "- commonName" + commonName + " - email: " + userEmail);
		}
		try{
			AccountCreationInProcessEmail email = this.emailFactory.createAccountCreationInProcessEmail(servletContext, new String[]{userEmail});
			
			email.sendMsg(commonName, uid );
			
		} catch (Exception e) {
			
			LOG.error(e);
		} 
		
	}
	

	public void sendAccountWasCreated(final ServletContext servletContext, final String uid, final String commonName, final String userEmail) {

		if(LOG.isDebugEnabled()){
			LOG.debug("uid: "+uid+ "- commonName" + commonName + " - email: " + userEmail);
		}
		try{
			AccountWasCreatedEmail email = this.emailFactory.createAccountWasCreatedEmail(servletContext, new String[]{userEmail});
			
			email.sendMsg(commonName, uid );
			
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
	public void sendChangePasswordURL(ServletContext servletContext, final String uid, final String commonName, final String url, final String userEmail) {
		
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
