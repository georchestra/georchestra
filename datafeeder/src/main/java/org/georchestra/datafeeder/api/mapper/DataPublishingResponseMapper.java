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

import org.georchestra.datafeeder.api.DatasetPublishingStatus;
import org.georchestra.datafeeder.api.PublishJobStatus;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CRSMapper.class)
public interface DataPublishingResponseMapper {

    @Mapping(target = "status", source = "publishStatus")
    PublishJobStatus toApi(DataUploadJob upload);

    @Mapping(target = "status", source = "publishStatus")
    @Mapping(target = "nativeName", source = "name")
    @Mapping(target = "publishedName", source = "publishing.publishedName")
    @Mapping(target = "title", source = "publishing.title")
    DatasetPublishingStatus toApi(DatasetUploadState upload);
}
