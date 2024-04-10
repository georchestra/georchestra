package org.georchestra.console.autoconfiguration;

import org.springframework.context.annotation.*;

@Configuration
@Conditional(IsRabbitMqEnabled.class)
@ImportResource({ "classpath:/spring/rabbit-listener-context.xml", "classpath:/spring/rabbit-sender-context.xml" })
public class RabbitAutoConfiguration {
}
