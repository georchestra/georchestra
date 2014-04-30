package org.geowebcache.util;

import org.apache.commons.lang.StringUtils;

/**
 * A simple URLMangler that would override values
 * provided by the servlet engine.
 * 
 * 
 * @author pmauduit
 */
public class GeorchestraURLMangler implements URLMangler {

	private final String baseURL;
	private final String contextPath;
	private final String path;

	public GeorchestraURLMangler(String baseUrl, String contextPath, String path) {
		this.baseURL = baseUrl;
		this.contextPath = contextPath;
		this.path = path;
	}

	public String buildURL(String b, String cP, String p) {
        return StringUtils.strip(baseURL, "/") + "/" + StringUtils.strip(contextPath, "/") + "/"
                + StringUtils.stripStart(path, "/");
	}

}
