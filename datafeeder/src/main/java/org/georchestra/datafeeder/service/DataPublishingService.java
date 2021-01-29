/*
 * Copyright (C) 2020, 2021 by the geOrchestra PSC
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
package org.georchestra.datafeeder.service;

import java.util.UUID;

import org.georchestra.datafeeder.api.DataPublishingApiController;
import org.georchestra.datafeeder.service.batch.publish.PublishingBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.NonNull;

/**
 * Service provider for {@link DataPublishingApiController}
 * <p>
 * Acts as a facade for internal processing of business processes required by
 * the controller, which in turn only takes care of the HTTP API layer,
 * delegating all processing to this service.
 */
@Service
public class DataPublishingService {

    private @Autowired PublishingBatchService publishingBatchService;

    @Async
    public void publish(@NonNull UUID uploadId) {
        publishingBatchService.runJob(uploadId);
    }

}
