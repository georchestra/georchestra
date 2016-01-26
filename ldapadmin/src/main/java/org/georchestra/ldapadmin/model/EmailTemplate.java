package org.georchestra.ldapadmin.model;

import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;

@Entity
@Table(schema = "ldapadmin", name = "email_template")
public class EmailTemplate {

    @Id
    @SequenceGenerator(name="email_template_seq", schema = "ldapadmin", sequenceName="email_template_seq", initialValue=1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_template_seq")
    private long id;
    private String name;
    private String content;

    public EmailTemplate() {}

    public EmailTemplate(long id, String name, String content) {
        this.id = id;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("id", this.getId());
        res.put("name", this.getName());
        res.put("content", this.getContent());
        return res;
    }
}
