package org.georchestra.datafeeder.service.batch.analysis;

import java.util.UUID;

import org.georchestra.datafeeder.model.AnalysisStatus;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.georchestra.datafeeder.repository.DatasetUploadStateRepository;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * {@link ItemProcessListener} around the whole analysis process of a single
 * {@link DatasetUploadState} to manage its
 * {@link DatasetUploadState#getStatus() status} life cycle.
 */
@Component
public class DatasetUploadStateUpdateListener implements ItemProcessListener<DatasetUploadState, DatasetUploadState> {

    private @Autowired DataUploadJobRepository jobRepository;
    private @Autowired DatasetUploadStateRepository datasetRepository;

    @Override
    public void beforeProcess(DatasetUploadState item) {
        datasetRepository.setDatasetStatus(item.getId(), AnalysisStatus.ANALYZING);
        item.setStatus(AnalysisStatus.ANALYZING);
        AnalysisStatus status = datasetRepository.findOne(item.getId()).getStatus();
        Assert.state(AnalysisStatus.ANALYZING == status, "Dataset status not updated: " + status);
    }

    @Override
    public void afterProcess(DatasetUploadState item, DatasetUploadState result) {
        save(item, AnalysisStatus.DONE, null);
    }

    @Override
    public void onProcessError(DatasetUploadState item, Exception e) {
        save(item, AnalysisStatus.ERROR, e.getMessage());
    }

    private void save(DatasetUploadState item, AnalysisStatus status, String error) {
        UUID uploadId = item.getJob().getJobId();
        jobRepository.incrementProgress(uploadId);
        datasetRepository.setDatasetStatus(item.getId(), status, error);
        item.setStatus(status);
        item.setError(error);
//        datasetRepository.saveAndFlush(item);

        AnalysisStatus st = datasetRepository.findOne(item.getId()).getStatus();
        DataUploadJob job = jobRepository.findOne(uploadId);
        Assert.state(status == st, "Dataset status not updated, expected " + st + ", got " + status);
    }
}
