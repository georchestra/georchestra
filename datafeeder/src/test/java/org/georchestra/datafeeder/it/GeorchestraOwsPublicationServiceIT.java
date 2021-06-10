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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.georchestra.datafeeder.app.DataFeederApplicationConfiguration;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.GeonetworkPublishingConfiguration;
import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.CoordinateReferenceSystemMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.service.publish.impl.GeorchestraOwsPublicationService;
import org.geoserver.openapi.model.catalog.AttributionInfo;
import org.geoserver.openapi.model.catalog.FeatureTypeInfo;
import org.geoserver.openapi.model.catalog.MetadataEntry;
import org.geoserver.openapi.model.catalog.MetadataLinkInfo;
import org.geoserver.openapi.model.catalog.MetadataMap;
import org.geoserver.openapi.v1.model.Layer;
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

    private @Autowired GeoServerClient geoServerClient;
    private @Autowired GeorchestraOwsPublicationService service;
    private @Autowired DataFeederConfigurationProperties configProperties;

    private final String hardCodedStoreName = "datafeeder";

    private DatasetUploadState shpDataset;

    /**
     * Native layer name for the uploaded file e.g. as resolved from the shapefile
     * file name
     */
    private static final String NATIVE_LAYERNAME = "Public Layer";
    /**
     * Layer name as imported into the target store (e.g., lower-cased, replaced
     * space by underscore
     */
    private static final String IMPORTED_LAYERNAME = "public_layer";
    /**
     * Layer name as published to geoserver (e.g. overrides the IMPORTED_LAYERNAME)
     */
    private static final String PULISHED_LAYERNAME = "PublicLayer";

    public @Before void setup() {
        geoServerClient.setDebugRequests(true);
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
            WorkspacesClient workspaces = geoServerClient.workspaces();
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
        publishing.setImportedName(IMPORTED_LAYERNAME);
        publishing.setKeywords(Arrays.asList("tag1", "tag 2"));
        publishing.setSrs("EPSG:4326");
        return dset;
    }

    @Test
    public void testPublishSingleShapefile() {
        WorkspacesClient workspaces = geoServerClient.workspaces();
        DataStoresClient dataStores = geoServerClient.dataStores();
        FeatureTypesClient featureTypes = geoServerClient.featureTypes();
        LayersClient layers = geoServerClient.layers();

        assertFalse(workspaces.findByName(EXPECTED_WORKSPACE).isPresent());

        assertNull(shpDataset.getPublishing().getPublishedWorkspace());
        service.publish(shpDataset, support.user());
        assertEquals(EXPECTED_WORKSPACE, shpDataset.getPublishing().getPublishedWorkspace());

        assertTrue(workspaces.findByName(EXPECTED_WORKSPACE).isPresent());
        assertTrue(dataStores.findByWorkspaceAndName(EXPECTED_WORKSPACE, hardCodedStoreName).isPresent());
        Optional<FeatureTypeInfo> featureType = featureTypes.getFeatureType(EXPECTED_WORKSPACE, hardCodedStoreName,
                PULISHED_LAYERNAME);
        assertTrue(featureType.isPresent());
        assertTrue(layers.getLayer(EXPECTED_WORKSPACE, PULISHED_LAYERNAME).isPresent());
        assertEquals(IMPORTED_LAYERNAME, featureType.get().getNativeName());
    }

    @Test
    public void testPublish_sets_layer_attribution_to_orgname_and_url() {
        final UserInfo user = support.user();
        service.publish(shpDataset, user);

        Layer layer = geoServerClient.layers().getLayer(EXPECTED_WORKSPACE, PULISHED_LAYERNAME)
                .orElseThrow(NoSuchElementException::new);
        AttributionInfo attribution = layer.getAttribution();
        assertNotNull(attribution);

        assertEquals(user.getOrganization().getName(), attribution.getTitle());
        assertEquals(user.getOrganization().getLinkage(), attribution.getHref());
    }

    @Test
    public void testPublish_sets_cache_settings() {
        final UserInfo user = support.user();
        service.publish(shpDataset, user);

        FeatureTypesClient featureTypes = geoServerClient.featureTypes();
        FeatureTypeInfo featureType = featureTypes
                .getFeatureType(EXPECTED_WORKSPACE, hardCodedStoreName, PULISHED_LAYERNAME)
                .orElseThrow(NoSuchElementException::new);

        MetadataMap metadataMap = featureType.getMetadata();
        assertNotNull(metadataMap);
        assertNotNull(metadataMap.getEntry());
        Map<String, String> values = metadataMap.getEntry().stream()
                .collect(Collectors.toMap(MetadataEntry::getAtKey, MetadataEntry::getValue));
        assertEquals("3600", values.get("cacheAgeMax"));
        assertEquals("true", values.get("cachingEnabled"));
    }

    @Test
    public void addMetadataLinks() {
        DatasetUploadState dataset = shpDataset;
        final UserInfo user = support.user();
        service.publish(dataset, user);

        // fake publishing of metadata record...
        final String metadataRecordId = UUID.randomUUID().toString();
        dataset.getPublishing().setMetadataRecordId(metadataRecordId);

        // test addMetadataLinks...
        service.addMetadataLinks(dataset);

        GeonetworkPublishingConfiguration gnConfig = this.configProperties.getPublishing().getGeonetwork();
        URL publicURL = gnConfig.getPublicUrl();
        final String xmlLink = String.format("%s/srv/api/records/%s/formatters/xml", publicURL, metadataRecordId);
        final String lang = "eng";
        final String htmlLink = String.format("%s/srv/%s/catalog.search#/metadata/%s", publicURL, lang,
                metadataRecordId);

        FeatureTypeInfo featureType = geoServerClient.featureTypes()
                .getFeatureType(EXPECTED_WORKSPACE, hardCodedStoreName, PULISHED_LAYERNAME)
                .orElseThrow(NoSuchElementException::new);

        assertNotNull(featureType.getMetadataLinks());
        List<MetadataLinkInfo> links = featureType.getMetadataLinks().getMetadataLink();
        assertNotNull(links);
        assertEquals(2, links.size());

        Map<String, MetadataLinkInfo> byType = links.stream()
                .collect(Collectors.toMap(MetadataLinkInfo::getType, Function.identity()));
        MetadataLinkInfo xml = byType.get("text/xml");
        MetadataLinkInfo html = byType.get("text/html");

        assertEquals("ISO19115:2003", xml.getMetadataType());
        assertEquals("ISO19115:2003", html.getMetadataType());
        assertNull(xml.getAbout());
        assertNull(html.getAbout());

        assertEquals(xmlLink, xml.getContent());
        assertEquals(htmlLink, html.getContent());
    }
}
