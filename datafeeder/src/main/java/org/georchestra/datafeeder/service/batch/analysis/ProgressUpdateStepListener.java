package org.georchestra.datafeeder.service.batch.analysis;

import java.util.UUID;

import javax.batch.api.listener.StepListener;

import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link StepListener} for each upload analysis step that just
 * {@link DataUploadJobRepository#incrementProgress increments by one} the
 * number of the job's {@link DataUploadJob#getFinishedSteps()} finished steps,
 * regardless of the step execution success status.
 */
@Component
@StepScope
@Slf4j
public class ProgressUpdateStepListener extends StepExecutionListenerSupport {

    private @Value("#{jobParameters['uploadId']}") UUID uploadId;
    private @Autowired DataUploadJobRepository repository;

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.debug("Incrementing progress of job {} by one", uploadId);
        repository.incrementProgress(uploadId);

        return null;// return null to not interfere with the chain's exit status
    }

}
