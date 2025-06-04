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

import java.util.Optional;

import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;

import org.georchestra.datafeeder.event.AnalysisFailedEvent;
import org.georchestra.datafeeder.event.AnalysisStartedEvent;
import org.georchestra.datafeeder.event.PublishFailedEvent;
import org.georchestra.datafeeder.event.PublishFinishedEvent;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.UserInfo;
import org.georchestra.datafeeder.repository.DataUploadJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Listens to application events and sends emails when jobs start, fail, or
 * succeed.
 * <p>
 * A {@link DatafeederEmailFactory} must be provided to create the actual
 * messages based on the target integration ecosystem (e.g. georchestra).
 */
@Slf4j(topic = "org.georchestra.datafeeder.email")
public @Service class EmailSendingService {

    private @Autowired DataUploadJobRepository repository;

    private @Autowired JavaMailSender emailSender;

    private @Autowired DatafeederEmailFactory templateEngine;

    @Async
    @Transactional
    @EventListener(AnalysisStartedEvent.class)
    public void sendAckEmail(AnalysisStartedEvent event) throws InterruptedException {
        log.info("Sending ack email");
        DataUploadJob job = event.getSource();
        // HACK: wait a little while for a dataset to be recognized
        Thread.sleep(2000);
        job = repository.getOne(event.getSource().getJobId());
        UserInfo user = event.getUser();
        Optional<MailMessage> message = templateEngine.createAckMessage(job, user);
        send(message);
    }

    @Async
    @Transactional
    @EventListener(AnalysisFailedEvent.class)
    public void sendAnalysisFailedEmail(AnalysisFailedEvent event) {
        log.info("Sending analysis failure email");
        DataUploadJob job = repository.getOne(event.getSource().getJobId());
        UserInfo user = event.getUser();
        Optional<MailMessage> message = templateEngine.createAnalysisFailureMessage(job, user, event.getCause());
        send(message);
    }

    @Async
    @Transactional
    @EventListener(PublishFailedEvent.class)
    public void sendPublishFailedEmail(PublishFailedEvent event) {
        log.info("Sending publication failure email");
        DataUploadJob job = repository.getOne(event.getSource().getJobId());
        UserInfo user = event.getUser();
        Optional<MailMessage> message = templateEngine.createPublishFailureMessage(job, user, event.getCause());
        send(message);
    }

    @Async
    @Transactional
    @EventListener(PublishFinishedEvent.class)
    public void sendPublishFinishedEmail(PublishFinishedEvent event) {
        log.info("Sending publication failure email");
        DataUploadJob job = repository.getOne(event.getSource().getJobId());
        UserInfo user = event.getUser();
        Optional<MailMessage> message = templateEngine.createPublishFinishedMessage(job, user);
        send(message);
    }

    private void send(Optional<MailMessage> message) {
        if (!message.isPresent()) {
            return;
        }
        MailMessage mailMessage = message.get();
        try {
            log.info(mailMessage.toString());
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
        } catch (MailAuthenticationException e) {
            log.warn("Authentication error sending mail message", e);
        } catch (MailException e) {
            log.warn("Error sending mail message", e);
        } catch (RuntimeException e) {
            log.warn("Unknown error sending mail message", e);
        }
    }

}
