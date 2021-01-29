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
package org.georchestra.datafeeder.service.batch.publish;

import java.net.URI;
import java.util.Objects;

import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockOWSPublicationService implements OWSPublicationService {

    private @Autowired MetadataPublicationService metadataService;

    @Override
    public void publish(@NonNull DatasetUploadState dataset) {
        log.info("MOCK publishing of OWS datasets for " + dataset.getJob().getJobId() + "/" + dataset.getName());
    }

    @Override
    public void addMetadataLink(@NonNull DatasetUploadState dataset) {
        log.info("MOCK publishing of OWS metadata links for " + dataset.getJob().getJobId() + "/" + dataset.getName());
        PublishSettings publishing = dataset.getPublishing();
        Objects.requireNonNull(publishing);
        Objects.requireNonNull(publishing.getMetadataRecordId());
        URI metadataLink = metadataService.buildMetadataRecordURI(publishing.getMetadataRecordId());
        // non-mock service should update the geoserver feature type adding the md link
        // here
        log.info("MOCK added metadata link " + metadataLink);
    }

}
