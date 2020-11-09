package org.georchestra.config.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class GeorchestraSecurityProxyAuthenticationManager implements AuthenticationManager {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) authentication.getCredentials();
            Object principal = authentication.getPrincipal();
            if (roles == null || roles.isEmpty()) {
                throw new AuthenticationCredentialsNotFoundException("No roles given for " + principal);
            }
            List<GrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            PreAuthenticatedAuthenticationToken auth = new PreAuthenticatedAuthenticationToken(principal, roles,
                    authorities);
            auth.setAuthenticated(true);
            return auth;
        }
        return authentication;
    }
}
