/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

import javax.annotation.PostConstruct;

import org.georchestra.datafeeder.batch.UserInfoPropertyEditor;
import org.georchestra.datafeeder.batch.service.PublishingBatchService;
import org.georchestra.datafeeder.event.PublishFailedEvent;
import org.georchestra.datafeeder.event.PublishFinishedEvent;
import org.georchestra.datafeeder.event.PublishStartedEvent;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.JobStatus;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.georchestra.datafeeder.service.FileStorageService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PublishJobLifeCycleStatusUpdateListener implements JobExecutionListener {

    private @Autowired PublishingBatchService service;
    private @Autowired ApplicationEventPublisher eventPublisher;
    private @Autowired DataUploadJobRepository repository;
    private @Autowired FileStorageService storageService;

    private @Value("#{jobParameters['uploadId']}") UUID uploadId;
    private @Value("#{jobParameters['user']}") String userStr;

    private @Autowired UserInfoPropertyEditor userInfoPropertyEditor;
    private UserInfo user;

    @PostConstruct
    public void initBinder() {
        userInfoPropertyEditor.setAsText(userStr);
        this.user = userInfoPropertyEditor.getValue();
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        if (uploadId == null)
            return;

        final BatchStatus status = jobExecution.getStatus();
        switch (status) {
        case STARTING:
        case STARTED:
            try {
                service.initializeJobPublishingStatus(uploadId);
                DataUploadJob job = repository.getOne(uploadId);
                eventPublisher.publishEvent(new PublishStartedEvent(job, user));
            } catch (RuntimeException e) {
                String message = e.getMessage();
                service.setPublishingStatusError(uploadId, message);

                DataUploadJob job = repository.getOne(uploadId);
                eventPublisher.publishEvent(new PublishFailedEvent(job, user, e));
                throw e;
            }
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
        case COMPLETED: {
            // Check for DataUploadJob.publishStatus,
            // we need to leave the spring-batch job complete normally or the
            // publishStatus/error won't be persisted, as spring-batch and our JPA
            // repositories share the same entity manager. If a publish process failed and
            // the exception was re-thrown, spring-batch would abort the current
            // transaction.
            DataUploadJob job = service.summarize(uploadId);
            if (job.getPublishStatus() == JobStatus.DONE) {
                eventPublisher.publishEvent(new PublishFinishedEvent(job, user));
            } else if (job.getPublishStatus() == JobStatus.ERROR) {
                eventPublisher.publishEvent(new PublishFailedEvent(job, user, null));
            }
            storageService.deletePackage(uploadId);
        }
            break;
        case ABANDONED:
        case FAILED: {
//			service.summarize(uploadId);
            DataUploadJob job = repository.getOne(uploadId);
            eventPublisher.publishEvent(new PublishFailedEvent(job, user, null));
        }
            break;
        case STOPPING:
        case STOPPED:
            service.setPublishingStatus(uploadId, JobStatus.PENDING);
            break;
        case STARTED:
        case UNKNOWN:
        default:
            break;
        }
    }
}
