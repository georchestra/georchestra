package org.georchestra.security;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpSession;

public class SecurityRequestHeaderProvider extends HeaderProvider {

    @Override
    protected Collection<Header> getCustomRequestHeaders(HttpSession session) {

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<Header> headers = new ArrayList<Header>();
        if (authentication.getName().equals("anonymousUser"))
             return headers;
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
