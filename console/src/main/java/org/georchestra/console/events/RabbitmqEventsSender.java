package org.georchestra.console.events;

import org.json.JSONObject;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.UUID;

public class RabbitmqEventsSender {
    public static final String OAUTH2_ACCOUNT_CREATION_RECEIVED = "OAUTH2-ACCOUNT-CREATION-RECEIVED";
    @Autowired
    private ApplicationContext applicationContext;

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