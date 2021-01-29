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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.service.FileStorageService;
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

@Controller
@Api(tags = { "File Upload" }) // hides the empty file-upload-api-controller entry in swagger-ui.html
@RolesAllowed({ "ROLE_USER", "ROLE_ADMINISTRATOR" })
public class FileUploadApiController implements FileUploadApi {

    private @Autowired FileStorageService storageService;
    private @Autowired DataUploadService uploadService;
    private @Autowired ApiResponseMapper mapper;
    private @Autowired AuthorizationService validityService;

    @Override
    public ResponseEntity<UploadJobStatus> uploadFiles(@RequestPart(value = "filename") List<MultipartFile> files) {
        UUID uploadId;
        DataUploadJob state;
        final String userName = validityService.getUserName();
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
        return ResponseEntity.accepted().body(response);
    }

    @Override
    public ResponseEntity<UploadJobStatus> findUploadJob(@PathVariable("jobId") UUID uploadId) {
        validityService.checkAccessRights(uploadId);
        DataUploadJob state = this.uploadService.findJob(uploadId)
                .orElseThrow(() -> ApiException.notFound("upload %s does not exist", uploadId));
        UploadJobStatus response = mapper.toApi(state);
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
            @RequestParam(value = "srs_reproject", required = false, defaultValue = "false") Boolean srsReproject) {

        validityService.checkAccessRights(jobId);

        Charset charset = encoding == null ? null : Charset.forName(encoding);

        if (featureIndex == null)
            featureIndex = 0;
        if (srsReproject == null)
            srsReproject = false;

        SimpleFeature feature;
        try {
            feature = uploadService.sampleFeature(jobId, typeName, featureIndex, charset, srs, srsReproject);
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest(e.getMessage());
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
            @RequestParam(value = "srs_reproject", required = false, defaultValue = "false") Boolean srsReproject) {

        validityService.checkAccessRights(jobId);

        boolean reproject = srsReproject == null ? false : srsReproject.booleanValue();
        BoundingBoxMetadata bounds = this.uploadService.computeBounds(jobId, typeName, srs, reproject);
        BoundingBox bbox = mapper.toApi(bounds);
        return ResponseEntity.ok(bbox);
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
        String userName = validityService.getUserName();
        List<DataUploadJob> all = this.uploadService.findUserJobs(userName);
        List<UploadJobStatus> response = all.stream().map(mapper::toApi).collect(Collectors.toList());
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
