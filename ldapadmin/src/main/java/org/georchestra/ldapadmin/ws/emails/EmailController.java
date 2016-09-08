/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

package org.georchestra.ldapadmin.ws.emails;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.dao.AdminLogDao;
import org.georchestra.ldapadmin.dao.AttachmentDao;
import org.georchestra.ldapadmin.dao.EmailDao;
import org.georchestra.ldapadmin.dao.EmailTemplateDao;
import org.georchestra.ldapadmin.ds.AccountDao;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.dto.Account;
import org.georchestra.ldapadmin.mailservice.EmailFactoryImpl;
import org.georchestra.ldapadmin.model.AdminLogEntry;
import org.georchestra.ldapadmin.model.AdminLogType;
import org.georchestra.ldapadmin.model.Attachment;
import org.georchestra.ldapadmin.model.EmailEntry;
import org.georchestra.ldapadmin.model.EmailTemplate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    private static final Log LOG = LogFactory.getLog(EmailController.class.getName());

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

        JSONArray emails = new JSONArray();
        for(EmailEntry email : this.emailRepository.findByRecipient(recipient))
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
                            HttpServletResponse response) throws NameNotFoundException, DataServiceException, MessagingException, IOException {
        try {
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
						
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new IOException(ex);
        }
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

}
