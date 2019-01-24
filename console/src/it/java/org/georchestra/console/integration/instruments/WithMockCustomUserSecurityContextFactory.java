package org.georchestra.console.integration.instruments;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;
import java.util.Set;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    WithMockCustomUserSecurityContextFactory() {}

        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
            Set<SimpleGrantedAuthority> grantedAuthorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_SUPERUSER"));
            User principal = new User("tmp", "password", true, true, true, true, grantedAuthorities);

            Authentication auth = new ModifiableUsernameToken(principal, principal.getPassword(), principal.getAuthorities());

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            return context;
        }
    }

