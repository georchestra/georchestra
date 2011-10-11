/**
 * 
 */
package com.camptocamp.security;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Filters out sec-username when not from trusted hosts
 * 
 * @author jeichar
 */
public class SecurityRequestHeaderFilter implements HeaderFilter {
	protected static transient Log log = LogFactory.getLog(SecurityRequestHeaderFilter.class);
	
    List<String> trustedHosts = Collections.emptyList();

    @Override
    public boolean filter(String headerName, HttpServletRequest originalRequest, HttpRequestBase proxyRequest) {
        String remoteHost = originalRequest.getRemoteHost();
        
        for (String host : trustedHosts) {
        	if (log.isDebugEnabled()) {
            	log.debug("filter: headerName: " + headerName + " check remote host: " + remoteHost + " against: " + host);
            }
        	if (remoteHost.equalsIgnoreCase(host)) {
        		if (log.isDebugEnabled()) {
        			log.debug("Return false for header: " + headerName + " because host: " + remoteHost + " is trusted.");
        		}
                return false;
            }
        }
        
        //
        // Return false if host is not trusted and header is sensitive
        //
        return headerName.equalsIgnoreCase("sec-username") ||
                headerName.equalsIgnoreCase("sec-roles");
    }

    public void setTrustedHosts(List<String> trustedHosts) {
        this.trustedHosts = trustedHosts;
    }
}
