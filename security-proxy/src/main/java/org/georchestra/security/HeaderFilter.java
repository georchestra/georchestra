/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Filters headers from being copied to the request
 *
 * @author jeichar
 */
public interface HeaderFilter {
    /**
     * If this method returns true the header will <strong>not</strong> be added to
     * the proxy request.
     */
    boolean filter(String headerName, HttpServletRequest originalRequest, HttpRequestBase proxyRequest);

}
