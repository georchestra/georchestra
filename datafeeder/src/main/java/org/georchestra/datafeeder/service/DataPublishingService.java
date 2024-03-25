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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.georchestra.datafeeder.api.DataPublishingApiController;
import org.georchestra.datafeeder.api.DatasetMetadata;
import org.georchestra.datafeeder.api.DatasetPublishRequest;
import org.georchestra.datafeeder.api.PublishRequest;
import org.georchestra.datafeeder.batch.service.PublishingBatchService;
import org.georchestra.datafeeder.model.CoordinateReferenceSystemMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Service provider for {@link DataPublishingApiController}
 * <p>
 * Acts as a facade for internal processing of business processes required by
 * the controller, which in turn only takes care of the HTTP API layer,
 * delegating all processing to this service.
 */
@Slf4j
@Service
public class DataPublishingService {

    private @Autowired PublishingBatchService publishingBatchService;

    public void publish(@NonNull UUID uploadId, @NonNull PublishRequest req, @NonNull UserInfo user) {
        DataUploadJob job = publishingBatchService.findJob(uploadId);

        job.getDatasets().forEach(dset -> {
            dset.setPublishing(new PublishSettings());
        });

        for (DatasetPublishRequest dreq : req.getDatasets()) {
            String nativeName = dreq.getNativeName();
            DatasetUploadState dset = getDataset(job, nativeName);
            PublishSettings publishing = dset.getPublishing();
            // set publish to true only for the requested datasets
            publishing.setPublish(true);
            String requestedPublishedName = dreq.getPublishedName() == null ? nativeName : dreq.getPublishedName();
            publishing.setPublishedName(requestedPublishedName);
            if (dset.getFormat() != DataSourceMetadata.DataSourceType.CSV) {
                String srs = resolvePublishSRS(dset.getNativeBounds().getCrs(), dreq.getSrs());
                publishing.setSrs(srs);
                publishing.setSrsReproject(dreq.getSrsReproject());
            }
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

            Map<String, Object> originalOpts = md.getOptions();
            publishing.setOptions(morphOptions(originalOpts));
        }
        publishingBatchService.save(job);
        publishingBatchService.runJob(uploadId, user);
    }

    Map<String, String> morphOptions(Map<String, Object> options) {
        Map<String, String> opts = null;
        if (options != null) {
            opts = options.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().toString()));
        }
        return opts;
    }

    /**
     *
     * @param nativeCRS  the datasets native CRS as identified from the upload, may
     *                   be {@code null}
     * @param requestSRS the EPSG code string requested through the dataset publish
     *                   request body, may be {@code null}
     * @return the EPSG code to publish the dataset with
     * @throws IllegalArgumentException if both {@code nativeCRS} and
     *                                  {@code requestSRS} are {@code null}
     */
    private @NonNull String resolvePublishSRS(@Nullable CoordinateReferenceSystemMetadata nativeCRS,
            @Nullable String requestSRS) {

        String srs;
        if (requestSRS == null) {
            if (nativeCRS == null) {
                throw new IllegalArgumentException(
                        "No SRS provided in the publish request, and no native CRS provided in the uploaded dataset");
            }
            srs = nativeCRS.getSrs();
            if (srs == null) {
                throw new IllegalArgumentException(
                        "No SRS provided in the publish request, and the native CRS EPSG code was not recognized");
            } else {
                log.debug("Publishing using the native SRS {}", requestSRS);
            }
        } else {
            log.debug("Publishing using the requested SRS {}", requestSRS);
            srs = requestSRS;
        }
        return srs;
    }

    private DatasetUploadState getDataset(DataUploadJob job, String nativeName) {
        DatasetUploadState dset = job.getDataset(nativeName)
                .orElseThrow(() -> new IllegalArgumentException("Dataset " + nativeName + " does not exist"));
        return dset;
    }

}
