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
package org.georchestra.datafeeder.service.publish.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.service.geonetwork.GeoNetworkRemoteService;
import org.georchestra.datafeeder.service.publish.MetadataPublicationService;
import org.georchestra.datafeeder.service.publish.impl.MetadataRecordProperties.OnlineResource;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.NonNull;

public class GeorchestraMetadataPublicationService implements MetadataPublicationService {

    private GeoNetworkRemoteService geonetwork;

    public @Autowired GeorchestraMetadataPublicationService(@NonNull GeoNetworkRemoteService geonetwork) {
        this.geonetwork = geonetwork;
    }

    @Override
    public URI buildMetadataRecordURI(@NonNull String recordId) {
        return geonetwork.buildMetadataRecordURI(recordId);
    }

    @Override
    public void publish(@NonNull DatasetUploadState dataset) {
        Objects.requireNonNull(dataset.getPublishing());

        MetadataRecordProperties mdProps = toRecordProperties(dataset);
        Supplier<String> resolvedTemplate = applyTemplate(mdProps);
        String generatedId = geonetwork.publish(resolvedTemplate);
        dataset.getPublishing().setMetadataRecordId(generatedId);
    }

    private Supplier<String> applyTemplate(MetadataRecordProperties mdProps) {
        // TODO Auto-generated method stub
        return null;
    }

    private MetadataRecordProperties toRecordProperties(DatasetUploadState d) {
        PublishSettings p = d.getPublishing();

        final String metadataId = UUID.randomUUID().toString();

        MetadataRecordProperties m = new MetadataRecordProperties();
        m.setMetadataId(metadataId);
        m.setName(p.getPublishedName());
        m.setTitle(p.getTitle());
        m.setAbstract(p.getAbstract());
        if (null != p.getKeywords())
            m.setKeywords(new ArrayList<>(p.getKeywords()));
        m.setCreationDate(p.getDatasetCreationDate());
        m.setLineage(null);
        m.setResourceType("series");
        m.getOnlineResources().add(wmsOnlineResource(d));
        m.getOnlineResources().add(wfsOnlineResource(d));
        m.getOnlineResources().add(downloadOnlineResource(d));

        URI uniqueResourceIdentifier = geonetwork.buildMetadataRecordURI(metadataId);
        m.setDataIdentifier(uniqueResourceIdentifier);

        m.setDatasetLanguage("eng");// REVISIT, from config?
        m.setDistributionFormat("ESRI Shapefile");
        m.setDistributionFormatVersion("1.0");
        // m.setSpatialRepresentation("vector"); REVISIT
//		p.getGeographicBoundingBox();
        return m;
    }

    private OnlineResource wmsOnlineResource(DatasetUploadState d) {
        // TODO Auto-generated method stub
        return null;
    }

    private OnlineResource wfsOnlineResource(DatasetUploadState d) {
        // TODO Auto-generated method stub
        return null;
    }

    private OnlineResource downloadOnlineResource(DatasetUploadState d) {
        // TODO Auto-generated method stub
        return null;
    }

}
