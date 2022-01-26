/*
 * Copyright (C) 2009 by the geOrchestra PSC
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
    private int smtpPort = -1;
    private String emailHtml;
    private String replyTo;
    private String from;
    private String bodyEncoding;
    private String subjectEncoding;

    private boolean frozen = false;

    public EmailDefaultParams() {
        // this is the default constructor for use by spring
    }

    /**
     * Copy constructor
     * 
     * @param defaults the defaults to copy into this object
     */
    protected EmailDefaultParams(EmailDefaultParams defaults) {
        smtpHost = defaults.smtpHost;
        smtpPort = defaults.smtpPort;
        emailHtml = defaults.emailHtml;
        replyTo = defaults.replyTo;
        from = defaults.from;
        bodyEncoding = defaults.bodyEncoding;
        subjectEncoding = defaults.subjectEncoding;
    }

    // -------------- Not public API -------------- //
    /**
     * Signals that the values for this object are set and may not be changed. This
     * is to ensure that when the defaults are set by Spring that later no one will
     * change them programatically.
     * 
     * The defaults should only be set via spring configuration.
     * 
     * Freeze will be called by the class that has the parameter set by spring.
     */
    public void freeze() {
        this.frozen = true;
        try {
            if (!InetAddress.getByName(smtpHost).isReachable(3000)) {
                throw new IllegalStateException(smtpHost + " is not a reachable address");
            }
        } catch (IOException e) {
            throw new IllegalStateException(smtpHost + " is not a reachable address");
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
}
