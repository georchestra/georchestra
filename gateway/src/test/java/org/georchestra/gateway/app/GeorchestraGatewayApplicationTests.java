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
package org.georchestra.gateway.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "georchestra.datadir=classpath:/test-datadir")
@ActiveProfiles({ "default", "test" })
class GeorchestraGatewayApplicationTests {

    private @Autowired Environment env;
    private @Autowired RouteLocator routeLocator;

    public @Test void contextLoadsFromDatadir() {
        assertEquals("classpath:/test-datadir", env.getProperty("georchestra.datadir"));

        assertEquals(
                "classpath:gateway.yml,optional:classpath:/test-datadir/default.properties,optional:classpath:/test-datadir/gateway/gateway.yaml",
                env.getProperty("spring.config.import"));

        Boolean propertyFromTestDatadir = env.getProperty("georchestra.test-datadir", Boolean.class);
        assertTrue(propertyFromTestDatadir,
                "Configuration property expected to load from classpath:/test-datadir/gateway/gateway.yaml not found");
    }

    public @Test void verifyRoutesLoadedFromDatadir() {
        Map<String, Route> routesById = routeLocator.getRoutes()
                .collect(Collectors.toMap(Route::getId, Function.identity())).block();

        Route testRoute = routesById.get("testRoute");
        assertNotNull(testRoute);
        assertEquals(URI.create("http://test.com:80"), testRoute.getUri());
    }
}
