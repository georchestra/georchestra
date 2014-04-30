package org.geowebcache.security;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.AuthenticationProvider;

/**
 * A provider that accepts {@link org.geowebcache.security.PreAuthToken} authentication objects.
 *
 * @author Jesse on 4/24/2014.
 */
public class PreAuthProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof PreAuthToken) {
            PreAuthToken authToken = (PreAuthToken) authentication;
            return authToken;
        }
        return null;
    }

    @Override
    public boolean supports(Class authentication) {
        return PreAuthToken.class.isAssignableFrom(authentication);
    }
}
