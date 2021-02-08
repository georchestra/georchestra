package org.georchestra.datafeeder.service.publish.mock;

import org.georchestra.datafeeder.service.publish.DataBackendService;
import org.georchestra.datafeeder.service.publish.MetadataPublicationService;
import org.georchestra.datafeeder.service.publish.OWSPublicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("mock")
public class MockPublishingServicesConfiguration {

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
