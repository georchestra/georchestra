package org.georchestra.console.integration.autoconfiguration;

import org.georchestra.console.autoconfiguration.RabbitAutoConfiguration;
import org.georchestra.console.integration.ConsoleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringJUnitConfig(locations = { "classpath:/webmvc-config-test.xml" })
@TestPropertySource(properties = { "enableRabbitmqEvents = false" })
public class RabbitMqAutoconfigurationNoRabbitIT extends ConsoleIntegrationTest {

    private @Autowired ApplicationContext context;

    public @Test void testContextLoads() {

        assertTrue(context.getBeansOfType(RabbitAutoConfiguration.class).size() == 0,
                "RabbitMQ autoconfiguration bean unexpected, as rabbitMQ is disabled");
        assertTrue(context.getBeansOfType(org.georchestra.console.events.RabbitmqEventsListener.class).size() == 0,
                "RabbitMQ events listener bean unexpected, as rabbitMQ is disabled");
        assertTrue(context.getBeansOfType(org.georchestra.console.events.RabbitmqEventsSender.class).size() == 0,
                "RabbitMQ events sender bean unexpected as rabbitMQ is disabled");
    }
}
