package org.georchestra.security;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Prevent preflight requests interception.
 */
public class RemovePreflightHandlerMapping extends DefaultAnnotationHandlerMapping {
    @Override
    protected HandlerExecutionChain getCorsHandlerExecutionChain(HttpServletRequest request,
            HandlerExecutionChain chain, CorsConfiguration config) {
        return chain; // Return the same chain it uses for everything else.
    }
}
