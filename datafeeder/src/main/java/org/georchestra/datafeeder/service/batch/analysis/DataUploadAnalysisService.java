package org.georchestra.datafeeder.service.batch.analysis;

import static org.georchestra.datafeeder.service.batch.analysis.UploadAnalysisConfiguration.JOB_NAME;
import static org.georchestra.datafeeder.service.batch.analysis.UploadAnalysisConfiguration.UPLOAD_ID_JOB_PARAM_NAME;

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

import org.georchestra.datafeeder.model.AnalysisStatus;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.SampleProperty;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.georchestra.datafeeder.repository.DatasetUploadStateRepository;
import org.georchestra.datafeeder.service.DatasetMetadata;
import org.georchestra.datafeeder.service.DatasetsService;
import org.georchestra.datafeeder.service.FileStorageService;
import org.georchestra.datafeeder.service.UploadPackage;
import org.geotools.util.Converters;
import org.locationtech.jts.geom.Geometry;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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

    private @Autowired @Setter FileStorageService fileStore;
    private @Autowired DataUploadJobRepository jobRepository;
    private @Autowired DatasetUploadStateRepository datasetRepository;
    private @Autowired DatasetsService datasetsService;

    private @Autowired JobLauncher jobLauncher;
    private @Autowired @Qualifier(JOB_NAME) Job uploadAnalysisJob;

    /**
     * Data upload analysis process step 0: creates a {@link DataUploadJob} with
     * {@link AnalysisStatus#PENDING PENDING} status for the {@link UploadPackage}
     * with the given id.
     * 
     * @throws IllegalArgumentException if no {@link UploadPackage} exists for the
     *                                  given {@code jobId}
     * @throws IllegalStateException    if some {@link IOException} happens loading
     *                                  the {@link UploadPackage} from
     *                                  {@link FileStorageService}
     */
    public DataUploadJob createJob(@NonNull UUID jobId, @NonNull String username) {
        log.info("Creating DataUploadState from UploadPackage {}", jobId);
        getUploadPack(jobId);
        DataUploadJob state = new DataUploadJob();
        state.setJobId(jobId);
        state.setStatus(AnalysisStatus.PENDING);
        state.setUsername(username);
        DataUploadJob saved = jobRepository.save(state);
        return saved;
    }

    public void runJob(@NonNull UUID jobId) {
        final String paramName = UPLOAD_ID_JOB_PARAM_NAME;
        final String paramValue = jobId.toString();
        final boolean identifying = true;

        final JobParameters params = new JobParametersBuilder()//
                .addString(paramName, paramValue, identifying)//
                .toJobParameters();
        log.info("Launching analisys job {}", jobId);
        JobExecution execution;
        try {
            execution = jobLauncher.run(uploadAnalysisJob, params);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException e) {
            log.error("Error running job {}", jobId, e);
            throw new RuntimeException("Error running job " + jobId, e);
        }
        log.info("Analysis job {} finished with status {}", jobId, execution.getStatus());
    }

    /**
     * Data upload analysis process step 1:
     * <p>
     * The {@link DataUploadJob} for the given id must have already been
     * {@link #createJob created}.
     * <p>
     * Initializes a data upload job, by setting its state to
     * {@link AnalysisStatus#ANALYZING ANALYZING}, clearing its
     * {@link DataUploadJob#getDatasets() datasets} and setting its progress to
     * zero.
     * <p>
     * Adds a {@link AnalysisStatus#PENDING PENDING} {@link DatasetUploadState} for
     * each dataset found on each uploaded file. For example, some uploaded files
     * like shapefiles, have a single dataset, but some other, like a geopackage,
     * may contain more than one dataset.
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
        job.setStatus(AnalysisStatus.ANALYZING);
        job.getDatasets().clear();

        Set<String> datasetFiles;
        try {
            datasetFiles = uploadPack.findDatasetFiles();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        int errored = 0;
        for (String fileRelativePath : datasetFiles) {
            Path path = uploadPack.resolve(fileRelativePath);
            List<String> typeNames;
            try {
                typeNames = this.datasetsService.getTypeNames(path);
                List<DatasetUploadState> fileDatasets = createPendingDatasets(fileRelativePath, path, typeNames);
                fileDatasets.forEach(d -> d.setJob(job));
                job.getDatasets().addAll(fileDatasets);
            } catch (Exception e) {
                errored++;
                DatasetUploadState dataset = createFailedDataset(fileRelativePath, path, e);
                dataset.setJob(job);
                job.getDatasets().add(dataset);
            }
        }
        int totalSteps = job.getDatasets().size() - errored;
        job.setTotalSteps(totalSteps);
        job.setFinishedSteps(0);
        jobRepository.save(job);
    }

    public DatasetUploadState analyze(DatasetUploadState item) throws Exception {
        log.info("analyzing dataset {}/{}#{}", item.getJob().getJobId(), item.getFileName(), item.getName());
        Objects.requireNonNull(item.getId(), "item has no id");
        checkStatus(item, AnalysisStatus.ANALYZING);

        final Path path = Paths.get(item.getAbsolutePath());
        final String typeName = item.getName();
        try {
            DatasetMetadata datasetMetadata = datasetsService.describe(path, typeName);
            item.setStatus(AnalysisStatus.DONE);
            item.setEncoding(datasetMetadata.getEncoding());
            item.setFeatureCount(datasetMetadata.getFeatureCount());
            item.setNativeBounds(datasetMetadata.getNativeBounds());
            String geometryWKT = Optional.ofNullable(datasetMetadata.getSampleGeometry()).map(Geometry::toText)
                    .orElse(null);
            List<SampleProperty> sampleProperties = sampleProperties(datasetMetadata.getSampleProperties());

            item.setSampleGeometryWKT(geometryWKT);
            item.setSampleProperties(sampleProperties);
        } catch (Exception e) {
            item.setStatus(AnalysisStatus.ERROR);
            item.setError(e.getMessage());
        }
        return item;
    }

    private void checkStatus(DatasetUploadState item, AnalysisStatus expected) {
        if (expected != item.getStatus()) {
            throw new IllegalStateException(String.format("Invalid status, expected %s, got %s. Item: %s#%s", expected,
                    item.getStatus(), item.getFileName(), item.getName()));
        }
    }

    public void save(List<? extends DatasetUploadState> items) {
        this.datasetRepository.save(items);
    }

    public void summarize(@NonNull UUID uploadId) {
        datasetRepository.flush();
        jobRepository.flush();
        DataUploadJob state = jobRepository.findByJobId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("DataUploadState does not exist: " + uploadId));
        AnalysisStatus status = determineJobStatus(state.getDatasets());
        state.setStatus(status);
        if (AnalysisStatus.ERROR == status) {
            state.setError(buildErrorMessage(state.getDatasets()));
        }
        jobRepository.save(state);
    }

    private String buildErrorMessage(List<DatasetUploadState> datasets) {
        return "Error analyzing the following datasets:\n"
                + datasets.stream().filter(d -> d.getStatus() == AnalysisStatus.ERROR)
                        .map(d -> d.getName() + ": " + d.getError()).collect(Collectors.joining("\n"));
    }

    private AnalysisStatus determineJobStatus(List<DatasetUploadState> datasets) {
        for (DatasetUploadState s : datasets) {
            DatasetUploadState d = this.datasetRepository.findOne(s.getId());
            AnalysisStatus status = d.getStatus();
            if (AnalysisStatus.ERROR == status) {
                return AnalysisStatus.ERROR;
            } else if (AnalysisStatus.DONE != status) {
                throw new IllegalStateException("Expected status DONE or ERROR, got " + status);
            }
        }
        return AnalysisStatus.DONE;
//        boolean anyFailed = datasets.stream().map(DatasetUploadState::getStatus).anyMatch(UploadStatus.ERROR::equals);
//        return anyFailed ? UploadStatus.ERROR : UploadStatus.DONE;
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
            UploadPackage uploadPack = fileStore.find(jobId);
            log.info("Creating PENDING DataUploadJob for upload package {}", uploadPack.getId());
            return uploadPack;
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
            dataset.setStatus(AnalysisStatus.PENDING);
            datasets.add(dataset);
        }
        return datasets;
    }

    private DatasetUploadState createFailedDataset(String fileRelativePath, Path path, Exception e) {
        DatasetUploadState dataset = new DatasetUploadState();
        dataset.setFileName(fileRelativePath);
        dataset.setAbsolutePath(path.toAbsolutePath().toString());
        dataset.setStatus(AnalysisStatus.ERROR);
        dataset.setError(e.getMessage());
        return dataset;
    }
}
