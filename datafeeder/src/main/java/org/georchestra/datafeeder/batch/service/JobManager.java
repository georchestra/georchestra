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
package org.georchestra.datafeeder.batch.service;

import java.io.IOException;
import java.util.UUID;

import org.georchestra.datafeeder.batch.analysis.UploadAnalysisJobConfiguration;
import org.georchestra.datafeeder.batch.publish.DataPublishingJobConfiguration;
import org.georchestra.datafeeder.model.UserInfo;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobManager {

    private @Autowired JobLauncher jobLauncher;
    private @Autowired ApplicationContext context;

    @Async
    public void launchUploadJobAnalysis(@NonNull UUID jobId) {
        final Job uploadAnalysisJob = context.getBean(UploadAnalysisJobConfiguration.JOB_NAME, Job.class);
        final String paramName = UploadAnalysisJobConfiguration.UPLOAD_ID_JOB_PARAM_NAME;
        final String paramValue = jobId.toString();
        final boolean identifying = true;

        final JobParameters params = new JobParametersBuilder()//
                .addString(paramName, paramValue, identifying)//
                .toJobParameters();
        log.info("Launching analisys job {}", jobId);
        JobExecution execution;
        try {
            execution = jobLauncher.run(uploadAnalysisJob, params);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException e) {
            log.error("Error running job {}", jobId, e);
            throw new RuntimeException("Error running job " + jobId, e);
        }
        log.info("Analysis job {} finished with status {}", jobId, execution.getStatus());
    }

    @Async
    public void launchPublishingProcess(@NonNull UUID jobId, @NonNull UserInfo user) {
        final Job dataPublishingJob = context.getBean(DataPublishingJobConfiguration.JOB_NAME, Job.class);
        final boolean identifying = true;

        final JobParameters params = new JobParametersBuilder()//
                .addString(DataPublishingJobConfiguration.JOB_PARAM_ID, jobId.toString(), identifying)//
                .addString(DataPublishingJobConfiguration.USER_PARAM, toString(user), false)//
                .toJobParameters();

        log.info("Launching publishing job {}", jobId);
        JobExecution execution;
        try {
            execution = jobLauncher.run(dataPublishingJob, params);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException e) {
            log.error("Error running job {}", jobId, e);
            throw new RuntimeException("Error running job " + jobId, e);
        }
        log.info("Publishing job {} finished with status {}", jobId, execution.getStatus());
    }

    public static String toString(@NonNull UserInfo user) {
        try {
            return new ObjectMapper().writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public UserInfo fromString(@NonNull String serializedUser) {
        try {
            return new ObjectMapper().reader().readValue(serializedUser, UserInfo.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
