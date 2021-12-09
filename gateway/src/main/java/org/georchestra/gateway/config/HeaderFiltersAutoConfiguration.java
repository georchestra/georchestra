/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.gateway.config;

import org.georchestra.gateway.headers.AddSecHeadersGatewayFilterFactory;
import org.georchestra.gateway.headers.RemoveHeadersGatewayFilterFactory;
import org.georchestra.gateway.headers.RemoveSecurityHeadersGatewayFilterFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GatewayConfig.class)
@AutoConfigureBefore(GatewayAutoConfiguration.class)
public class HeaderFiltersAutoConfiguration {

    /**
     * {@link GatewayFilterFactory} to add all necessary {@literal sec-*} request
     * headers to proxied requests
     */
    public @Bean AddSecHeadersGatewayFilterFactory addSecHeadersGatewayFilterFactory() {
        return new AddSecHeadersGatewayFilterFactory();
    }

    /**
     * General purpose {@link GatewayFilterFactory} to remove incoming HTTP request
     * headers based on a Java regular expression
     */
    public @Bean RemoveHeadersGatewayFilterFactory removeHeadersGatewayFilterFactory() {
        return new RemoveHeadersGatewayFilterFactory();
    }

    /**
     * {@link GatewayFilterFactory} to remove incoming HTTP {@literal sec-*} HTTP
     * request headers to prevent impersonation from outside
     */
    public @Bean RemoveSecurityHeadersGatewayFilterFactory removeSecurityHeadersGatewayFilterFactory() {
        return new RemoveSecurityHeadersGatewayFilterFactory();
    }
}
