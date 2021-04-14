/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.datafeeder.batch.service;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.JobStatus;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.georchestra.datafeeder.service.publish.DataBackendService;
import org.georchestra.datafeeder.service.publish.MetadataPublicationService;
import org.georchestra.datafeeder.service.publish.OWSPublicationService;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Encapsulates implementation of processing steps required by the publish job;
 * with methods intended to be called by the job {@link Step steps}
 *
 */
@Slf4j
public class PublishingBatchService {

    private @Autowired JobManager jobManager;

    private @Autowired DataUploadJobRepository repository;
    private @Autowired DataBackendService backendService;
    private @Autowired OWSPublicationService owsService;
    private @Autowired MetadataPublicationService metadataService;

    public void runJob(@NonNull UUID jobId) {
        jobManager.launchPublishingProcess(jobId);
    }

    public DataUploadJob findJob(@NonNull UUID jobId) {
        return repository.findByJobId(jobId).orElseThrow(() -> new IllegalArgumentException("job not found: " + jobId));
    }

    private void checkAnalisisComplete(DataUploadJob job) {
        if (job.getAnalyzeStatus() != JobStatus.DONE) {
            throw new IllegalStateException(String.format("Datasets analysis not complete for job %s: %s",
                    job.getJobId(), job.getAnalyzeStatus()));
        }
    }

    private void checkPublishingStatus(@NonNull UUID jobId, @NonNull JobStatus actual, @NonNull JobStatus expected) {
        if (!actual.equals(expected)) {
            throw new IllegalStateException(String.format(
                    "Unexpected publishing status. Job: %s, status: %s, expected: %s", jobId, actual, expected));
        }
    }

    private DataUploadJob findAndCheckPublishStatusIsRunning(@NonNull UUID jobId) {
        DataUploadJob job = findJob(jobId);
        checkAnalisisComplete(job);
        checkPublishingStatus(jobId, job.getPublishStatus(), JobStatus.RUNNING);
        return job;
    }

    @Transactional
    public void setPublishingStatus(@NonNull UUID jobId, @NonNull JobStatus status) {
        log.info("Publish {}: Set publish status {}", jobId, status);
        int recordsAffected = repository.setPublishingStatus(jobId, status);
        if (recordsAffected != 1) {
            throw new IllegalArgumentException("Job " + jobId + " does not exist");
        }
    }

    @Transactional
    public void setPublishingStatusError(@NonNull UUID jobId, @NonNull String message) {
        log.info("Publish {}: Set publish status {} '{}'", jobId, JobStatus.ERROR, message);
        int recordsAffected = repository.setPublishingStatus(jobId, JobStatus.ERROR, message);
        if (recordsAffected != 1) {
            throw new IllegalArgumentException("Job " + jobId + " does not exist");
        }
    }

    /**
     * Initializing step, checks the {@link DataUploadJob#getAnalyzeStatus()
     * analysis} is complete and set the {@link DataUploadJob#getPublishStatus()
     * publishing} status to {@link JobStatus#RUNNING running}
     */
    @Transactional
    public void initializeJobPublishingStatus(@NonNull UUID jobId) {
        log.info("Publish {}: Initialize job status to {}", jobId, JobStatus.RUNNING);
        DataUploadJob job = findJob(jobId);
        checkAnalisisComplete(job);
        job.setPublishStatus(JobStatus.RUNNING);
        save(job);
    }

    /**
     * 
     * @param jobId
     */
    @Transactional
    public void prepareTargetStoreForJobDatasets(@NonNull UUID jobId) {
        log.info("Publish {}: Prepare target store for uploaded datasets", jobId);
        DataUploadJob job = findAndCheckPublishStatusIsRunning(jobId);

        backendService.prepareBackend(job);

        job.getDatasets().forEach(dset -> {
            dset.setPublishStatus(JobStatus.RUNNING);
            if (dset.getPublishing() == null) {
                dset.setPublishing(new PublishSettings());
            }
        });
        save(job);
    }

    public void importDatasetsToTargetDatastore(@NonNull UUID jobId) {
        log.info("Publish {}: importing datasets to target database", jobId);
        DataUploadJob job = findAndCheckPublishStatusIsRunning(jobId);
        doOnEachRunningDataset(job, backendService::importDataset);
        save(job);
    }

    public void publishDatasetsToGeoServer(@NonNull UUID jobId) {
        log.info("Publish {}: Publish datasets to GeoServer", jobId);
        DataUploadJob job = findAndCheckPublishStatusIsRunning(jobId);
        doOnEachRunningDataset(job, owsService::publish);
        save(job);
    }

    @Transactional
    public DataUploadJob save(DataUploadJob job) {
        return repository.saveAndFlush(job);
    }

    public void publishDatasetsMetadataToGeoNetwork(@NonNull UUID jobId) {
        log.info("Publish {}: Publish datasets metadata to GeoNetwork", jobId);
        DataUploadJob job = findAndCheckPublishStatusIsRunning(jobId);
        doOnEachRunningDataset(job, metadataService::publish);
        save(job);
    }

    public void addMetadataLinksToGeoServerDatasets(@NonNull UUID jobId) {
        log.info("Publish {}: Add metadata links to GeoServer layer infos", jobId);
        DataUploadJob job = findAndCheckPublishStatusIsRunning(jobId);
        doOnEachRunningDataset(job, dset -> {
            if (JobStatus.RUNNING == dset.getPublishStatus()) {
                owsService.addMetadataLink(dset);
                dset.setPublishStatus(JobStatus.DONE);
            }
        });
        save(job);
    }

    private void doOnEachRunningDataset(DataUploadJob job, Consumer<DatasetUploadState> consumer) {
        for (DatasetUploadState dataset : job.getDatasets()) {
            if (JobStatus.RUNNING == dataset.getPublishStatus()) {
                try {
                    consumer.accept(dataset);
                } catch (Exception e) {
                    dataset.setPublishStatus(JobStatus.ERROR);
                    dataset.setError(e.getMessage());
                }
            }
        }
        save(job);
    }

    @Transactional
    public void summarize(@NonNull UUID jobId) {
        log.info("Publish {}: summarize status", jobId);
        DataUploadJob job = this.findJob(jobId);
        if (job.getPublishStatus() != JobStatus.ERROR) {
            JobStatus status = determineJobStatus(job.getDatasets());
            job.setPublishStatus(status);
            if (JobStatus.ERROR == status) {
                String errorMessage = buildErrorMessage(job.getDatasets());
                log.info("Publish {}: summarized status is ERROR '{}'", jobId, errorMessage);
                job.setError(errorMessage);
            }
        }
        save(job);
    }

    private JobStatus determineJobStatus(List<DatasetUploadState> datasets) {
        for (DatasetUploadState d : datasets) {
            JobStatus status = d.getPublishStatus();
            if (JobStatus.ERROR == status) {
                return JobStatus.ERROR;
            } else if (JobStatus.DONE != status) {
                throw new IllegalStateException("Expected status DONE or ERROR, got " + status);
            }
        }
        return JobStatus.DONE;
    }

    private String buildErrorMessage(List<DatasetUploadState> datasets) {
        return "Error publishing the following datasets:\n"
                + datasets.stream().filter(d -> d.getPublishStatus() == JobStatus.ERROR)
                        .map(d -> d.getName() + ": " + d.getError()).collect(Collectors.joining("\n"));
    }

}
