package org.georchestra.security;

import org.apache.http.client.methods.HttpRequestBase;

import javax.servlet.http.HttpServletRequest;

/**
 * Filters headers from being copied to the request
 * 
 * @author jeichar
 */
public interface HeaderFilter {
    /**
     * If this method returns true the header will <strong>not</strong> be added to the proxy request.
     */
    boolean filter(String headerName, HttpServletRequest originalRequest, HttpRequestBase proxyRequest);

}
