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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.georchestra.datafeeder.model.DataUploadState;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.SampleProperty;
import org.georchestra.datafeeder.model.UploadStatus;
import org.georchestra.datafeeder.repository.DataUploadRepository;
import org.geotools.util.Converters;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataUploadService {

    private @Autowired FileStorageService storageService;
    private @Autowired DatasetsService datasetsService;
    private @Autowired DataUploadRepository repository;

    public DataUploadState createJob(@NonNull UUID jobId, @NonNull String username) {
        DataUploadState state = new DataUploadState();
        state.setJobId(jobId);
        state.setStatus(UploadStatus.PENDING);
        state.setUsername(username);
        DataUploadState saved = repository.save(state);
        return saved;
    }

    public DataUploadState findJob(UUID uploadId) {
        return repository.findByJobId(uploadId).orElseThrow(() -> new NoSuchElementException());
    }

    /**
     * Asynchronously starts the analysis process for the upload pack given by its
     * id, returns immediately with {@link DataUploadState#getStatus() status}
     * {@link UploadStatus#PENDING} and an empty
     * {@link DataUploadState#getDatasets() datasets} list.
     */
    @Async
    public void analyze(@NonNull UUID uploadId) {
        UploadPackage uploadPack = getUploadPack(uploadId);
        repository.setJobStatus(uploadId, UploadStatus.ANALYZING);
        DataUploadState job = this.findJob(uploadId);
        List<DatasetUploadState> datasets = datasets(uploadPack);

        DataUploadState state = new DataUploadState();
        state.setJobId(uploadId);
        state.setStatus(UploadStatus.PENDING);
        // return state;
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
        DataSourceMetadata dataSource = datasetsService.describe(path);
        List<DatasetMetadata> fileDatasets = dataSource.getDatasets();
        List<DatasetUploadState> states = new ArrayList<>(fileDatasets.size());
        for (DatasetMetadata md : fileDatasets) {
            ds.setEncoding(md.getEncoding());
            ds.setName(md.getTypeName());
            ds.setNativeBounds(md.getNativeBounds());
            Optional<Geometry> sampleGeometry = md.sampleGeometry();
            ds.setSampleGeometryWKT(sampleGeometry.map(Geometry::toText).orElse(null));
            ds.setSampleProperties(sampleProperties(md.getSampleProperties()));
            ds.setFeatureCount(md.getFeatureCount());
            ds.setStatus(UploadStatus.ANALYZING);
        }
        return states;
    }

    private List<SampleProperty> sampleProperties(Map<String, Object> sampleProperties) {
        if (sampleProperties != null) {
            return sampleProperties.entrySet().stream().map(this::sampleProperty).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private SampleProperty sampleProperty(Map.Entry<String, Object> e) {
        SampleProperty p = new SampleProperty();
        p.setName(e.getKey());
        Object value = e.getValue();
        if (value != null) {
            String v = Converters.convert(value, String.class);
            p.setValue(v);
            p.setType(value.getClass().getSimpleName());
        }
        return p;
    }

    public List<DataUploadState> findAllJobs() {
        return repository.findAll();
    }

    public List<DataUploadState> findUserJobs(@NonNull String userName) {
        return repository.findAllByUsername(userName);
    }

    public void abortAndRemove(@NonNull UUID jobId) {
        throw new UnsupportedOperationException("unimplemented");
    }

    public void remove(@NonNull UUID jobId) {
        repository.delete(jobId);
    }

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

}
