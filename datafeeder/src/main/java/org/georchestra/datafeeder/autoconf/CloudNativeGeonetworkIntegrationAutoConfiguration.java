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
