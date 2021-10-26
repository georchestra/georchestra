package org.georchestra.security;

import static org.georchestra.commons.security.SecurityHeaders.IMP_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.IMP_USERNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ImpersonateUserRequestHeaderProviderTest {

    @Test
    public void testGetCustomRequestHeadersUntrustedUser() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(IMP_USERNAME, "imp-user");
        request.addHeader(IMP_ROLES, "ROLE_IMP");

        // Reset auth
        SecurityContextHolder.getContext().setAuthentication(null);

        final ImpersonateUserRequestHeaderProvider provider = new ImpersonateUserRequestHeaderProvider();
        List<String> trustedUsers = new ArrayList<String>();
        trustedUsers.add("jeichar");
        provider.setTrustedUsers(trustedUsers);
        assertEquals(0, provider.getCustomRequestHeaders(request, null).size());

        Authentication auth = new UsernamePasswordAuthenticationToken("randomUser", "random");
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertEquals(0, provider.getCustomRequestHeaders(request, null).size());
    }

    @Test
    public void testGetCustomRequestHeadersTrustedUser() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(IMP_USERNAME, "imp-user");
        request.addHeader(IMP_ROLES, "ROLE_IMP");

        final ImpersonateUserRequestHeaderProvider provider = new ImpersonateUserRequestHeaderProvider();
        List<String> trustedUsers = new ArrayList<String>();
        trustedUsers.add("jeichar");
        provider.setTrustedUsers(trustedUsers);
        assertEquals(0, provider.getCustomRequestHeaders(request, null).size());

        Authentication auth = new UsernamePasswordAuthenticationToken("jeichar", "random");
        SecurityContextHolder.getContext().setAuthentication(auth);
        final Map<String, String> customRequestHeaders = provider.getCustomRequestHeaders(request, null);
        assertEquals(2, customRequestHeaders.size());
        assertContains(customRequestHeaders, SEC_USERNAME, "imp-user");
        assertContains(customRequestHeaders, SEC_ROLES, "ROLE_IMP");
    }

    private void assertContains(Map<String, String> customRequestHeaders, String headerName,
            String expectedHeaderValue) {

        String actualValue = customRequestHeaders.get(headerName);
        assertEquals("Wrong value for header: " + headerName, expectedHeaderValue, actualValue);
    }

}