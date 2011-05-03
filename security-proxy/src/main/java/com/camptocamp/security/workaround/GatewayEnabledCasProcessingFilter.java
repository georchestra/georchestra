package com.camptocamp.security.workaround;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.Authentication;
import org.springframework.security.ui.cas.CasProcessingFilter;

public class GatewayEnabledCasProcessingFilter extends CasProcessingFilter {

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
