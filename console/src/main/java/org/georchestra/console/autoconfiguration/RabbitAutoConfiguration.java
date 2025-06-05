/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.console.autoconfiguration;

import org.springframework.context.annotation.*;

@Configuration
@PropertySource(value = { "file:${georchestra.datadir}/default.properties",
        "file:${georchestra.datadir}/console/console.properties}" }, ignoreResourceNotFound = true)
@Conditional(IsRabbitMqEnabled.class)
@ImportResource({ "classpath:/spring/rabbit-listener-context.xml", "classpath:/spring/rabbit-sender-context.xml" })

public class RabbitAutoConfiguration {
}
