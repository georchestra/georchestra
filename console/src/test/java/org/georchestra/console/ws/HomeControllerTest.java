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

package org.georchestra.console.ws;

import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.georchestra.console.bs.ExpiredTokenCleanTask;
import org.georchestra.console.bs.ExpiredTokenManagement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author pmauduit
 *
 */
public class HomeControllerTest {

    private HomeController ctrl;
    private ExpiredTokenCleanTask tokenTask = Mockito.mock(ExpiredTokenCleanTask.class);
    private ExpiredTokenManagement expiredTokenMgmt = new ExpiredTokenManagement(tokenTask);

    @Before
    public void setUp() {
        expiredTokenMgmt.setDelayInDays(1);
        ctrl = new HomeController(expiredTokenMgmt);
        ctrl.setPublicContextPath("/console");
    }

    @Test
    public void testHomeControllerAnonymousRedirectToCas() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ctrl.root(request, response);

        assertTrue("expected 302, got " + response.getStatus(),
                response.getStatus() == HttpServletResponse.SC_MOVED_TEMPORARILY);
        assertTrue("bad redirectUrl, got " + response.getRedirectedUrl(),
                response.getRedirectedUrl().contains("/account/userdetails?login"));
    }

    @Test
    public void testHomeControllerAnonymous() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        ctrl.root(request, response);

        assertTrue("expected 302, got " + response.getStatus(),
                response.getStatus() == HttpServletResponse.SC_MOVED_TEMPORARILY);
        assertTrue("bad redirectUrl, got " + response.getRedirectedUrl(),
                response.getRedirectedUrl().contains("/account/userdetails?login"));
    }

    @Test
    public void testHomeControllerAuthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(SEC_ROLES, "ROLE_ADMINISTRATOR");
        ctrl.root(request, response);

        assertTrue(response.getRedirectedUrl().endsWith("/account/userdetails"));

    }

    @Test
    public void testHomeControllerLdapAdmin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(SEC_ROLES, "ROLE_SUPERUSER");
        ctrl.root(request, response);

        assertTrue(response.getRedirectedUrl().endsWith("/manager/"));

    }

}
