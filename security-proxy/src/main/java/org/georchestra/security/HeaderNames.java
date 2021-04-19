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

package org.georchestra.security;

import org.georchestra.commons.security.SecurityHeaders;

/**
 * A collection of header names used in the proxy, besides the ones defined in
 * {@link SecurityHeaders}
 *
 * @author Jesse on 5/5/2014.
 */
public class HeaderNames {
    public static final String PROTECTED_HEADER_PREFIX = "sec-";
    // note: JSESSIONID is strictly speaking not a Header,
    // but usual part of the the Cookie / Set-Cookie header
    public static final String JSESSION_ID = "JSESSIONID";
    public static final String SET_COOKIE_ID = "Set-Cookie";
    public static final String COOKIE_ID = "Cookie";
    public static final String CONTENT_LENGTH = "content-length";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HOST = "host";
    public static final String LOCATION = "location";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String CHUNKED = "chunked";
    public static final String BASIC_AUTH_HEADER = "Authorization";
    public static final String REFERER_HEADER_NAME = "Referer";
}
