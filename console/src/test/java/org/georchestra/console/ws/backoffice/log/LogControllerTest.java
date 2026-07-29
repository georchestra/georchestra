/*
 * Copyright (C) 2009-2026 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.console.ws.backoffice.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.georchestra.console.dao.AdminLogDao;
import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.model.AdminLogEntry;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.model.DelegationEntry;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Regression tests for https://github.com/georchestra/georchestra/issues/4689
 *
 * A delegated (non-superuser) admin must only see admin log entries they
 * authored themselves, or role-change entries for roles they are delegated on.
 * Other admins' attribute/org changes, and role changes outside of the
 * delegation, must stay hidden even if the target user is in the delegated org.
 */
public class LogControllerTest {

    private LogController logCtrl;

    private AdminLogDao logDao;
    private DelegationDao delegationDao;
    private OrgsDao orgsDao;
    private AdvancedDelegationDao advancedDelegationDao;

    private static final String DELEGATE = "delegate1";
    private static final String OTHER_ADMIN = "otheradmin";
    private static final String TARGET = "target1";

    @BeforeEach
    public void setUp() {
        logDao = mock(AdminLogDao.class);
        delegationDao = mock(DelegationDao.class);
        orgsDao = mock(OrgsDao.class);
        advancedDelegationDao = mock(AdvancedDelegationDao.class);

        logCtrl = new LogController();
        ReflectionTestUtils.setField(logCtrl, "logDao", logDao);
        ReflectionTestUtils.setField(logCtrl, "delegationDao", delegationDao);
        ReflectionTestUtils.setField(logCtrl, "orgsDao", orgsDao);
        ReflectionTestUtils.setField(logCtrl, "advancedDelegationDao", advancedDelegationDao);

        DelegationEntry delegation = new DelegationEntry();
        delegation.setUid(DELEGATE);
        delegation.setOrgs(new String[] { "myorg" });
        delegation.setRoles(new String[] { "BAR", "BAZ" });
        when(delegationDao.findFirstByUid(eq(DELEGATE))).thenReturn(delegation);

        Org org = new Org();
        org.setShortName("myorg");
        org.setMembers(new LinkedList<>(List.of(TARGET)));
        when(orgsDao.findByCommonName(eq("myorg"))).thenReturn(org);

        Set<String> usersUnderDelegation = new HashSet<>();
        usersUnderDelegation.add(TARGET);
        when(advancedDelegationDao.findUsersUnderDelegation(eq(DELEGATE))).thenReturn(usersUnderDelegation);

        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        List<GrantedAuthority> authorities = new LinkedList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new PreAuthenticatedAuthenticationToken(DELEGATE, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private AdminLogEntry entry(String admin, AdminLogType type, String role) {
        String changed = new JSONObject().put("field", role).toString();
        return new AdminLogEntry(admin, TARGET, type, new Date(), changed);
    }

    private List<AdminLogEntry> sampleEntries() {
        List<AdminLogEntry> entries = new LinkedList<>();
        // authored by the delegate themselves: always visible
        entries.add(entry(DELEGATE, AdminLogType.USER_ATTRIBUTE_CHANGED, null));
        // role change on a role the delegate is delegated on: visible
        entries.add(entry(OTHER_ADMIN, AdminLogType.CUSTOM_ROLE_ADDED, "BAR"));
        // role change on a role NOT delegated to this admin: must stay hidden
        entries.add(entry(OTHER_ADMIN, AdminLogType.CUSTOM_ROLE_REMOVED, "QUUX"));
        // attribute change (e.g. org change) made by another admin: must stay hidden
        entries.add(entry(OTHER_ADMIN, AdminLogType.USER_ATTRIBUTE_CHANGED, null));
        return entries;
    }

    @Test
    public void homepageWidgetHidesLogsOutsideDelegation() {
        when(logDao.myFindByTargets(anySet(), any(Pageable.class))).thenReturn(sampleEntries());

        List<AdminLogEntry> result = logCtrl.find(new MockHttpServletRequest(), 500, 0);

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .allMatch(e -> e.getAdmin().equals(DELEGATE) || (e.getType() == AdminLogType.CUSTOM_ROLE_ADDED
                        && new JSONObject(e.getChanged()).getString("field").equals("BAR"))));
    }

    @Test
    public void perTargetPageHidesLogsOutsideDelegation() {
        when(logDao.findByTarget(eq(TARGET), any(Pageable.class))).thenReturn(sampleEntries());

        List<AdminLogEntry> result = logCtrl.find(TARGET, 500, 0);

        assertEquals(2, result.size());
    }
}
