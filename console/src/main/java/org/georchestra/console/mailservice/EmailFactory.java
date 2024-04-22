/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

package org.georchestra.console.mailservice;

import static java.util.Collections.singletonList;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Manage e-mails required for this application
 */
public class EmailFactory {

    private String smtpHost;
    private int smtpPort;
    private boolean emailHtml;
    private String replyTo;
    private String from;
    private String bodyEncoding;
    private String subjectEncoding;
    private String templateEncoding;

    @Autowired
    private GeorchestraConfiguration georConfig;

    private String accountWasCreatedEmailFile;
    private String accountWasCreatedEmailSubject;

    private String accountCreationInProcessEmailFile;
    private String accountCreationInProcessEmailSubject;

    private String newAccountRequiresModerationEmailFile;
    private String newAccountRequiresModerationEmailSubject;

    private String changePasswordEmailFile;
    private String changePasswordEmailSubject;

    private String changePasswordOAuth2EmailFile;
    private String changePasswordOAuth2EmailSubject;

    private String changeEmailAddressEmailFile;
    private String changeEmailAddressEmailSubject;

    private String accountUidRenamedEmailFile;
    private String accountUidRenamedEmailSubject;

    private String newAccountNotificationEmailFile;

    private String newOAuth2AccountNotificationEmailFile;

    private String newAccountNotificationEmailSubject;

    private String newOAuth2AccountNotificationEmailSubject;

    private String publicUrl;
    private String instanceName;

    private String administratorEmail;

    public void sendAccountWasCreatedEmail(ServletContext servletContext, String recipient, String userName, String uid)
            throws MessagingException {
        sendAccountWasCreatedEmail(servletContext, recipient, userName, uid, true);
    }

    public void sendAccountWasCreatedEmail(ServletContext servletContext, String recipient, String userName, String uid,
            boolean reallySend) throws MessagingException {
        Email email = new Email(singletonList(recipient), this.accountWasCreatedEmailSubject, this.smtpHost,
                this.smtpPort, this.emailHtml, this.replyTo, this.from, this.bodyEncoding, this.subjectEncoding,
                this.templateEncoding, this.accountWasCreatedEmailFile, servletContext, this.georConfig, this.publicUrl,
                this.instanceName);
        email.set("name", userName);
        email.set("uid", uid);
        email.send(reallySend);
    }

    /**
     * e-mail to the user to inform the account requires the moderator's validation
     */
    public void sendAccountCreationInProcessEmail(ServletContext servletContext, String recipient, String userName,
            String uid) throws MessagingException {
        sendAccountCreationInProcessEmail(servletContext, recipient, userName, uid, true);
    }

    public void sendAccountCreationInProcessEmail(ServletContext servletContext, String recipient, String userName,
            String uid, boolean reallySend) throws MessagingException {

        Email email = new Email(singletonList(recipient), this.accountCreationInProcessEmailSubject, this.smtpHost,
                this.smtpPort, this.emailHtml, this.replyTo, this.from, this.bodyEncoding, this.subjectEncoding,
                this.templateEncoding, this.accountCreationInProcessEmailFile, servletContext, this.georConfig,
                this.publicUrl, this.instanceName);
        email.set("name", userName);
        email.set("uid", uid);
        email.send(reallySend);
    }

    /**
     * emails to the moderator to inform that a new user is waiting authorization.
     */
    public void sendNewAccountRequiresModerationEmail(ServletContext servletContext, List<String> recipients,
            String userName, String uid, String userEmail, String userOrg) throws MessagingException {
        sendNewAccountRequiresModerationEmail(servletContext, recipients, userName, uid, userEmail, userOrg, true);
    }

    public void sendNewAccountRequiresModerationEmail(ServletContext servletContext, List<String> recipients,
            String userName, String uid, String userEmail, String userOrg, boolean reallySend)
            throws MessagingException {

        Email email = new Email(recipients, this.newAccountRequiresModerationEmailSubject, this.smtpHost, this.smtpPort,
                this.emailHtml, userEmail, // Reply-to
                this.from, // From
                this.bodyEncoding, this.subjectEncoding, this.templateEncoding,
                this.newAccountRequiresModerationEmailFile, servletContext, this.georConfig, this.publicUrl,
                this.instanceName);
        email.set("name", userName);
        email.set("uid", uid);
        email.set("email", userEmail);
        email.set("org", userOrg);
        email.send(reallySend);
    }

    public void sendChangePasswordEmail(ServletContext servletContext, String recipient, String userName, String uid,
            String url) throws MessagingException {
        sendChangePasswordEmail(servletContext, recipient, userName, uid, url, true);
    }

    public void sendChangePasswordEmail(ServletContext servletContext, String recipient, String userName, String uid,
            String url, boolean reallySend) throws MessagingException {
        Email email = new Email(singletonList(recipient), this.changePasswordEmailSubject, this.smtpHost, this.smtpPort,
                this.emailHtml, this.replyTo, this.from, this.bodyEncoding, this.subjectEncoding, this.templateEncoding,
                this.changePasswordEmailFile, servletContext, this.georConfig, this.publicUrl, this.instanceName);
        email.set("name", userName);
        email.set("uid", uid);
        email.set("url", url);
        email.send(reallySend);
    }

    public void sendChangePasswordOAuth2Email(ServletContext servletContext, String recipient, String userName,
            String providerId) throws MessagingException {
        sendChangePasswordOAuth2Email(servletContext, recipient, userName, providerId, true);
    }

    public void sendChangePasswordOAuth2Email(ServletContext servletContext, String recipient, String userName,
            String providerId, boolean reallySend) throws MessagingException {
        Email email = new Email(singletonList(recipient), this.changePasswordOAuth2EmailSubject, this.smtpHost,
                this.smtpPort, this.emailHtml, this.replyTo, this.from, this.bodyEncoding, this.subjectEncoding,
                this.templateEncoding, this.changePasswordOAuth2EmailFile, servletContext, this.georConfig,
                this.publicUrl, this.instanceName);
        email.set("name", userName);
        email.set("provider", providerId);
        email.send(reallySend);
    }

    public void sendChangeEmailAddressEmail(ServletContext servletContext, String recipient, String userName,
            String uid, String url) throws MessagingException {
        sendChangeEmailAddressEmail(servletContext, recipient, userName, uid, url, true);
    }

    public void sendChangeEmailAddressEmail(ServletContext servletContext, String recipient, String userName,
            String uid, String url, boolean reallySend) throws MessagingException {
        Email email = new Email(singletonList(recipient), this.changeEmailAddressEmailSubject, this.smtpHost,
                this.smtpPort, this.emailHtml, this.replyTo, this.from, this.bodyEncoding, this.subjectEncoding,
                this.templateEncoding, this.changeEmailAddressEmailFile, servletContext, this.georConfig,
                this.publicUrl, this.instanceName);
        email.set("name", userName);
        email.set("uid", uid);
        email.set("url", url);
        email.send(reallySend);
    }

    public void sendAccountUidRenamedEmail(ServletContext servletContext, String recipient, String userName, String uid)
            throws MessagingException {
        sendAccountUidRenamedEmail(servletContext, recipient, userName, uid, true);
    }

    public void sendAccountUidRenamedEmail(ServletContext servletContext, String recipient, String userName, String uid,
            boolean reallySend) throws MessagingException {

        Email email = new Email(singletonList(recipient), this.accountUidRenamedEmailSubject, this.smtpHost,
                this.smtpPort, this.emailHtml, this.replyTo, this.from, this.bodyEncoding, this.subjectEncoding,
                this.templateEncoding, this.accountUidRenamedEmailFile, servletContext, this.georConfig, this.publicUrl,
                this.instanceName);
        email.set("name", userName);
        email.set("uid", uid);
        email.send(reallySend);
    }

    public void sendNewAccountNotificationEmail(ServletContext servletContext, List<String> recipients, String fullName,
            String uid, String emailAddress, String userOrg) throws MessagingException {
        sendNewAccountNotificationEmail(servletContext, recipients, fullName, uid, emailAddress, userOrg, true);
    }

    public void sendNewAccountNotificationEmail(ServletContext servletContext, List<String> recipients, String fullName,
            String uid, String emailAddress, String userOrg, boolean reallySend) throws MessagingException {

        Email email = new Email(recipients, this.newAccountNotificationEmailSubject, this.smtpHost, this.smtpPort,
                this.emailHtml, emailAddress, // Reply-to
                this.from, this.bodyEncoding, this.subjectEncoding, this.templateEncoding,
                this.newAccountNotificationEmailFile, servletContext, this.georConfig, this.publicUrl,
                this.instanceName);
        email.set("name", fullName);
        email.set("uid", uid);
        email.set("email", emailAddress);
        if (userOrg == null) {
            userOrg = "";
        }
        email.set("org", userOrg);
        email.send(reallySend);
    }

    public void sendNewOAuth2AccountNotificationEmail(List<String> recipients, String fullName, String localUid,
            String emailAddress, String providerName, String providerUid, String userOrg, boolean reallySend)
            throws MessagingException {

        Email email = new Email(recipients, this.newOAuth2AccountNotificationEmailSubject, this.smtpHost, this.smtpPort,
                this.emailHtml, emailAddress, // Reply-to
                this.from, this.bodyEncoding, this.subjectEncoding, this.templateEncoding,
                this.newOAuth2AccountNotificationEmailFile, null, this.georConfig, this.publicUrl, this.instanceName);
        email.set("name", fullName);
        email.set("uid", localUid);
        email.set("email", emailAddress);
        if (userOrg == null) {
            userOrg = "";
        }
        email.set("org", userOrg);
        email.set("providerName", providerName);
        email.set("providerUid", providerUid);
        email.send(reallySend);
    }

    public MimeMessage createEmptyMessage() {
        // Instanciate MimeMessage
        final Session session = Session.getInstance(System.getProperties(), null);
        session.getProperties().setProperty("mail.smtp.host", this.smtpHost);
        session.getProperties().setProperty("mail.smtp.port", (new Integer(this.smtpPort)).toString());
        return new MimeMessage(session);
    }

    /*
     * Setters for unit tests
     */

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public void setEmailHtml(boolean emailHtml) {
        this.emailHtml = emailHtml;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setBodyEncoding(String bodyEncoding) {
        this.bodyEncoding = bodyEncoding;
    }

    public void setSubjectEncoding(String subjectEncoding) {
        this.subjectEncoding = subjectEncoding;
    }

    public void setTemplateEncoding(String templateEncoding) {
        this.templateEncoding = templateEncoding;
    }

    public void setGeorConfig(GeorchestraConfiguration georConfig) {
        this.georConfig = georConfig;
    }

    public void setAccountWasCreatedEmailFile(String accountWasCreatedEmailFile) {
        this.accountWasCreatedEmailFile = accountWasCreatedEmailFile;
    }

    public void setAccountWasCreatedEmailSubject(String accountWasCreatedEmailSubject) {
        this.accountWasCreatedEmailSubject = accountWasCreatedEmailSubject;
    }

    public void setAccountCreationInProcessEmailFile(String accountCreationInProcessEmailFile) {
        this.accountCreationInProcessEmailFile = accountCreationInProcessEmailFile;
    }

    public void setAccountCreationInProcessEmailSubject(String accountCreationInProcessEmailSubject) {
        this.accountCreationInProcessEmailSubject = accountCreationInProcessEmailSubject;
    }

    public void setNewAccountRequiresModerationEmailFile(String newAccountRequiresModerationEmailFile) {
        this.newAccountRequiresModerationEmailFile = newAccountRequiresModerationEmailFile;
    }

    public void setNewAccountRequiresModerationEmailSubject(String newAccountRequiresModerationEmailSubject) {
        this.newAccountRequiresModerationEmailSubject = newAccountRequiresModerationEmailSubject;
    }

    public void setChangePasswordEmailFile(String changePasswordEmailFile) {
        this.changePasswordEmailFile = changePasswordEmailFile;
    }

    public void setChangePasswordEmailSubject(String changePasswordEmailSubject) {
        this.changePasswordEmailSubject = changePasswordEmailSubject;
    }

    public void setChangePasswordOAuth2EmailSubject(String changePasswordOAuth2EmailSubject) {
        this.changePasswordOAuth2EmailSubject = changePasswordOAuth2EmailSubject;
    }

    public void setChangePasswordOAuth2EmailFile(String changePasswordOAuth2EmailFile) {
        this.changePasswordOAuth2EmailFile = changePasswordOAuth2EmailFile;
    }

    public void setChangeEmailAddressEmailFile(String changePasswordEmailFile) {
        this.changeEmailAddressEmailFile = changePasswordEmailFile;
    }

    public void setChangeEmailAddressEmailSubject(String changePasswordEmailSubject) {
        this.changeEmailAddressEmailSubject = changePasswordEmailSubject;
    }

    public void setAccountUidRenamedEmailFile(String accountUidRenamedEmailFile) {
        this.accountUidRenamedEmailFile = accountUidRenamedEmailFile;
    }

    public void setAccountUidRenamedEmailSubject(String accountUidRenamedEmailSubject) {
        this.accountUidRenamedEmailSubject = accountUidRenamedEmailSubject;
    }

    public void setNewAccountNotificationEmailFile(String newAccountNotificationEmailFile) {
        this.newAccountNotificationEmailFile = newAccountNotificationEmailFile;
    }

    public void setNewOAuth2AccountNotificationEmailFile(String newOAuth2AccountNotificationEmailFile) {
        this.newOAuth2AccountNotificationEmailFile = newOAuth2AccountNotificationEmailFile;
    }

    public void setNewAccountNotificationEmailSubject(String newAccountNotificationEmailSubject) {
        this.newAccountNotificationEmailSubject = newAccountNotificationEmailSubject;
    }

    public void setNewOAuth2AccountNotificationEmailSubject(String newOAuth2AccountNotificationEmailSubject) {
        this.newOAuth2AccountNotificationEmailSubject = newOAuth2AccountNotificationEmailSubject;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public void setAdministratorEmail(String administratorEmail) {
        this.administratorEmail = administratorEmail;
    }
}
