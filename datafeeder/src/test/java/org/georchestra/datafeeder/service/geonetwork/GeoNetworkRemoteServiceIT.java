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

import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;

import org.fao.geonet.client.RecordsApi;
import org.fao.geonet.client.model.GroupPrivilege;
import org.fao.geonet.client.model.SharingResponse;
import org.georchestra.datafeeder.autoconf.GeorchestraIntegrationAutoConfiguration;
import org.georchestra.datafeeder.it.IntegrationTestSupport;
import org.georchestra.datafeeder.model.Organization;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.service.DataFeederServiceConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.InputSource;

import com.google.common.io.CharStreams;

@SpringBootTest(//
        classes = { //
                GeorchestraIntegrationAutoConfiguration.class, //
                DataFeederServiceConfiguration.class, //
                IntegrationTestSupport.class }, //
        webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "it" })
public class GeoNetworkRemoteServiceIT {

    private @Autowired GeoNetworkRemoteService service;

    private @Autowired DefaultGeoNetworkClient client;

    @Test
    public void buildMetadataRecordURI() {
        URI recordUri = service.buildMetadataRecordIdentifier("someid");
        assertEquals("https://georchestra.mydomain.org/geonetwork?uuid=someid", recordUri.toString());
    }

    @Test
    public void publish_OK() throws IOException {
        final String id = UUID.randomUUID().toString();
        final String record = loadSampleRecord(id);

        final UserInfo user = new UserInfo();
        user.setEmail("psc@georchestra.org");
        user.setTitle("testadmin");
        user.setUsername("testadmin");
        user.setRoles(Arrays.asList("ROLE_ADMINISTRATOR", "ROLE_GN_ADMIN", "ROLE_IMPORT"));
        user.setFirstName("admin");
        user.setLastName("test");

        // we don't have gn groups in the it compose?
        String group = null;
        GeoNetworkResponse response = service.publish(id, () -> record, group, user, true);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        final String publishedRecord = service.getRecordById(id);
        assertNotNull(publishedRecord);
        try {
            DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(publishedRecord)));
        } catch (Exception e) {
            fail("Published record not returned as valid XML", e);
        }

        RecordsApi api = new RecordsApi(client.newApiClient());
        SharingResponse resp = api.getRecordSharingSettings(id);

        GroupPrivilege privPubGroupAll = resp.getPrivileges().stream().filter(grp -> grp.getGroup() == 1).findAny()
                .get();

        // A metadata is published if the privilege "view" is set to true on the
        // hardcoded "All" group
        assertEquals(privPubGroupAll.getOperations().get("view"), true);
    }

    @Test
    public void publish_OK_skip_publication() throws IOException {
        final String id = UUID.randomUUID().toString();
        final String record = loadSampleRecord(id);

        final UserInfo user = new UserInfo();
        user.setEmail("psc@georchestra.org");
        user.setTitle("testadmin");
        user.setUsername("testadmin");
        user.setRoles(Arrays.asList("ROLE_ADMINISTRATOR", "ROLE_GN_ADMIN", "ROLE_IMPORT"));
        user.setFirstName("admin");
        user.setLastName("test");
        String group = "PSC";
        GeoNetworkResponse response = service.publish(id, () -> record, group, user, false);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        final String publishedRecord = service.getRecordById(id);
        assertNotNull(publishedRecord);
        try {
            DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(publishedRecord)));
        } catch (Exception e) {
            fail("Published record not returned as valid XML", e);
        }

        RecordsApi api = new RecordsApi(client.newApiClient());
        SharingResponse resp = api.getRecordSharingSettings(id);

        GroupPrivilege privPubGroupAll = resp.getPrivileges().stream().filter(grp -> grp.getGroup() == 1).findAny()
                .get();

        assertEquals(privPubGroupAll.getOperations().get("view"), false);
    }

    @Test
    public void publish_OK_with_group() throws IOException {
        final String id = UUID.randomUUID().toString();
        final String record = loadSampleRecord(id);

        final UserInfo user = new UserInfo();
        user.setEmail("psc@georchestra.org");
        user.setTitle("testadmin");
        user.setUsername("testadmin");
        user.setRoles(Arrays.asList("ROLE_ADMINISTRATOR", "ROLE_GN_ADMIN", "ROLE_IMPORT"));
        user.setFirstName("admin");
        user.setLastName("test");

        String group = "PSC";
        GeoNetworkResponse response = service.publish(id, () -> record, group, user, true);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatus());

        final String publishedRecord = service.getRecordById(id);
        assertNotNull(publishedRecord);
        try {
            DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(publishedRecord)));
        } catch (Exception e) {
            fail("Published record not returned as valid XML", e);
        }
    }

    private String loadSampleRecord(final String id) throws IOException {
        final String template;
        try (InputStream in = getClass().getResourceAsStream("/sample_record.xml")) {
            template = CharStreams.toString(new InputStreamReader(in));
        }
        final String record = template.replaceAll("\\$\\{recordId\\}", id);
        return record;
    }

}
