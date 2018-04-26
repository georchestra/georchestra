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

package org.georchestra.extractorapp.ws;

/**
 * Default params with the recipients and message
 * 
 * @author jeichar
 */
public class CompleteEmailParams extends EmailDefaultParams {

    private final String[] recipients;
    private final String   message;
    private final String   subject;

    /**
     * Copies the defaults to the new object (defaults are not modified)
     * 
     * @param defaults
     */
    public CompleteEmailParams(EmailDefaultParams defaults, String[] recipients, String subject, String message) {
        super(defaults);
        this.recipients = recipients;
        this.message = message;
        this.subject = subject;
    }

    public String[] getRecipients() {
        return recipients;
    }

    public String getMessage() {
        return message;
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public void freeze() {
        // do nothing. Only the actual
        // defaults object can be frozen
    }
}
