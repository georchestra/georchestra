package org.georchestra.console.events;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.PostConstruct;

@Configuration
@ImportResource({ "file:webapps/console/WEB-INF/spring/rabbit-listener-context.xml",
        "file:webapps/console/WEB-INF/spring/rabbit-sender-context.xml" })
@PropertySource(value = "file:${georchestra.datadir}/default.properties")
@ConditionalOnExpression("${enableRabbitmqEvents:true}")
@ConditionalOnBean(name = "logUtils")
public class RabbitmqEventsAutoConfiguration {

}