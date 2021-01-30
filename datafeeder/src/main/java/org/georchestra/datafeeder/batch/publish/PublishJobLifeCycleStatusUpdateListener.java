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
package org.georchestra.datafeeder.batch.publish;

import java.util.UUID;

import org.georchestra.datafeeder.batch.service.PublishingBatchService;
import org.georchestra.datafeeder.model.JobStatus;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PublishJobLifeCycleStatusUpdateListener implements JobExecutionListener {

    private @Value("#{jobParameters['uploadId']}") UUID uploadId;
    private @Autowired DataUploadJobRepository repository;
    private @Autowired PublishingBatchService service;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        if (uploadId == null)
            return;

        final BatchStatus status = jobExecution.getStatus();
        switch (status) {
        case STARTING:
        case STARTED:
            service.initializePublishingStatus(uploadId);
            break;
        default:
            break;
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (uploadId == null)
            return;

        final BatchStatus status = jobExecution.getStatus();
        log.info("publish job id: {}, status: {}", uploadId, status);
        switch (status) {
        case COMPLETED:
        case ABANDONED:
        case FAILED:
            service.summarize(uploadId);
            break;
        case STOPPING:
        case STOPPED:
            repository.setPublishingStatus(uploadId, JobStatus.PENDING);
            break;
        case STARTED:
        case UNKNOWN:
        default:
            break;
        }
    }
}
