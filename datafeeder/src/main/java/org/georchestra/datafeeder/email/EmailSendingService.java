/*
 * Copyright (C) 2021 by the geOrchestra PSC
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

import java.util.Optional;

import javax.mail.internet.MimeMessage;

import org.georchestra.datafeeder.event.AnalysisFailedEvent;
import org.georchestra.datafeeder.event.AnalysisStartedEvent;
import org.georchestra.datafeeder.event.PublishFailedEvent;
import org.georchestra.datafeeder.event.PublishFinishedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Listens to application events and sends emails when jobs start, fail, or
 * succeed.
 * <p>
 * A {@link DatafeederEmailFactory} must be provided to create the actual
 * messages based on the target integration ecosystem (e.g. georchestra).
 */
@Slf4j
public @Service class EmailSendingService {

    private @Autowired JavaMailSender emailSender;

    private @Autowired DatafeederEmailFactory templateEngine;

    @EventListener(AnalysisStartedEvent.class)
    public void sendAckEmail(AnalysisStartedEvent event) {
        log.info("Sending ack email");
        Optional<MailMessage> message = templateEngine.createAckMessage(event.getSource(), event.getUser());
        send(message);
    }

    @EventListener(AnalysisFailedEvent.class)
    public void sendAnalysisFailedEmail(AnalysisFailedEvent event) {
        log.info("Sending analysis failure email");
        Optional<MailMessage> message = templateEngine.createAnalysisFailureMessage(event.getSource(), event.getUser());
        send(message);
    }

    @EventListener(PublishFailedEvent.class)
    public void sendPublishFailedEmail(PublishFailedEvent event) {
        log.info("Sending publication failure email");
        Optional<MailMessage> message = templateEngine.createPublishFailureMessage(event.getSource(), event.getUser());
        send(message);
    }

    @EventListener(PublishFinishedEvent.class)
    public void sendPublishFinishedEmail(PublishFinishedEvent event) {
        log.info("Sending publication failure email");
        Optional<MailMessage> message = templateEngine.createPublishFinishedMessage(event.getSource(), event.getUser());
        send(message);
    }

    private void send(Optional<MailMessage> message) {
        if (message.isPresent()) {
            MailMessage mailMessage = message.get();
            if (mailMessage instanceof MimeMailMessage) {
                MimeMessage mimeMessage = ((MimeMailMessage) mailMessage).getMimeMessage();
                emailSender.send(mimeMessage);
            } else if (mailMessage instanceof SimpleMailMessage) {
                emailSender.send((SimpleMailMessage) mailMessage);
            } else {
                throw new IllegalArgumentException(
                        "Unknown mail message type, expected MimeMailMessage or SimpleMailMessage, got "
                                + mailMessage.getClass().getCanonicalName());
            }
        }
    }

}
