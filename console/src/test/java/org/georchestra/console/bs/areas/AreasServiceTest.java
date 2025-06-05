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

package org.georchestra.console.bs.areas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.ds.users.Account;
import org.geotools.data.geojson.store.GeoJSONDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.FeatureIteratorIterator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;

import com.google.common.collect.Streams;

import lombok.NonNull;

public class AreasServiceTest {

    private AreasService service;
    private @NonNull GeorchestraConfiguration georConfig;

    private Account accountMock;
    private OrgsDao orgsDaoMock;
    private Org orgMock;

    private static Map<String, Geometry> geomsByInseeComId = new HashMap<>();

    public static @BeforeClass void loadTestData() throws IOException {
        SimpleFeatureSource featureSource = new GeoJSONDataStore(AreasServiceTest.class.getResource("cities.geojson"))
                .getFeatureSource();

        try (SimpleFeatureIterator it = featureSource.getFeatures().features()) {
            geomsByInseeComId = Streams.stream(new FeatureIteratorIterator<SimpleFeature>(it)).collect(Collectors
                    .toMap(f -> (String) f.getAttribute("INSEE_COM"), f -> (Geometry) f.getDefaultGeometry()));
        }
        assertEquals(3, geomsByInseeComId.size());
        assertEquals(Set.of("2B298", "2A322", "2B277"), geomsByInseeComId.keySet());
    }

    public @Before void beforeEach() {
        georConfig = new GeorchestraConfiguration("console");
        accountMock = mock(Account.class);
        orgMock = mock(Org.class);
        orgsDaoMock = mock(OrgsDao.class);
        when(orgsDaoMock.findByUser(same(accountMock))).thenReturn(orgMock);
        service = new AreasService(orgsDaoMock, georConfig, "cities.geojson");
    }

    @Test
    public void getAreaOfCompetence_Org_cities_null() throws IOException {
        when(orgMock.getCities()).thenReturn(null);
        assertNull(service.getAreaOfCompetence(accountMock));
    }

    @Test
    public void getAreaOfCompetence_Org_cities_empty_returns_null() throws IOException {
        when(orgMock.getCities()).thenReturn(Collections.emptyList());
        assertNull(service.getAreaOfCompetence(accountMock));
    }

    @Test
    public void getAreaOfCompetence_absolute_url() throws IOException {
        service.setAreasURI(getClass().getResource("cities.geojson").toExternalForm());

        List<String> INSEE_COM_ids = List.of("2B298", "2B277");
        Geometry expected = geomsByInseeComId.get("2B298").buffer(0d).union(geomsByInseeComId.get("2B277").buffer(0d));
        testGetAreaOfCompetence(INSEE_COM_ids, expected);
    }

    @Test
    public void getAreaOfCompetence_absolute_no_ids_match() throws IOException {
        service.setAreasURI(getClass().getResource("cities.geojson").toExternalForm());

        List<String> INSEE_COM_ids = List.of("123", "456");// no ids match
        Geometry expectedEmptyPolygon = new GeometryFactory().createEmpty(2);
        testGetAreaOfCompetence(INSEE_COM_ids, expectedEmptyPolygon);
    }

    @Test
    public void getAreaOfCompetence_some_ids_match() throws IOException {
        service.setAreasURI(getClass().getResource("cities.geojson").toExternalForm());

        List<String> INSEE_COM_ids = List.of("123_no_match", "2B298", "2A322", "2B277", "456_no_match");
        Geometry expected = geomsByInseeComId.get("2B298").buffer(0d).union(geomsByInseeComId.get("2A322").buffer(0d))
                .union(geomsByInseeComId.get("2B277").buffer(0d));
        testGetAreaOfCompetence(INSEE_COM_ids, expected);
    }

    private void testGetAreaOfCompetence(List<String> inseeComIds, Geometry expected) throws IOException {
        when(orgMock.getCities()).thenReturn(inseeComIds);
        Geometry areaOfCompetence = service.getAreaOfCompetence(accountMock);
        assertNotNull(areaOfCompetence);

        assertEquals(expected, areaOfCompetence);
    }
}
