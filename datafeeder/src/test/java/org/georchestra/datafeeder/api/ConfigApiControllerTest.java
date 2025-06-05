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

package org.georchestra.datafeeder.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.service.DatasetsService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;

@SpringBootTest(classes = { ConfigApiController.class }, webEnvironment = WebEnvironment.MOCK)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "test", "mock" })
public class ConfigApiControllerTest {

    private @MockBean DatasetsService mockDatasetsService;
    private @Autowired ConfigApiController controller;
    private @Autowired DataFeederConfigurationProperties config;
    private @Value("${georchestra.datadir}") String datadir;

    public @After void reset() {
        String expected = String.format("file:%s/datafeeder/frontend-config.json", datadir);
        config.setFrontEndConfigFile(URI.create(expected));
    }

    public @Test void verifyConfiguration() {
        URI frontEndConfigFile = config.getFrontEndConfigFile();
        assertNotNull(frontEndConfigFile);

        String expected = String.format("file:%s/datafeeder/frontend-config.json", datadir);
        assertEquals(URI.create(expected), frontEndConfigFile);
    }

    public @Test void getFrontendConfig() {
        ResponseEntity<Object> frontendConfig = controller.getFrontendConfig();
        assertEquals(HttpStatus.OK, frontendConfig.getStatusCode());
        Object body = frontendConfig.getBody();
        assertNotNull(body);
        assertThat(body, instanceOf(JsonNode.class));
        assertEquals("5MB", ((JsonNode) body).get("maxFileUploadSize").asText());
    }

}
