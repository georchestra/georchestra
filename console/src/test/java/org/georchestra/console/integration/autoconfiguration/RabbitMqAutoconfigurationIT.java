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

package org.georchestra.console.integration.autoconfiguration;

import org.georchestra.console.autoconfiguration.RabbitAutoConfiguration;
import org.georchestra.console.integration.ConsoleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebAppConfiguration
@SpringJUnitConfig(locations = {"classpath:/webmvc-config-test.xml"})
@TestPropertySource(properties = { "enableRabbitmqEvents = true", "rabbitmqPort =  1234", "rabbitmqHost = my-rabbit" })
public class RabbitMqAutoconfigurationIT extends ConsoleIntegrationTest {

    private @Autowired ApplicationContext context;

    public @Test void testContextLoads() {
        assertNotNull(context.getBean(RabbitAutoConfiguration.class), "expected the RabbitMq autoconfiguration bean");
        assertNotNull(context.getBean(org.georchestra.console.events.RabbitmqEventsListener.class),
                "expected the RabbitMq eventsListener bean");
        assertNotNull(context.getBean(org.georchestra.console.events.RabbitmqEventsSender.class),
                "expected the RabbitMq eventsSender bean");

        assertNotNull(context.getBean(org.georchestra.console.ws.utils.LogUtils.class), "expected the LogUtils bean");

        CachingConnectionFactory rabbitFactory = (CachingConnectionFactory) context.getBean("connectionFactory");
        assertEquals(rabbitFactory.getHost(), "my-rabbit");
        assertEquals(rabbitFactory.getPort(), 1234);
    }

}
