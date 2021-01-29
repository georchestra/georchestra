/*
 * Copyright (C) 2020, 2021 by the geOrchestra PSC
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

import org.georchestra.datafeeder.service.batch.publish.task.GeoNetworkTasklet;
import org.georchestra.datafeeder.service.batch.publish.task.GeoServerGeoNetworkUpdateTasklet;
import org.georchestra.datafeeder.service.batch.publish.task.GeoServerTasklet;
import org.georchestra.datafeeder.service.batch.publish.task.PostGisTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * Spring-batch configuration for the data publishing workflow.
 *
 */
@Configuration
@ComponentScan
@EnableBatchProcessing
public class DataPublishingConfiguration {

    public static final String JOB_PARAM_ID = "id";
    public static final String JOB_NAME = "publishJob";

    private @Autowired JobBuilderFactory jobBuilderFactory;
    private @Autowired StepBuilderFactory stepBuilderFactory;

    @Bean(name = DataPublishingConfiguration.JOB_NAME)
    public Job publishingJob(//
            // TODO: Do we need an initializer? @Qualifier("publishInitializer") Step
            // initializer, //
            @Qualifier("postGisStep") Step postgis, //
            @Qualifier("geoserverStep") Step geoserver, //
            @Qualifier("geonetworkStep") Step geonetwork, //
            @Qualifier("geoserverGeonetworkUpdateStep") Step geoserverGeonetworkUpdate) {

        return jobBuilderFactory.get(JOB_NAME)//
                .incrementer(new RunIdIncrementer())//
                // TODO: Add Listener!
                // .listener(jobLifeCycleStatusUpdateListener())//
                // steps...
                // TODO: Do we need an initializer? .start(initializer)//
                .start(postgis)//
                .next(geoserver)//
                .next(geonetwork)//
                .next(geoserverGeonetworkUpdate)//
                .build();
    }

    @Bean
    public PostGisTasklet postGisTasklet() {
        return new PostGisTasklet();
    }

    @Bean
    public GeoServerTasklet geoServerTasklet() {
        return new GeoServerTasklet();
    }

    @Bean
    public GeoNetworkTasklet geoNetworkTasklet() {
        return new GeoNetworkTasklet();
    }

    @Bean
    public GeoServerGeoNetworkUpdateTasklet geoServerGeoNetworkUpdateTasklet() {
        return new GeoServerGeoNetworkUpdateTasklet();
    }

    @Bean
    public Step postGisStep() {
        return stepBuilderFactory.get("postGisStep")//
                .tasklet(postGisTasklet())//
                .build();
    }

    @Bean
    public Step geoserverStep() {
        return stepBuilderFactory.get("geoserverStep")//
                .tasklet(geoServerTasklet())//
                .build();
    }

    @Bean
    public Step geonetworkStep() {
        return stepBuilderFactory.get("geonetworkStep")//
                .tasklet(geoNetworkTasklet())//
                .build();
    }

    @Bean
    public Step geoserverGeonetworkUpdateStep() {
        return stepBuilderFactory.get("geoserverGeonetworkUpdateStep")//
                .tasklet(geoServerGeoNetworkUpdateTasklet())//
                .build();
    }
}