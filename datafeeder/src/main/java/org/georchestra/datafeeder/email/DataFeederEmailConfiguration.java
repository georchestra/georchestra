/*
 * Copyright (C) 2020, 2021 by the geOrchestra PSC
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

import javax.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

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
@Slf4j
public class DataFeederEmailConfiguration {

    public @PostConstruct void notifyAvailability() {
        log.info("Enabling email job notifications");
    }

    @Bean
    public EmailSendingService emailSendingService() {
        log.info("Email job notifications is enabled");
        return new EmailSendingService();
    }

}
