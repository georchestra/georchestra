package org.georchestra.ldapadmin.mailservice;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.georchestra.lib.mailservice.AbstractEmailFactory;

/**
 * Creates the e-mails required for this application.
 * 
 * @author Mauricio Pazos
 *
 */
class EmailFactoryImpl extends AbstractEmailFactory {
	
	private String emailNewAccountFile;

	private String emailChangePasswordFile;

	private ServletContext servletContext;

	public String getEmailNewAccountFile() {
		return emailNewAccountFile;
	}

	public void setEmailNewAccountFile(String emailNewAccountFile) {
		this.emailNewAccountFile = emailNewAccountFile;
	}

	public String getEmailChangewPasswordFile() {
		return emailChangePasswordFile;
	}

	public void setEmailChangePasswordFile(String emailNewPasswordFile) {
		this.emailChangePasswordFile = emailNewPasswordFile;
	}
	

	public ChangePasswordEmail createChangePasswordEmail(ServletContext servletContext, String[] recipients) throws IOException {
		
		ChangePasswordEmail mail =  new ChangePasswordEmail(
				recipients, 
				emailSubject,
				this.smtpHost,
				this.smtpPort,
				this.replyTo,
				this.from,
				this.bodyEncoding,
				this.subjectEncoding,
				this.languages,
				this.emailChangePasswordFile,
				servletContext);
		
		return mail;
	}
	
	public NewAccountEmail createNewAccountEmail(ServletContext servletContext, String[] recipients) throws IOException {
		
		NewAccountEmail mail =  new NewAccountEmail(
				recipients, 
				emailSubject,
				this.smtpHost,
				this.smtpPort,
				this.replyTo,
				this.from,
				this.bodyEncoding,
				this.subjectEncoding,
				this.languages,
				this.emailNewAccountFile, 
				servletContext );
		
		return mail;
	}
}
