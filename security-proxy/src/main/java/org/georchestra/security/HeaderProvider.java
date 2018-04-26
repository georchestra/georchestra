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

import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;

public abstract class HeaderProvider {
    /**
     * Called by {@link HeadersManagementStrategy#configureRequestHeaders(HttpServletRequest, HttpRequestBase)} to allow
     * extra headers to be added to the copied headers.
     */
    protected Collection<Header> getCustomRequestHeaders(HttpSession session, HttpServletRequest originalRequest) {
        return Collections.emptyList();
    }

    /**
     * Called by {@link HeadersManagementStrategy#configureRequestHeaders(HttpServletRequest, HttpRequestBase)} to allow
     * extra headers to be added to the copied headers.
     */
    protected Collection<Header> getCustomResponseHeaders() {
        return Collections.emptyList();
    }

}
