/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @see ProxyTrustAnotherProxy
 */
public class TrustedProxyRequestHeaderProvider extends HeaderProvider {

    @PostConstruct
    public void init() {
        logger.info("Will forward incoming headers for pre-authorized requests from trusted proxies");
    }

    @Override
    public Map<String, String> getCustomRequestHeaders(HttpServletRequest originalRequest, String targetServiceName) {
        if (!isPreAuthorized(originalRequest)) {
            return Collections.emptyMap();
        }
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> e = originalRequest.getHeaderNames();
        while (e.hasMoreElements()) {
            String headerName = e.nextElement();
            String value = originalRequest.getHeader(headerName);
            if (logger.isDebugEnabled()) {
                logger.debug("Adding header: " + headerName + ", value: " + value);
            }
            headers.put(headerName, value);
        }
        return headers;
    }
}
