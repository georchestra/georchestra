package org.georchestra.ldapadmin.model;

import org.json.JSONException;
import org.json.JSONObject;

import javax.activation.MimeType;
import javax.persistence.*;

@Entity
@Table(schema = "ldapadmin", name = "admin_attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private String name;
    @Lob
    private byte[] content;
    private String mimeType;

    public Attachment() {}

    public Attachment(String name, String mimeType, byte[] content) {
        this.name = name;
        this.mimeType = mimeType;
        this.content = content;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("id", this.getId());
        res.put("name", this.getName());
        res.put("mimeType", this.getMimeType());
        res.put("size", this.content.length);
        return res;
    }

    /*
     * Generic getter, setter
     */

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getContent() { return content; }

    public void setContent(byte[] content) { this.content = content; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getMimeType() { return mimeType; }

    public void setMimeType(String mimeType) { this.mimeType = mimeType; }


}
