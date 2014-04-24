/**
 * 
 */
package org.georchestra.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

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
    	try {
	        String remoteHost = originalRequest.getRemoteHost();
	        InetAddress remoteHostAddress =  InetAddress.getByName(remoteHost);
	        
	        for (String host : trustedHosts) {
	
	        	InetAddress hostAddress = InetAddress.getByName(host);
	        	
	        	if (log.isDebugEnabled()) {
	            	log.debug("filter: headerName: " + headerName + " check remote host: " + remoteHost + " against: " + host);
	            }
	        	
	        	if (remoteHostAddress.equals(hostAddress)) {
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

    	} catch (UnknownHostException e) {
			log.error(e);
			throw new IllegalStateException(e.getMessage() +  ". Check the trusted host configuration.");
		}
    }

    public void setTrustedHosts(List<String> trustedHosts) {
        this.trustedHosts = trustedHosts;
    }
}
