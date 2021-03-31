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
package org.georchestra.datafeeder.service;

import java.util.UUID;

import org.georchestra.datafeeder.api.DataPublishingApiController;
import org.georchestra.datafeeder.api.DatasetMetadata;
import org.georchestra.datafeeder.api.DatasetPublishRequest;
import org.georchestra.datafeeder.api.PublishRequest;
import org.georchestra.datafeeder.batch.service.PublishingBatchService;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NonNull;

/**
 * Service provider for {@link DataPublishingApiController}
 * <p>
 * Acts as a facade for internal processing of business processes required by
 * the controller, which in turn only takes care of the HTTP API layer,
 * delegating all processing to this service.
 */
@Service
public class DataPublishingService {

    private @Autowired PublishingBatchService publishingBatchService;

    public void publish(@NonNull UUID uploadId, @NonNull PublishRequest req) {
        DataUploadJob job = publishingBatchService.findJob(uploadId);
        for (DatasetPublishRequest dreq : req.getDatasets()) {
            String nativeName = dreq.getNativeName();
            DatasetUploadState dset = job.getDataset(nativeName)
                    .orElseThrow(() -> new IllegalArgumentException("Dataset " + nativeName + " does not exist"));
            PublishSettings publishing = dset.getPublishing();
            if (publishing == null) {
                publishing = new PublishSettings();
                dset.setPublishing(publishing);
            }
            String requestedPublishedName = dreq.getPublishedName() == null ? nativeName : dreq.getPublishedName();
            publishing.setPublishedName(requestedPublishedName);
            String srs = dreq.getSrs();
            if (srs == null && null != dset.getNativeBounds() && null != dset.getNativeBounds().getCrs()) {
                srs = dset.getNativeBounds().getCrs().getSrs();
            }
            publishing.setSrs(srs);
            publishing.setSrsReproject(dreq.getSrsReproject());
            String encoding = dreq.getEncoding();
            if (null == encoding) {
                encoding = dset.getEncoding();
            }
            publishing.setEncoding(encoding);

            DatasetMetadata md = dreq.getMetadata();
            publishing.setTitle(md.getTitle());
            publishing.setAbstract(md.getAbstract());
            publishing.setDatasetCreationDate(md.getCreationDate());
            publishing.setDatasetCreationProcessDescription(md.getCreationProcessDescription());
            publishing.setKeywords(md.getTags());
            Integer scale = md.getScale() == null ? 25_000 : md.getScale();
            publishing.setScale(scale);
        }
        publishingBatchService.save(job);

        publishingBatchService.runJob(uploadId);
    }

}
