package org.georchestra.console.integration.instruments;


import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;
import java.util.Set;

public class WithMockRandomUidUserSecurityContextFactory implements WithSecurityContextFactory<WithMockRandomUidUser> {

    WithMockRandomUidUserSecurityContextFactory() {}

        @Override
        public SecurityContext createSecurityContext(WithMockRandomUidUser customUser) {
            String userAdminName = ("IT_USER_" + RandomStringUtils.randomAlphabetic(8)).toLowerCase();

            Set<SimpleGrantedAuthority> grantedAuthorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_SUPERUSER"));
            User principal = new User(userAdminName, "password", true, true, true, true, grantedAuthorities);

            Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            return context;
        }
    }

