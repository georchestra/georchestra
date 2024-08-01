package org.georchestra.console.integration.autoconfiguration;

import org.georchestra.console.autoconfiguration.RabbitAutoConfiguration;
import org.georchestra.console.integration.ConsoleIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
@TestPropertySource(properties = { "enableRabbitmqEvents = true", "rabbitmqPort =  1234", "rabbitmqHost = my-rabbit" })
public class RabbitMqAutoconfigurationIT extends ConsoleIntegrationTest {

    private @Autowired ApplicationContext context;

    public @Test void testContextLoads() {
        assertNotNull("expected the RabbitMq autoconfiguration bean", context.getBean(RabbitAutoConfiguration.class));
        assertNotNull("expected the RabbitMq eventsListener bean",
                context.getBean(org.georchestra.console.events.RabbitmqEventsListener.class));
        assertNotNull("expected the RabbitMq eventsSender bean",
                context.getBean(org.georchestra.console.events.RabbitmqEventsSender.class));

        assertNotNull("expected the LogUtils bean", context.getBean(org.georchestra.console.ws.utils.LogUtils.class));

        CachingConnectionFactory rabbitFactory = (CachingConnectionFactory) context.getBean("connectionFactory");
        assertEquals(rabbitFactory.getHost(), "my-rabbit");
        assertEquals(rabbitFactory.getPort(), 1234);
    }
}
