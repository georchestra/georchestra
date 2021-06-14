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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import org.georchestra.datafeeder.api.mapper.FileUploadResponseMapper;
import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.service.FileStorageService;
import org.georchestra.datafeeder.service.UploadPackage;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Controller
@Api(tags = { "File Upload" }) // hides the empty file-upload-api-controller entry in swagger-ui.html
@RolesAllowed({ "ROLE_USER", "ROLE_ADMINISTRATOR" })
public class FileUploadApiController implements FileUploadApi {

    private @Autowired FileStorageService storageService;
    private @Autowired DataUploadService uploadService;
    private @Autowired FileUploadResponseMapper mapper;
    private @Autowired AuthorizationService validityService;

    @Override
    public ResponseEntity<UploadJobStatus> uploadFiles(@RequestPart(value = "filename") List<MultipartFile> files) {
        UUID uploadId;
        DataUploadJob state;
        if (files.isEmpty()) {
            throw ApiException.badRequest("No files provided in multi-part item 'filename'");
        }
        try {
            String username = validityService.getUserName();
            UploadPackage uploadPack = storageService.createPackageFromUpload(files);
            uploadId = uploadPack.getId();
            state = uploadService.createJob(uploadId, username);
            UserInfo user = validityService.getUserInfo();
            uploadService.analyze(uploadId, user);
        } catch (IOException e) {
            throw new RuntimeException(e);// TODO translate to ResponseStatusException
        }
        UploadJobStatus response = mapper.toApi(state);
        response.add(linkTo(methodOn(FileUploadApiController.class).findUploadJob(uploadId)).withSelfRel());
        return ResponseEntity.accepted().body(response);
    }

    @Override
    public ResponseEntity<UploadJobStatus> findUploadJob(@PathVariable("jobId") UUID uploadId) {
        validityService.checkAccessRights(uploadId);
        DataUploadJob state = this.uploadService.findJob(uploadId)
                .orElseThrow(() -> ApiException.notFound("upload %s does not exist", uploadId));
        UploadJobStatus response = mapper.toApi(state);
        response.add(linkTo(methodOn(FileUploadApiController.class).findUploadJob(uploadId)).withSelfRel());
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/upload/{jobId}/{typeName}/sampleFeature", produces = {
            "application/geo+json;charset=UTF-8" }, method = RequestMethod.GET)
    @Override
    public ResponseEntity<Object> getSampleFeature(@PathVariable("jobId") UUID jobId,
            @PathVariable("typeName") String typeName,
            @RequestParam(value = "featureIndex", required = false) Integer featureIndex,
            @RequestParam(value = "encoding", required = false) String encoding,
            @RequestParam(value = "srs", required = false) String srs,
            @RequestParam(value = "srsOverride", required = false) String srsOverride) {

        validityService.checkAccessRights(jobId);

        Charset charset = encoding == null ? null : Charset.forName(encoding);

        if (featureIndex == null)
            featureIndex = 0;

        SimpleFeature feature;
        try {
            feature = uploadService.sampleFeature(jobId, typeName, featureIndex, charset, srs, srsOverride);
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest(e.getMessage());
        } catch (IOException e) {
            throw ApiException.internalServerError(e, e.getMessage());
        }
        FeatureJSON encoder = new FeatureJSON();
        encoder.setEncodeFeatureBounds(true);
        encoder.setEncodeFeatureCRS(true);
        StringWriter writer = new StringWriter();
        try {
            encoder.writeFeature(feature, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Object body = writer.toString();
        return ResponseEntity.ok(body);

    }

    @Override
    public ResponseEntity<BoundingBox> getBounds(@PathVariable("jobId") UUID jobId,
            @PathVariable("typeName") String typeName, @RequestParam(value = "srs", required = false) String srs,
            @RequestParam(value = "srsOverride", required = false) String srsOverride) {

        validityService.checkAccessRights(jobId);

        BoundingBoxMetadata bounds;
        try {
            bounds = this.uploadService.computeBounds(jobId, typeName, srs, srsOverride);
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest(e.getMessage());
        } catch (IOException e) {
            throw ApiException.internalServerError(e, e.getMessage());
        }
        BoundingBox bbox = mapper.toApi(bounds);
        return ResponseEntity.ok(bbox);
    }

    @Override
    @RolesAllowed("ROLE_ADMINISTRATOR")
    public ResponseEntity<List<UploadJobStatus>> findAllUploadJobs() {
        List<DataUploadJob> all = this.uploadService.findAllJobs();
        List<UploadJobStatus> response = all.stream().map(mapper::toApi).collect(Collectors.toList());
        response.forEach(job -> {
            job.add(linkTo(methodOn(FileUploadApiController.class).findUploadJob(job.getJobId())).withSelfRel());
        });
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<UploadJobStatus>> findUserUploadJobs() {
        String userName = validityService.getUserName();
        List<DataUploadJob> all = this.uploadService.findUserJobs(userName);
        List<UploadJobStatus> response = all.stream().map(mapper::toApi).collect(Collectors.toList());
        response.forEach(job -> {
            job.add(linkTo(methodOn(FileUploadApiController.class).findUploadJob(job.getJobId())).withSelfRel());
        });
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> removeJob(//
            @PathVariable("jobId") UUID jobId, //
            @RequestParam(value = "abort", required = false, defaultValue = "false") Boolean abort) {

        validityService.checkAccessRights(jobId);
        if (Boolean.TRUE.equals(abort)) {
            this.uploadService.abortAndRemove(jobId);
        } else {
            this.uploadService.remove(jobId);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
