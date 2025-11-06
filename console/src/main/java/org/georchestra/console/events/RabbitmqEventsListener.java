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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.georchestra.console.mailservice.EmailFactory;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.users.AccountDao;
import org.json.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class RabbitmqEventsListener implements MessageListener, ServletContextAware {

    private @Autowired @Setter LogUtils logUtils;

    private @Autowired @Setter RoleDao roleDao;

    private @Autowired @Setter AccountDao accountDao;

    private @Autowired @Setter EmailFactory emailFactory;

    private @Autowired @Setter RabbitmqEventsSender rabbitmqEventsSender;

    private ServletContext context;

    public void setServletContext(ServletContext servletContext) {
        this.context = servletContext;
    }

    public void onMessage(Message message) {
        String messageBody = new String(message.getBody());
        JSONObject jsonObj = new JSONObject(messageBody);
        String uid = jsonObj.getString("uid");
        String subject = jsonObj.getString("subject");

        if (subject.equals("EXTERNAL-ACCOUNT-CREATION")) {
            try {
                String fullName = jsonObj.getString("fullName");
                String localUid = jsonObj.getString("localUid");
                String email = jsonObj.getString("email");
                String providerName = jsonObj.getString("providerName");
                String providerUid = jsonObj.getString("providerUid");
                String organization = null;
                if (jsonObj.has("organization")) {
                    organization = jsonObj.getString("organization");
                }
                List<String> superUserAdmins = this.roleDao.findByCommonName("SUPERUSER").getUserList().stream()
                        .map(user -> {
                            try {
                                return this.accountDao.findByUID(user).getEmail();
                            } catch (DataServiceException e) {
                                throw new RuntimeException(e);
                            }
                        }).collect(Collectors.toList());

                this.emailFactory.sendNewExternalAccountNotificationEmail(context, superUserAdmins, fullName, localUid,
                        email, providerName, providerUid, organization, true);

                logUtils.createExternalLog(localUid, AdminLogType.EXTERNAL_USER_CREATED, null);
                rabbitmqEventsSender.sendAcknowledgementMessageToGateway(
                        "new External account creation notification for " + email + " has been received by console");
            } catch (Exception e) {
                log.error("Error while processing rabbitMq message, message will be discarded for future processing.",
                        e);
            }
        }
    }
}