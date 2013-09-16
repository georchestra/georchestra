package org.georchestra.lib.mailservice;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Email {
	
	protected static final Log LOG = LogFactory.getLog(Email.class.getName());
	
    private final int DEB_MODEM = 56;
    private final int DEB_ADSL = 2000;
    private final int DEB_T1 = 20000;

	private String smtpHost;
    private int smtpPort = -1;
    private String emailHtml;
    private String replyTo;
    private String from;
    private String bodyEncoding;
    private String subjectEncoding;
    private String[] languages;
    private String[] recipients;
    private String subject;

	private String fileTemplate;

	private String emailBody;

    public Email( String[] recipients,
			final String emailSubject, final String smtpHost, final int smtpPort, final String emailHtml,
			final String replyTo, final String from, final String bodyEncoding,
			final String subjectEncoding, final String[] languages, String fileTemplate) {
		
		this.recipients = recipients;
		this.subject = emailSubject;
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
		this.emailHtml = emailHtml;
		this.replyTo = replyTo;
		this.from = from;
		this.bodyEncoding = bodyEncoding;
		this.subjectEncoding = subjectEncoding;
		this.languages = languages;
		this.fileTemplate = fileTemplate;
		
		if(LOG.isDebugEnabled()){
			LOG.debug(this.toString());
		}
	}
    
    
    
    @Override
	public String toString() {
		return "Email [DEB_MODEM=" + DEB_MODEM + ", DEB_ADSL=" + DEB_ADSL
				+ ", DEB_T1=" + DEB_T1 + ", smtpHost=" + smtpHost
				+ ", smtpPort=" + smtpPort + ", emailHtml=" + emailHtml
				+ ", replyTo=" + replyTo + ", from=" + from + ", bodyEncoding=" + bodyEncoding
				+ ", subjectEncoding=" + subjectEncoding + ", languages="
				+ Arrays.toString(languages) + ", recipients="
				+ Arrays.toString(recipients) + ", subject=" + subject
				+ ", fileTemplate=" + fileTemplate + ", emailBody=" + emailBody
				+ "]";
	}

	/**
     * Read the body from template
     * @param servletContext 
     * @return
     */
    protected String getBodyTemplate() {
    	
    	if(this.emailBody == null){
    		this.emailBody = loadBody(toAbsoltuPath(this.fileTemplate));
    	}
    	return this.emailBody;
    }
    
    protected abstract String toAbsoltuPath(String fileTemplate);
    
    /**
     * Loads the body template.
     * 
     * @param fileName path + file name
     * @return
     * @throws IOException
     */
    private String loadBody(final String fileName) {
    	
    	BufferedReader reader = null;
    	String body = null;
        try {
        	reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8") );
        	StringBuilder builder = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line).append("\n");
            }
            body = builder.toString();
            
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
    

	protected void sendMsg( final String msg) throws AddressException, MessagingException {
		
		if(LOG.isDebugEnabled() ){
			
			LOG.debug("body: "+ msg );
		}
		
		final Properties props = System.getProperties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.protocol.port", smtpPort); // from extractorapp
        //props.put("mail.smtp.port", smtpPort);
        
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
            if ("true".equalsIgnoreCase(emailHtml)) {
				bodyPart.setText(msg, bodyEncoding, "html");
			} else {
				bodyPart.setText(msg, bodyEncoding, "text");
			}
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

}
