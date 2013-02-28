/**
 * 
 */
package org.georchestra.security.workaround;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

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
