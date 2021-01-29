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

import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.PublishSettings;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockOWSPublicationService implements OWSPublicationService {

    private @Autowired MetadataPublicationService metadataService;

    @Override
    public void publishDatasets(DataUploadJob job) {
        log.info("MOCK publishing of OWS datasets for " + job.getJobId());
    }

    @Override
    public void addMetadataLinks(DataUploadJob job) {
        log.info("MOCK publishing of OWS metadata links for " + job.getJobId());
        job.getDatasets().forEach(d -> {
            PublishSettings publishing = d.getPublishing();
            Objects.requireNonNull(publishing);
            Objects.requireNonNull(publishing.getMetadataRecordId());
            URI metadataLink = metadataService.buildMetadataRecordURI(publishing.getMetadataRecordId());
            log.info("MOCK added metadata link " + metadataLink);
        });
    }

}
