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
package org.georchestra.console.ws.backoffice.users;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.georchestra.console.bs.areas.AreasService;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.mock.web.MockHttpServletResponse;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;

public class AreaOfCompetenceControllerTest {

    private AreaOfCompetenceController controller;
    private @NonNull AccountDao accountsMock;
    private @NonNull AreasService areasMock;

    private MockHttpServletResponse response;
    private Account account;

    private static final ObjectMapper mapper = new ObjectMapper();

    public @BeforeClass static void beforeAll() {
        mapper.registerModule(new JtsModule());
    }

    public @Before void setup() throws Exception {
        accountsMock = mock(AccountDao.class);
        areasMock = mock(AreasService.class);
        controller = new AreaOfCompetenceController(accountsMock, areasMock);
        controller.setUsernameResolver(() -> "testuser");
        response = new MockHttpServletResponse();

        account = mock(Account.class);
        when(account.getUid()).thenReturn("testuser");
        when(accountsMock.findByUID(eq("testuser"))).thenReturn(account);
    }

    @Test
    public void testMultipolygon() throws Exception {
        Geometry expected = geom("MULTIPOLYGON(((0 0, 0 1, 1 1, 1 0, 0 0)))");

        testGetCurrentUserAreaOfCompetence(expected);
    }

    @Test
    public void testNull() throws Exception {
        Geometry expected = null;

        testGetCurrentUserAreaOfCompetence(expected);
    }

    @Test
    public void testEmptyCollection() throws Exception {
        testGetCurrentUserAreaOfCompetence(new GeometryFactory().createEmpty(-1));
    }

    @Test
    public void testEmptyPoint() throws Exception {
        testGetCurrentUserAreaOfCompetence(new GeometryFactory().createEmpty(01));
    }

    @Test
    public void testEmptyLine() throws Exception {
        testGetCurrentUserAreaOfCompetence(new GeometryFactory().createEmpty(1));
    }

    @Test
    public void testEmptyPolygon() throws Exception {
        testGetCurrentUserAreaOfCompetence(new GeometryFactory().createEmpty(2));
    }

    private void testGetCurrentUserAreaOfCompetence(Geometry expected) throws Exception {
        when(areasMock.getAreaOfCompetence(same(account))).thenReturn(expected);
        controller.getCurrentUserAreaOfCompetence(response);

        String geoJsonResponseBody = response.getContentAsString();
        Geometry response = mapper.readValue(geoJsonResponseBody, Geometry.class);
        assertEquals(expected, response);
    }

    private Geometry geom(String wkt) {
        try {
            return new WKTReader().read(wkt);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
