package org.georchestra.console.mailservice;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.FileUtils;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmailTest {


    private String[] recipients;
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

    private Email mail;


    @Before
    public void doSetUp() {
        this.recipients = new String[]{"admin@georchestra.com", "delegated@georchestra.com"};
        this.emailSubject = "your account has been created";
        this.smtpHost = "127.0.0.1";
        this.smtpPort = 25;
        this.replyTo = "test@localhost";
        this.from = "georchestra@localhost";
        this.languages = new String[]{ "en/US" };
        this.simpleTemplate = "simple-template.txt";
        this.htmlTemplate = "html-template.txt";
        this.utf8Template = "utf8-template.txt";
        this.isoTemplate = "iso-template.txt";
        this.replaceTemplate = "replace-template.txt";
        this.replaceTemplateResult = "replace-template-result.txt";
        this.servletContext = Mockito.mock(ServletContext.class);
        this.georchestraConfiguration = Mockito.mock(GeorchestraConfiguration.class);
        this.publicUrl = "http://localhost:8080";
        Mockito.when(this.servletContext.getRealPath(this.simpleTemplate))
                .thenReturn(this.getClass().getClassLoader().getResource(this.simpleTemplate).getPath());
        Mockito.when(this.servletContext.getRealPath(this.htmlTemplate))
                .thenReturn(this.getClass().getClassLoader().getResource(this.htmlTemplate).getPath());
        Mockito.when(this.servletContext.getRealPath(this.utf8Template))
                .thenReturn(this.getClass().getClassLoader().getResource(this.utf8Template).getPath());
        Mockito.when(this.servletContext.getRealPath(this.isoTemplate))
                .thenReturn(this.getClass().getClassLoader().getResource(this.isoTemplate).getPath());
        Mockito.when(this.servletContext.getRealPath(this.replaceTemplate))
                .thenReturn(this.getClass().getClassLoader().getResource(this.replaceTemplate).getPath());
        Mockito.when(this.georchestraConfiguration.getProperty(Mockito.eq("publicUrl")))
                .thenReturn(this.publicUrl);
    }

    @Test
    public void testRecipientFromEtc() throws MessagingException, IOException {
        Email email = new Email(recipients, emailSubject, smtpHost, smtpPort, false, replyTo, from, "us/ascii",
                "us/ascii", "ascii", this.simpleTemplate, this.servletContext, this.georchestraConfiguration);

        // Generate message
        MimeMessage message = email.send(false);

        // Compare TO: list with recipients
        Address[] addresses = message.getRecipients(Message.RecipientType.TO);
        List<String> toList = new ArrayList<>(addresses.length);
        for(Address address : addresses){
            toList.add(address.toString());
        }
        assertEquals("emails listed in TO: field", Arrays.asList(this.recipients), toList);

        // Check FROM: field
        assertEquals("email in FROM: field", this.from, message.getFrom()[0].toString());

        // Check Reply-To: header
        assertEquals("email in Reply-To: header", this.replyTo, message.getReplyTo()[0].toString());

        // Check Subject: header
        assertEquals("email in Subject: header", this.emailSubject, message.getSubject());

        // Check email body
        String expectedBody = FileUtils.readFileToString(new File(this.getClass().getClassLoader().getResource(this.simpleTemplate).getPath()), "ascii");
        expectedBody = expectedBody.replaceAll("\\{publicUrl\\}", this.publicUrl);
        assertEquals(expectedBody, message.getContent());

        // Check encoding (body and subject)


    }

    @Test
    public void testHtmlEmail() throws MessagingException, IOException {
        Email email = new Email(recipients, emailSubject, smtpHost, smtpPort, true, replyTo, from, "us/ascii",
                "us/ascii", "ascii", this.htmlTemplate, this.servletContext, this.georchestraConfiguration);

        // Generate message
        Message message = email.send(false);

        // Check email body
        String expectedBody = FileUtils.readFileToString(new File(this.getClass().getClassLoader().getResource(this.htmlTemplate).getPath()), "ascii");
        expectedBody = expectedBody.replaceAll("\\{publicUrl\\}", this.publicUrl);
        assertEquals(expectedBody, message.getContent());
    }

    @Test
    public void testUTF8EmailEncoding() throws MessagingException, IOException {
        Email email = new Email(recipients, "Compte créé", smtpHost, smtpPort, false, replyTo, from, "UTF-8",
                "UTF-8", "UTF-8", this.utf8Template, this.servletContext, this.georchestraConfiguration);

        // Generate message
        Message message = email.send(false);

        // Check email body
        String expectedBody = FileUtils.readFileToString(new File(this.getClass().getClassLoader().getResource(this.utf8Template).getPath()), "UTF-8");
        expectedBody = expectedBody.replaceAll("\\{publicUrl\\}", this.publicUrl);
        assertEquals(expectedBody, message.getContent());
    }

    @Test
    public void testLatinEmailEncoding() throws IOException, MessagingException {
        Email email = new Email(recipients, "Compte créé", smtpHost, smtpPort, false, replyTo, from, "UTF-8",
                "UTF-8", "ISO-8859-15", this.isoTemplate, this.servletContext, this.georchestraConfiguration);

        // Generate message
        Message message = email.send(false);

        // Check email body
        String expectedBody = FileUtils.readFileToString(new File(this.getClass().getClassLoader().getResource(this.isoTemplate).getPath()), "ISO-8859-15");
        expectedBody = expectedBody.replaceAll("\\{publicUrl\\}", this.publicUrl);
        assertEquals(expectedBody, message.getContent());
    }


    @Test
    public void testReplace() throws IOException, MessagingException {
        Email email = new Email(recipients, "Compte créé", smtpHost, smtpPort, false, replyTo, from, "UTF-8",
                "UTF-8", "ISO-8859-15", this.replaceTemplate, this.servletContext, this.georchestraConfiguration);
        email.set("uid", "testadmin");
        email.set("var1", "value1");
        email.set("name", "Test Admin");
        Message message = email.send(false);

        String expectedBody = FileUtils.readFileToString(new File(this.getClass().getClassLoader().getResource(this.replaceTemplateResult).getPath()), "ascii");
        assertEquals(expectedBody, message.getContent());
    }

}
