package org.georchestra.ldapadmin.mailservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Email {

	protected static final Log LOG = LogFactory.getLog(Email.class.getName());

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

	protected GeorchestraConfiguration georConfig;
	
    public Email( String[] recipients,
			final String emailSubject, final String smtpHost, final int smtpPort, final String emailHtml,
			final String replyTo, final String from, final String bodyEncoding,
			final String subjectEncoding, final String[] languages, String fileTemplate, GeorchestraConfiguration georConfig) {

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
		this.georConfig = georConfig;

		if(LOG.isDebugEnabled()){
			LOG.debug("Email instanciated: " + this.toString());
		}
	}



    @Override
	public String toString() {
		return "Email [smtpHost=" + smtpHost
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
    		this.emailBody = loadBody(toAbsolutePath(this.fileTemplate));
    	}
    	return this.emailBody;
    }

    protected abstract String toAbsolutePath(String fileTemplate);

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
                return FileUtils.readFileToString(new File(georConfig.getContextDataDir(), "templates/" + basename));
            } catch (IOException e) {
                LOG.error("Unable to get the template from geOrchestra datadir. Falling back on the default template provided by the webapp.", e);
            }
        }

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

        if (msg != null) {
            /* See http://www.rgagnon.com/javadetails/java-0321.html */
            if ("true".equalsIgnoreCase(emailHtml)) {
                message.setContent(msg, "text/html; charset=" + bodyEncoding);
            } else {
                message.setContent(msg, "text/plain; charset=" + bodyEncoding);
            }
            LOG.debug(msg);
        }

        Transport.send(message);
        LOG.debug("email has been sent to:\n"
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
