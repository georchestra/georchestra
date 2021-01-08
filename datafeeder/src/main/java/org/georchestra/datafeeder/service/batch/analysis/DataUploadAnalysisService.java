package org.georchestra.datafeeder.service.batch.analysis;

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
import org.georchestra.datafeeder.model.SampleProperty;
import org.georchestra.datafeeder.model.UploadStatus;
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

@Service
@Slf4j
public class DataUploadAnalysisService {

    private @Autowired @Setter FileStorageService fileStore;
    private @Autowired DataUploadJobRepository jobRepository;
    private @Autowired DatasetUploadStateRepository datasetRepository;
    private @Autowired DatasetsService datasetsService;

    /**
     * Data upload analysis process step 0: creates a {@link DataUploadJob} with
     * {@link UploadStatus#PENDING PENDING} status for the {@link UploadPackage}
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
        state.setStatus(UploadStatus.PENDING);
        state.setUsername(username);
        DataUploadJob saved = jobRepository.save(state);
        return saved;
    }

    /**
     * Data upload analysis process step 1:
     * <p>
     * The {@link DataUploadJob} for the given id must have already been
     * {@link #createJob created}.
     * <p>
     * Initializes a data upload job, by setting its state to
     * {@link UploadStatus#ANALYZING ANALYZING}, clearing its
     * {@link DataUploadJob#getDatasets() datasets} and setting its progress to
     * zero.
     * <p>
     * Adds a {@link UploadStatus#PENDING PENDING} {@link DatasetUploadState} for
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
    @Transactional
    public void initialize(@NonNull UUID uploadId) throws IOException {
        log.info("Initializing DataUploadState from UploadPackage {}", uploadId);
        final UploadPackage uploadPack = getUploadPack(uploadId);

        DataUploadJob job = jobRepository.findByJobId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("DataUploadState does not exist: " + uploadId));
        job.setStatus(UploadStatus.ANALYZING);
        job.getDatasets().clear();

        Set<String> datasetFiles = uploadPack.findDatasetFiles();

        int errored = 0;
        for (String fileRelativePath : datasetFiles) {
            Path path = uploadPack.resolve(fileRelativePath);
            List<String> typeNames;
            try {
                typeNames = this.datasetsService.getTypeNames(path);
                List<DatasetUploadState> fileDatasets = createPendingDatasets(fileRelativePath, path, typeNames);
                fileDatasets.forEach(d -> d.setJob(job));
                job.getDatasets().addAll(fileDatasets);
            } catch (RuntimeException e) {
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
        Objects.requireNonNull(item.getId(), "item has no id");
        checkStatus(item, UploadStatus.ANALYZING);

        final Path path = Paths.get(item.getAbsolutePath());
        final String typeName = item.getName();

        DatasetMetadata datasetMetadata = datasetsService.describe(path, typeName);

        item.setEncoding(datasetMetadata.getEncoding());

        item.setFeatureCount(datasetMetadata.getFeatureCount());
        item.setNativeBounds(datasetMetadata.getNativeBounds());
        String geometryWKT = Optional.ofNullable(datasetMetadata.getSampleGeometry()).map(Geometry::toText)
                .orElse(null);
        List<SampleProperty> sampleProperties = sampleProperties(datasetMetadata.getSampleProperties());

        item.setSampleGeometryWKT(geometryWKT);
        item.setSampleProperties(sampleProperties);

        return item;
    }

    private void checkStatus(DatasetUploadState item, UploadStatus expected) {
        if (expected != item.getStatus()) {
            throw new IllegalStateException(String.format("Invalid status, expected %s, got %s. Item: %s#%s", expected,
                    item.getStatus(), item.getFileName(), item.getName()));
        }
    }

    @Transactional
    public void save(List<? extends DatasetUploadState> items) {
        this.datasetRepository.save(items);
    }

    @Transactional
    public void summarize(@NonNull UUID uploadId) {
        DataUploadJob state = jobRepository.findByJobId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("DataUploadState does not exist: " + uploadId));
        UploadStatus status = determineJobStatus(state.getDatasets());
        state.setStatus(status);
        if (UploadStatus.ERROR == status) {
            state.setError(buildErrorMessage(state.getDatasets()));
        }
        jobRepository.save(state);
    }

    private String buildErrorMessage(List<DatasetUploadState> datasets) {
        return "Error analyzing the following datasets:\n"
                + datasets.stream().filter(d -> d.getStatus() == UploadStatus.ERROR)
                        .map(d -> d.getName() + ": " + d.getError()).collect(Collectors.joining("\n"));
    }

    private UploadStatus determineJobStatus(List<DatasetUploadState> datasets) {
        boolean anyFailed = datasets.stream().map(DatasetUploadState::getStatus).anyMatch(UploadStatus.ERROR::equals);
        return anyFailed ? UploadStatus.ERROR : UploadStatus.DONE;
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
            dataset.setStatus(UploadStatus.PENDING);
            datasets.add(dataset);
        }
        return datasets;
    }

    private DatasetUploadState createFailedDataset(String fileRelativePath, Path path, RuntimeException e) {
        DatasetUploadState dataset = new DatasetUploadState();
        dataset.setFileName(fileRelativePath);
        dataset.setAbsolutePath(path.toAbsolutePath().toString());
        dataset.setStatus(UploadStatus.ERROR);
        dataset.setError(e.getMessage());
        return dataset;
    }
}
