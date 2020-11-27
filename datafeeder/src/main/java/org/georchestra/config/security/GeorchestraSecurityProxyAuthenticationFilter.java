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
    protected GeorchestraUserDetails getPreAuthenticatedPrincipal(HttpServletRequest request) {
        for (String h : Collections.list(request.getHeaderNames())) {
            System.err.printf("%s=%s%n", h, request.getHeader(h));
        }
        final boolean preAuthenticated = Boolean.parseBoolean(request.getHeader("sec-proxy"));
        if (preAuthenticated) {
            String username = request.getHeader("sec-username");
            final boolean anonymous = username == null;
            if (anonymous) {
                username = "anonymousUser";
            }
            List<String> roles = extractRoles(request);
            String email = request.getHeader("sec-email");
            String firstName = request.getHeader("sec-firstname");
            String lastName = request.getHeader("sec-lastname");
            String organization = request.getHeader("sec-org");
            String organizationName = request.getHeader("sec-orgname");
            return new GeorchestraUserDetails(username, roles, email, firstName, lastName, organization,
                    organizationName, anonymous);
        }
        return null;
    }

    /**
     * @return {@code true} if the request comes from georchestra's security proxy
     */
    @Override
    protected Boolean getPreAuthenticatedCredentials(HttpServletRequest request) {
        return Boolean.parseBoolean(request.getHeader("sec-proxy"));
    }

    private List<String> extractRoles(HttpServletRequest request) {
        String rolesHeader = request.getHeader("sec-roles");
        if (StringUtils.isEmpty(rolesHeader)) {
            return Collections.emptyList();
        }

        String[] roles = rolesHeader.split(";");
        log.info("roles: {}", roles == null ? null : Arrays.toString(roles));
        return Arrays.stream(roles).filter(StringUtils::hasText).collect(Collectors.toList());
    }

}
