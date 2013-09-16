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
	private String accountWasCreatedEmailSubject;

	private String accountCreationInProcessEmailFile;
	private String accountCreationInProcessEmailSubject;

	private String newAccountRequiresSignupEmailFile;
	private String newAccountRequiresSignupEmailSubject;
	
	private String changePasswordEmailFile;
	private String changePasswordEmailSubject;

	private ServletContext servletContext;


	public void setAccountWasCreatedEmailFile(String accountWasCreatedEmailFile) {
		this.accountWasCreatedEmailFile = accountWasCreatedEmailFile;
	}
	public void setAccountWasCreatedEmailSubject(String subject) {
		this.accountWasCreatedEmailSubject = subject;
	}

	public void setAccountCreationInProcessEmailFile(String file) {
		this.accountCreationInProcessEmailFile = file;
	}
	

	public void setAccountCreationInProcessEmailSubject(String subject) {
		this.accountCreationInProcessEmailSubject = subject;
	}
	
	public void setNewAccountRequiresSignupEmailFile(String file) {
		this.newAccountRequiresSignupEmailFile = file;
	}

	public void setNewAccountRequiresSignupEmailSubject(String subject) {
		this.newAccountRequiresSignupEmailSubject = subject;
	}
	
	public void setChangePasswordEmailFile(String file) {
		this.changePasswordEmailFile = file;
	}
	
	public void setChangePasswordEmailSubject(String subject) {
		this.changePasswordEmailSubject = subject;
	}
	
	public ChangePasswordEmail createChangePasswordEmail(ServletContext servletContext, String[] recipients) throws IOException {
		super.emailSubject =changePasswordEmailSubject; 
		ChangePasswordEmail mail =  new ChangePasswordEmail(
				recipients, 
				super.emailSubject,
				this.smtpHost,
				this.smtpPort,
				this.emailHtml,
				this.replyTo,
				this.from,
				this.bodyEncoding,
				this.subjectEncoding,
				this.languages,
				this.changePasswordEmailFile,
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
		
		super.emailSubject =this.newAccountRequiresSignupEmailSubject;
		
		NewAccountRequiresSignupEmail mail =  new NewAccountRequiresSignupEmail(
				recipients, 
				super.emailSubject,
				this.smtpHost,
				this.smtpPort,
				this.emailHtml,
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
		
		super.emailSubject =this.accountCreationInProcessEmailFile;

		AccountCreationInProcessEmail mail =  new AccountCreationInProcessEmail(
				recipients, 
				super.emailSubject,
				this.smtpHost,
				this.smtpPort,
				this.emailHtml,
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
		
		super.emailSubject =this.accountWasCreatedEmailSubject;

		AccountWasCreatedEmail mail =  new AccountWasCreatedEmail(
				recipients, 
				super.emailSubject,
				this.smtpHost,
				this.smtpPort,
				this.emailHtml,
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
