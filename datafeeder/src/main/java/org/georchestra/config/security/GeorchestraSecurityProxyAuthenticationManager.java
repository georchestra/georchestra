package org.georchestra.config.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class GeorchestraSecurityProxyAuthenticationManager implements AuthenticationManager {

    @Override
    public PreAuthenticatedAuthenticationToken authenticate(Authentication authentication)
            throws AuthenticationException {
        Object principal = authentication.getPrincipal();

        if (principal instanceof GeorchestraUserDetails) {
            GeorchestraUserDetails user = (GeorchestraUserDetails) principal;
            PreAuthenticatedAuthenticationToken auth;
            if (user.isAnonymous()) {
                auth = createAnonymousAuthenticationToken();
            } else {
                Object credentials = null;// i.e. password
                Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
                auth = new PreAuthenticatedAuthenticationToken(principal, credentials, authorities);
            }
            auth.setAuthenticated(true);
            return auth;
        }
        return null;
    }

    private PreAuthenticatedAuthenticationToken createAnonymousAuthenticationToken() {
        List<String> roles = Collections.singletonList("ROLE_ANONYMOUS");
        GeorchestraUserDetails principal = new GeorchestraUserDetails("anonymousUser", roles, null, null, null, null,
                null, true);
        Object credentials = null;// i.e. password
        return new PreAuthenticatedAuthenticationToken(principal, credentials, principal.getAuthorities());
    }
}
