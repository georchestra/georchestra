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
package org.georchestra.datafeeder.api;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.service.DataPublishingService;
import org.georchestra.datafeeder.service.DataUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.Api;

@Controller
@RolesAllowed({ "ROLE_USER", "ROLE_ADMINISTRATOR" })
@Api(tags = { "Data Publishing" }) // hides the empty data-publishing-api-controller entry in swagger-ui.html
public class DataPublishingApiController implements DataPublishingApi {

    private @Autowired AuthorizationService authorizationService;
    private @Autowired DataPublishingService dataPublishingService;
    private @Autowired DataUploadService uploadService;
    private @Autowired DataPublishingResponseMapper mapper;

    @Override
    public ResponseEntity<PublishJobStatus> getPublishingStatus(@PathVariable("jobId") UUID jobId) {
        authorizationService.checkAccessRights(jobId);

        PublishJobStatus status = mapper.toApi(getOrNotFound(jobId));

        return ResponseEntity.ok().body(status);
    }

    private DataUploadJob getOrNotFound(UUID jobId) {
        DataUploadJob upload = this.uploadService.findJob(jobId)
                .orElseThrow(() -> ApiException.notFound("upload %s does not exist", jobId));
        return upload;
    }

    @Override
    public ResponseEntity<PublishJobStatus> publish(@PathVariable("jobId") UUID jobId,
            @RequestBody(required = false) PublishRequest publishRequest) {

        authorizationService.checkAccessRights(jobId);

        // launch the async job
        dataPublishingService.publish(jobId, publishRequest);

        DataUploadJob upload = getOrNotFound(jobId);
        PublishJobStatus status = mapper.toApi(upload);

        return ResponseEntity.accepted().body(status);
    }

}
