package org.georchestra.ldapadmin.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "ldapadmin", name = "admin_emails")
public class EmailEntry {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    private UUID sender;
    private UUID recipient;
    private String subject;
    private Date date;
    private String body;

    @ManyToMany(targetEntity = Attachment.class, fetch = FetchType.EAGER)
    @JoinTable(schema = "ldapadmin", name="admin_emails_attchments")
    private List<Attachment> attachments;

    public EmailEntry(){}

    public EmailEntry(long id, UUID sender, UUID recipient, String subject, Date date, String body, List<Attachment> attachments) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.date = date;
        this.body = body;
        this.attachments = attachments;
    }


    public JSONObject toJSON() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("id", this.getId());
        res.put("sender", this.getSender());
        res.put("recipient", this.getRecipient());
        res.put("subject", this.getSubject());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        res.put("date", dateFormat.format(this.getDate()));
        res.put("body", this.getBody());
        JSONArray array = new JSONArray();
        for(Attachment att : this.getAttachments())
            array.put(att.toJSON());
        res.put("attachments", array);
        return res;
    }


    /*
     * Generic getter, setter
     */
    public long getId() { return this.id; }

    public void setId(long id) {
        this.id = id;
    }

    public UUID getSender() {
        return this.sender;
    }

    public UUID getRecipient() {
        return this.recipient;
    }

    public String getSubject(){
        return this.subject;
    }

    public String getBody() {
        return this.body;
    }

    public List<Attachment> getAttachments() {
        return this.attachments;
    }

    public void setSender(UUID sender) {
        this.sender = sender;
    }

    public void setRecipient(UUID recipient) {
        this.recipient = recipient;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
