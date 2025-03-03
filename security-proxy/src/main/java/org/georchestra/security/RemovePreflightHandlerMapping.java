package org.georchestra.security;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Prevent preflight requests interception.
 */
public class RemovePreflightHandlerMapping extends RequestMappingHandlerMapping {
    @Override
    protected HandlerExecutionChain getCorsHandlerExecutionChain(HttpServletRequest request,
            HandlerExecutionChain chain, CorsConfiguration config) {
        return chain; // Return the same chain it uses for everything else.
    }
}
