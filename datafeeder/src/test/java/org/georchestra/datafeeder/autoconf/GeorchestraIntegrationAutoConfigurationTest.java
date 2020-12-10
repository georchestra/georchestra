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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.georchestra.datafeeder.api.DataFeederApiConfiguration;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.FileUploadConfig;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
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
@SpringBootTest(classes = DataFeederApiConfiguration.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "test" })
public class GeorchestraIntegrationAutoConfigurationTest {

    private @MockBean DataUploadService dataUploadService;
    private @MockBean FileStorageService fileStorageService;

    private @Autowired ApplicationContext context;

    public @Test void testDefaultDotPropertiesFromGeorchestraDatadir() {
        Environment env = context.getEnvironment();
        assertEquals("is default.properties loaded from src/test/resources/datadir?", "https",
                env.getProperty("scheme"));
        assertEquals("is default.properties loaded from src/test/resources/datadir?", "georchestra.test.org",
                env.getProperty("domainName"));
    }

    public @Test void testDataFeederPropertiesFromGeorchestraDatadir() {
        DataFeederConfigurationProperties props = context.getBean(DataFeederConfigurationProperties.class);
        FileUploadConfig fileUpload = props.getFileUpload();
        assertNotNull(fileUpload);

        final String msg = "is default.properties loaded from src/test/resources/datadir/datafeeder?";
        assertEquals(msg, "5MB", fileUpload.getMaxFileSize());
        assertEquals(msg, "10MB", fileUpload.getMaxRequestSize());
        assertEquals(msg, "1MB", fileUpload.getFileSizeThreshold());

        String expectedTmp = System.getProperty("java.io.tmpdir") + "/datafeeder/tmp";
        assertEquals(msg, expectedTmp, fileUpload.getTemporaryLocation());

        Path expectedPersistent = Paths.get(System.getProperty("java.io.tmpdir") + "/datafeeder/uploads");
        assertEquals(msg, expectedPersistent, fileUpload.getPersistentLocation());
    }
}
