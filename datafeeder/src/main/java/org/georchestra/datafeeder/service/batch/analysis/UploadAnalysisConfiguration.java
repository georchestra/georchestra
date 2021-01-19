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

import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.service.UploadPackage;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring-batch configuration for the uploaded datasets analysis workflow
 * <p>
 * File upload job consists of the following steps:
 * <p>
 * <ul>
 * <li>1- Read {@link UploadPackage}, return new {@link DataUploadJob}
 * <li>2- Initialize {@link DataUploadJob}, return {@link DatasetUploadState
 * List<DatasetUploadState>}
 * <li>3- Analyze each {@link DatasetUploadState}
 * <li>4- Save and update {@link DataUploadJob}
 * </ul>
 *
 */
@Configuration
@ComponentScan
@EnableBatchProcessing
public class UploadAnalysisConfiguration {

    public static final String UPLOAD_ID_JOB_PARAM_NAME = "uploadId";
    public static final String JOB_NAME = "analyzeUploadJob";

    private @Autowired JobBuilderFactory jobs;
    private @Autowired StepBuilderFactory steps;

    @Bean(name = UploadAnalysisConfiguration.JOB_NAME)
    public Job analyzeUploadJob(//
            @Qualifier("initializeDataUploadState") Step initializer, //
            @Qualifier("analyzeDatasets") Step analyzer) {

        return jobs.get("analyzeUploadJob")//
                .incrementer(new RunIdIncrementer())//
                .listener(jobLifeCycleStatusUpdateListener())//
                // steps...
                .start(initializer)//
                .next(analyzer)//
                .build();
    }

    @JobScope
    public @Bean JobLifeCycleStatusUpdateListener jobLifeCycleStatusUpdateListener() {
        return new JobLifeCycleStatusUpdateListener();
    }

    public @Bean Step initializeDataUploadState(DataUploadStateInitializer initializer) {
        return steps.get("initializeDataUploadState")//
                .tasklet(initializer)//
                .build();
    }

    public @Bean Step analyzeDatasets(DataUploadAnalysisService service, DatasetUploadStateItemReader reader,
            DatasetUploadStateUpdateListener itemStatusUpdater) {

        TaskletStep step = steps.get("analyzeDataset")//
                .<DatasetUploadState, DatasetUploadState>chunk(1)//
                .reader(reader)//
                .processor(service::analyze)//
                .writer(service::save)//
                .listener(itemStatusUpdater)//
                .build();//
        return step;
    }
}