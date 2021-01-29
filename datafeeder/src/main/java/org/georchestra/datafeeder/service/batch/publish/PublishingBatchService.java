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
package org.georchestra.datafeeder.service.batch.publish;

import static org.georchestra.datafeeder.service.batch.publish.DataPublishingConfiguration.JOB_NAME;
import static org.georchestra.datafeeder.service.batch.publish.DataPublishingConfiguration.JOB_PARAM_ID;

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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Encapsulates implementation of processing steps required by the publish job;
 * with methods intended to be called by the job {@link Step steps}
 *
 */
@Slf4j
public class PublishingBatchService {

    private @Autowired JobLauncher jobLauncher;
    private @Autowired @Qualifier(JOB_NAME) Job job;

    private @Autowired DataUploadJobRepository repository;
    private @Autowired DataBackendService backendService;
    private @Autowired OWSPublicationService owsService;
    private @Autowired MetadataPublicationService metadataService;

    public void runJob(@NonNull UUID jobId) {
        final String paramName = JOB_PARAM_ID;
        final String paramValue = jobId.toString();
        final boolean identifying = true;

        final JobParameters params = new JobParametersBuilder()//
                .addString(paramName, paramValue, identifying)//
                .toJobParameters();
        log.info("Launching publishing job {}", jobId);
        JobExecution execution;
        try {
            execution = jobLauncher.run(job, params);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException e) {
            log.error("Error running job {}", jobId, e);
            throw new RuntimeException("Error running job " + jobId, e);
        }
        log.info("Publishing job {} finished with status {}", jobId, execution.getStatus());
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

    public void setPublishingStatus(@NonNull UUID jobId, @NonNull JobStatus status) {
        int recordsAffected = repository.setPublishingStatus(jobId, status);
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
    public void initializePublishingStatus(@NonNull UUID jobId) {
        DataUploadJob job = findJob(jobId);
        checkAnalisisComplete(job);
        job.getDatasets().forEach(dset -> {
            dset.setPublishStatus(JobStatus.RUNNING);
            if (dset.getPublishing() == null) {
                dset.setPublishing(new PublishSettings());
            }
        });
        job.setPublishStatus(JobStatus.RUNNING);
        save(job);
    }

    /**
     * 
     * @param jobId
     */
    public void prepareTargetStoreForJobDatasets(@NonNull UUID jobId) {
        DataUploadJob job = findAndCheckPublishStatusIsRunning(jobId);
        backendService.prepareBackend(job);
        save(job);
    }

    public void importDatasetsToTargetDatastore(@NonNull UUID jobId) {
        DataUploadJob job = findAndCheckPublishStatusIsRunning(jobId);
        doOnEachRunningDataset(job, backendService::importDataset);
        save(job);
    }

    public void publishDatasetsToGeoServer(@NonNull UUID jobId) {
        DataUploadJob job = findAndCheckPublishStatusIsRunning(jobId);
        doOnEachRunningDataset(job, owsService::publish);
        save(job);
    }

    public DataUploadJob save(DataUploadJob job) {
        return repository.saveAndFlush(job);
    }

    public void publishDatasetsMetadataToGeoNetwork(@NonNull UUID jobId) {
        DataUploadJob job = findAndCheckPublishStatusIsRunning(jobId);
        doOnEachRunningDataset(job, metadataService::publish);
        save(job);
    }

    public void addMetadataLinksToGeoServerDatasets(@NonNull UUID jobId) {
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
    public void summarize(@NonNull UUID uploadId) {
        DataUploadJob job = this.findJob(uploadId);
        JobStatus status = determineJobStatus(job.getDatasets());
        job.setPublishStatus(status);
        if (JobStatus.ERROR == status) {
            job.setError(buildErrorMessage(job.getDatasets()));
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
