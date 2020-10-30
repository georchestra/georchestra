package org.georchestra.datafeeder.autoconf;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * {@link EnableAutoConfiguration @EnableAutoConfiguration} auto-configuration
 * to integrate this application as a geOrchestra SDI service when the
 * {@code georchestra} Spring profile is active.
 */
@Configuration
@Profile("georchestra")
public class GeorchestraIntegrationConfiguration {

}
