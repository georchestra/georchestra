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
