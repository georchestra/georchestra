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
package org.georchestra.config.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.georchestra.datafeeder.api.AuthorizationService;
import org.georchestra.datafeeder.api.DataFeederApiConfiguration;
import org.georchestra.datafeeder.service.DataPublishingService;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.service.DatasetsService;
import org.georchestra.datafeeder.service.FileStorageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test suite for {@link GeorchestraSecurityProxyAuthenticationConfiguration},
 * which shall be enabled through the {@code georchestra} spring profile
 * 
 * @see SecurityTestController
 */
@SpringBootTest(classes = DataFeederApiConfiguration.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "test" })
public class GeorchestraSecurityProxyAuthenticationConfigurationTest {

    private @MockBean DataUploadService dataUploadService;
    private @MockBean FileStorageService fileStorageService;
    private @MockBean AuthorizationService mockDataUploadValidityService;
    private @MockBean DataPublishingService mockDataPublishingService;
    private @MockBean DatasetsService mockDatasetsService;

    private @LocalServerPort int port;
    private @Value("${server.context-path}") String contextPath;

    private @Autowired TestRestTemplate template;

    private String baseURI;

    private HttpHeaders requestHeaders;

    private ResponseEntity<GeorchestraUserDetails> response;

    public @Before void before() {
        baseURI = "http://localhost:" + port + contextPath + "/test/security/georchestra";

        requestHeaders = new HttpHeaders();
        requestHeaders.set("sec-proxy", "true");
        requestHeaders.set("sec-username", "testUserName");
        requestHeaders.set("sec-email", "test@email.com");
        requestHeaders.set("sec-firstname", "Test");
        requestHeaders.set("sec-lastname", "Lastnametest");
        requestHeaders.set("sec-org", "test-org");
        requestHeaders.set("sec-orgname", "Test Organization");
    }

    public ResponseEntity<GeorchestraUserDetails> doGet(String relaitvePath) {
        HttpHeaders headers = this.requestHeaders;
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        String uri = baseURI + relaitvePath;
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        return template.exchange(uri, HttpMethod.GET, requestEntity, GeorchestraUserDetails.class);
    }

    public @Test void requestNotProxied() {
        requestHeaders.clear();
        response = doGet("/anonymous");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    public @Test void testAnonymous() {
        requestHeaders.remove("sec-username");
        response = doGet("/anonymous");
        assertEquals(HttpStatus.OK, response.getStatusCode());

        GeorchestraUserDetails authenticatedAs = response.getBody();
        assertNotNull(authenticatedAs);
        assertEquals("anonymousUser", authenticatedAs.getUsername());

        response = doGet("/user");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        response = doGet("/admin");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    public @Test void testUserRole() {
        requestHeaders.set("sec-roles", "ROLE_USER");

        response = doGet("/anonymous");
        assertEquals(HttpStatus.OK, response.getStatusCode());

        response = doGet("/user");
        assertEquals(HttpStatus.OK, response.getStatusCode());

        GeorchestraUserDetails authenticatedAs = response.getBody();
        assertNotNull(authenticatedAs);
        assertEquals("testUserName", authenticatedAs.getUsername());

        response = doGet("/admin");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

    }

    public @Test void testAdministrator() {
        requestHeaders.set("sec-roles", "ROLE_ADMINISTRATOR");

        response = doGet("/anonymous");
        assertEquals(HttpStatus.OK, response.getStatusCode());

        response = doGet("/user");
        assertEquals(HttpStatus.OK, response.getStatusCode());

        response = doGet("/admin");
        assertEquals(HttpStatus.OK, response.getStatusCode());

        GeorchestraUserDetails authenticatedAs = response.getBody();
        assertNotNull(authenticatedAs);
        assertEquals("testUserName", authenticatedAs.getUsername());
    }

}
