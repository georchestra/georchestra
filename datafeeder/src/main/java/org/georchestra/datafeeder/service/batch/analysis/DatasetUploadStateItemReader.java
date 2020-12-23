package org.georchestra.datafeeder.service.batch.analysis;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.repository.DatasetUploadStateRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class DatasetUploadStateItemReader implements ItemReader<DatasetUploadState> {

    private @Value("#{jobParameters['uploadId']}") UUID uploadId;
    private @Autowired DatasetUploadStateRepository repository;
    private Iterator<DatasetUploadState> iterator;

    public @Override synchronized DatasetUploadState read() throws UnexpectedInputException {
        if (iterator == null) {
            List<DatasetUploadState> all = repository.findAllByJobId(uploadId);
            this.iterator = all.iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }
}