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

package org.georchestra.datafeeder.service.publish.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import org.fao.geonet.client.ApiException;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.GeonetworkPublishingConfiguration;
import org.georchestra.datafeeder.service.geonetwork.GeoNetworkRemoteService;
import org.junit.Before;
import org.junit.Test;

public class GeorchestraTemplateMapperTest {

    private GeorchestraTemplateMapper mapper;
    private GeoNetworkRemoteService mockService;
    private GeonetworkPublishingConfiguration config;

    private static final String VALID_RECORD_ID = "mock-id";
    private static final String MOCK_RECORD = "<MD_Metadata/>";

    public @Before void setup() {
        mockService = mock(GeoNetworkRemoteService.class);
        when(mockService.getRecordById(any(String.class))).thenThrow(ApiException.class);

        mapper = new GeorchestraTemplateMapper();
        mapper.setGeonetwork(mockService);

        DataFeederConfigurationProperties props = new DataFeederConfigurationProperties();
        mapper.setConfig(props);
        this.config = props.getPublishing().getGeonetwork();
        assertNotNull(config);
    }

    @Test
    public void loadTransform_URI_not_provided_returns_default() {
        config.setTemplateTransform(null);
        URI xslContents = mapper.resolveTransformURI();
        URI expected = mapper.getDefaultTransformURI();

        assertEquals(expected, xslContents);
    }

    @Test
    public void loadTransform_URI_provided() throws URISyntaxException {
        URI uri = getClass().getResource("just_id_transform.xsl").toURI();
        config.setTemplateTransform(uri);

        URI defaultXSL = mapper.getDefaultTransformURI();
        URI actualXSL = mapper.resolveTransformURI();
        assertNotNull(actualXSL);
        assertNotEquals(defaultXSL, actualXSL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadTemplateRecord_badRecordId() {
        config.setTemplateRecord(null);
        config.setTemplateRecordId("non-existent-id");

        mapper.loadTemplateRecord();
    }

    @Test
    public void loadTemplateRecord_provided_recordId() {
        config.setTemplateRecord(null);
        config.setTemplateRecordId(VALID_RECORD_ID);

        when(mockService.getRecordById(eq(VALID_RECORD_ID))).thenReturn(MOCK_RECORD);

        String record = mapper.loadTemplateRecord();
        assertEquals(MOCK_RECORD, record);
    }

    @Test
    public void loadTemplateRecord_provided_recordURI() throws URISyntaxException {
        config.setTemplateRecordId(null);
        config.setTemplateRecord(getClass().getResource("just_id_template.xml").toURI());

        String defaultTemplateRecord = mapper.loadDefaultTemplateRecord();
        String loaded = mapper.loadTemplateRecord();
        assertNotNull(loaded);
        assertNotEquals(defaultTemplateRecord, loaded);
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadTemplateRecord_provided_recordURI_invalid() {
        config.setTemplateRecordId(null);
        URI invalidURI = URI.create("file:non-existent-record.xml");
        config.setTemplateRecord(invalidURI);

        mapper.loadTemplateRecord();
    }

    @Test
    public void loadTemplateRecord_id_or_URI_not_provided_returns_default() throws URISyntaxException {
        config.setTemplateRecordId(null);
        config.setTemplateRecord(null);

        String defaultTemplateRecord = mapper.loadDefaultTemplateRecord();
        String loaded = mapper.loadTemplateRecord();
        assertEquals(defaultTemplateRecord, loaded);
    }
}
