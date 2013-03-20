package org.georchestra.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Filters headers from being copied to the request
 * 
 * @author jeichar
 */
public interface HeaderFilter {
    boolean filter(String headerName, HttpServletRequest originalRequest, HttpRequestBase proxyRequest);

}
