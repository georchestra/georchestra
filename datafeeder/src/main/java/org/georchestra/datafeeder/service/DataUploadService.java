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
package org.georchestra.datafeeder.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.georchestra.datafeeder.model.DataUploadState;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.UploadStatus;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataUploadService {

    private @Autowired FileStorageService storageService;
    private @Autowired DatasetsService datasetsService;

    private UploadPackage getUploadPack(UUID uploadId) {
        UploadPackage pack;
        try {
            pack = storageService.find(uploadId);
        } catch (FileNotFoundException fnf) {
            throw new IllegalArgumentException("upload pack does not exist: " + uploadId);
        } catch (IOException e) {
            throw new RuntimeException(e);// TODO better exception handling
        }
        return pack;
    }

    public void analyze(UUID uploadId) {
        getUploadPack(uploadId);
        log.warn("TODO: implement analyze, job " + uploadId);
    }

    public DataUploadState findJobState(UUID uploadId) {
        UploadPackage pack = getUploadPack(uploadId);
        DataUploadState stub = new DataUploadState();
        stub.setJobId(uploadId);
        stub.setDatasets(datasets(pack));
        stub.setProgress(0.5);
        stub.setStatus(UploadStatus.ANALYZING);
        return stub;
    }

    private List<DatasetUploadState> datasets(UploadPackage pack) {
        Set<String> datasetFileNames;
        try {
            datasetFileNames = pack.findDatasets();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<DatasetUploadState> states = new ArrayList<>();
        for (String fileName : datasetFileNames) {
            List<DatasetUploadState> ds = loadDataset(pack, fileName);
            states.addAll(ds);
        }
        return states;
    }

    private List<DatasetUploadState> loadDataset(UploadPackage pack, String fileName) {
        Path path = pack.resolve(fileName);
        DatasetUploadState ds = new DatasetUploadState();
        List<DatasetMetadata> fileDatasets = datasetsService.describe(path);
        List<DatasetUploadState> states = new ArrayList<>(fileDatasets.size());
        for (DatasetMetadata md : fileDatasets) {
            ds.setEncoding(md.getEncoding());
            ds.setName(md.getTypeName());
            ds.setNativeBounds(md.getNativeBounds());
            Optional<Geometry> sampleGeometry = md.sampleGeometry();
            ds.setSampleGeometryWKT(sampleGeometry.map(Geometry::toText).orElse(null));
            ds.setSampleProperties(md.getSampleProperties());
            ds.setStatus(UploadStatus.ANALYZING);
        }
        return states;
    }

    public List<DataUploadState> findAllJobs() {
        throw new UnsupportedOperationException("unimplemented");
    }

    public List<DataUploadState> findUserJobs(String userName) {
        throw new UnsupportedOperationException("unimplemented");
    }

    public void abortAndRemove(UUID jobId) {
        throw new UnsupportedOperationException("unimplemented");
    }

    public void remove(UUID jobId) {
        throw new UnsupportedOperationException("unimplemented");
    }
}
