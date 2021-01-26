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

import java.util.UUID;

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
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PublishingBatchService {


    private @Autowired JobLauncher jobLauncher;
    private @Autowired @Qualifier(JOB_NAME) Job job;

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

}
