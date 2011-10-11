package com.camptocamp.security.workaround;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;

public class GatewayEnabledCasProcessingFilter extends CasAuthenticationFilter {

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }
    
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            Authentication authResult) throws IOException, ServletException {
        
        super.successfulAuthentication(request, response, authResult);
    }
}
