package org.georchestra.ldapadmin.model;

import javax.persistence.*;

@Entity
@Table(name = "adminAttachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private String content;

    public Attachment() {}

    public Attachment(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
