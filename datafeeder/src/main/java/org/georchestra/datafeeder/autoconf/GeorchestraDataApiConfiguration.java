package org.georchestra.datafeeder.autoconf;

import lombok.extern.slf4j.Slf4j;
import org.georchestra.datafeeder.config.PostgisSchemasConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@Slf4j(topic = "org.georchestra.datafeeder.autoconf")
@Profile("data-api-schemas")
public class GeorchestraDataApiConfiguration {

    @ConfigurationProperties(prefix = "postgis.schemas")
    public @Bean PostgisSchemasConfiguration postgisSchemasConfiguration() {
        return new PostgisSchemasConfiguration();
    }
}
