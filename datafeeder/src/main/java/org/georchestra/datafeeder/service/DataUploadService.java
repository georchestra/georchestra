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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.georchestra.datafeeder.api.FileUploadApiController;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.UploadStatus;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.georchestra.datafeeder.service.batch.analysis.DataUploadAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import lombok.NonNull;

/**
 * Service provider for {@link FileUploadApiController}
 */
public class DataUploadService {

    private @Autowired DataUploadJobRepository repository;
    private @Autowired DataUploadAnalysisService analysisService;

    public Optional<DataUploadJob> findJob(UUID uploadId) {
        return repository.findByJobId(uploadId);
    }

    public DataUploadJob createJob(@NonNull UUID jobId, @NonNull String username) {
        return analysisService.createJob(jobId, username);
    }

    /**
     * Asynchronously starts the analysis process for the upload pack given by its
     * id, returns immediately with {@link DataUploadJob#getStatus() status}
     * {@link UploadStatus#PENDING} and an empty {@link DataUploadJob#getDatasets()
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
}
