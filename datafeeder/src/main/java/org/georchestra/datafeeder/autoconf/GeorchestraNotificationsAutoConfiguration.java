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

package org.georchestra.datafeeder.autoconf;

import org.georchestra.datafeeder.email.DatafeederEmailFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Configuration for sending emails upon application events like job
 * started/failed/succeeded based on georchestra's email configuration
 * <p>
 * This configuration will be enabled if the mail sender is configured (i.e.
 * there is a {@link JavaMailSender} in the application context), and the
 * configuration property {@code datafeeder.email.send} is {@code true} or not
 * defined (i.e. defaults to {@code true})
 */
@Configuration
@Profile("georchestra")
@AutoConfigureAfter(MailSenderAutoConfiguration.class)
@ConditionalOnBean(JavaMailSender.class)
@ConditionalOnProperty(prefix = "datafeeder.email", name = "send", havingValue = "true", matchIfMissing = true)
public class GeorchestraNotificationsAutoConfiguration {

    @Bean
    public DatafeederEmailFactory georchestraEmailFactory() {
        return new GeorchestraEmailFactory();
    }
}
