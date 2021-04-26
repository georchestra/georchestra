/*
 * Copyright (C) 2020 by the geOrchestra PSC
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
package org.georchestra.config.security;

import static org.georchestra.commons.security.SecurityHeaders.SEC_PROXY;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.georchestra.commons.security.SecurityHeaders;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "org.georchestra.config.security")
public class GeorchestraSecurityProxyAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    @Override
    protected GeorchestraUserDetails getPreAuthenticatedPrincipal(HttpServletRequest request) {
        final boolean preAuthenticated = getPreAuthenticatedCredentials(request);
        if (preAuthenticated) {
            Map<String, String> headers = extractSecHeaders(request);
            if (log.isDebugEnabled()) {
                log.debug("security-proxy headers: {}",
                        headers.entrySet().stream().map(n -> String.format("%s: '%s'", n.getKey(), n.getValue()))
                                .collect(Collectors.joining(",")));
            }

            GeorchestraUserDetails preAuthPrincipal = GeorchestraUserDetails.fromHeaders(headers);
            log.debug("principal: {}", preAuthPrincipal);
            return preAuthPrincipal;
        }
        return null;
    }

    /**
     * @return {@code true} if the request comes from georchestra's security proxy
     */
    @Override
    protected Boolean getPreAuthenticatedCredentials(HttpServletRequest request) {
        return Boolean.parseBoolean(SecurityHeaders.decode(request.getHeader(SEC_PROXY)));
    }

    private Map<String, String> extractSecHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())//
                .stream()//
                .filter(name -> name.startsWith("sec-"))//
                .collect(Collectors.toMap(Function.identity(), name -> request.getHeader(name)));
    }
}
