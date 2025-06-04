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

package org.georchestra.datafeeder.email;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Sets up an {@link EmailSendingService} only if {@link DatafeederEmailFactory}
 * and {@link JavaMailSender} beans have been configured.
 * <p>
 * {@link EmailSendingService} listens to application events and sends emails
 * when jobs start, fail, or succeed.
 */
@Configuration
@Profile({ "!test" }) // REVISIT
@ConditionalOnBean({ DatafeederEmailFactory.class, JavaMailSender.class })
@Slf4j(topic = "org.georchestra.datafeeder.email")
public class DataFeederEmailConfiguration {

    private @Autowired Environment env;

    public @PostConstruct void notifyAvailability() {
        log.info("Enabling email job notifications");
    }

    @Bean
    public EmailSendingService emailSendingService() {
        log.info("Email job notifications is enabled. Properties:");
        MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(sources.spliterator(), false).filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames()).flatMap(Arrays::<String>stream)
                .filter(propName -> propName.startsWith("spring.mail.")).forEach(propName -> {
                    String value = env.getProperty(propName);
                    if (propName.contains("passw")) {
                        value = StringUtils.hasText(value) ? (value.charAt(0) + "*****") : "<empty>";
                    } else if (!StringUtils.hasText(value)) {
                        value = "<empty>";
                    }
                    log.info("{}={}", propName, value);
                });
        return new EmailSendingService();
    }

}
