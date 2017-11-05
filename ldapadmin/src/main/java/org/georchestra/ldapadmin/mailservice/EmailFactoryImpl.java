/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.ldapadmin.mailservice;

import java.io.IOException;

import javax.servlet.ServletContext;

/**
 * Creates the e-mails required for this application.
 *
 * @author Mauricio Pazos
 *
 */
public class EmailFactoryImpl extends AbstractEmailFactory {

	private String accountWasCreatedEmailFile;
	private String accountWasCreatedEmailSubject;

	private String accountCreationInProcessEmailFile;
	private String accountCreationInProcessEmailSubject;

	private String newAccountRequiresModerationEmailFile;
	private String newAccountRequiresModerationEmailSubject;

	private String changePasswordEmailFile;
	private String changePasswordEmailSubject;
	
	private String accountUidRenamedEmailFile;
	private String accountUidRenamedEmailSubject;
	
	public void setAccountUidRenamedEmailSubject(String accountUidRenamedEmailSubject) {
		this.accountUidRenamedEmailSubject = accountUidRenamedEmailSubject;
	}

	public void setAccountUidRenamedEmailFile(String accountUidRenamedEmailFile) {
		this.accountUidRenamedEmailFile = accountUidRenamedEmailFile;
	}

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

	public void setNewAccountRequiresModerationEmailFile(String file) {
		this.newAccountRequiresModerationEmailFile = file;
	}

	public void setNewAccountRequiresModerationEmailSubject(String subject) {
		this.newAccountRequiresModerationEmailSubject = subject;
	}

	public void setChangePasswordEmailFile(String file) {
		this.changePasswordEmailFile = file;
	}

	public void setChangePasswordEmailSubject(String subject) {
		this.changePasswordEmailSubject = subject;
	}

	public ChangePasswordEmail createChangePasswordEmail(ServletContext servletContext, String[] recipients) throws IOException {
	    super.emailSubject = this.changePasswordEmailSubject;
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
				servletContext,
				this.georConfig);

		return mail;
	}

	/**
	 * emails to the moderator to inform that a new user is waiting authorization.
	 *
	 * @param servletContext
	 * @param userEmail
	 * @param recipients
	 * @return
	 * @throws IOException
	 */
	public NewAccountRequiresModerationEmail createNewAccountRequiresModerationEmail(ServletContext servletContext, String userEmail, String[] recipients) throws IOException {
	    // TODO: What is the purpose of this affectation ? Unused aftewards, and isn't it dangerous in a
	    // context of a bean (only one instance shared at runtime ?) ?
		super.emailSubject =this.newAccountRequiresModerationEmailSubject;

		NewAccountRequiresModerationEmail mail =  new NewAccountRequiresModerationEmail(
				recipients,
				super.emailSubject,
				this.smtpHost,
				this.smtpPort,
				this.emailHtml,
				userEmail, // Reply-to
				this.from, // From
				this.bodyEncoding,
				this.subjectEncoding,
				this.languages,
				this.newAccountRequiresModerationEmailFile,
				servletContext,
				this.georConfig);

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

		super.emailSubject =this.accountCreationInProcessEmailSubject;

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
				servletContext,
				this.georConfig);

		return mail;
	}

	public AccountWasCreatedEmail createAccountWasCreatedEmail(ServletContext servletContext, String[] recipients) {

		super.emailSubject = this.accountWasCreatedEmailSubject;

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
				servletContext,
				this.georConfig);

		return mail;

	}
	
	public AccountUidRenamedEmail createAccountUidRenamedEmail(ServletContext servletContext, String[] recipients) {

		super.emailSubject = this.accountUidRenamedEmailSubject;

		AccountUidRenamedEmail mail = new AccountUidRenamedEmail(
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
				this.accountUidRenamedEmailFile,
				servletContext,
				this.georConfig
				);

		return mail;
	}
}
