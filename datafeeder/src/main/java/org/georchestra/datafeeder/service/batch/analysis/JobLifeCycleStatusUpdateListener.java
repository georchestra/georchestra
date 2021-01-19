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
package org.georchestra.datafeeder.service.batch.analysis;

import java.util.UUID;

import org.georchestra.datafeeder.model.UploadStatus;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JobLifeCycleStatusUpdateListener implements JobExecutionListener {

    private @Value("#{jobParameters['uploadId']}") UUID uploadId;
    private @Autowired DataUploadJobRepository repository;
    private @Autowired @Setter DataUploadAnalysisService service;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        if (uploadId == null)
            return;

        final BatchStatus status = jobExecution.getStatus();
        log.info("upload job id: {}, status: {}", uploadId, status);
        switch (status) {
        case STARTING:
        case STARTED:
            repository.setJobStatus(uploadId, UploadStatus.ANALYZING);
            break;
        case ABANDONED:
        case COMPLETED:
        case FAILED:
        case STOPPED:
        case STOPPING:
        case UNKNOWN:
        default:
            break;
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (uploadId == null)
            return;

        final BatchStatus status = jobExecution.getStatus();
        log.info("upload job id: {}, status: {}", uploadId, status);
        switch (status) {
        case COMPLETED:
            service.summarize(uploadId);
            break;
        case ABANDONED:
        case FAILED:
            repository.setJobStatus(uploadId, UploadStatus.ERROR);
            break;
        case STOPPING:
        case STOPPED:
            repository.setJobStatus(uploadId, UploadStatus.PENDING);
            break;
        case STARTED:
        case UNKNOWN:
        default:
            break;
        }
    }
}
