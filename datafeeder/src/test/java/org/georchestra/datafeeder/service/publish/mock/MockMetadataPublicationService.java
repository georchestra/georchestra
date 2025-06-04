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

package org.georchestra.datafeeder.service.publish.mock;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.service.publish.MetadataPublicationService;
import org.springframework.http.MediaType;

import lombok.NonNull;

public class MockMetadataPublicationService implements MetadataPublicationService {

    @Override
    public void publish(DatasetUploadState dataset, @NonNull UserInfo user) {
        PublishSettings publishState = dataset.getPublishing();
        Objects.requireNonNull(publishState);
        publishState.setMetadataRecordId(UUID.randomUUID().toString());
    }

    @Override
    public Optional<URI> buildMetadataRecordURL(@NonNull String recordId, MediaType contentType) {
        return Optional.of(URI.create("https://mock.csw.org/?id=" + recordId + "&type=" + contentType.getType()));
    }

}
