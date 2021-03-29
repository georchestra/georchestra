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
package org.georchestra.datafeeder.it;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.georchestra.datafeeder.app.DataFeederApplicationConfiguration;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.service.DatasetsService;
import org.georchestra.datafeeder.service.publish.impl.GeorchestraDataBackendService;
import org.georchestra.datafeeder.test.TestData;
import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = { //
        DataFeederApplicationConfiguration.class, //
        IntegrationTestSupport.class }, //
        webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "it" })
public class GeorchestraDataBackendServiceIT {

    private static final String ORGANIZATION_NAME = "Datafeeder Test Org";
    private static final String EXPECTED_SCHEMA_NAME = "datafeeder_test_org";

    public @Autowired @Rule IntegrationTestSupport support;
    public @Rule TestData testData = new TestData();

    public @Autowired GeorchestraDataBackendService service;
    public @Autowired DatasetsService datasetsService;

    private Connection testConnection;
    private DataUploadJob job;
    private DatasetUploadState dataset;
    private PublishSettings publishing;

    public @Before void before() throws SQLException {
        testConnection = support.createLocalPostgisConnection();
        support.deleteLocalDatabaseSchema(EXPECTED_SCHEMA_NAME);

        job = new DataUploadJob();
        job.getUser().setOrganization(ORGANIZATION_NAME);
        dataset = new DatasetUploadState();
        dataset.setJob(job);
        publishing = new PublishSettings();
        dataset.setPublishing(publishing);
    }

    public @After void after() throws SQLException {
        if (testConnection != null)
            testConnection.close();
    }

    public @Test void prepareBackend_Null_Org_Name() {
        job.getUser().setOrganization(null);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.prepareBackend(job));
        assertThat(ex.getMessage(), containsString("Georchestra organization name not provided"));
    }

    public @Test void prepareBackend_Creates_Database_Schema() throws SQLException {
        List<String> pre = support.getDatabaseSchemas();
        assertFalse(pre.toString(), pre.contains(EXPECTED_SCHEMA_NAME));

        service.prepareBackend(job);

        List<String> post = support.getDatabaseSchemas();
        assertTrue(post.toString(), post.contains(EXPECTED_SCHEMA_NAME));

        service.prepareBackend(job);
        post = support.getDatabaseSchemas();
        assertTrue(post.toString(), post.contains(EXPECTED_SCHEMA_NAME));
    }

    public @Test void importDataset_Null_PublishingSettings() {
        dataset.setName("somename");
        dataset.setPublishing(null);
        NullPointerException ex = assertThrows(NullPointerException.class, () -> service.importDataset(dataset));
        assertThat(ex.getMessage(), containsString("Dataset 'publishing' settings is null"));
    }

    public @Test void importDataset_AbsoluteFile_Preconditions() {
        dataset.setName("somename");
        dataset.setAbsolutePath(null);

        NullPointerException npe = assertThrows(NullPointerException.class, () -> service.importDataset(dataset));
        assertThat(npe.getMessage(), containsString("absolutePath not provided"));

        dataset.setAbsolutePath("/tmp/nonexistent/" + UUID.randomUUID());
        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                () -> service.importDataset(dataset));
        assertThat(iae.getMessage(), containsString("Dataset absolutePath is not a file"));
    }

    public @Test void importDataset_Null_Name() throws IOException {
        dataset.setAbsolutePath(testData.archSitesShapefile().toAbsolutePath().toString());
        dataset.setName(null);

        NullPointerException npe = assertThrows(NullPointerException.class, () -> service.importDataset(dataset));
        assertThat(npe.getMessage(), containsString("Dataset name is null"));
    }

    public @Test void importDataset_Invalid_NativeName() throws IOException {
        dataset.setAbsolutePath(testData.archSitesShapefile().toAbsolutePath().toString());
        dataset.setName("invalidName");

        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                () -> service.importDataset(dataset));
        assertThat(iae.getMessage(), containsString("Dataset name 'invalidName' does not exist"));
    }

    public @Test void importDataset_Shapefile() throws IOException {
        dataset.setAbsolutePath(testData.archSitesShapefile().toAbsolutePath().toString());
        dataset.setName("archsites");

        service.prepareBackend(job);

        service.importDataset(dataset);

        DataStore sourceds = datasetsService.resolveSourceDataStore(dataset);
        DataStore targetds = datasetsService.loadDataStore(service.resolveConnectionParams(job));
        String typeName = dataset.getName();
        try {
            SimpleFeatureSource orig = sourceds.getFeatureSource(typeName);
            SimpleFeatureSource imported = targetds.getFeatureSource(typeName);
            assertEquals(orig.getCount(Query.ALL), imported.getCount(Query.ALL));
        } finally {
            sourceds.dispose();
            targetds.dispose();
        }
    }
}
