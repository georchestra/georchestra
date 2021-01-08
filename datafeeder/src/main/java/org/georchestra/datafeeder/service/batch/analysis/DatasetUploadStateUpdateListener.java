package org.georchestra.datafeeder.service.batch.analysis;

import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.UploadStatus;
import org.georchestra.datafeeder.repository.DatasetUploadStateRepository;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link ItemProcessListener} around the whole analysis process of a single
 * {@link DatasetUploadState} to manage its
 * {@link DatasetUploadState#getStatus() status} life cycle.
 */
@Component
public class DatasetUploadStateUpdateListener implements ItemProcessListener<DatasetUploadState, DatasetUploadState> {

    private @Autowired DatasetUploadStateRepository repository;

    @Override
    public void beforeProcess(DatasetUploadState item) {
        repository.setDatasetStatus(item.getId(), UploadStatus.ANALYZING);
        item.setStatus(UploadStatus.ANALYZING);
    }

    @Override
    public void afterProcess(DatasetUploadState item, DatasetUploadState result) {
        result.setStatus(UploadStatus.DONE);
        repository.save(result);
    }

    @Override
    public void onProcessError(DatasetUploadState item, Exception e) {
        item.setStatus(UploadStatus.ERROR);
        item.setError(e.getMessage());
        repository.save(item);
    }

}
