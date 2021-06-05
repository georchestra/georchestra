/*
 * Copyright (C) 2020 by the geOrchestra PSC
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
package org.georchestra.datafeeder.autoconf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.georchestra.datafeeder.api.AuthorizationService;
import org.georchestra.datafeeder.api.DataFeederApiConfiguration;
import org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.FileUploadConfig;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.PublishingConfiguration;
import org.georchestra.datafeeder.service.DataPublishingService;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.service.DatasetsService;
import org.georchestra.datafeeder.service.FileStorageService;
import org.georchestra.datafeeder.service.publish.MetadataPublicationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test suite for {@link GeorchestraIntegrationAutoConfiguration}, which shall
 * be enabled through the {@code georchestra} spring profile
 */
@SpringBootTest(classes = DataFeederApiConfiguration.class, webEnvironment = WebEnvironment.MOCK)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "test" })
public class GeorchestraIntegrationAutoConfigurationTest {

    private @MockBean DataUploadService dataUploadService;
    private @MockBean FileStorageService fileStorageService;
    private @MockBean AuthorizationService mockDataUploadValidityService;
    private @MockBean DataPublishingService mockDataPublishingService;
    private @MockBean MetadataPublicationService mockMetadataPublicationService;
    private @MockBean DatasetsService mockDatasetsService;
    private @MockBean PublishJobProgressTracker mockPublishJobProgressTracker;

    private @Autowired ApplicationContext context;

    public @Test void testDefaultDotPropertiesFromGeorchestraDatadir() {
        Environment env = context.getEnvironment();
        assertEquals("is default.properties loaded from src/test/resources/datadir?", "https",
                env.getProperty("scheme"));
        assertEquals("is default.properties loaded from src/test/resources/datadir?", "georchestra.test.org",
                env.getProperty("domainName"));
    }

    public @Test void testDataFeederPropertiesFromGeorchestraDatadir() throws MalformedURLException {
        DataFeederConfigurationProperties props = context.getBean(DataFeederConfigurationProperties.class);
        FileUploadConfig fileUpload = props.getFileUpload();
        assertNotNull(fileUpload);

        final String msg = "is datafeeder.properties loaded from src/test/resources/datadir/datafeeder?";
        assertEquals(msg, "5MB", fileUpload.getMaxFileSize());
        assertEquals(msg, "10MB", fileUpload.getMaxRequestSize());
        assertEquals(msg, "1MB", fileUpload.getFileSizeThreshold());

        String expectedTmp = System.getProperty("java.io.tmpdir") + "/datafeeder/tmp";
        assertEquals(msg, expectedTmp, fileUpload.getTemporaryLocation());

        Path expectedPersistent = Paths.get(System.getProperty("java.io.tmpdir") + "/datafeeder/uploads");
        assertEquals(msg, expectedPersistent, fileUpload.getPersistentLocation());

        PublishingConfiguration publishing = props.getPublishing();
        assertNotNull(publishing);
        assertNotNull(publishing.getGeoserver());
        assertNotNull(publishing.getGeonetwork());
        assertNotNull(publishing.getBackend());

        assertEquals(new URL("http://localhost:8080/geoserver/rest"), publishing.getGeoserver().getApiUrl());
        assertEquals("properties loaded from src/test/resources/datadir/**?",
                new URL("https://georchestra.test.org/geoserver"), publishing.getGeoserver().getPublicUrl());
        assertEquals(new URL("http://localhost:8081/geonetwork"), publishing.getGeonetwork().getApiUrl());
        assertEquals("properties loaded from src/test/resources/datadir/**?",
                new URL("https://georchestra.test.org/geonetwork"), publishing.getGeonetwork().getPublicUrl());

        Map<String, String> local = publishing.getBackend().getLocal();
        assertEquals("postgis", local.get("dbtype"));
        assertEquals("localhost", local.get("host"));
        assertEquals("datafeeder", local.get("database"));

        Map<String, String> gsBackendTemplate = publishing.getBackend().getGeoserver();
        // just a random test property to assert props are loaded from
        // datafeeder.properties
        assertEquals("true", gsBackendTemplate.get("testFromDatafeederPropertiesFile"));

        assertEquals("postgis", gsBackendTemplate.get("dbtype"));
        assertEquals("false", gsBackendTemplate.get("Loose bbox"));
        assertEquals("true", gsBackendTemplate.get("Estimated extends"));
    }
}
