/**
 * 
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
