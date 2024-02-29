package org.georchestra.console.filters;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class UserChangeFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Check if the user has the same roles as the ones in the session
        Set<String> sessionRoles = Set.of(((HttpServletRequest) request).getHeader("sec-roles").split(";"));
        if (!sessionRoles.equals(roles)) {
            ((HttpServletRequest) request).getSession().invalidate();
            ((HttpServletResponse) response).setStatus(419);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
