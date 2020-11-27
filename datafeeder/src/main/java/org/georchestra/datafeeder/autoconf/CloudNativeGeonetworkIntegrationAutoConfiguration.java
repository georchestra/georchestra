package org.georchestra.datafeeder.autoconf;

import javax.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * {@link EnableAutoConfiguration @EnableAutoConfiguration} auto-configuration
 * to integrate this application as a
 * <a href="https://github.com/geonetwork/geonetwork-microservices">GeoNetworks
 * micro-service</a> when the {@code georchestra} Spring profile is active.
 */
@Configuration
@Profile("geonetwork")
public class CloudNativeGeonetworkIntegrationAutoConfiguration {

    public @PostConstruct void fail() {
        throw new ApplicationContextException("'geonetwork' profile auto-configuration not yet implemented");
    }
}
