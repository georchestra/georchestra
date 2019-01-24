package org.georchestra.console.integration.instruments;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ModifiableUsernameToken extends UsernamePasswordAuthenticationToken {
    String userName;

    public ModifiableUsernameToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    @Override
    public String getName() {
        return userName;
    }

    public void setUserName(String name) {
        userName = name;
    }
}
