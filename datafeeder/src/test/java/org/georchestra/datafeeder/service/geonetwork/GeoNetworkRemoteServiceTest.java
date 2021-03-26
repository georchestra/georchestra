/*
 * Copyright (C) 2020, 2021 by the geOrchestra PSC
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
package org.georchestra.datafeeder.service.geonetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URL;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.ExternalApiConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class GeoNetworkRemoteServiceTest {

    private GeoNetworkRemoteService service;

    private URL apiURL;
    private URL publicURL;
    private GeoNetworkClient mockClient;

    public @Before void before() throws Exception {
        apiURL = new URL("http://geonetwork:8080/geonetwork");
        publicURL = new URL("https://test.georchestra.mydomain.org/geonetwork");

        ExternalApiConfiguration config = new ExternalApiConfiguration();
        config.setApiUrl(apiURL);
        config.setPublicUrl(publicURL);

        mockClient = mock(GeoNetworkClient.class);

        service = spy(new GeoNetworkRemoteService(config, mockClient));
    }

    @Test(expected = NullPointerException.class)
    public void buildMetadataRecordURI_NullId() {
        service.buildMetadataRecordURI(null);
    }

    @Test
    public void buildMetadataRecordURI() {
        URI uri = service.buildMetadataRecordURI("testid");
        assertNotNull(uri);
        assertEquals("https://test.georchestra.mydomain.org/geonetwork?uuid=testid", uri.toString());
    }

    @Test
    public void publish_OK() {
        final String id = "633f2882-2a90-4f98-9739-472a72d31b64";
        final String record = String.format(//
                "<gmd:MD_Metadata>\n"//
                        + "  <gmd:fileIdentifier>\n"//
                        + "      <gco:CharacterString>%s</gco:CharacterString>\n"//
                        + "  </gmd:fileIdentifier\n"//
                        + "<gmd:MD_Metadata>\n",
                id);//

        String expectedURL = "http://geonetwork:8080/geonetwork";
        GeoNetworkResponse response = new GeoNetworkResponse();
        response.setStatus(HttpStatus.CREATED);

        when(mockClient.putXmlRecord(eq(expectedURL), any(HttpHeaders.class), eq(id), eq(record))).thenReturn(response);

        GeoNetworkResponse ret = service.publish(id, () -> record);
        assertSame(response, ret);
    }

}
