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
package org.georchestra.datafeeder.api;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import lombok.NonNull;

@Controller
@Api(tags = { "File Upload" }) // hides the empty file-upload-api-controller entry in swagger-ui.html
@RolesAllowed({ "ROLE_USER", "ROLE_ADMINISTRATOR" })
public class FileUploadApiController implements FileUploadApi {

    private @Autowired FileStorageService storageService;
    private @Autowired DataUploadService uploadService;
    private @Autowired ApiResponseMapper mapper;

    @Override
    public ResponseEntity<UploadJobStatus> uploadFiles(@RequestPart(value = "filename") List<MultipartFile> files) {
        UUID uploadId;
        DataUploadJob state;
        final String userName = getUserName();
        if (files.isEmpty()) {
            throw ApiException.badRequest("No files provided in multi-part item 'filename'");
        }
        try {
            uploadId = storageService.saveUploads(files);
            state = uploadService.createJob(uploadId, userName);
            uploadService.analyze(uploadId);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);// TODO translate to ResponseStatusException
        }
        UploadJobStatus response = mapper.toApi(state);
        return ResponseEntity.ok().body(response);
    }

    @Override
    public ResponseEntity<Void> analyze(@PathVariable("jobId") UUID uploadId) {
        DataUploadJob state = getAndCheckAccessRights(uploadId);
        uploadService.analyze(state.getJobId());
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<UploadJobStatus> findUploadJob(@PathVariable("jobId") UUID uploadId) {
        DataUploadJob state = getAndCheckAccessRights(uploadId);
        UploadJobStatus response = mapper.toApi(state);
        return ResponseEntity.ok(response);
    }

    @Override
    @RolesAllowed("ROLE_ADMINISTRATOR")
    public ResponseEntity<List<UploadJobStatus>> findAllUploadJobs() {
        List<DataUploadJob> all = this.uploadService.findAllJobs();
        List<UploadJobStatus> response = all.stream().map(mapper::toApi).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<UploadJobStatus>> findUserUploadJobs() {
        String userName = getUserName();
        List<DataUploadJob> all = this.uploadService.findUserJobs(userName);
        List<UploadJobStatus> response = all.stream().map(mapper::toApi).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> removeJob(//
            @PathVariable("jobId") UUID jobId, //
            @RequestParam(value = "abort", required = false, defaultValue = "false") Boolean abort) {

        if (Boolean.TRUE.equals(abort)) {
            this.uploadService.abortAndRemove(jobId);
        } else {
            this.uploadService.remove(jobId);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private @NonNull String getUserName() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        String userName = auth.getName();
        return userName;
    }

    private @NonNull boolean isAdministrator() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMINISTRATOR"::equals);
    }

    private DataUploadJob getOrNotFound(UUID uploadId) {
        DataUploadJob state = this.uploadService.findJob(uploadId)
                .orElseThrow(() -> ApiException.notFound("upload %s does not exist", uploadId));
        return state;
    }

    private DataUploadJob getAndCheckAccessRights(UUID uploadId) {
        DataUploadJob state = getOrNotFound(uploadId);
        final String userName = getUserName();
        if (!userName.equals(state.getUsername()) && !isAdministrator()) {
            throw ApiException.forbidden("User %s has no access rights to this upload", userName);
        }
        return state;
    }
}
