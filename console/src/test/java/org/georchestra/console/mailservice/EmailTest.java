/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EmailTest {

    private List<String> recipients;
    private String emailSubject;
    private String smtpHost;
    private int smtpPort;
    private String replyTo;
    private String from;
    private String[] languages;
    private String simpleTemplate;
    private String htmlTemplate;
    private String utf8Template;
    private String isoTemplate;
    private String replaceTemplate;
    private String replaceTemplateResult;
    private ServletContext servletContext;
    private GeorchestraConfiguration georchestraConfiguration;
    private String publicUrl;
    private String instanceName;

    private Email mail;

    @Before
    public void doSetUp() {
        this.recipients = Arrays.asList("admin@georchestra.com", "delegated@georchestra.com");
        this.emailSubject = "your account has been created";
        this.smtpHost = "127.0.0.1";
        this.smtpPort = 25;
        this.replyTo = "test@localhost";
        this.from = "georchestra@localhost";
        this.languages = new String[] { "en/US" };
        this.simpleTemplate = "simple-template.txt";
        this.htmlTemplate = "html-template.txt";
        this.utf8Template = "utf8-template.txt";
        this.isoTemplate = "iso-template.txt";
        this.replaceTemplate = "replace-template.txt";
        this.replaceTemplateResult = "replace-template-result.txt";
        this.servletContext = Mockito.mock(ServletContext.class);
        this.georchestraConfiguration = Mockito.mock(GeorchestraConfiguration.class);
        this.publicUrl = "http://localhost:8080";
        this.instanceName = "geOrchestra";
        Mockito.when(this.servletContext.getRealPath("/WEB-INF/templates/" + this.simpleTemplate))
                .thenReturn(this.getClass().getClassLoader().getResource(this.simpleTemplate).getPath());
        Mockito.when(this.servletContext.getRealPath("/WEB-INF/templates/" + this.htmlTemplate))
                .thenReturn(this.getClass().getClassLoader().getResource(this.htmlTemplate).getPath());
        Mockito.when(this.servletContext.getRealPath("/WEB-INF/templates/" + this.utf8Template))
                .thenReturn(this.getClass().getClassLoader().getResource(this.utf8Template).getPath());
        Mockito.when(this.servletContext.getRealPath("/WEB-INF/templates/" + this.isoTemplate))
                .thenReturn(this.getClass().getClassLoader().getResource(this.isoTemplate).getPath());
        Mockito.when(this.servletContext.getRealPath("/WEB-INF/templates/" + this.replaceTemplate))
                .thenReturn(this.getClass().getClassLoader().getResource(this.replaceTemplate).getPath());
    }

    @Test
    public void testRecipientFromEtc() throws MessagingException, IOException {
        Email email = new Email(recipients, emailSubject, smtpHost, smtpPort, false, replyTo, from, "us/ascii",
                "us/ascii", "ascii", this.simpleTemplate, this.servletContext, this.georchestraConfiguration, publicUrl,
                instanceName);

        // Generate message
        MimeMessage message = email.send(false);

        // Compare TO: list with recipients
        Address[] addresses = message.getRecipients(Message.RecipientType.TO);
        List<String> toList = new ArrayList<>(addresses.length);
        for (Address address : addresses) {
            toList.add(address.toString());
        }
        assertEquals("emails listed in TO: field", this.recipients, toList);

        // Check FROM: field
        assertEquals("email in FROM: field", this.from, message.getFrom()[0].toString());

        // Check Reply-To: header
        assertEquals("email in Reply-To: header", this.replyTo, message.getReplyTo()[0].toString());

        // Check Subject: header
        assertEquals("email in Subject: header", this.emailSubject, message.getSubject());

        // Check email body
        String expectedBody = FileUtils.readFileToString(
                new File(this.getClass().getClassLoader().getResource(this.simpleTemplate).getPath()), "ascii");
        expectedBody = expectedBody.replaceAll("\\{publicUrl\\}", this.publicUrl);
        assertEquals(expectedBody, message.getContent());

        // Check encoding (body and subject)

    }

    @Test
    public void testHtmlEmail() throws MessagingException, IOException {
        Email email = new Email(recipients, emailSubject, smtpHost, smtpPort, true, replyTo, from, "us/ascii",
                "us/ascii", "ascii", this.htmlTemplate, this.servletContext, this.georchestraConfiguration, publicUrl,
                instanceName);

        // Generate message
        Message message = email.send(false);

        // Check email body
        String expectedBody = FileUtils.readFileToString(
                new File(this.getClass().getClassLoader().getResource(this.htmlTemplate).getPath()), "ascii");
        expectedBody = expectedBody.replaceAll("\\{publicUrl\\}", this.publicUrl);
        assertEquals(expectedBody, message.getContent());
    }

    @Test
    public void testUTF8EmailEncoding() throws MessagingException, IOException {
        Email email = new Email(recipients, "Compte créé", smtpHost, smtpPort, false, replyTo, from, "UTF-8", "UTF-8",
                "UTF-8", this.utf8Template, this.servletContext, this.georchestraConfiguration, publicUrl,
                instanceName);

        // Generate message
        Message message = email.send(false);

        // Check email body
        String expectedBody = FileUtils.readFileToString(
                new File(this.getClass().getClassLoader().getResource(this.utf8Template).getPath()), "UTF-8");
        expectedBody = expectedBody.replaceAll("\\{publicUrl\\}", this.publicUrl);
        assertEquals(expectedBody, message.getContent());
    }

    @Test
    public void testLatinEmailEncoding() throws IOException, MessagingException {
        Email email = new Email(recipients, "Compte créé", smtpHost, smtpPort, false, replyTo, from, "UTF-8", "UTF-8",
                "ISO-8859-15", this.isoTemplate, this.servletContext, this.georchestraConfiguration, publicUrl,
                instanceName);

        // Generate message
        Message message = email.send(false);

        // Check email body
        String expectedBody = FileUtils.readFileToString(
                new File(this.getClass().getClassLoader().getResource(this.isoTemplate).getPath()), "ISO-8859-15");
        expectedBody = expectedBody.replaceAll("\\{publicUrl\\}", this.publicUrl);
        assertEquals(expectedBody, message.getContent());
    }

    @Test
    public void testReplace() throws IOException, MessagingException {
        Email email = new Email(recipients, "Compte créé", smtpHost, smtpPort, false, replyTo, from, "UTF-8", "UTF-8",
                "ISO-8859-15", this.replaceTemplate, this.servletContext, this.georchestraConfiguration, publicUrl,
                instanceName);
        email.set("uid", "testadmin");
        email.set("var1", "value1");
        email.set("name", "Test Admin");
        Message message = email.send(false);

        String expectedBody = FileUtils.readFileToString(
                new File(this.getClass().getClassLoader().getResource(this.replaceTemplateResult).getPath()), "ascii");
        assertEquals(expectedBody, message.getContent());
    }

}
