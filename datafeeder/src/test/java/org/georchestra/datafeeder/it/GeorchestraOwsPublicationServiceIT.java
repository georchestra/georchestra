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

import static org.georchestra.datafeeder.it.IntegrationTestSupport.EXPECTED_WORKSPACE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.georchestra.datafeeder.app.DataFeederApplicationConfiguration;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.CoordinateReferenceSystemMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.service.publish.impl.GeorchestraOwsPublicationService;
import org.geoserver.openapi.v1.model.NamedLink;
import org.geoserver.openapi.v1.model.WorkspaceSummary;
import org.geoserver.restconfig.client.DataStoresClient;
import org.geoserver.restconfig.client.FeatureTypesClient;
import org.geoserver.restconfig.client.GeoServerClient;
import org.geoserver.restconfig.client.LayersClient;
import org.geoserver.restconfig.client.ServerException;
import org.geoserver.restconfig.client.WorkspacesClient;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = { //
        DataFeederApplicationConfiguration.class, //
        IntegrationTestSupport.class }, //
        webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "it" })
@Slf4j
public class GeorchestraOwsPublicationServiceIT {

    public @Autowired @Rule IntegrationTestSupport support;

    private @Autowired GeoServerClient client;
    private @Autowired GeorchestraOwsPublicationService service;
    private @Autowired DataFeederConfigurationProperties configProperties;

    private DatasetUploadState shpDataset;

    private static final String NATIVE_LAYERNAME = "public_layer";
    private static final String PULISHED_LAYERNAME = "PublicLayer";

    public @Before void setup() {
        client.setDebugRequests(true);
        deleteWorkspace(EXPECTED_WORKSPACE);
        shpDataset = buildShapefileDatasetFromDefaultGeorchestraDataDirectory();

        // replace the configured geoserver datastore connection parameters by a
        // "directory of shapefiles" set of parameters
        Map<String, String> params = configProperties.getPublishing().getBackend().getGeoserver();
        params.clear();
        params.put(ShapefileDataStoreFactory.FILE_TYPE.key, "shapefile");
        params.put(ShapefileDataStoreFactory.URLP.key, "file:data/automated_tests");
    }

    private void deleteWorkspace(@NonNull String workspaceName) {
        try {
            // clean up here instead of at @After in case some failing test remainings need
            // to be diagnosed in geoserver
            WorkspacesClient workspaces = client.workspaces();
            Optional<WorkspaceSummary> workspace = workspaces.findByName(workspaceName);
            if (workspace.isPresent()) {
                workspaces.deleteRecursively(workspaceName);
                assertFalse(workspaces.findByName(workspaceName).isPresent());
            }
        } catch (ServerException.NotFound ok) {
            // doesn't matter
        } catch (RuntimeException e) {
            log.warn("Error trying to delete workspace {}", workspaceName, e);
            throw e;
        }
    }

    /**
     * Expects GeoServer instance to be configured with the default geOrchestra data
     * directory, having {@code file:data/automated_tests/public_layer.shp}
     */
    private DatasetUploadState buildShapefileDatasetFromDefaultGeorchestraDataDirectory() {
        DataUploadJob job = new DataUploadJob();

        DatasetUploadState dset = new DatasetUploadState();
        dset.setJob(job);
        dset.setName(NATIVE_LAYERNAME);

        dset.setNativeBounds(new BoundingBoxMetadata());
        dset.getNativeBounds().setCrs(new CoordinateReferenceSystemMetadata());
        dset.getNativeBounds().getCrs().setSrs("EPSG:4326");
        dset.getNativeBounds().setMinx(-86d);
        dset.getNativeBounds().setMaxx(77d);
        dset.getNativeBounds().setMiny(-17d);
        dset.getNativeBounds().setMaxy(51d);

        PublishSettings publishing = new PublishSettings();
        dset.setPublishing(publishing);
        publishing.setPublishedName(PULISHED_LAYERNAME);
        publishing.setKeywords(Arrays.asList("tag1", "tag 2"));
        publishing.setSrs("EPSG:4326");
        return dset;
    }

    @Test
    public void testPublishSingleShapefile() {
        WorkspacesClient workspaces = client.workspaces();
        DataStoresClient dataStores = client.dataStores();
        FeatureTypesClient featureTypes = client.featureTypes();
        LayersClient layers = client.layers();
        final String hardCodedStoreName = "datafeeder";

        assertFalse(workspaces.findByName(EXPECTED_WORKSPACE).isPresent());

        assertNull(shpDataset.getPublishing().getPublishedWorkspace());
        service.publish(shpDataset, support.user());
        assertEquals(EXPECTED_WORKSPACE, shpDataset.getPublishing().getPublishedWorkspace());

        assertTrue(workspaces.findByName(EXPECTED_WORKSPACE).isPresent());
        assertTrue(dataStores.findByWorkspaceAndName(EXPECTED_WORKSPACE, hardCodedStoreName).isPresent());
        List<NamedLink> findFeatureTypes = featureTypes.findFeatureTypes(EXPECTED_WORKSPACE, hardCodedStoreName);
        // System.err.println(findFeatureTypes);
        assertTrue(featureTypes.getFeatureType(EXPECTED_WORKSPACE, hardCodedStoreName, PULISHED_LAYERNAME).isPresent());
        assertTrue(layers.getLayer(EXPECTED_WORKSPACE, PULISHED_LAYERNAME).isPresent());
    }

}
