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
package org.georchestra.datafeeder.api.mapper;

import org.georchestra.datafeeder.api.AnalysisStatusEnum;
import org.georchestra.datafeeder.api.BoundingBox;
import org.georchestra.datafeeder.api.DatasetUploadStatus;
import org.georchestra.datafeeder.api.UploadJobStatus;
import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.JobStatus;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.Map;

@Mapper(componentModel = "spring", uses = CRSMapper.class, unmappedSourcePolicy = ReportingPolicy.WARN, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FileUploadResponseMapper {

    @Mapping(target = "status", source = "analyzeStatus")
    @Mapping(target = "progress", ignore = true) // set by setJobProgress() @Aftermapping
    UploadJobStatus toApi(DataUploadJob state);

    @Mapping(target = "status", source = "analyzeStatus")
    DatasetUploadStatus toApi(DatasetUploadState dataset);

    BoundingBox toApi(BoundingBoxMetadata bounds);

    default AnalysisStatusEnum toApi(JobStatus jobStatus) {
        if (jobStatus == null) {
            return null;
        }
        switch (jobStatus) {
        case PENDING:
            return AnalysisStatusEnum.PENDING;
        case RUNNING:
            return AnalysisStatusEnum.ANALYZING;
        case DONE:
            return AnalysisStatusEnum.DONE;
        case ERROR:
            return AnalysisStatusEnum.ERROR;
        default:
            throw new IllegalArgumentException("Unexpected enum constant: " + jobStatus);
        }
    }

    /**
     * Analysis job doesn't really do progress reporting, it proved to be too fast
     * to justify it
     */
    default @AfterMapping void setJobProgress(DataUploadJob source, @MappingTarget UploadJobStatus target) {
        switch (source.getAnalyzeStatus()) {
        case DONE:
        case ERROR:
            target.setProgress(1.0);
            break;
        case PENDING:
        case RUNNING:
        default:
            target.setProgress(0.0);
            break;
        }
    }
}
