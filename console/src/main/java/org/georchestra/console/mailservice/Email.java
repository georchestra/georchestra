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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;

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

    private String publicUrl;
    private String instanceName;
    private GeorchestraConfiguration georConfig;
    private ServletContext servletContext;

    public Email(List<String> recipients, String emailSubject, String smtpHost, int smtpPort, boolean emailHtml,
            String replyTo, String from, String bodyEncoding, String subjectEncoding, String templateEncoding,
            String fileTemplate, ServletContext servletContext, GeorchestraConfiguration georConfig, String publicUrl,
            String instanceName) {

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
        this.publicUrl = publicUrl;
        this.instanceName = instanceName;
        this.servletContext = servletContext;
        // Load template from filesystem
        this.emailBody = this.loadBody(fileTemplate);
    }

    public void set(String key, String value) {
        this.emailBody = this.emailBody.replaceAll("\\{" + key + "\\}", value);
    }

    @Override
    public String toString() {
        return "Email{" + "smtpHost='" + smtpHost + '\'' + ", smtpPort=" + smtpPort + ", emailHtml='" + emailHtml + '\''
                + ", replyTo='" + replyTo + '\'' + ", from='" + from + '\'' + ", bodyEncoding='" + bodyEncoding + '\''
                + ", subjectEncoding='" + subjectEncoding + '\'' + ", recipients="
                + recipients.stream().collect(Collectors.joining(",")) + ", subject='" + subject + '\''
                + ", emailBody='" + emailBody + '\'' + '}';
    }

    /**
     * Loads the body template.
     *
     * if available, the templates will be resolved from the geOrchestra datadir,
     * and if not defined, a one inside the webapp will be used.
     *
     * @param fileName the filename to open, without the path to it.
     * @return
     * @throws IOException
     */
    private String loadBody(final String fileName) {

        if ((georConfig != null) && (georConfig.activated())) {
            try {
                File fileTmpl = Paths.get(georConfig.getContextDataDir(), "templates", fileName).toFile();

                return FileUtils.readFileToString(fileTmpl, templateEncoding);
            } catch (IOException e) {
                LOG.error("Unable to get the template from geOrchestra datadir. "
                        + "Falling back on the default template provided by the webapp.", e);
            }
        }
        /* Trying to resolve the templates from inside the webapp */
        String tmplFromWebapp = this.servletContext
                .getRealPath(Paths.get("/WEB-INF", "templates", fileName).toString());

        String body = null;
        try {
            body = FileUtils.readFileToString(new File(tmplFromWebapp), templateEncoding);
        } catch (IOException e) {
            LOG.error(e);
        }
        return body;
    }

    public MimeMessage send() throws MessagingException {
        return this.send(true);
    }

    public MimeMessage send(boolean reallySend) throws MessagingException {

        // Replace {publicUrl} token with the configured public URL
        this.emailBody = this.emailBody.replaceAll("\\{publicUrl\\}", publicUrl);
        this.emailBody = this.emailBody.replaceAll("\\{instanceName\\}", instanceName);
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
        if (isValidEmailAddress(replyTo)) {
            message.setReplyTo(new InternetAddress[] { new InternetAddress(replyTo) });
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
        if (reallySend)
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