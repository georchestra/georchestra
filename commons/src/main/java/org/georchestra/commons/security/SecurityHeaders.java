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

package org.georchestra.commons.security;

/**
 * A collection of header names commonly used by the security-proxy gateway
 * application
 *
 * @author Jesse on 5/5/2014.
 */
public class SecurityHeaders {
    // well-known header names
    public static final String SEC_PROXY = "sec-proxy";
    public static final String SEC_USERNAME = "sec-username";
    public static final String SEC_ORG = "sec-org";
    public static final String SEC_ORGNAME = "sec-orgname";
    public static final String SEC_ROLES = "sec-roles";
    public static final String SEC_FIRSTNAME = "sec-firstname";
    public static final String SEC_LASTNAME = "sec-lastname";
    public static final String SEC_EMAIL = "sec-email";
    public static final String SEC_TEL = "sec-tel";
    public static final String IMP_ROLES = "imp-roles";
    public static final String IMP_USERNAME = "imp-username";

    public static String decode(String headerValue) {
        return headerValue;
    }
}
