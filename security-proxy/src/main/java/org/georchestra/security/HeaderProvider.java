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

import static org.georchestra.security.HeaderNames.PRE_AUTH_REQUEST_PROPERTY;

import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;

public abstract class HeaderProvider {
    protected static final Log logger = LogFactory.getLog(HeaderProvider.class.getPackage().getName());

    /**
     * Called by
     * {@link HeadersManagementStrategy#configureRequestHeaders(HttpServletRequest, HttpRequestBase)}
     * to allow extra headers to be added to the copied headers.
     * 
     * @param originalRequest   request being proxified
     * @param targetServiceName service name as defined in
     *                          {@code targets-mappings.properties}
     */
    public Collection<Header> getCustomRequestHeaders(HttpServletRequest originalRequest, String targetServiceName) {
        return Collections.emptyList();
    }

    /**
     * Called by
     * {@link HeadersManagementStrategy#configureRequestHeaders(HttpServletRequest, HttpRequestBase)}
     * to allow extra headers to be added to the copied headers.
     */
    protected Collection<Header> getCustomResponseHeaders() {
        return Collections.emptyList();
    }

    /**
     * @return {@code true} if the request comes from a trusted proxy and has been
     *         deemed pre-authorized by setting the {@code pre-auth} request
     *         attribute to {@code true}
     * @see ProxyTrustAnotherProxy
     */
    public static boolean isPreAuthorized(HttpServletRequest originalRequest) {
        return Boolean.TRUE.equals(originalRequest.getAttribute(PRE_AUTH_REQUEST_PROPERTY));
    }
}
