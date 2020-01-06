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

package org.georchestra.geowebcache.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A filter that checks the headers of the request and determines if the user is already logged in,
 * and therefore the credentials the user is permitted.
 *
 * @author Jesse on 4/24/2014.
 */
public class PreAuthFilter implements Filter {
    private static final Log logger = LogFactory.getLog(PreAuthFilter.class);
    public static final String SEC_USERNAME = "sec-username";
    public static final String SEC_ROLES = "sec-roles";

    public PreAuthFilter() {
        logger.warn("PreAuthFilter");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.warn("PreAuthFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            final String username = httpServletRequest.getHeader(SEC_USERNAME);
            if (username != null) {
                SecurityContext context = SecurityContextHolder.getContext();
                Authentication authentication = createAuthentication(httpServletRequest);
                context.setAuthentication(authentication);

                logger.warn(
                        "Populated SecurityContextHolder with pre-auth token: '"
                                + context.getAuthentication()
                                + "'");

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Populated SecurityContextHolder with pre-auth token: '"
                                    + context.getAuthentication()
                                    + "'");
                }
            } else {
                logger.warn("SecurityContextHolder not populated with pre-auth token");
                if (logger.isDebugEnabled()) {
                    logger.debug("SecurityContextHolder not populated with pre-auth token");
                }
            }
        }

        chain.doFilter(request, response);
    }

    private Authentication createAuthentication(HttpServletRequest httpServletRequest) {
        final String username = httpServletRequest.getHeader(SEC_USERNAME);
        final String rolesString = httpServletRequest.getHeader(SEC_ROLES);
        Set<String> roles = new LinkedHashSet<String>();
        if (rolesString != null) {
            roles.addAll(Arrays.asList(rolesString.split(";")));
        }
        return new PreAuthToken(username, roles);
    }

    @Override
    public void destroy() {}
}
