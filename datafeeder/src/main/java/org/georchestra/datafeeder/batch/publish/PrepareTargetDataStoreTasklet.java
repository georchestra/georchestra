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
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.JobStatus;
import org.georchestra.datafeeder.model.UserInfo;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import com.google.common.base.Throwables;

import lombok.extern.slf4j.Slf4j;

/**
 * Tasklet to prepare the target data store where to copy Datasets (usually a
 * PostGIS database, that might require creating a postgis database, or schema).
 */
@Slf4j
public class PrepareTargetDataStoreTasklet implements Tasklet {

    private @Autowired PublishingBatchService service;
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
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            service.step0_prepareTargetStoreForJobDatasets(uploadId, user);
        } catch (RuntimeException e) {
            log.error("Error preparing target store", e);
            String message = e.getMessage();
            if (message == null)
                message = Throwables.getRootCause(e).getMessage();
            if (message == null)
                message = "Unknown reason";
            service.setPublishingStatusError(uploadId, message);
            DataUploadJob job = service.findJob(uploadId);
            Assert.isTrue(job.getPublishStatus() == JobStatus.ERROR, "Expected ERROR, got " + job.getPublishStatus());
            throw e;
        }
        return RepeatStatus.FINISHED;
    }

}
