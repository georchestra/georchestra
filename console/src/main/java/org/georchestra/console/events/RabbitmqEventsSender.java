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

package org.georchestra.console.events;

import org.json.JSONObject;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.UUID;

public class RabbitmqEventsSender {
    public static final String OAUTH2_ACCOUNT_CREATION_RECEIVED = "OAUTH2-ACCOUNT-CREATION-RECEIVED";

    @Autowired
    private AmqpTemplate eventTemplate;

    public void setEventTemplate(AmqpTemplate eventTemplate) {
        this.eventTemplate = eventTemplate;
    }

    public AmqpTemplate getEventTemplate() {
        return this.eventTemplate;
    }

    public void sendAcknowledgementMessageToGateway(String msg) throws Exception {
        // beans
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("uid", UUID.randomUUID());
        jsonObj.put("subject", OAUTH2_ACCOUNT_CREATION_RECEIVED);
        jsonObj.put("msg", msg); // bean
        eventTemplate.convertAndSend("routing-console", jsonObj.toString());// send
    }
}