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
	
	private String accountWasCreatedEmailFile;

	private String accountCreationInProcessEmailFile;

	private String emailChangePasswordFile;

	private String newAccountRequiresSignupEmailFile;
	

	private ServletContext servletContext;


	public void setAccountWasCreatedEmailFile(String accountWasCreatedEmailFile) {
		this.accountWasCreatedEmailFile = accountWasCreatedEmailFile;
	}

	public String getAccountCreationInProcessEmailFile() {
		return accountCreationInProcessEmailFile;
	}

	public void setAccountCreationInProcessEmailFile(String emailNewAccountFile) {
		this.accountCreationInProcessEmailFile = emailNewAccountFile;
	}

	public void setEmailChangePasswordFile(String emailNewPasswordFile) {
		this.emailChangePasswordFile = emailNewPasswordFile;
	}
	

	public void setNewAccountRequiresSignupEmailFile(
			String newAccountRequiresSignupEmailFile) {
		this.newAccountRequiresSignupEmailFile = newAccountRequiresSignupEmailFile;
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
	
	/**
	 * emails to the moderator to inform that a new user is waiting authorization. 
	 * 
	 * @param servletContext
	 * @param recipients
	 * @return
	 * @throws IOException
	 */
	public NewAccountRequiresSignupEmail createNewAccountRequiresSignupEmail(ServletContext servletContext, String[] recipients) throws IOException {
		
		NewAccountRequiresSignupEmail mail =  new NewAccountRequiresSignupEmail(
				recipients, 
				emailSubject,
				this.smtpHost,
				this.smtpPort,
				this.replyTo,
				this.from,
				this.bodyEncoding,
				this.subjectEncoding,
				this.languages,
				this.newAccountRequiresSignupEmailFile, 
				servletContext );
		
		return mail;
	}

	/** 
	 * e-mail to the user to inform the account requires the moderator's singnup
	 * 
	 * @param servletContext
	 * @param strings
	 * @return
	 */
	public AccountCreationInProcessEmail createAccountCreationInProcessEmail(
			ServletContext servletContext, String[] recipients) {
		
		
		AccountCreationInProcessEmail mail =  new AccountCreationInProcessEmail(
				recipients, 
				emailSubject,
				this.smtpHost,
				this.smtpPort,
				this.replyTo,
				this.from,
				this.bodyEncoding,
				this.subjectEncoding,
				this.languages,
				this.accountCreationInProcessEmailFile, 
				servletContext );
		
		return mail;
	}

	public AccountWasCreatedEmail createAccountWasCreatedEmail(ServletContext servletContext, String[] recipients) {
		
		AccountWasCreatedEmail mail =  new AccountWasCreatedEmail(
				recipients, 
				emailSubject,
				this.smtpHost,
				this.smtpPort,
				this.replyTo,
				this.from,
				this.bodyEncoding,
				this.subjectEncoding,
				this.languages,
				this.accountWasCreatedEmailFile, 
				servletContext );
		
		return mail;
		
	}
}
