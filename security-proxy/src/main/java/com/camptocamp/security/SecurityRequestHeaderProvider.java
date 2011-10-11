package com.camptocamp.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityRequestHeaderProvider extends HeaderProvider {

    @Override
    protected Collection<Header> getCustomRequestHeaders() {

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        Collection<GrantedAuthority> authorities = authentication.getAuthorities();
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("sec-username", authentication.getName()));
        StringBuilder roles = new StringBuilder();
        for (GrantedAuthority grantedAuthority : authorities) {
            if (roles.length() != 0) roles.append(",");

            roles.append(grantedAuthority.getAuthority());
        }
        headers.add(new BasicHeader("sec-roles", roles.toString()));

        return headers;
    }
}
