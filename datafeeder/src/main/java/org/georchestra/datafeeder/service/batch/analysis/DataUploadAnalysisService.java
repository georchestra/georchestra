package org.georchestra.datafeeder.service.batch.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.FileNameUtils;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.SampleProperty;
import org.georchestra.datafeeder.model.UploadStatus;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.georchestra.datafeeder.repository.DatasetUploadStateRepository;
import org.georchestra.datafeeder.service.DataSourceMetadata;
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

    @Transactional
    public void initialize(@NonNull UUID uploadId) throws IOException {

        log.info("Loading pack {}", uploadId);
        UploadPackage uploadPack = fileStore.find(uploadId);
        log.info("Creating DataUploadState from UploadPackage {}", uploadId);

        DataUploadJob state = jobRepository.findByJobId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("DataUploadState does not exist: " + uploadId));
        state.setStatus(UploadStatus.ANALYZING);
        state.setProgress(0);
        state.getDatasets().clear();
        jobRepository.save(state);

        Set<String> datasetFiles = uploadPack.findDatasetFiles();

        for (String relativePath : datasetFiles) {
            DatasetUploadState dataset = createDatasetState(uploadPack, relativePath);
            dataset.setJob(state);
            state.getDatasets().add(dataset);
        }
        jobRepository.save(state);
    }

    private DatasetUploadState createDatasetState(UploadPackage uploadPack, String relativePath) {
        Path fileName = Paths.get(relativePath);
        Path absolutePath = uploadPack.resolve(relativePath);
        DatasetUploadState dataset = new DatasetUploadState();
        dataset.setFileName(fileName.toString());
        dataset.setAbsolutePath(absolutePath.toString());

        dataset.setStatus(UploadStatus.PENDING);
        String datasetName = fileName.getFileName().toString();
        datasetName = FileNameUtils.getBaseName(datasetName);
        dataset.setName(datasetName);
        return dataset;
    }

    @Transactional
    public DatasetUploadState analyze(DatasetUploadState item) throws Exception {
        item.setStatus(UploadStatus.DONE);
        throw new UnsupportedOperationException();
        // return item;
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

    private List<DatasetUploadState> loadDataset(DatasetUploadState ds) {
        Path fullPath = Paths.get(ds.getAbsolutePath());
        DataSourceMetadata dataSource = datasetsService.describe(fullPath);
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
}
