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
package org.georchestra.datafeeder.batch.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.JobStatus;
import org.georchestra.datafeeder.model.SampleProperty;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.georchestra.datafeeder.repository.DatasetUploadStateRepository;
import org.georchestra.datafeeder.service.DatasetMetadata;
import org.georchestra.datafeeder.service.DatasetsService;
import org.georchestra.datafeeder.service.FileStorageService;
import org.georchestra.datafeeder.service.UploadPackage;
import org.geotools.util.Converters;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralized service to perform uploaded dataset analysis, used by
 * spring-batch steps and listeners.
 */
@Service
@Slf4j
public class DataUploadAnalysisService {

    private @Autowired JobManager jobManager;

    private @Autowired @Setter FileStorageService fileStore;
    private @Autowired DataUploadJobRepository jobRepository;
    private @Autowired DatasetUploadStateRepository datasetRepository;
    private @Autowired DatasetsService datasetsService;

    /**
     * Data upload analysis process step 0: creates a {@link DataUploadJob} with
     * {@link JobStatus#PENDING PENDING} status for the {@link UploadPackage} with
     * the given id.
     * 
     * @throws IllegalArgumentException if no {@link UploadPackage} exists for the
     *                                  given {@code jobId}
     * @throws IllegalStateException    if some {@link IOException} happens loading
     *                                  the {@link UploadPackage} from
     *                                  {@link FileStorageService}
     */
    public DataUploadJob createJob(@NonNull UUID jobId, @NonNull String username) {
        log.info("Creating PENDING DataUploadJob for upload package {}", jobId);
        UploadPackage uploadPack = getUploadPack(jobId);
        DataUploadJob state = new DataUploadJob();
        state.setJobId(jobId);
        state.setAnalyzeStatus(JobStatus.PENDING);
        state.setUsername(username);
        DataUploadJob saved = jobRepository.save(state);
        return saved;
    }

    public void runJob(@NonNull UUID jobId, @NonNull UserInfo user) {
        jobManager.launchUploadJobAnalysis(jobId, user);
    }

    /**
     * Data upload analysis process step 1:
     * <p>
     * The {@link DataUploadJob} for the given id must have already been
     * {@link #createJob created}.
     * <p>
     * Initializes a data upload job, by setting its state to
     * {@link JobStatus#ANALYZING ANALYZING}, clearing its
     * {@link DataUploadJob#getDatasets() datasets} and setting its progress to
     * zero.
     * <p>
     * Adds a {@link JobStatus#PENDING PENDING} {@link DatasetUploadState} for each
     * dataset found on each uploaded file. For example, some uploaded files like
     * shapefiles, have a single dataset, but some other, like a geopackage, may
     * contain more than one dataset.
     * 
     * @throws IllegalArgumentException if no {@link UploadPackage} or
     *                                  {@link DataUploadJob} exists for the given
     *                                  {@code jobId}
     * @throws IllegalStateException    if some {@link IOException} happens loading
     *                                  the {@link UploadPackage} from
     *                                  {@link FileStorageService}
     */
    public void initialize(@NonNull UUID uploadId) {
        log.info("Initializing DataUploadState from UploadPackage {}", uploadId);
        final UploadPackage uploadPack = getUploadPack(uploadId);

        DataUploadJob job = jobRepository.findByJobId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("DataUploadState does not exist: " + uploadId));
        job.setAnalyzeStatus(JobStatus.RUNNING);
        job.getDatasets().clear();

        Set<String> datasetFiles;
        try {
            datasetFiles = uploadPack.findDatasetFiles();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        for (String fileRelativePath : datasetFiles) {
            Path path = uploadPack.resolve(fileRelativePath);
            List<String> typeNames;
            try {
                typeNames = this.datasetsService.getTypeNames(path);
                List<DatasetUploadState> fileDatasets = createPendingDatasets(fileRelativePath, path, typeNames);
                fileDatasets.forEach(d -> d.setJob(job));
                job.getDatasets().addAll(fileDatasets);
            } catch (Exception e) {
                DatasetUploadState dataset = createFailedDataset(fileRelativePath, path, e);
                dataset.setJob(job);
                job.getDatasets().add(dataset);
            }
        }
        jobRepository.save(job);
    }

    public DatasetUploadState analyze(DatasetUploadState item) throws Exception {
        log.info("analyzing dataset {}/{}#{}", item.getJob().getJobId(), item.getFileName(), item.getName());
        Objects.requireNonNull(item.getId(), "item has no id");
        checkStatus(item, JobStatus.RUNNING);

        final Path path = Paths.get(item.getAbsolutePath());
        final String typeName = item.getName();
        try {
            DatasetMetadata datasetMetadata = datasetsService.describe(path, typeName);
            item.setAnalyzeStatus(JobStatus.DONE);
            item.setEncoding(datasetMetadata.getEncoding());
            item.setFeatureCount(datasetMetadata.getFeatureCount());
            item.setNativeBounds(datasetMetadata.getNativeBounds());
            String geometryWKT = Optional.ofNullable(datasetMetadata.getSampleGeometry()).map(Geometry::toText)
                    .orElse(null);
            List<SampleProperty> sampleProperties = sampleProperties(datasetMetadata.getSampleProperties());

            item.setSampleGeometryWKT(geometryWKT);
            item.setSampleProperties(sampleProperties);
        } catch (Exception e) {
            item.setAnalyzeStatus(JobStatus.ERROR);
            item.setError(e.getMessage());
        }
        return item;
    }

    private void checkStatus(DatasetUploadState item, JobStatus expected) {
        if (expected != item.getAnalyzeStatus()) {
            throw new IllegalStateException(String.format("Invalid status, expected %s, got %s. Item: %s#%s", expected,
                    item.getAnalyzeStatus(), item.getFileName(), item.getName()));
        }
    }

    public void save(List<? extends DatasetUploadState> items) {
        this.datasetRepository.saveAll(items);
    }

    @Transactional
    public void summarize(@NonNull UUID uploadId) {
        datasetRepository.flush();
        jobRepository.flush();
        DataUploadJob state = jobRepository.findByJobId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("DataUploadState does not exist: " + uploadId));
        JobStatus status = determineJobStatus(state.getDatasets());
        state.setAnalyzeStatus(status);
        if (JobStatus.ERROR == status) {
            state.setError(buildErrorMessage(state.getDatasets()));
        }
        jobRepository.save(state);
    }

    private String buildErrorMessage(List<DatasetUploadState> datasets) {
        return "Error analyzing the following datasets:\n"
                + datasets.stream().filter(d -> d.getAnalyzeStatus() == JobStatus.ERROR)
                        .map(d -> d.getName() + ": " + d.getError()).collect(Collectors.joining("\n"));
    }

    private JobStatus determineJobStatus(List<DatasetUploadState> datasets) {
        for (DatasetUploadState d : datasets) {
            JobStatus status = d.getAnalyzeStatus();
            if (JobStatus.ERROR == status) {
                return JobStatus.ERROR;
            } else if (JobStatus.DONE != status) {
                throw new IllegalStateException("Expected status DONE or ERROR, got " + status);
            }
        }
        return JobStatus.DONE;
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

    private UploadPackage getUploadPack(UUID jobId) {
        try {
            return fileStore.find(jobId);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Upload pack " + jobId + " does not exist");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<DatasetUploadState> createPendingDatasets(String fileRelativePath, Path path, List<String> typeNames) {

        List<DatasetUploadState> datasets = new ArrayList<>();
        for (String typeName : typeNames) {
            DatasetUploadState dataset = new DatasetUploadState();
            dataset.setName(typeName);
            dataset.setFileName(fileRelativePath);
            dataset.setAbsolutePath(path.toAbsolutePath().toString());
            dataset.setAnalyzeStatus(JobStatus.PENDING);
            datasets.add(dataset);
        }
        return datasets;
    }

    private DatasetUploadState createFailedDataset(String fileRelativePath, Path path, Exception e) {
        DatasetUploadState dataset = new DatasetUploadState();
        dataset.setFileName(fileRelativePath);
        dataset.setAbsolutePath(path.toAbsolutePath().toString());
        dataset.setAnalyzeStatus(JobStatus.ERROR);
        dataset.setError(e.getMessage());
        return dataset;
    }
}
