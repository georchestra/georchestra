package org.georchestra.console.autoconfiguration;

import org.springframework.context.annotation.*;

@Configuration
@PropertySource(value = { "file:${georchestra.datadir}/default.properties",
        "file:${georchestra.datadir}/console/console.properties}" }, ignoreResourceNotFound = true)
@Conditional(IsRabbitMqEnabled.class)
@ImportResource({ "classpath:/spring/rabbit-listener-context.xml", "classpath:/spring/rabbit-sender-context.xml" })
public class RabbitAutoConfiguration {
}
