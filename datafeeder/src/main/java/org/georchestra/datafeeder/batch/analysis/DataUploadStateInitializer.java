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
package org.georchestra.datafeeder.batch.analysis;

import java.util.Objects;
import java.util.UUID;

import org.georchestra.datafeeder.batch.service.DataUploadAnalysisService;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.JobStatus;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.georchestra.datafeeder.service.FileStorageService;
import org.georchestra.datafeeder.service.UploadPackage;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Setter;

/**
 * {@link Tasklet} that prepares the initial state of a {@link DataUploadJob},
 * with one {@link DatasetUploadState} per uploaded dataset in the upload job,
 * and saves it to the {@link DataUploadJobRepository}
 * <p>
 * It takes the upload job id from the {@link JobParameters} and asks the
 * {@link FileStorageService} for the corresponding {@link UploadPackage},
 * creates one {@link JobStatus#PENDING PENDING} state
 * {@link DatasetUploadState} for each dataset, and saves the
 * {@link DataUploadJob}.
 *
 * @see DataUploadAnalysisService#initialize
 */
@Component
@StepScope
public class DataUploadStateInitializer implements Tasklet {

    private @Value("#{jobParameters['uploadId']}") UUID uploadId;
    private @Autowired @Setter DataUploadAnalysisService service;

    public @Override RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Objects.requireNonNull(uploadId,
                () -> String.format("Job parameter not provided: " + UploadAnalysisJobConfiguration.JOB_PARAM_NAME));
        service.initialize(uploadId);
        return RepeatStatus.FINISHED;
    }
}
