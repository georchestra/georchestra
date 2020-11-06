package org.georchestra.datafeeder.autoconf;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * {@link EnableAutoConfiguration @EnableAutoConfiguration} auto-configuration
 * to integrate this application as a geOrchestra SDI service when the
 * {@code georchestra} Spring profile is active.
 */
@Configuration
@Profile("georchestra")
@Import(GeorchestraDatadirConfiguration.class)
public class GeorchestraIntegrationConfiguration {

    @ConfigurationProperties
    public @Bean DataFeederConfigurationProperties configProperties() {
        return new DataFeederConfigurationProperties();
    }
}
