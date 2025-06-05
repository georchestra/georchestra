/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.security;

import static org.georchestra.commons.security.SecurityHeaders.IMP_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.IMP_USERNAME;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
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
        assertEquals(expectedHeaderValue, actualValue, "Wrong value for header: " + headerName);
    }

}