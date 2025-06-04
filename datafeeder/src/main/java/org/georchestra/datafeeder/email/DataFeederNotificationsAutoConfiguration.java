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

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Sets up an {@link EmailSendingService} only if {@link DatafeederEmailFactory}
 * and {@link JavaMailSender} beans have been configured.
 * <p>
 * {@link EmailSendingService} listens to application events and sends emails
 * when jobs start, fail, or succeed.
 */
@Configuration
@AutoConfigureAfter(MailSenderAutoConfiguration.class)
@Import({ DataFeederEmailConfiguration.class, DataFeederEmailUnavailableNotifier.class })
public class DataFeederNotificationsAutoConfiguration {

}
