/*
 * Copyright (C) 2020 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.datafeeder.autoconf;

import org.georchestra.config.security.GeorchestraSecurityProxyAuthenticationConfiguration;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.service.publish.impl.GeorchestraPublishingServicesConfiguration;
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
@Import({ GeorchestraDatadirConfiguration.class, GeorchestraSecurityProxyAuthenticationConfiguration.class,
        GeorchestraPublishingServicesConfiguration.class })
public class GeorchestraIntegrationAutoConfiguration {

    @ConfigurationProperties(prefix = "datafeeder")
    public @Bean DataFeederConfigurationProperties configProperties() {
        return new DataFeederConfigurationProperties();
    }
}
