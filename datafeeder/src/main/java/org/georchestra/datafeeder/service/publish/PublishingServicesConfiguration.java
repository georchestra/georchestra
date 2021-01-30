package org.georchestra.datafeeder.service.publish;

import org.georchestra.datafeeder.service.publish.mock.MockDataBackendService;
import org.georchestra.datafeeder.service.publish.mock.MockMetadataPublicationService;
import org.georchestra.datafeeder.service.publish.mock.MockOWSPublicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PublishingServicesConfiguration {

    @ConditionalOnMissingBean(DataBackendService.class)
    public @Bean DataBackendService dataBackendService() {
        return new MockDataBackendService();
    }

    @ConditionalOnMissingBean(OWSPublicationService.class)
    public @Bean OWSPublicationService owsPublicationService() {
        return new MockOWSPublicationService();
    }

    @ConditionalOnMissingBean(MetadataPublicationService.class)
    public @Bean MetadataPublicationService metadataPublicationService() {
        return new MockMetadataPublicationService();
    }
}
