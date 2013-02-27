package org.georchestra.extractorapp.ws;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Email {
	
	protected static final Log LOG = LogFactory.getLog(Email.class
            .getPackage().getName());
	
    private final int DEB_MODEM = 56;
    private final int DEB_ADSL = 2000;
    private final int DEB_T1 = 20000;

	private String smtpHost;
    private int smtpPort = -1;
    private String replyTo;
    private String from;
    private String bodyEncoding;
    private String subjectEncoding;
    private String[] languages;
    private String[] recipients;
    private String subject;
	
	public Email(HttpServletRequest request, String[] recipients,
			final String emailSubject, final String smtpHost, final int smtpPort,
			final String replyTo, final String from, final String bodyEncoding,
			final String subjectEncoding, final String[] languages) {
		
		this.recipients = recipients;
		this.subject = emailSubject;
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
		this.replyTo = replyTo;
		this.from = from;
		this.bodyEncoding = bodyEncoding;
		this.subjectEncoding = subjectEncoding;
		this.languages = languages;
	}
	
	public abstract void sendAck() throws AddressException, MessagingException; 
	
	public abstract void sendDone(List<String> successes, List<String> failures,
            List<String> oversized, long fileSize) throws MessagingException;

	protected void sendMsg(final String msg) throws AddressException, MessagingException {
		
		final Properties props = System.getProperties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.protocol.port", smtpPort);
        final Session session = Session.getInstance(props, null);
        final MimeMessage message = new MimeMessage(session);
        
        if (isValidEmailAddress(from)) {
            message.setFrom(new InternetAddress(from));
        }
        boolean validRecipients = false;
        for (String recipient : recipients) {
            if (isValidEmailAddress(recipient)) {
                validRecipients = true;
                message.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(recipient));
            }
        }

        if (!validRecipients) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(from));
            message.setSubject(
                    "[ERREUR] Message non délivré : "
                            + subject,
                            subjectEncoding);
        } else {
            message.setSubject(subject, subjectEncoding);
        }

        Multipart multipart = new MimeMultipart();

        if (msg != null) {
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(msg, bodyEncoding, "html");
            bodyPart.setContentLanguage(languages);
            multipart.addBodyPart(bodyPart);
            LOG.debug(msg);
        }

        message.setContent(multipart);
        Transport.send(message);
        LOG.debug("extraction email has been sent to:\n"
                + Arrays.toString(recipients));
	}
	
	protected static boolean isValidEmailAddress(String address) {
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
	
	protected String format(List<String> list) {
        if (list.isEmpty()) {
            return "<p>aucune</p>";
        }
        StringBuilder b = new StringBuilder("<ul>");
        for (String string : list) {
            b.append("<li>");
            b.append(string);
            b.append("</li>");
        }
        b.append("</ul>");

        return b.toString();
    }
    
	protected String formatTimeEstimation(String msg, final long fileSize) {
    	
    	long fSizeBits = fileSize*8;
    	long tModem = fSizeBits / DEB_MODEM;
    	long tADSL = fSizeBits / DEB_ADSL;
    	long tT1 = fSizeBits / DEB_T1;
    	
    	msg = msg.replace("{fSize}", String.valueOf(fileSize));
    	msg = msg.replace("{tModem}", String.format("%02d:%02d", tModem/3600, (tModem%3600)/60));
    	msg = msg.replace("{tADSL}", String.format("%02d:%02d", tADSL/3600, (tADSL%3600)/60));
    	msg = msg.replace("{tT1}", String.format("%02d:%02d", tT1/3600, (tT1%3600)/60));
   	
    	return msg;
    }
}
