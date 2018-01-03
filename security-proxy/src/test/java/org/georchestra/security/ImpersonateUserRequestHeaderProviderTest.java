package org.georchestra.security;

import org.apache.http.Header;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ImpersonateUserRequestHeaderProviderTest {

    @Test
    public void testGetCustomRequestHeadersUntrustedUser() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderNames.IMP_USERNAME, "imp-user");
        request.addHeader(HeaderNames.IMP_ROLES, "ROLE_IMP");

        // Reset auth
        SecurityContextHolder.getContext().setAuthentication(null);

        final ImpersonateUserRequestHeaderProvider provider = new ImpersonateUserRequestHeaderProvider();
        List<String> trustedUsers = new ArrayList<String>();
        trustedUsers.add("jeichar");
        provider.setTrustedUsers(trustedUsers);
        assertEquals(0, provider.getCustomRequestHeaders(null, request).size());

        Authentication auth = new UsernamePasswordAuthenticationToken("randomUser", "random");
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertEquals(0, provider.getCustomRequestHeaders(null, request).size());
    }

    @Test
    public void testGetCustomRequestHeadersTrustedUser() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderNames.IMP_USERNAME, "imp-user");
        request.addHeader(HeaderNames.IMP_ROLES, "ROLE_IMP");

        final ImpersonateUserRequestHeaderProvider provider = new ImpersonateUserRequestHeaderProvider();
        List<String> trustedUsers = new ArrayList<String>();
        trustedUsers.add("jeichar");
        provider.setTrustedUsers(trustedUsers);
        assertEquals(0, provider.getCustomRequestHeaders(null, request).size());

        Authentication auth = new UsernamePasswordAuthenticationToken("jeichar", "random");
        SecurityContextHolder.getContext().setAuthentication(auth);
        final Collection<Header> customRequestHeaders = provider.getCustomRequestHeaders(null, request);
        assertEquals(2, customRequestHeaders.size());
        assertContains(customRequestHeaders, HeaderNames.SEC_USERNAME, "imp-user");
        assertContains(customRequestHeaders, HeaderNames.SEC_ROLES, "ROLE_IMP");
    }

    private void assertContains(Collection<Header> customRequestHeaders, String headerName, String expectedHeaderValue) {

        for (Header header : customRequestHeaders) {
            if (header.getName().equals(headerName)) {
                assertEquals("Wrong value for header: " + headerName, expectedHeaderValue, header.getValue());
                return;
            }
        }

        throw new AssertionError("No header "+ headerName +": "+ expectedHeaderValue + " in: " + customRequestHeaders);
    }


}