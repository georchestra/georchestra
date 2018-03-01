package org.georchestra.console.mailservice;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class EmailTest {


    protected String[] recipients;
    protected String emailSubject;
    protected String smtpHost;
    protected int smtpPort;
    protected String emailHtml;
    protected String replyTo;
    protected String from;
    protected String bodyEncoding;
    protected String subjectEncoding;
    protected String[] languages;
    protected String fileTemplate;

    protected Email mail;

    @Before
    public void doSetUp() {
        recipients = new String[]{ "me@localhost" };
        emailSubject = "your account has been created";
        smtpHost = "127.0.0.1";
        smtpPort = 25;
        emailHtml = "<html><body><h1>It works</h1></body></html>";
        replyTo = "test@localhost";
        from = "georchestra@localhost";
        bodyEncoding = "us/ascii";
        subjectEncoding = "us/ascii";
        languages = new String[]{ "en/US" };
        fileTemplate = "/dev/null";
    }

    @Test
    public void testMailToString() {
        if (mail != null) {

            assertTrue(mail.toString().equals("Email [smtpHost=127.0.0.1, smtpPort=25, "
                    + "emailHtml=<html><body><h1>It works</h1></body></html>, replyTo=test@localhost, "
                    + "from=georchestra@localhost, bodyEncoding=us/ascii, subjectEncoding=us/ascii, "
                    + "languages=[en/US], recipients=[me@localhost], subject=your account has been created, "
                    + "fileTemplate=/dev/null, emailBody=null]"));
        }
    }

    @Test
    public void testSendMessage() {

    }
}
