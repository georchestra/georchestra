package org.georchestra.datafeeder.service.batch.analysis;

import java.util.Objects;
import java.util.UUID;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Setter;

/**
 * {@link Tasklet} that finishes the analysis process by calling
 * {@link DataUploadAnalysisService#summarize(UUID)}
 *
 */
@Component
@StepScope
public class DataUploadStateSumarizer implements Tasklet {

    private @Value("#{jobParameters['uploadId']}") UUID uploadId;
    private @Autowired @Setter DataUploadAnalysisService service;

    public @Override RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Objects.requireNonNull(uploadId, () -> String
                .format("Job parameter not provided: " + UploadAnalysisConfiguration.UPLOAD_ID_JOB_PARAM_NAME));
        service.summarize(uploadId);
        return RepeatStatus.FINISHED;
    }
}
