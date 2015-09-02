package org.georchestra.ldapadmin.mailservice;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import javax.servlet.http.HttpServletRequest;

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

// TODO REFACTOR this package was copied from extractrapp.
// It should be refactored in a new module in order to share it.

public abstract class AbstractEmailFactory {

	protected String smtpHost;
	protected int smtpPort = -1;
	protected String emailHtml;
	protected String replyTo;
	protected String from;
	protected String bodyEncoding;
	protected String subjectEncoding;
	protected String[] languages;
	protected String  emailAckTemplateFile;
	protected String  emailTemplateFile;
	protected String  emailSubject;

    private boolean frozen = false;

    @Autowired
    protected GeorchestraConfiguration georConfig;

    public AbstractEmailFactory() {
        // this is the default constructor for use by spring
    }

	// -------------- Not public API -------------- //
    /**
     * Signals that the values for this object are set and may not
     * be changed.  This is to ensure that when the defaults are set
     * by Spring that later no one will change them programatically.
     *
     * The defaults should only be set via spring configuration.
     *
     * Freeze will be called by the class that has the parameter
     * set by spring.
     */
    public void freeze() {
        this.frozen = true;
        try {
            // just ensure smtpHost resolves to a valid ip
            InetAddress ip = InetAddress.getByName(smtpHost);
        } catch (IOException e) {
            throw new IllegalStateException(smtpHost + " doesnt resolve");
        }
        if (smtpPort < 0) {
            throw new IllegalStateException(smtpPort + " is not a legal port make sure it is correctly configured");
        }
        if (replyTo == null || from == null) {
            if (replyTo == null && from == null) {
                throw new IllegalStateException("Either or both from or replyTo must have a valid value");
            }
            if (replyTo == null) {
                replyTo = from;
            } else {
                from = replyTo;
            }
        }
        if (bodyEncoding == null) {
            bodyEncoding = "UTF-8";
        }
        if (subjectEncoding == null) {
            subjectEncoding = bodyEncoding;
        }
    }

    protected String readFile(HttpServletRequest request, final String path) throws IOException {
    	String realPath = request.getSession().getServletContext().getRealPath(path);
    	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(realPath), "UTF-8") );
        StringBuilder builder = new StringBuilder();
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line);
            }
        } finally {
            reader.close();
        }
        return builder.toString();
    }

    private void checkState() {
        if (frozen) {
            throw new IllegalStateException(this.getClass().getName() + " has already been frozen");
        }
    }

    // -------------- Bean setters/getters -------------- //
    public String getSmtpHost() {
        return smtpHost;
    }
    public void setSmtpHost(String smtpHost) {
        checkState();
        this.smtpHost = smtpHost;
    }
    public int getSmtpPort() {
        return smtpPort;
    }
    public void setSmtpPort(int port) {
        checkState();
        this.smtpPort = port;
    }
    public String getEmailHtml() {
        return emailHtml;
    }
    public void setEmailHtml(String emailHtml) {
        checkState();
        this.emailHtml = emailHtml;
    }
    public String getReplyTo() {
        return replyTo;
    }
    public void setReplyTo(String replyTo) {
        checkState();
        this.replyTo = replyTo;
    }
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        checkState();
        this.from = from;
    }
    public String getBodyEncoding() {
        return bodyEncoding;
    }
    public void setBodyEncoding(String bodyEncoding) {
        checkState();
        this.bodyEncoding = bodyEncoding;
    }
    public String getSubjectEncoding() {
        return subjectEncoding;
    }
    public void setSubjectEncoding(String subjectEncoding) {
        checkState();
        this.subjectEncoding = subjectEncoding;
    }
    public String[] getLanguages() {
        return languages;
    }
    public void setLanguages(String[] languages) {
        checkState();
        this.languages = languages;
    }

    public void setEmailAckTemplateFile(String emailAckTemplateFile) {
		this.emailAckTemplateFile = emailAckTemplateFile;
	}

	public void setEmailTemplateFile(String emailTemplateFile) {
		this.emailTemplateFile = emailTemplateFile;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}
}
