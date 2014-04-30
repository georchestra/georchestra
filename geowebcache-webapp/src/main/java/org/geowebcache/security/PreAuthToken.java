package org.geowebcache.security;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;

import java.util.Set;

/**
 * An authentication that is obtained by reading the credentials from the headers.
 *
 * @see org.geowebcache.security.PreAuthFilter
 *
 * @author Jesse on 4/24/2014.
 */
public class PreAuthToken extends AbstractAuthenticationToken{

    private final String principal;

    public PreAuthToken(String username, Set<String> roles) {
        super(createGrantedAuthorities(roles));
        this.principal = username;

        setAuthenticated(true);
        UserDetails details = new User(username, "", true, true, true, true, super.getAuthorities());
        setDetails(details);

    }

    private static GrantedAuthority[] createGrantedAuthorities(Set<String> roles) {
        GrantedAuthority[] authorities = new GrantedAuthority[roles.size()];
        int i = 0;
        for (String role : roles) {
            authorities[i] = new GrantedAuthorityImpl(role);
            i++;
        }
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
