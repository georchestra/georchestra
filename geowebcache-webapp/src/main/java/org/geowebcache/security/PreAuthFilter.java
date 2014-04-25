package org.geowebcache.security;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * A filter that checks the headers of the request and determines if the user is already logged in, and therefore
 * the credentials the user is permitted.
 *
 * @author Jesse on 4/24/2014.
 */
public class PreAuthFilter implements Filter {
    private static final Log logger = LogFactory.getLog(PreAuthFilter.class);
    public static final String SEC_USERNAME = "sec-username";
    public static final String SEC_ROLES = "sec-roles";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            final String username = httpServletRequest.getHeader(SEC_USERNAME);
            if (username != null) {
                SecurityContextHolder.getContext().setAuthentication(createAuthentication(httpServletRequest));

                if (logger.isDebugEnabled()) {
                    logger.debug("Populated SecurityContextHolder with pre-auth token: '"
                                 + SecurityContextHolder.getContext().getAuthentication() + "'");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("SecurityContextHolder not populated with pre-auth token");
                }
            }
        }

        chain.doFilter(request,response);
    }

    private Authentication createAuthentication(HttpServletRequest httpServletRequest) {
        final String username = httpServletRequest.getHeader(SEC_USERNAME);
        final String rolesString = httpServletRequest.getHeader(SEC_ROLES);
        Set<String> roles = new LinkedHashSet<String>();
        if (rolesString != null) {
            roles.addAll(Arrays.asList(rolesString.split(",")));
        }
        return new PreAuthToken(username, roles);
    }

    @Override
    public void destroy() {

    }
}