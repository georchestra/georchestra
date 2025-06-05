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

package org.georchestra.datafeeder.service.publish;

import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.model.UserInfo;

import lombok.NonNull;

public interface OWSPublicationService {

    /**
     * Publishes the given dataset to the OWS Server.
     * <p>
     * Once this method returns, {@link PublishSettings#getPublishedName()
     * dataset.getPublishing().getPublishedName()} may have changed by the service
     * to avoid layer name duplication, and
     * {@link PublishSettings#getPublishedWorkspace()
     * dataset.getPublishing().getPublishedWorkspace()} must not be {@code null}
     */
    void publish(DatasetUploadState dataset, @NonNull UserInfo user);

    /**
     * Updates the published dataset metadata on the OWS service for the published
     * layer given by the {@link DatasetUploadState}'s published
     * {@link PublishSettings#getPublishedWorkspace() workspace name} and
     * {@link PublishSettings#getPublishedName() layer name}, adding a metadata-link
     * pointing to the metadata {@link PublishSettings#getMetadataRecordId() record
     * id}.
     */
    void addMetadataLinks(DatasetUploadState dataset);

}
