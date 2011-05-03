package org.georchestra.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProxyProcessingFilter implements Filter {
    private static final Log logger = LogFactory.getLog(ProxyProcessingFilter.class);
    private List<String> securityProxyServers = new ArrayList<String>();
    @Override
    public void destroy() {
        // nothing to do
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        if (!(request instanceof HttpServletRequest)) {
            throw new ServletException("Can only process HttpServletRequest");
        }

        if (!(response instanceof HttpServletResponse)) {
            throw new ServletException("Can only process HttpServletResponse");
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String remoteHost = httpRequest.getRemoteHost();
        String username = httpRequest.getHeader("sec-username"); 
        boolean usernamePresent = username !=null && !"roleAnonymous".equals(username);
        if((fromSecurityProxyServer(remoteHost)) &&
                usernamePresent){
            if (logger.isDebugEnabled()) {
                logger.debug("username: " + username);
            }

            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            String roleHeaderKey = "sec-roles";
            String roleString = httpRequest.getHeader(roleHeaderKey )==null?"":httpRequest.getHeader(roleHeaderKey);

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
                if(usernamePresent) {
                String roleHeaderKey = "sec-roles";
                String roleString = httpRequest.getHeader(roleHeaderKey )==null?"":httpRequest.getHeader(roleHeaderKey);
                  logger.warn("Security headers found from untrusted server: "+remoteHost+" expected one of "+
                    toString(securityProxyServers)+"\nheaders:\n\tusername: "+username+"\n\t roles:"+roleString);
                } else {
                  logger.debug("no security headers from configured authorized servers"+toString(securityProxyServers)+", so no authentication");
                }
                
            }

            SecurityContextHolder.getContext().setAuthentication(null);
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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to do
    }

    public void setSecurityProxyServers(List<String> securityProxyServers) {
        this.securityProxyServers = securityProxyServers;
    }
    
    
}
