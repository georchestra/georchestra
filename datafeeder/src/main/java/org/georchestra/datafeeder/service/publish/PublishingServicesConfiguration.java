package org.georchestra.datafeeder.service.publish;

import org.georchestra.datafeeder.service.publish.impl.GeorchestraPublishingServicesConfiguration;
import org.georchestra.datafeeder.service.publish.mock.MockPublishingServicesConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ MockPublishingServicesConfiguration.class, GeorchestraPublishingServicesConfiguration.class })
public class PublishingServicesConfiguration {
}
