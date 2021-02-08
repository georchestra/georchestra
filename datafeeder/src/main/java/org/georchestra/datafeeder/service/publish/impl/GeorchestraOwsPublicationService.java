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

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import org.georchestra.datafeeder.autoconf.GeorchestraNameNormalizer;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.service.publish.MetadataPublicationService;
import org.georchestra.datafeeder.service.publish.OWSPublicationService;
import org.geoserver.openapi.model.catalog.FeatureTypeInfo;
import org.geoserver.openapi.model.catalog.MetadataLinkInfo;
import org.geoserver.openapi.model.catalog.WorkspaceInfo;
import org.geoserver.openapi.v1.model.Layer;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.NonNull;

public class GeorchestraOwsPublicationService implements OWSPublicationService {

    private @Autowired MetadataPublicationService metadataPublicationService;

    private @Autowired GeoServerRemoteService geoserver;
    private @Autowired GeorchestraNameNormalizer nameResolver;

    public @Override void publish(@NonNull DatasetUploadState dataset) {
        PublishSettings publishing = dataset.getPublishing();
        Objects.requireNonNull(publishing);
        Objects.requireNonNull(publishing.getPublishedName());

        final String workspaceName = resolveWorkspace(dataset);
        final String publishedLayerName = resolveUniqueLayerName(workspaceName, publishing.getPublishedName());

        FeatureTypeInfo fti = buildPublishingFeatureType(workspaceName, publishedLayerName, dataset);
        geoserver.create(fti);

        publishing.setPublishedWorkspace(workspaceName);
        publishing.setPublishedName(publishedLayerName);
    }

    private FeatureTypeInfo buildPublishingFeatureType(String workspaceName, String publishedLayerName,
            @NonNull DatasetUploadState dataset) {
        // TODO Auto-generated method stub
        return null;
    }

    private String resolveUniqueLayerName(@NonNull String workspace, @NonNull String proposedName) {
        String layerName = nameResolver.resolveLayerName(proposedName);
        Optional<Layer> existing;
        int deduplicatingCounter = 0;
        do {
            existing = geoserver.findLayerByName(workspace, layerName);
            proposedName += "_" + (++deduplicatingCounter);
        } while (existing.isPresent());
        return layerName;
    }

    private String resolveWorkspace(@NonNull DatasetUploadState dataset) {
        final @NonNull String orgName = dataset.getJob().getOrganizationName();
        final String workspaceName = nameResolver.resolveWorkspaceName(orgName);

        WorkspaceInfo ws = geoserver.getOrCreateWorkspace(workspaceName);

        return ws.getName();
    }

    public @Override void addMetadataLink(@NonNull DatasetUploadState dataset) {
        PublishSettings publishing = dataset.getPublishing();

        Objects.requireNonNull(publishing);
        Objects.requireNonNull(publishing.getPublishedWorkspace());
        Objects.requireNonNull(publishing.getPublishedName());
        Objects.requireNonNull(publishing.getMetadataRecordId());

        final String workspace = publishing.getPublishedWorkspace();
        final String layerName = publishing.getPublishedName();
        final String metadataRecordId = publishing.getMetadataRecordId();

        FeatureTypeInfo fti = geoserver.getFeatureTypeInfo(workspace, layerName);

        MetadataLinkInfo metadatalink = buildMetadataLink(metadataRecordId);

        fti.addMetadatalinksItem(metadatalink);

        geoserver.update(fti);
    }

    private MetadataLinkInfo buildMetadataLink(String metadataRecordId) {
        final URI recordURI = metadataPublicationService.buildMetadataRecordURI(metadataRecordId);
        MetadataLinkInfo info = new MetadataLinkInfo();
        info.setId(metadataRecordId);
        info.setContent(recordURI.toString());
        info.setMetadataType("ISO-19139");// TODO: revisit correct value
        // info.setAbout("???");
        return info;
    }

}
