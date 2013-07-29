package org.georchestra.ldapadmin.mailservice;

import java.io.IOException;

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
	

	public ChangePasswordEmail createChangePasswordEmail(String[] recipients) throws IOException {
		
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
				this.emailChangePasswordFile);
		
		return mail;
	}
	
	public NewAccountEmail createNewAccountEmail(String[] recipients) throws IOException {
		
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
				this.emailNewAccountFile);
		
		return mail;
	}
}
