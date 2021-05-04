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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.georchestra.datafeeder.api.DatasetPublishingStatus;
import org.georchestra.datafeeder.api.PublishJobStatus;
import org.georchestra.datafeeder.api.PublishStepEnum;
import org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker;
import org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.DatasetPublishingStep;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = CRSMapper.class, unmappedSourcePolicy = ReportingPolicy.WARN, unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class DataPublishingResponseMapper {

    private @Autowired PublishJobProgressTracker progressTracker;

    @Mapping(target = "status", source = "publishStatus")
    @Mapping(target = "progress", ignore = true) // set by setJobProgress() @Aftermapping
    public abstract PublishJobStatus toApi(DataUploadJob upload);

    @Mapping(target = "status", source = "publishStatus")
    @Mapping(target = "nativeName", source = "name")
    @Mapping(target = "publishedName", source = "publishing.publishedName")
    @Mapping(target = "publishedWorkspace", source = "publishing.publishedWorkspace")
    @Mapping(target = "metadataRecordId", source = "publishing.metadataRecordId")
    @Mapping(target = "title", source = "publishing.title")
    @Mapping(target = "publish", source = "publishing.publish")
    @Mapping(target = "progress", ignore = true) // set by setDatasetProgress() @Aftermapping
    @Mapping(target = "progressStep", ignore = true) // set by setDatasetProgress() @Aftermapping
    public abstract DatasetPublishingStatus toApi(DatasetUploadState upload);

    public @AfterMapping void setJobProgress(DataUploadJob source, @MappingTarget PublishJobStatus target) {
        double progress = progressTracker.getProgress(source);
        target.setProgress(progress);

        List<DatasetPublishingStatus> datasets = target.getDatasets();
        Collections.sort(datasets, (d1, d2) -> {
            int c = Comparator.comparing(DatasetPublishingStatus::getProgressStep).reversed().compare(d1, d2);
            if (c != 0) {
                return c;
            }
            String pn1 = d1.getPublishedName() == null ? d1.getNativeName() : d1.getPublishedName();
            String pn2 = d2.getPublishedName() == null ? d2.getNativeName() : d2.getPublishedName();
            return pn1.compareTo(pn2);
        });
    }

    abstract PublishStepEnum toApi(DatasetPublishingStep publishStep);

    public @AfterMapping void setDatasetProgress(DatasetUploadState source,
            @MappingTarget DatasetPublishingStatus target) {
        double progress = progressTracker.getProgress(source);
        DatasetPublishingStep currentStep = progressTracker.getCurrentStep(source);
        target.setProgress(progress);
        target.setProgressStep(toApi(currentStep));
    }
}
