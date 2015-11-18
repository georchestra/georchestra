package org.georchestra.ldapadmin.model;

import javax.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "adminEmails")
public class EmailEntry {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    private UUID sender;
    private UUID recipient;
    private String subject;
    private String body;

    @ManyToMany(targetEntity = Attachment.class)
    private List<Integer> attachments;

    public EmailEntry(){}

    public EmailEntry(long id, UUID sender, UUID recipient, String subject, String body, List<Integer> attachments) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
    }
    public long getId() { return this.id; }

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

    public List<Integer> getAttachments() {
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

    public void setAttachments(List<Integer> attachments) {
        this.attachments = attachments;
    }
}
