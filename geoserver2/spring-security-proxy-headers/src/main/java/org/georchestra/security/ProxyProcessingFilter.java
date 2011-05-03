package org.georchestra.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.ui.SpringSecurityFilter;

public class ProxyProcessingFilter extends SpringSecurityFilter implements InitializingBean {
    private static final Log logger = LogFactory.getLog(ProxyProcessingFilter.class);

    public static String ANONYMOUS_USERNAME = "anonymousUser";
    
    private List<String> securityProxyServers = new ArrayList<String>();
    private boolean forceHeadersUsage = true;
    private String anonymousUsername = ANONYMOUS_USERNAME;
    
    /* (non-Javadoc)
     * @see org.springframework.security.ui.SpringSecurityFilter#doFilterHttp(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
     */
    @Override
    protected void doFilterHttp(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
    	
        String remoteHost = request.getRemoteHost();
        String username = request.getHeader("sec-username");
        
        if((fromSecurityProxyServer(remoteHost)) &&
                username != null && !anonymousUsername.equals(username)){
            if (logger.isDebugEnabled()) {
                logger.debug("username: " + username);
            }

            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            String roleHeaderKey = "sec-roles";
            String roleString = request.getHeader(roleHeaderKey) == null ? "" : request.getHeader(roleHeaderKey);

            if (logger.isDebugEnabled()) {
                logger.debug("Roles: " + roleString);
            }

            for (String auth : roleString.split(",")) {
                authorities.add(new GrantedAuthorityImpl(auth));
            }
            
            GrantedAuthority[] grantedAuthorities = authorities.toArray(new GrantedAuthority[authorities.size()]);
            UsernamePasswordAuthenticationToken authResult = new UsernamePasswordAuthenticationToken(username,
                    "proxy-processing-filter", grantedAuthorities);

            // Authentication success
            if (logger.isDebugEnabled()) {
                logger.debug("Authentication success: " + authResult.toString());
            }

            SecurityContextHolder.getContext().setAuthentication(authResult);
        } else {            
            // Authentication failed
            if (logger.isDebugEnabled()) {
                logger.debug("no security headers from configured authorized servers"+toString(securityProxyServers)+", so no authentication");
            }

            if (forceHeadersUsage) {
            	//
            	// Clear security context if no header or request not coming from proxy.
            	//
            	SecurityContextHolder.getContext().setAuthentication(null);
            }
        }
        chain.doFilter(request, response);
    }

    private String toString(List<String> list) {
        StringBuilder buf = new StringBuilder();
        for (String string : list) {
            if(buf.length() > 0) buf.append(", ");
            buf.append(string);
        }
        return buf.toString();
    }

    private boolean fromSecurityProxyServer(String remoteHost) {
        for (String string : securityProxyServers) {
            if(string.equals(remoteHost)) return true;
        }
        return false;
    }

    public void setSecurityProxyServers(List<String> securityProxyServers) {
        this.securityProxyServers = securityProxyServers;
    }


	/* (non-Javadoc)
     * @see org.springframework.core.Ordered#getOrder()
     */
    public int getOrder() {
	    return 410;
    }


	/* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
	    // TODO Auto-generated method stub
	    
    }

	public void setForceHeadersUsage(boolean forceHeadersUsage) {
    	this.forceHeadersUsage = forceHeadersUsage;
    }

	public void setAnonymousUsername(String anonymousUsername) {
    	this.anonymousUsername = anonymousUsername;
    }
    
}
