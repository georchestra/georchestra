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
package org.georchestra.datafeeder.service.batch;

import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

/**
 * File upload job:
 * <p>
 * upload -> save -> unpack -> analyze
 *
 */
@Configuration
//@EnableBatchProcessing
public class DatafeederBatchConfiguration {

//    @Autowired
//    JobBuilderFactory jobBuilderFactory;
//
//    @Autowired
//    StepBuilderFactory stepBuilderFactory;

//    @Bean
//    public Job uploadAndAnalyzeDatasetJob(JobCompletionNotificationListener listener, Step step1) {
//        return jobBuilderFactory//
//                .get("uploadAndAnalyzeDatasetJob")//
//                .incrementer(new RunIdIncrementer())//
//                .listener(listener)//
//                .flow(step1)//
//                .end().build();
//    }
//
//    @Bean
//    public Step uploadFile(ItemWriter<MultipartFile> writer) {
//        return stepBuilderFactory//
//                .get("uploadFile")//
//                .reader(reader()).processor(processor()).writer(writer).build();
//    }

    private ItemReader<MultipartFile> reader() {
        // TODO Auto-generated method stub
        return null;
    }
}