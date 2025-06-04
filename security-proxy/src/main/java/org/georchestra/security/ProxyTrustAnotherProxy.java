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

import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;
import static org.georchestra.security.HeaderNames.PRE_AUTH_REQUEST_PROPERTY;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class ProxyTrustAnotherProxy extends AbstractPreAuthenticatedProcessingFilter {

    private static String AUTH_HEADER = SEC_USERNAME;
    private static final Log logger = LogFactory.getLog(ProxyTrustAnotherProxy.class.getPackage().getName());

    /* The default value is an empty list of trusted proxies */
    private String rawProxyValue = "";
    private Set<InetAddress> trustedProxies = new HashSet<InetAddress>();

    @PostConstruct
    public void init() {
        if (rawProxyValue == "") {
            logger.info("\"trustedProxy\" property is not defined. Skipping bean configuration");
            return;
        }
        rawProxyValue = rawProxyValue.trim();

        String[] rawProxyList;
        if (rawProxyValue.length() != 0) {
            rawProxyList = rawProxyValue.split("\\s*,\\s*");
        } else {
            rawProxyList = new String[0];
        }

        for (String proxy : rawProxyList) {
            InetAddress trustedProxyAddress;
            try {
                trustedProxyAddress = InetAddress.getByName(proxy);
            } catch (UnknownHostException e) {
                logger.error("Unable to lookup " + proxy + ". skipping.");
                continue;
            }
            this.trustedProxies.add(trustedProxyAddress);
            logger.info("Add trusted proxy : " + trustedProxyAddress);
        }
        if (this.trustedProxies.size() == 0) {
            logger.info("No trusted proxy loaded");
        } else {
            logger.info("Successful loading of " + this.trustedProxies.size() + " trusted proxy");
        }

        this.setContinueFilterChainOnUnsuccessfulAuthentication(true);
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        if (this.trustedProxies.isEmpty()) {
            return null;
        }
        try {
            if (this.trustedProxies.contains(InetAddress.getByName(request.getRemoteAddr()))) {
                String username = request.getHeader(AUTH_HEADER);
                if (username != null) {
                    logger.debug("Request from a trusted proxy, so log in user : " + username);
                    request.setAttribute(PRE_AUTH_REQUEST_PROPERTY, Boolean.TRUE);
                } else {
                    logger.debug("Request from a trusted proxy, but no sec-username header found");
                }
                return username;
            } else {
                logger.debug("Request from a NON trusted proxy, bypassing log in");
                return null;
            }
        } catch (UnknownHostException e) {
            logger.error("Unable to resolve remote address : " + request.getRemoteAddr());
            return null;
        }
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

    public void setRawProxyValue(String rawProxyValue) {
        this.rawProxyValue = rawProxyValue;
    }
}
