/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
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

package org.georchestra.console.ws.emails;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.console.dao.*;
import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.dto.Account;
import org.georchestra.console.mailservice.EmailFactoryImpl;
import org.georchestra.console.model.AdminLogEntry;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.model.Attachment;
import org.georchestra.console.model.EmailEntry;
import org.georchestra.console.model.EmailTemplate;
import org.georchestra.console.ws.backoffice.utils.ResponseUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

@Controller
public class EmailController {

    @Autowired
    private EmailDao emailRepository;

    @Autowired
    private AttachmentDao attachmentRepo;

    @Autowired
    private EmailTemplateDao emailTemplateRepo;

    @Autowired
	private AccountDao accountDao;

    @Autowired
    private EmailFactoryImpl emailFactory;

    @Autowired
    private AdminLogDao logRepo;

    @Autowired
    private GeorchestraConfiguration georConfig;

    @Autowired
    private AdvancedDelegationDao advancedDelegationDao;

    private static final Log LOG = LogFactory.getLog(EmailController.class.getName());
    private Collection<String> recipientWhiteList;

    /*
     * produces = MediaType.APPLICATION_JSON_VALUE
     * generate : content type : application/json; charset=UTF-8
     * WRONG !
     *
     * produces = "application/json; charset=utf-8"
     * generate :  content type :  application/json;charset=utf-8
     * RIGHT !
     *
     */

    /**
     * Return a JSON list of Emails sent to specified user
     *
     * @param recipient recipient login
     * @return JSON list of Emails sent to specified user
     * @throws JSONException
     */
    @RequestMapping(value="/{recipient}/emails",
                    method= RequestMethod.GET,
                    produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String emailsList(@PathVariable String recipient) throws JSONException {
        this.checkAuthorisation(recipient);
        JSONArray emails = new JSONArray();
        for(EmailEntry email : this.emailRepository.findByRecipientOrderByDateDesc(recipient))
            emails.put(email.toJSON());
        JSONObject res = new JSONObject();
        res.put("emails", emails);

        return res.toString();
    }


    /**
     * Send an email and store it in database
     *
     * @param recipient recipient login
     * @param subject subject of email
     * @param content content of email (text part in html)
     * @param attachmentsIds comma separated list of attachments identifier
     * @return identifier of new email sent prefixed by "OK : "
     * @throws NameNotFoundException if sender cannot be found
     * @throws DataServiceException if there is issues while contacting LDAP server
     * @throws MessagingException if email cannot be sent through SMTP protocol
     * @throws IOException if email cannot be sent through SMTP protocol
     */
    @RequestMapping(value="{recipient}/sendEmail", method = RequestMethod.POST)
    @ResponseBody
    public String sendEmail(@PathVariable String recipient,
                            @RequestParam("subject") String subject,
                            @RequestParam("content") String content,
                            @RequestParam("attachments") String attachmentsIds,
                            HttpServletRequest request,
                            HttpServletResponse response)
            throws NameNotFoundException, DataServiceException, MessagingException, JSONException {

        this.checkAuthorisation(recipient);

        EmailEntry email = new EmailEntry();
        String sender = request.getHeader("sec-username");
        email.setSender(sender);
        email.setRecipient(recipient);
        email.setSubject(subject);
        email.setDate(new Date());
        email.setBody(content);

        attachmentsIds = attachmentsIds.trim();
        List<Attachment> attachments = new LinkedList<Attachment>();
        if (attachmentsIds.length() > 0) {
            String[] attachmentsIdsList = attachmentsIds.split("\\s?,\\s?");
            for (String attId : attachmentsIdsList) {
                Attachment att = this.attachmentRepo.findOne(Long.parseLong(attId));
                if (att == null)
                    throw new NameNotFoundException("Unable to find attachment with ID : " + attId);
                attachments.add(att);
            }
        }
        email.setAttachments(attachments);
        this.send(email);

        AdminLogEntry log = new AdminLogEntry(sender, recipient, AdminLogType.EMAIL_SENT, new Date());
        this.emailRepository.save(email);
        response.setContentType("application/json");
        return email.toJSON().toString();

    }



    /**
     * This service can be used to test email sending
     * @param recipient login of recipient
     * @return Html page to test email sending
     */
    @RequestMapping(value="{recipient}/sendEmail", method = RequestMethod.GET)
    @ResponseBody
    public String sendEmail(@PathVariable String recipient){
        return "<form method=POST>" +
                "recipient : " + recipient + "<br>" +
                "subject : <input type='test' name='subject'><br>" +
                "content : <textarea name='content'></textarea><br>" +
                "comma separated list of attachment identifier <input type='text' name='attachments'><br>" +
                "<input type='submit'>" +
                "</form>";

    }


    /**
     * List available attachments in database
     * @return JSON object containing all attachments available in database
     * @throws JSONException if there is error when encoding to JSON
     */
    @RequestMapping(value="/attachments",
                    method = RequestMethod.GET,
                    produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String attachments() throws JSONException {
        JSONArray attachments = new JSONArray();
        for(Attachment att : this.attachmentRepo.findAll()){
            JSONObject attachment = new JSONObject();
            attachment.put("id",att.getId());
            attachment.put("name",att.getName());
            attachment.put("mimeType",att.getMimeType());
            attachments.put(attachment);
        }

        JSONObject res = new JSONObject();
        res.put("attachments", attachments);
        return res.toString();
    }

    /**
     * Generate a list of template found in database
     * @return JSON object containing all templates found in database
     * @throws JSONException if templates cannot be encoded to JSON
     */
    @RequestMapping(value="/emailTemplates",
                    method = RequestMethod.GET,
                    produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String emailTemplates() throws JSONException {
        JSONArray emailTemplates = new JSONArray();
        for(EmailTemplate temp : this.emailTemplateRepo.findAll()){
            JSONObject attachment = new JSONObject();
            attachment.put("id", temp.getId());
            attachment.put("name", temp.getName());
            attachment.put("content", temp.getContent());
            emailTemplates.put(attachment);
        }

        JSONObject res = new JSONObject();
        res.put("templates", emailTemplates);
        return res.toString();
    }

    /**
     * Send an email based on json payload. Recipient should be present in LDAP directory or in configured whitelist.
     *
     * Json sent should have following keys :
     *
     * - to      : json array of email to send email to ex: ["you@rm.fr", "another-guy@rm.fr"]
     * - cc      : json array of email to 'CC' email ex: ["him@rm.fr"]
     * - bcc     : json array of email to add recipient as blind CC ["secret@rm.fr"]
     * - subject : subject of email
     * - body    : Body of email
     *
     * Either 'to', 'cc' or 'bcc' parameter must be present in request. 'subject' and 'body' are mandatory.
     *
     * complete json example :
     *
     * {
     *   "to": ["you@rm.fr", "another-guy@rm.fr"],
     *   "cc": ["him@rm.fr"],
     *   "bcc": ["secret@rm.fr"],
     *   "subject": "test email",
     *   "body": "Hi, this a test EMail, please do not reply."
     * }
     *
     */
    @RequestMapping(value = "/emailProxy", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8", consumes="application/json")
    @ResponseBody
    public String emailProxy(@RequestBody String rawRequest, HttpServletRequest request)
            throws JSONException, MessagingException, UnsupportedEncodingException, DataServiceException {

        JSONObject payload = new JSONObject(rawRequest);
        InternetAddress[] to = this.populateRecipient("to", payload);
        InternetAddress[] cc = this.populateRecipient("cc", payload);
        InternetAddress[] bcc = this.populateRecipient("bcc", payload);

        this.checkSubject(payload);
        this.checkBody(payload);
        this.checkRecipient(to, cc, bcc);

        LOG.info("EMail request : user=" + request.getHeader("sec-username")
                + " to=" + this.extractAddress("to", payload)
                + " cc=" + this.extractAddress("cc", payload)
                + " bcc=" + this.extractAddress("bcc", payload)
                + " roles=" + request.getHeader("sec-roles"));

        LOG.debug("EMail request : " + payload.toString());

        // Instanciate MimeMessage
        Properties props = System.getProperties();
        props.put("mail.smtp.host", this.emailFactory.getSmtpHost());
        props.put("mail.protocol.port", this.emailFactory.getSmtpPort());
        Session session = Session.getInstance(props, null);
        MimeMessage message = new MimeMessage(session);

        // Generate From header
        InternetAddress from = new InternetAddress();
        from.setAddress(this.georConfig.getProperty("emailProxyFromAddress"));
        from.setPersonal(request.getHeader("sec-firstname") + " " + request.getHeader("sec-lastname"));
        message.setFrom(from);

        // Generate Reply-to header
        InternetAddress replyTo = new InternetAddress();
        replyTo.setAddress(request.getHeader("sec-email"));
        replyTo.setPersonal(request.getHeader("sec-firstname") + " " + request.getHeader("sec-lastname"));
        message.setReplyTo(new Address[]{replyTo});

        // Generate to, cc and bcc headers
        if(to.length > 0)
            message.setRecipients(Message.RecipientType.TO, to);
        if(cc.length > 0)
            message.setRecipients(Message.RecipientType.CC, cc);
        if(bcc.length > 0)
            message.setRecipients(Message.RecipientType.BCC, bcc);

        // Add subject and body
        message.setSubject(payload.getString("subject"), "UTF-8");
        message.setText(payload.getString("body"), "UTF-8", "plain");
        message.setSentDate(new Date());

        // finally send message
        Transport.send(message);

        JSONObject res = new JSONObject();
        res.put("success", true);
        return res.toString();
    }

    /**
     * Checks 'subject' of request against configuration
     * @param payload JSONObject to search subject in
     */
    private void checkSubject(JSONObject payload) throws JSONException {

        // Checks that subject is present
        if(!payload.has("subject") || payload.getString("subject").length() == 0)
            throw new JSONException("No subject specified, 'subject' field is required");

        // Check subject size
        if(payload.getString("subject").length() > Integer.parseInt(georConfig.getProperty("emailProxyMaxSubjectSize")))
            throw new IllegalArgumentException("Subject is too long, it should not exceed " +
                    georConfig.getProperty("emailProxyMaxSubjectSize") + " bytes");
    }

    /**
     * Checks 'body' of request against configuration
     * @param payload JSONObject to search body in
     */
    private void checkBody(JSONObject payload) throws JSONException {

         // Checks that body is present
        if(!payload.has("body"))
            throw new JSONException("No body specified, 'body' field is required");

        // Check subject and body size
        if(payload.getString("body").length() > Integer.parseInt(georConfig.getProperty("emailProxyMaxBodySize")))
            throw new IllegalArgumentException("Body is too long, it should not exceed " +
                    georConfig.getProperty("emailProxyMaxBodySize") + " bytes");

    }

    /**
     * Checks recipients of request against configuration
     * @param to array of recipients for 'to' field
     * @param cc array of recipients for 'cc' field
     * @param bcc array of recipients for 'bcc' field
     */
    private void checkRecipient(InternetAddress[] to,
                                InternetAddress[] cc,
                                InternetAddress[] bcc) throws JSONException, DataServiceException {

        if(to.length == 0 && cc.length == 0 && bcc.length == 0)
            throw new JSONException("One of 'to', 'cc' or 'bcc' must be present in request");

        // Check recipient count against proxyMaxRecipient
        if((to.length + cc.length + bcc.length) > Integer.parseInt(georConfig.getProperty("emailProxyMaxRecipient")))
            throw new IllegalArgumentException("Too many recipient in request, max recipient : "
                    + georConfig.getProperty("emailProxyMaxRecipient"));

        // Check Recipients validity
        for(int i = 0; i < to.length; i++)
            if(!this.recipientIsAllowed(to[i].getAddress()))
                throw new IllegalArgumentException("Recipient not allowed : " + to[i].getAddress());
        for(int i = 0; i < cc.length; i++)
            if(!this.recipientIsAllowed(cc[i].getAddress()))
                throw new IllegalArgumentException("Recipient not allowed : " + cc[i].getAddress());
        for(int i = 0; i < bcc.length; i++)
            if(!this.recipientIsAllowed(bcc[i].getAddress()))
                throw new IllegalArgumentException("Recipient not allowed : " + bcc[i].getAddress());

    }

    /**
     * Check if recipient is under delegation for delegated admins
     *
     * @param recipient
     * @throws AccessDeniedException if current does not have permissions on recipient
     */
    private void checkAuthorisation(String recipient){
        // check if recipient is under delegation for delegated admins
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!auth.getAuthorities().contains(this.advancedDelegationDao.ROLE_SUPERUSER))
            if(!this.advancedDelegationDao.findUsersUnderDelegation(auth.getName()).contains(recipient))
                throw new AccessDeniedException("User " + recipient + " not under delegation");
    }

    /**
     * Return EMail addresses in JSON array to a String
     * @param field field name under which EMails are store in payload
     * @param payload JsonObject to search EMails addresses
     * @return
     * @throws JSONException
     */
    private String extractAddress(String field, JSONObject payload) throws JSONException {
        StringBuilder res = new StringBuilder();
        if(payload.has(field)){
            JSONArray rawTo = payload.getJSONArray(field);
            for(int i = 0; i < rawTo.length(); i++){
                if(i > 0)
                    res.append(",");
                res.append(rawTo.getString(i));
            }
        }
        return res.toString();
    }

    /**
     * Create an java String list based on json array found in json ojbect
     *
     * @param field field name where to find json array to parse
     * @param request full object where to search for key
     * @return java list of extracted values
     */
    private InternetAddress[] populateRecipient(String field, JSONObject request) throws JSONException, AddressException {
        List<InternetAddress> res = new LinkedList<InternetAddress>();
        if(request.has(field)){
            JSONArray rawTo = request.getJSONArray(field);
            for(int i = 0; i < rawTo.length(); i++){
                InternetAddress to = new InternetAddress();
                to.setAddress(rawTo.getString(i));
                to.validate();
                res.add(to);
            }
        }
        return res.toArray(new InternetAddress[res.size()]);
    }

    private boolean recipientIsAllowed(String recipient) throws DataServiceException {
        // Load configuration if not already loaded
        if(this.recipientWhiteList == null)
            this.recipientWhiteList = Arrays.asList(this.georConfig.getProperty("emailProxyRecipientWhitelist").split("\\s*,\\s*"));

        // Check recipient in whitelist
        if(this.recipientWhiteList.contains(recipient))
            return true;

        // Check recipient in LDAP and against delegation for delegated admins
        try {
            Account account = this.accountDao.findByEmail(recipient);
            if(account == null)
                return false;
            this.checkAuthorisation(account.getUid());
            return true;
        } catch (NameNotFoundException ex){
            return false;
        } catch (AccessDeniedException ex){
            return false;
        }
    }


    /**
     * Send EmailEntry to smtp server
     *
     * @param email email to send
     * @throws NameNotFoundException if recipient cannot be found in LDAP server
     * @throws DataServiceException if LDAP server is not available
     * @throws MessagingException if some field of email cannot be encoded (malformed email address)
     */
    private void send(EmailEntry email) throws NameNotFoundException, DataServiceException, MessagingException {

        final Properties props = System.getProperties();
        props.put("mail.smtp.host", this.emailFactory.getSmtpHost());
        props.put("mail.protocol.port", this.emailFactory.getSmtpPort());

        final Session session = Session.getInstance(props, null);
        final MimeMessage message = new MimeMessage(session);

        Account recipient = this.accountDao.findByUID(email.getRecipient());
        InternetAddress[] senders = {new InternetAddress(this.accountDao.findByUID(email.getSender()).getEmail())};

        message.addFrom(senders);
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.getEmail()));
        message.setSubject(email.getSubject());
        message.setHeader("Date", (new MailDateFormat()).format(email.getDate()));

        // Mail content
        Multipart multiPart = new MimeMultipart("alternative");

        // attachments
        for(Attachment att : email.getAttachments()) {
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setDataHandler(new DataHandler(new ByteArrayDataSource(att.getContent(), att.getMimeType())));
            mbp.setFileName(att.getName());
            multiPart.addBodyPart(mbp);
        }

        // html part
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(email.getBody(), "text/html; charset=utf-8");
        multiPart.addBodyPart(htmlPart);

        message.setContent(multiPart);

        // Send message
        Transport.send(message);
    }

    // Setter for unit tests
    public void setGeorConfig(GeorchestraConfiguration georConfig) {
        this.georConfig = georConfig;
    }

    // Getter for unit tests
    public AccountDao getAccountDao() {
        return accountDao;
    }

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

}
