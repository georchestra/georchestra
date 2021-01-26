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

import java.util.UUID;

import org.georchestra.datafeeder.model.AnalysisStatus;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.georchestra.datafeeder.repository.DatasetUploadStateRepository;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link ItemProcessListener} around the whole analysis process of a single
 * {@link DatasetUploadState} to manage its
 * {@link DatasetUploadState#getStatus() status} life cycle.
 */
@Component
public class DatasetUploadStateUpdateListener implements ItemProcessListener<DatasetUploadState, DatasetUploadState> {

    private @Autowired DataUploadJobRepository jobRepository;
    private @Autowired DatasetUploadStateRepository datasetRepository;

    @Override
    public void beforeProcess(DatasetUploadState item) {
        datasetRepository.setDatasetStatus(item.getId(), AnalysisStatus.ANALYZING);
        item.setStatus(AnalysisStatus.ANALYZING);
    }

    @Override
    public void afterProcess(DatasetUploadState item, DatasetUploadState result) {
        UUID uploadId = item.getJob().getJobId();
        jobRepository.incrementProgress(uploadId);
    }

    @Override
    public void onProcessError(DatasetUploadState item, Exception e) {
        UUID uploadId = item.getJob().getJobId();
        jobRepository.incrementProgress(uploadId);
    }
}
