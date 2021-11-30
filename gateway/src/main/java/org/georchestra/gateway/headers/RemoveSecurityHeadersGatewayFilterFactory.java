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
package org.georchestra.gateway.headers;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;

/**
 * Georchestra-specific {@link GatewayFilterFactory} to remove all incoming
 * {@code sec-*} and {@code Authorization} (basic auth) request headers.
 * <p>
 * Sample usage:
 * 
 * <pre>
 * <code>
 * spring:
 *   cloud:
 *    gateway:
 *      routes:
 *      - id: root
 *        uri: http://backend-service/context
 *        filters:
 *        - RemoveSecurityHeaders
 * </code>
 * </pre>
 * 
 * @see RemoveHeadersGatewayFilterFactory
 */
public class RemoveSecurityHeadersGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final String DEFAULT_SEC_HEADERS_PATTERN = "(?i)(sec-.*|Authorization)";

    private final RemoveHeadersGatewayFilterFactory delegate = new RemoveHeadersGatewayFilterFactory();
    private final RemoveHeadersGatewayFilterFactory.RegExConfig config = new RemoveHeadersGatewayFilterFactory.RegExConfig(
            DEFAULT_SEC_HEADERS_PATTERN);

    public RemoveSecurityHeadersGatewayFilterFactory() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(Object unused) {
        return delegate.apply(config);
    }
}
