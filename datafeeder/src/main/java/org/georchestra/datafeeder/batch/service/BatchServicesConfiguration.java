package org.georchestra.datafeeder.batch.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class BatchServicesConfiguration {

    public @Bean JobManager jobManager() {
        return new JobManager();
    }

    public @Bean DataUploadAnalysisService dataUploadAnalysisService() {
        return new DataUploadAnalysisService();
    }

    public @Bean PublishingBatchService publishingBatchService() {
        return new PublishingBatchService();
    }
}
