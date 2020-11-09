package org.georchestra.config.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeorchestraSecurityProxyAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String principal = request.getHeader("sec-username");
        return principal;
    }

    @Override
    protected List<String> getPreAuthenticatedCredentials(HttpServletRequest request) {
        String rolesHeader = request.getHeader("sec-roles");
        if (StringUtils.isEmpty(rolesHeader)) {
            return Collections.emptyList();
        }
        String[] roles = rolesHeader.split(";");
        List<String> credentials = Arrays.stream(roles).filter(StringUtils::hasText).collect(Collectors.toList());
        log.info("roles: " + credentials);
        return credentials;
    }

}
