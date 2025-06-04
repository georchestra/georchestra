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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

public class UrlFormEncodedPostFilter extends OncePerRequestFilter {
    public void destroy() {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getMethod().equalsIgnoreCase(HttpMethod.POST.name()) && isFormContentType(request)) {
            // Morph the request to a WrappedRequest, as this will
            // allow us to read the body content in the Proxy.java class once again.
            request = new WrappedRequest(request);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Returns if the request is a POST x-www-form-urlencoded or not.
     *
     * @return true if this is the case, else false.
     *
     */
    private boolean isFormContentType(HttpServletRequest request) {
        if (request.getContentType() == null) {
            return false;
        }
        String contentType = request.getContentType().split(";")[0].trim();

        return "application/x-www-form-urlencoded".equalsIgnoreCase(contentType);
    }

    private static class WrappedRequest extends HttpServletRequestWrapper {
        byte[] content = new byte[0];
        private static Logger Logger = LoggerFactory.getLogger(WrappedRequest.class);

        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request
         * @throws IllegalArgumentException if the request is null
         */
        public WrappedRequest(HttpServletRequest request) {
            super(request);

            try {
                this.content = IOUtils.toByteArray(request.getInputStream());
            } catch (IOException e) {
                Logger.error("Unable to extract body payload", e);
            }
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new CachedContentInputStream(this.content);
        }

        @Override
        public BufferedReader getReader() throws IOException {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.content);
            return new BufferedReader(new InputStreamReader(byteArrayInputStream));
        }

    }
}
