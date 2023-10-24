package org.georchestra.console.events;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

public class DefaultPropsInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static Logger LOGGER = Logger.getLogger(DefaultPropsInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            applicationContext.getEnvironment().getPropertySources()
                    .addFirst(new ResourcePropertySource(new FileSystemResource(
                            System.getProperty("georchestra.datadir", "/etc/georchestra") + "/default.properties")));
        } catch (IOException e) {
            LOGGER.error("Cannot find default props file");
            LOGGER.error(e.getStackTrace());
        }
    }

}