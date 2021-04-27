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
package org.georchestra.datafeeder.service.publish.impl;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.georchestra.datafeeder.autoconf.GeorchestraNameNormalizer;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.BackendConfiguration;
import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.Envelope;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.service.geoserver.GeoServerRemoteService;
import org.georchestra.datafeeder.service.publish.MetadataPublicationService;
import org.georchestra.datafeeder.service.publish.OWSPublicationService;
import org.geoserver.openapi.model.catalog.DataStoreInfo;
import org.geoserver.openapi.model.catalog.EnvelopeInfo;
import org.geoserver.openapi.model.catalog.FeatureTypeInfo;
import org.geoserver.openapi.model.catalog.KeywordInfo;
import org.geoserver.openapi.model.catalog.MetadataLinkInfo;
import org.geoserver.openapi.model.catalog.NamespaceInfo;
import org.geoserver.openapi.model.catalog.ProjectionPolicy;
import org.geoserver.openapi.model.catalog.WorkspaceInfo;
import org.geoserver.openapi.v1.model.DataStoreResponse;
import org.geoserver.openapi.v1.model.Layer;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link OWSPublicationService} relying on {@link GeoServerRemoteService} to
 * implement geOrchestra specific business rules on how to interact with
 * GeoServer.
 * <p>
 * For instance:
 * <ul>
 * <li>Datasets are published to a workspace named after the geOrchestra short
 * organization name the calling user belongs to (
 * {@link DataUploadJob#getOrganizationName()}. The workspace is created if it
 * doesn't already exist.
 * <li>The GeoServer Datastore connection parameters are resolved from the
 * {@link BackendConfiguration#getGeoserver() geoserver} connection parameters
 * template provided by the externalized
 * {@link DataFeederConfigurationProperties} configuration's
 * {@code datafeeder.publishing.backend.geoserver.*} property map.
 * </ul>
 */
@Slf4j
public class GeorchestraOwsPublicationService implements OWSPublicationService {

    private @Autowired MetadataPublicationService metadataPublicationService;
    private @Autowired DataFeederConfigurationProperties configProperties;
    private @Autowired GeoServerRemoteService geoserver;
    private @Autowired GeorchestraNameNormalizer nameResolver;

    public @Override void publish(@NonNull DatasetUploadState dataset, @NonNull UserInfo user) {
        requireNonNull(dataset.getJob());
        requireNonNull(user.getOrganization(), "organization name not provided");
        requireNonNull(user.getOrganization().getId(), "organization name not provided");
        requireNonNull(dataset.getName(), "dataset native name not provided");

        PublishSettings publishing = dataset.getPublishing();
        requireNonNull(publishing);
        requireNonNull(publishing.getPublishedName());

        final String workspaceName = resolveWorkspace(user);
        final String dataStoreName = resolveDataStoreName(dataset);
        final String publishedLayerName = resolveUniqueLayerName(workspaceName, publishing.getPublishedName());

        Optional<DataStoreResponse> dataStore = geoserver.findDataStore(workspaceName, dataStoreName);
        if (!dataStore.isPresent()) {
            geoserver.create(buildDataStoreInfo(workspaceName, dataStoreName, user));
        }

        FeatureTypeInfo requestBody = buildPublishingFeatureType(workspaceName, dataStoreName, publishedLayerName,
                dataset);

        FeatureTypeInfo response = geoserver.create(requestBody);

        publishing.setPublishedWorkspace(workspaceName);
        publishing.setPublishedName(publishedLayerName);

        EnvelopeInfo latLonBoundingBox = response.getLatLonBoundingBox();
        if (latLonBoundingBox != null) {
            Envelope geographicBoundingBox = new Envelope();
            geographicBoundingBox.setMinx(latLonBoundingBox.getMinx());
            geographicBoundingBox.setMiny(latLonBoundingBox.getMiny());
            geographicBoundingBox.setMaxx(latLonBoundingBox.getMaxx());
            geographicBoundingBox.setMaxy(latLonBoundingBox.getMaxy());
            publishing.setGeographicBoundingBox(geographicBoundingBox);
        }
    }

    public @Override void addMetadataLink(@NonNull DatasetUploadState dataset) {
        PublishSettings publishing = dataset.getPublishing();

        requireNonNull(publishing);
        requireNonNull(publishing.getPublishedWorkspace());
        requireNonNull(publishing.getPublishedName());
        requireNonNull(publishing.getMetadataRecordId());

        final String workspace = publishing.getPublishedWorkspace();
        final String dataStore = resolveDataStoreName(dataset);
        final String layerName = publishing.getPublishedName();
        final String metadataRecordId = publishing.getMetadataRecordId();

        FeatureTypeInfo fti = geoserver.findFeatureTypeInfo(workspace, dataStore, layerName)
                .orElseThrow(() -> new IllegalArgumentException("FeatureType not found"));

        MetadataLinkInfo metadatalink = buildMetadataLink(metadataRecordId);

        FeatureTypeInfo toUpdate = new FeatureTypeInfo();
        toUpdate.setNativeName(fti.getNativeName());
        toUpdate.setName(fti.getName());
        toUpdate.setNamespace(fti.getNamespace());
        toUpdate.setStore(new DataStoreInfo());
        toUpdate.getStore().setName(fti.getStore().getName());
        toUpdate.getStore().setWorkspace(new WorkspaceInfo());
        toUpdate.getStore().getWorkspace().setName(fti.getStore().getWorkspace().getName());
        // toUpdate.addMetadatalinksItem(metadatalink);

        // TODO: revisit, giving an error.
        try {
            log.warn("Unable to add metadatalinks to geoserver feature type, its REST API doesn't yet work with JSON");
            // geoserver.update(toUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private @NonNull DataStoreInfo buildDataStoreInfo(@NonNull String workspaceName, @NonNull String dataStoreName,
            @NonNull UserInfo user) {

        DataStoreInfo ds = new DataStoreInfo();
        ds.connectionParameters(buildConnectionParameters(user));
        ds.setName(dataStoreName);
        ds.setEnabled(true);
        ds.setWorkspace(new WorkspaceInfo().name(workspaceName));
        ds.setDescription("Datafeeder uploaded datasets");
        return ds;
    }

    private Map<String, String> buildConnectionParameters(@NonNull UserInfo user) {
        Map<String, String> connectionParams = new HashMap<>(
                configProperties.getPublishing().getBackend().getGeoserver());

        String schema = nameResolver.resolveDatabaseSchemaName(user.getOrganization().getId());
        for (String k : connectionParams.keySet()) {
            String v = connectionParams.get(k);
            if ("<schema>".equals(v)) {
                connectionParams.put(k, schema);
            }
        }
        return new HashMap<>(connectionParams);
    }

    private String resolveDataStoreName(@NonNull DatasetUploadState dataset) {
        return "datafeeder";
    }

    private FeatureTypeInfo buildPublishingFeatureType(String workspace, String dataStore, String layerName,
            @NonNull DatasetUploadState dataset) {

        PublishSettings publishing = dataset.getPublishing();
        FeatureTypeInfo ft = new FeatureTypeInfo();

        ft.setNativeName(dataset.getName());
        ft.setName(layerName);
        ft.setNamespace(new NamespaceInfo().prefix(workspace));

        ft.setTitle(publishing.getTitle());
        // ft.setDescription(publishing.getAbstract());
        ft.setAbstract(publishing.getAbstract());
        ft.setAdvertised(true);
        ft.setEnabled(true);
        // ft.setKeywords(buildKeywords(publishing.getKeywords()));

        BoundingBoxMetadata nativeBounds = dataset.getNativeBounds();
        if (nativeBounds != null) {
            ft.setNativeBoundingBox(buildEnvelope(nativeBounds));
            ft.setNativeCRS(nativeBounds.getCrs().getSrs());
        }
        ft.setSrs(publishing.getSrs());
        if (Boolean.TRUE.equals(publishing.getSrsReproject())) {
            ft.setProjectionPolicy(ProjectionPolicy.REPROJECT_TO_DECLARED);
        } else {
            ft.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
        }

        ft.setStore(new DataStoreInfo().name(dataStore));
        publishing.getSrs();
        publishing.getSrsReproject();
        return ft;
    }

    private EnvelopeInfo buildEnvelope(BoundingBoxMetadata bounds) {
        if (bounds == null)
            return null;

        EnvelopeInfo env = new EnvelopeInfo();
        if (bounds.getCrs() != null)
            env.setCrs(bounds.getCrs().getSrs());
        return env.minx(bounds.getMinx()).maxx(bounds.getMaxx()).miny(bounds.getMiny()).maxy(bounds.getMaxy());
    }

    private List<KeywordInfo> buildKeywords(List<String> keywords) {
        if (keywords == null)
            return null;
        return keywords.stream().map(s -> new KeywordInfo().value(s)).collect(Collectors.toList());
    }

    private String resolveUniqueLayerName(final @NonNull String workspace, final @NonNull String proposedName) {
        String layerName = nameResolver.resolveLayerName(proposedName);
        Optional<Layer> existingGsLayer = geoserver.findLayerByName(workspace, layerName);
        for (int deduplicatingCounter = 1; existingGsLayer.isPresent(); deduplicatingCounter++) {
            layerName = nameResolver.resolveLayerName(proposedName) + "_" + deduplicatingCounter;
            existingGsLayer = geoserver.findLayerByName(workspace, layerName);
        }
        return layerName;
    }

    private String resolveWorkspace(@NonNull UserInfo user) {
        final @NonNull String orgName = user.getOrganization().getId();
        final String workspaceName = nameResolver.resolveWorkspaceName(orgName);

        WorkspaceInfo ws = geoserver.getOrCreateWorkspace(workspaceName);

        return ws.getName();
    }

    private MetadataLinkInfo buildMetadataLink(String metadataRecordId) {
        final URI recordURI = metadataPublicationService.buildMetadataRecordURL(metadataRecordId);
        MetadataLinkInfo info = new MetadataLinkInfo();
        info.setId(metadataRecordId);
        info.setContent(recordURI.toString());
        info.setMetadataType("ISO19115:2003");// TODO: revisit correct value
        info.setAbout("XML metadata record");
        return info;
    }

}
