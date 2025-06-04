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

package org.georchestra.console.ws.backoffice.orgs;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.model.DelegationEntry;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.Validation;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class OrgsControllerTest {

    private DelegationEntry mockEntry1;
    private DelegationEntry mockEntry2;
    private Org mockOrg;
    private OrgsDao mockOrgsDao;
    private DelegationDao delegationDaoMock;
    private LogUtils mockLogUtils;

    @Before
    public void grantRight() {
        MockitoAnnotations.initMocks(this);
        // Set user connected through spring security
        List<GrantedAuthority> role = new LinkedList<GrantedAuthority>();
        role.add(new SimpleGrantedAuthority("ROLE_SUPERUSER"));
        Authentication auth = new PreAuthenticatedAuthenticationToken("testadmin", null, role);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void updateFromRequestTakePendingIntoAccount() throws JSONException, IOException {
        OrgsDao mockOrgsDao = mock(OrgsDao.class);
        OrgsController toTest = new OrgsController(mockOrgsDao);
        Org mockOrg = mock(Org.class);
        JSONObject jsonInput = new JSONObject();
        jsonInput.put("pending", true);

        toTest.updateFromRequest(mockOrg, jsonInput);

        verify(mockOrg).setPending(true);

        jsonInput = new JSONObject();
        jsonInput.put("pending", false);

        toTest.updateFromRequest(mockOrg, jsonInput);

        verify(mockOrg).setPending(false);
    }

    @Test
    public void deleteOrgDeleteDelegation() throws IOException, SQLException {
        OrgsController toTest = createToTest();
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getWriter()).thenReturn(mock(PrintWriter.class));

        toTest.deleteOrg("csc", mockResponse);

        verify(mockOrgsDao).delete(mockOrg);
        verify(mockEntry1).removeOrg("csc");
        verify(mockEntry2).removeOrg("csc");
        verify(delegationDaoMock).save(mockEntry1);
        verify(delegationDaoMock).save(mockEntry2);
    }

    @Test
    public void updateOrgUpateDelegation() throws IOException, SQLException {
        OrgsController toTest = createToTest();
        when(mockOrg.getId()).thenReturn("c2c42");
        when(mockOrgsDao.reGenerateId("c2c", "csc")).thenReturn("c2c42");
        when(mockEntry1.getOrgs()).thenReturn(new String[] { "momorg" });
        when(mockEntry2.getOrgs()).thenReturn(new String[] {});
        JSONObject reqUsr = new JSONObject().put("shortName", "c2c");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(reqUsr.toString().getBytes());

        // when(toTest.validation.validateOrgUnicity(mockOrgsDao,
        // reqUsr)).thenReturn(true);

        toTest.updateOrgInfos("csc", request);

        verify(mockOrgsDao).update(mockOrg);
        verify(mockEntry1).removeOrg("csc");
        verify(mockEntry1).setOrgs(aryEq(new String[] { "momorg", "c2c42" }));
        verify(mockEntry2).removeOrg("csc");
        verify(mockEntry2).setOrgs(aryEq(new String[] { "c2c42" }));
        verify(delegationDaoMock).save(mockEntry1);
        verify(delegationDaoMock).save(mockEntry2);
    }

    private OrgsController createToTest() throws SQLException {
        mockOrgsDao = mock(OrgsDao.class);
        delegationDaoMock = mock(DelegationDao.class);
        mockLogUtils = mock(LogUtils.class);
        mockOrg = mock(Org.class);
        AdvancedDelegationDao advancedDelegationDaoMock = mock(AdvancedDelegationDao.class);
        Validation mockValidation = mock(Validation.class);
        when(mockValidation.validateOrgField(anyString(), any(JSONObject.class))).thenReturn(true);
        when(mockValidation.validateUrl(anyString())).thenReturn(true);
        JSONObject mockChanges = new JSONObject();
        when(mockValidation.validateOrgUnicity(mockOrgsDao, mockChanges)).thenReturn(true);
        when(mockValidation.validateOrgUnicity(eq(mockOrgsDao), any(JSONObject.class))).thenReturn(true);
        OrgsController toTest = new OrgsController(mockOrgsDao);
        toTest.delegationDao = delegationDaoMock;
        toTest.advancedDelegationDao = advancedDelegationDaoMock;
        toTest.validation = mockValidation;
        toTest.logUtils = mockLogUtils;
        when(mockOrgsDao.findByCommonName("csc")).thenReturn(mockOrg);
        mockEntry1 = mock(DelegationEntry.class);
        mockEntry2 = mock(DelegationEntry.class);
        Mockito.when(advancedDelegationDaoMock.findByOrg("csc"))
                .thenReturn(Arrays.asList(new DelegationEntry[] { mockEntry1, mockEntry2 }));
        return toTest;
    }
}
