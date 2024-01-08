package org.georchestra.console.events;

import org.georchestra.console.mailservice.EmailFactory;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.users.AccountDao;
import org.json.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.georchestra.console.model.AdminLogType;

import javax.mail.MessagingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RabbitmqEventsListener implements MessageListener {

    private LogUtils logUtils;

    private RoleDao roleDao;

    private AccountDao accountDao;

    private EmailFactory emailFactory;

    private RabbitmqEventsSender rabbitmqEventsSender;

    public void setLogUtils(LogUtils logUtils) {
        this.logUtils = logUtils;
    }

    public LogUtils getLogUtils() {
        return this.logUtils;
    }

    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public RoleDao getRoleDao() {
        return this.roleDao;
    }

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public AccountDao getAccountDao() {
        return this.accountDao;
    }

    public void setEmailFactory(EmailFactory emailFactory) {
        this.emailFactory = emailFactory;
    }

    public EmailFactory getEmailFactory() {
        return this.emailFactory;
    }

    public void setRabbitmqEventsSender(RabbitmqEventsSender rabbitmqEventsSender) {
        this.rabbitmqEventsSender = rabbitmqEventsSender;
    }

    public RabbitmqEventsSender getRabbitmqEventsSender() {
        return this.rabbitmqEventsSender;
    }

    private static Set<String> synReceivedMessageUid = Collections.synchronizedSet(new HashSet<String>());

    public void onMessage(Message message) {
        String messageBody = new String(message.getBody());
        JSONObject jsonObj = new JSONObject(messageBody);
        String uid = jsonObj.getString("uid");
        String subject = jsonObj.getString("subject");

        if (subject.equals("OAUTH2-ACCOUNT-CREATION") && !synReceivedMessageUid.stream().anyMatch(s -> s.equals(uid))) {
            try {
                String username = jsonObj.getString("username");
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

                this.emailFactory.sendNewOAuth2AccountNotificationEmail(superUserAdmins, username, email, providerName,
                        providerUid, organization, true);

                synReceivedMessageUid.add(uid);
                logUtils.createOAuth2Log(email, AdminLogType.OAUTH2_USER_CREATED, null);
                rabbitmqEventsSender.sendAcknowledgementMessageToGateway(
                        "new OAuth2 account creation notification for " + email + " has been received by console");
            } catch (DataServiceException e) {
                throw new RuntimeException(e);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}