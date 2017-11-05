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

package org.geowebcache.util;

import org.apache.commons.lang.StringUtils;

/**
 * A simple URLMangler that overrides values provided by the servlet engine,
 * only keeping the most significant parts of the URL (after the hostname and
 * the webapp name).
 *
 * @author pmauduit
 */
public class GeorchestraURLMangler implements URLMangler {

	private final String baseURL;
	private final String contextPath;

	public GeorchestraURLMangler(String baseUrl, String contextPath) {
		this.baseURL = baseUrl;
		this.contextPath = contextPath;
	}

	public String buildURL(String baseURL, String contextPath, String path) {
        return StringUtils.strip(this.baseURL, "/") + "/" + StringUtils.strip(this.contextPath, "/") + "/"
                + StringUtils.stripStart(path, "/");
	}

}
