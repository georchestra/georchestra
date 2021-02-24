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
package org.georchestra.datafeeder.batch.publish;

import java.util.UUID;

import org.georchestra.datafeeder.batch.service.PublishingBatchService;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.JobStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

/**
 * Tasklet to prepare the target data store where to copy Datasets (usually a
 * PostGIS database, that might require creating a postgis database, or schema).
 */
public class PrepareTargetDataStoreTasklet implements Tasklet {

    private @Value("#{jobParameters['uploadId']}") UUID uploadId;
    private @Autowired PublishingBatchService service;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            service.prepareTargetStoreForJobDatasets(uploadId);
        } catch (RuntimeException e) {
            String message = e.getMessage();
            service.setPublishingStatusError(uploadId, message);
            DataUploadJob job = service.findJob(uploadId);
            Assert.isTrue(job.getPublishStatus() == JobStatus.ERROR, "Expected ERROR, got " + job.getPublishStatus());
            throw e;
        }
        return RepeatStatus.FINISHED;
    }

}
