/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Email {

	protected static final Log LOG = LogFactory.getLog(Email.class.getName());

    private String smtpHost;
    private int smtpPort;
    private boolean emailHtml;
    private String replyTo;
    private String from;
    private String bodyEncoding;
    private String subjectEncoding;
    private String templateEncoding;
    private List<String> recipients;
    private String subject;
	private String emailBody;

	protected GeorchestraConfiguration georConfig;

    public Email( List<String> recipients,
                 String emailSubject, String smtpHost, int smtpPort, boolean emailHtml, String replyTo, String from,
                 String bodyEncoding, String subjectEncoding, String templateEncoding, String fileTemplate,
                 ServletContext servletContext, GeorchestraConfiguration georConfig) {

        this.recipients = recipients;
        this.subject = emailSubject;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.emailHtml = emailHtml;
        this.replyTo = replyTo;
        this.from = from;
        this.bodyEncoding = bodyEncoding;
        this.subjectEncoding = subjectEncoding;
        this.templateEncoding = templateEncoding;
        this.georConfig = georConfig;

        // Load template from filesystem
        this.emailBody = this.loadBody(servletContext.getRealPath(fileTemplate));
    }

    public void set(String key, String value) {
        this.emailBody = this.emailBody.replaceAll("\\{" + key + "\\}", value);
    }

    @Override
    public String toString() {
        return "Email{" +
                "smtpHost='" + smtpHost + '\'' +
                ", smtpPort=" + smtpPort +
                ", emailHtml='" + emailHtml + '\'' +
                ", replyTo='" + replyTo + '\'' +
                ", from='" + from + '\'' +
                ", bodyEncoding='" + bodyEncoding + '\'' +
                ", subjectEncoding='" + subjectEncoding + '\'' +
                ", recipients=" + recipients.stream().collect(Collectors.joining(",")) +
                ", subject='" + subject + '\'' +
                ", emailBody='" + emailBody + '\'' +
                '}';
    }

    /**
     * Loads the body template.
     *
     * @param fileName path + file name
     * @return
     * @throws IOException
     */
    private String loadBody(final String fileName) {

        if ((georConfig != null) && (georConfig.activated())) {
            try {
                String basename = FilenameUtils.getName(fileName);
                return FileUtils.readFileToString(new File(georConfig.getContextDataDir(), "templates/" + basename), templateEncoding);
            } catch (IOException e) {
                LOG.error("Unable to get the template from geOrchestra datadir. Falling back on the default template provided by the webapp.", e);
            }
        }

    	BufferedReader reader = null;
    	String body = null;
        try {
            body = FileUtils.readFileToString(new File(fileName), templateEncoding);
        } catch (Exception e ){
        	LOG.error(e);
        } finally {
            try {
            	if(reader != null)	reader.close();
			} catch (IOException e) {
	        	LOG.error(e);
			}
        }
        return body;
    }

    public MimeMessage send() throws MessagingException {
        return this.send(true);
    }

	public MimeMessage send(boolean reallySend) throws MessagingException {

		// Replace {publicUrl} token with the configured public URL
        this.emailBody = this.emailBody.replaceAll("\\{publicUrl\\}", this.georConfig.getProperty("publicUrl"));
        LOG.debug("body: " + this.emailBody);

        final Session session = Session.getInstance(System.getProperties(), null);
        session.getProperties().setProperty("mail.smtp.host", smtpHost);
        session.getProperties().setProperty("mail.smtp.port", (new Integer(smtpPort)).toString());

        final MimeMessage message = new MimeMessage(session);

        if (isValidEmailAddress(from)) {
            message.setFrom(new InternetAddress(from));
        }
        for (String recipient : recipients) {
            if (!isValidEmailAddress(recipient))
                throw new AddressException("Invalid recipient : " + recipient);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        }
        if (isValidEmailAddress(replyTo)){
            message.setReplyTo(new InternetAddress[]{new InternetAddress(replyTo)});
        }

        message.setSubject(subject, subjectEncoding);

        if (this.emailBody != null) {
            if (emailHtml) {
                message.setContent(this.emailBody, "text/html; charset=" + bodyEncoding);
            } else {
                message.setContent(this.emailBody, "text/plain; charset=" + bodyEncoding);
            }
        }

        // Finally send the message
        if(reallySend)
            Transport.send(message);
        LOG.debug("email has been sent to:\n" + recipients.stream().collect(Collectors.joining(",")));
        return message;
	}

	private static boolean isValidEmailAddress(String address) {
        if (address == null) {
            return false;
        }

        boolean hasCharacters = address.trim().length() > 0;
        boolean hasAt = address.contains("@");

        if (!hasCharacters || !hasAt)
            return false;

        String[] parts = address.trim().split("@", 2);

        boolean mainPartNotEmpty = parts[0].trim().length() > 0;
        boolean hostPartNotEmpty = parts[1].trim().length() > 0;
        return mainPartNotEmpty && hostPartNotEmpty;
    }

}