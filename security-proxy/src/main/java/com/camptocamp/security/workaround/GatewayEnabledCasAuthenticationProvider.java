/**
 * 
 */
package com.camptocamp.security.workaround;

import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.providers.cas.CasAuthenticationProvider;

/**
 * @author jeichar
 */
public class GatewayEnabledCasAuthenticationProvider extends CasAuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            return super.authenticate(authentication);
        } catch (BadCredentialsException e){
            return authentication;
        }
    }
    
}
