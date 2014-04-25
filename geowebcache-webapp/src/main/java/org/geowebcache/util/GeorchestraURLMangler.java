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

	private String baseURL;
	private String contextPath;
	private String path;
	
	public void setBaseURL(String baseURL) {this.baseURL = baseURL; }
    public void setContextPath(String contextPath) {this.contextPath = contextPath; }
	public void setPath(String path) {this.path = path; }

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
