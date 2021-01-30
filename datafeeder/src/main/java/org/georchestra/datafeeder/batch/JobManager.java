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
package org.georchestra.datafeeder.batch;

import java.util.UUID;

import org.georchestra.datafeeder.batch.analysis.UploadAnalysisJobConfiguration;
import org.georchestra.datafeeder.batch.publish.DataPublishingJobConfiguration;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobManager {

    private @Autowired JobLauncher jobLauncher;

    private @Autowired @Qualifier(UploadAnalysisJobConfiguration.JOB_NAME) Job uploadAnalysisJob;
    private @Autowired @Qualifier(DataPublishingJobConfiguration.JOB_NAME) Job dataPublishingJob;

    @Async
    public void launchUploadJobAnalysis(@NonNull UUID jobId) {
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
    public void launchPublishingProcess(@NonNull UUID jobId) {
        final String paramName = DataPublishingJobConfiguration.JOB_PARAM_ID;
        final String paramValue = jobId.toString();
        final boolean identifying = true;

        final JobParameters params = new JobParametersBuilder()//
                .addString(paramName, paramValue, identifying)//
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

}
