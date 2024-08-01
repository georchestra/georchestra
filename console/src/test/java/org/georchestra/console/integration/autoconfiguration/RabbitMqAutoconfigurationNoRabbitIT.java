package org.georchestra.console.integration.autoconfiguration;

import org.georchestra.console.autoconfiguration.RabbitAutoConfiguration;
import org.georchestra.console.integration.ConsoleIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
@TestPropertySource(properties = { "enableRabbitmqEvents = false" })
public class RabbitMqAutoconfigurationNoRabbitIT extends ConsoleIntegrationTest {

    private @Autowired ApplicationContext context;

    public @Test void testContextLoads() {

        assertTrue("RabbitMQ autoconfiguration bean unexpected, as rabbitMQ is disabled",
                context.getBeansOfType(RabbitAutoConfiguration.class).size() == 0);
        assertTrue("RabbitMQ events listener bean unexpected, as rabbitMQ is disabled",
                context.getBeansOfType(org.georchestra.console.events.RabbitmqEventsListener.class).size() == 0);
        assertTrue("RabbitMQ events sender bean unexpected as rabbitMQ is disabled",
                context.getBeansOfType(org.georchestra.console.events.RabbitmqEventsSender.class).size() == 0);
    }
}
