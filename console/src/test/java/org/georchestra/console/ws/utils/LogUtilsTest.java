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

package org.georchestra.console.ws.utils;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.georchestra.console.model.AdminLogEntry;
import org.georchestra.console.model.AdminLogType;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class LogUtilsTest {

    private AdminLogEntry log;

    @Before
    public void resetSecurityContext() {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
    }

    @Test
    public void createLogNoAuth() {
        SecurityContextHolder.clearContext();
        LogUtils logUtils = new LogUtils();
        log = logUtils.createLog("csc", AdminLogType.ORG_CREATED, null);
        assertEquals(null, log);
    }

    @Test
    public void createLogWithAuthAndValues() {
        // Set user connected through spring security
        List<GrantedAuthority> role = new LinkedList<GrantedAuthority>();
        role.add(new SimpleGrantedAuthority("ROLE_SUPERUSER"));
        Authentication auth = new PreAuthenticatedAuthenticationToken("testadmin", null, role);
        SecurityContextHolder.getContext().setAuthentication(auth);

        LogUtils logUtils = new LogUtils();
        JSONObject json = logUtils.getLogDetails("name", "monkey1", "monkey2", AdminLogType.ORG_CREATED);

        log = logUtils.createLog("csc", AdminLogType.ORG_CREATED, json.toString());
        assertEquals("testadmin", log.getAdmin());
        assertEquals(json.toString(), log.getChanged());
        assertEquals("csc", log.getTarget());
        assertEquals(AdminLogType.ORG_CREATED, log.getType());
    }

    @Test
    public void createLogWithAuthButNoValues() {
        List<GrantedAuthority> role = new LinkedList<GrantedAuthority>();
        role.add(new SimpleGrantedAuthority("ROLE_SUPERUSER"));
        Authentication auth = new PreAuthenticatedAuthenticationToken("testadmin", null, role);
        SecurityContextHolder.getContext().setAuthentication(auth);

        LogUtils logUtils = new LogUtils();

        log = logUtils.createLog("csc", AdminLogType.ORG_CREATED, null);

        assertEquals("testadmin", log.getAdmin());
        assertEquals("csc", log.getTarget());
        assertEquals(AdminLogType.ORG_CREATED, log.getType());
    }

    @Test
    public void getLogDetailsWithoutNull() {
        LogUtils logUtils = new LogUtils();
        JSONObject json = logUtils.getLogDetails("name", "monkey1", "monkey2", AdminLogType.ORG_CREATED);

        assertEquals("name", json.get("field"));
        assertEquals("monkey1", json.get("old"));
        assertEquals("monkey2", json.get("new"));
        assertEquals("An org was created", json.get("type"));
    }

    @Test
    public void getLogDetailsWithAllAsNull() {
        LogUtils logUtils = new LogUtils();
        JSONObject json = logUtils.getLogDetails(null, null, null, null);

        assertEquals("", json.get("field"));
        assertEquals("", json.get("old"));
        assertEquals("", json.get("new"));
        assertEquals("", json.get("type"));
    }

}
