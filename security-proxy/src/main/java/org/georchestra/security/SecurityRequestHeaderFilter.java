/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;

import javax.servlet.http.HttpServletRequest;

/**
 * Filters out sec-username when not from trusted hosts
 * 
 * @author jeichar
 */
public class SecurityRequestHeaderFilter implements HeaderFilter {
	protected static transient Log log = LogFactory.getLog(SecurityRequestHeaderFilter.class);


    @Override
    public boolean filter(String headerName, HttpServletRequest originalRequest, HttpRequestBase proxyRequest) {
	        return headerName.equalsIgnoreCase(HeaderNames.SEC_USERNAME) ||
	                headerName.equalsIgnoreCase(HeaderNames.SEC_ROLES) ||
	                headerName.equalsIgnoreCase(HeaderNames.IMP_USERNAME) ||
	                headerName.equalsIgnoreCase(HeaderNames.IMP_ROLES);
    }
}
