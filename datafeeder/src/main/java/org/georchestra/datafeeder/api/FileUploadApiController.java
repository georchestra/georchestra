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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.georchestra.datafeeder.model.DataUploadState;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import lombok.NonNull;

public @Controller class FileUploadApiController implements FileUploadApi {

    private @Autowired NativeWebRequest currentRequest;
    private @Autowired FileStorageService storageService;
    private @Autowired DataUploadService uploadService;
    private @Autowired ApiResponseMapper mapper;

    public @Override Optional<NativeWebRequest> getRequest() {
        return Optional.of(currentRequest);
    }

    @Override
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<UploadJobStatus> findUploadJob(@PathVariable("jobId") UUID uploadId) {
        DataUploadState state = uploadService.findJobState(uploadId);
        UploadJobStatus response = mapper.toApi(state);
        return ResponseEntity.ok(response);

    }

    @Override
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<UploadJobStatus> uploadFiles(@RequestPart(value = "filename") List<MultipartFile> files) {
        UUID uploadId;
        try {
            uploadId = storageService.saveUploads(files);
            uploadService.analyze(uploadId);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);// TODO translate to ResponseStatusException
        }
        DataUploadState state = uploadService.findJobState(uploadId);
        UploadJobStatus response = mapper.toApi(state);
        return ResponseEntity.ok(response);
    }

    @Override
    @RolesAllowed("ROLE_ADMINISTRATOR")
    public ResponseEntity<List<UploadJobStatus>> findAllUploadJobs() {
        List<DataUploadState> all = this.uploadService.findAllJobs();
        List<UploadJobStatus> response = all.stream().map(mapper::toApi).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Override
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<List<UploadJobStatus>> findUserUploadJobs() {
        String userName = getUserName();
        List<DataUploadState> all = this.uploadService.findUserJobs(userName);
        List<UploadJobStatus> response = all.stream().map(mapper::toApi).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Override
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<Void> removeJob(//
            @PathVariable("jobId") UUID jobId, //
            @RequestParam(value = "abort", required = false, defaultValue = "false") Boolean abort) {

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private @NonNull String getUserName() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = context.getAuthentication();
        String userName = auth.getName();
        return userName;
    }
}
