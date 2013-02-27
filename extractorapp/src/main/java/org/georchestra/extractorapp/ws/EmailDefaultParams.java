package org.georchestra.extractorapp.ws;

import java.io.IOException;
import java.net.InetAddress;


/**
 * The common parameters for sending emails. This class can be frozen if it is a
 * defaults class so that it cannot be changed after original configuration.
 * 
 * @author jeichar
 */
public class EmailDefaultParams {
    private String smtpHost;
    private int smptPort = -1;
    private String replyTo;
    private String from;
    private String bodyEncoding;
    private String subjectEncoding;
    private String[] languages;
    
    private boolean frozen = false;
    
    public EmailDefaultParams() {
        // this is the default constructor for use by spring
    }
    
    /**
     * Copy constructor
     * @param defaults the defaults to copy into this object
     */
    protected EmailDefaultParams(EmailDefaultParams defaults) {
        smtpHost = defaults.smtpHost;
        smptPort = defaults.smptPort;
        replyTo = defaults.replyTo;
        from = defaults.from;
        bodyEncoding = defaults.bodyEncoding;
        subjectEncoding = defaults.subjectEncoding;
        languages = defaults.languages;
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
        if (SharedConstants.inProduction()) {
            try {
                if (!InetAddress.getByName(smtpHost).isReachable(3000)) {
                    throw new IllegalStateException(smtpHost + " is not a reachable address");
                }
            } catch (IOException e) {
                throw new IllegalStateException(smtpHost + " is not a reachable address");
            }
            if (smptPort < 0) {
                throw new IllegalStateException(smptPort + " is not a legal port make sure it is correctly configured");
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
    }
    
    private void checkState() {
        if (frozen) {
            throw new IllegalStateException("EmailDefaultParams have already been frozen");
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
    public int getSmptPort() {
        return smptPort;
    }
    public void setSmtpPort(int port) {
        checkState();
        this.smptPort = port;
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
}
