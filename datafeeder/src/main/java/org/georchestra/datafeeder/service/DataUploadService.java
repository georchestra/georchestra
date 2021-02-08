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
package org.georchestra.datafeeder.service;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.georchestra.datafeeder.api.FileUploadApiController;
import org.georchestra.datafeeder.batch.service.DataUploadAnalysisService;
import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.JobStatus;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import lombok.NonNull;

/**
 * Service provider for {@link FileUploadApiController}
 */
public class DataUploadService {

    private @Autowired DataUploadJobRepository repository;
    private @Autowired DataUploadAnalysisService analysisService;
    private @Autowired DatasetsService datasetsService;

    public Optional<DataUploadJob> findJob(UUID uploadId) {
        return repository.findByJobId(uploadId);
    }

    public DataUploadJob createJob(@NonNull UUID jobId, @NonNull String username, String orgName) {
        return analysisService.createJob(jobId, username, orgName);
    }

    /**
     * Asynchronously starts the analysis process for the upload pack given by its
     * id, returns immediately with {@link DataUploadJob#getStatus() status}
     * {@link JobStatus#PENDING} and an empty {@link DataUploadJob#getDatasets()
     * datasets} list.
     */
    @Async
    public void analyze(@NonNull UUID uploadId) {
        analysisService.runJob(uploadId);
    }

    public List<DataUploadJob> findAllJobs() {
        return repository.findAllByOrderByCreatedDateDesc();
    }

    public List<DataUploadJob> findUserJobs(@NonNull String userName) {
        return repository.findAllByUsernameOrderByCreatedDateDesc(userName);
    }

    public void abortAndRemove(@NonNull UUID jobId) {
        throw new UnsupportedOperationException("unimplemented");
    }

    public void remove(@NonNull UUID jobId) {
        repository.delete(jobId);
    }

    public SimpleFeature sampleFeature(@NonNull UUID jobId, @NonNull String typeName, int featureN, Charset encoding,
            String srs, boolean srsReproject) {

        DatasetUploadState dataset = getDataset(jobId, typeName);
        Path path = Paths.get(dataset.getAbsolutePath());
        return datasetsService.getFeature(path, typeName, encoding, featureN, srs, srsReproject);
    }

    public BoundingBoxMetadata computeBounds(@NonNull UUID jobId, @NonNull String typeName, String srs,
            boolean reproject) {

        DatasetUploadState dataset = getDataset(jobId, typeName);
        Path path = Paths.get(dataset.getAbsolutePath());
        return datasetsService.getBounds(path, typeName, srs, reproject);
    }

    private DatasetUploadState getDataset(UUID jobId, String typeName) {
        DataUploadJob job = findJob(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job " + jobId + " does not exist"));
        DatasetUploadState dataset = job.getDatasets().stream().filter(d -> d.getName().equals(typeName)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("dataset " + typeName + " does not exist"));
        return dataset;
    }

}
