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

package org.georchestra.datafeeder.batch.analysis;

import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.georchestra.datafeeder.batch.UserInfoPropertyEditor;
import org.georchestra.datafeeder.batch.service.DataUploadAnalysisService;
import org.georchestra.datafeeder.event.AnalysisFailedEvent;
import org.georchestra.datafeeder.event.AnalysisFinishedEvent;
import org.georchestra.datafeeder.event.AnalysisStartedEvent;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.JobStatus;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UploadJobLifeCycleStatusUpdateListener implements JobExecutionListener {

    private @Autowired DataUploadJobRepository repository;
    private @Autowired @Setter DataUploadAnalysisService service;
    private @Autowired ApplicationEventPublisher eventPublisher;

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
        log.info("upload job id: {}, status: {}", uploadId, status);
        switch (status) {
        case STARTING:
        case STARTED:
            repository.setAnalyzeStatus(uploadId, JobStatus.RUNNING);
            DataUploadJob job = repository.getOne(uploadId);
            eventPublisher.publishEvent(new AnalysisStartedEvent(job, user));
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
        case COMPLETED: {
            service.summarize(uploadId);
            DataUploadJob job = repository.getOne(uploadId);
            eventPublisher.publishEvent(new AnalysisFinishedEvent(job, user));
        }
            break;
        case ABANDONED:
        case FAILED: {
            service.summarize(uploadId);
            DataUploadJob job = repository.getOne(uploadId);
            List<Throwable> failureExceptions = jobExecution.getFailureExceptions();
            eventPublisher.publishEvent(new AnalysisFailedEvent(job, user, null));
        }
            break;
        case STOPPING:
        case STOPPED:
            repository.setAnalyzeStatus(uploadId, JobStatus.PENDING);
            break;
        case STARTED:
        case UNKNOWN:
        default:
            break;
        }
    }
}
