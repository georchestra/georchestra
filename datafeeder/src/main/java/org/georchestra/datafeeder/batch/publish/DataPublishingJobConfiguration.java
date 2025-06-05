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

import org.georchestra.datafeeder.batch.service.PublishingBatchService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * Spring-batch configuration for the data publishing workflow.
 *
 */
@Configuration
@EnableBatchProcessing
public class DataPublishingJobConfiguration {

    public static final String JOB_PARAM_ID = "uploadId";
    public static final String USER_PARAM = "user";
    public static final String JOB_NAME = "publishJob";

    private @Autowired JobBuilderFactory jobBuilderFactory;
    private @Autowired StepBuilderFactory stepBuilderFactory;

    public @Bean PublishJobProgressTracker publishJobProgressTracker() {
        return new PublishJobProgressTracker();
    }

    public @Bean PublishingBatchService publishingBatchService() {
        return new PublishingBatchService();
    }

    @Bean(name = DataPublishingJobConfiguration.JOB_NAME)
    public Job publishingJob(//
            @Qualifier("preparePostGisStep") Step preparePostgis, //
            @Qualifier("postGisStep") Step postgis, //
            @Qualifier("geoserverStep") Step geoserver, //
            @Qualifier("geonetworkStep") Step geonetwork, //
            @Qualifier("geoserverGeonetworkUpdateStep") Step geoserverGeonetworkUpdate) {

        return jobBuilderFactory.get(JOB_NAME)//
                .incrementer(new RunIdIncrementer())//
                .listener(publishJobLifeCycleStatusUpdateListener())//
                .start(preparePostgis)//
                .next(postgis)//
                .next(geoserver)//
                .next(geonetwork)//
                .next(geoserverGeonetworkUpdate)//
                .build();
    }

    @JobScope
    public @Bean PublishJobLifeCycleStatusUpdateListener publishJobLifeCycleStatusUpdateListener() {
        return new PublishJobLifeCycleStatusUpdateListener();
    }

    @StepScope
    public @Bean PrepareTargetDataStoreTasklet prepareTargetDataStoreTasklet() {
        return new PrepareTargetDataStoreTasklet();
    }

    @StepScope
    public @Bean DataImportTasklet postGisTasklet() {
        return new DataImportTasklet();
    }

    @StepScope
    public @Bean GeoServerTasklet geoServerTasklet() {
        return new GeoServerTasklet();
    }

    @StepScope
    public @Bean GeoNetworkTasklet geoNetworkTasklet() {
        return new GeoNetworkTasklet();
    }

    @StepScope
    public @Bean GeoServerGeoNetworkUpdateTasklet geoServerGeoNetworkUpdateTasklet() {
        return new GeoServerGeoNetworkUpdateTasklet();
    }

    public @Bean Step preparePostGisStep() {
        return stepBuilderFactory.get("preparePostGisStep")//
                .tasklet(prepareTargetDataStoreTasklet())//
                .build();
    }

    public @Bean Step postGisStep() {
        return stepBuilderFactory.get("postGisStep")//
                .tasklet(postGisTasklet())//
                .build();
    }

    public @Bean Step geoserverStep() {
        return stepBuilderFactory.get("geoserverStep")//
                .tasklet(geoServerTasklet())//
                .build();
    }

    public @Bean Step geonetworkStep() {
        return stepBuilderFactory.get("geonetworkStep")//
                .tasklet(geoNetworkTasklet())//
                .build();
    }

    public @Bean Step geoserverGeonetworkUpdateStep() {
        return stepBuilderFactory.get("geoserverGeonetworkUpdateStep")//
                .tasklet(geoServerGeoNetworkUpdateTasklet())//
                .build();
    }
}