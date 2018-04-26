/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

package org.georchestra.ldapadmin.model;

import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(schema = "ldapadmin", name = "admin_attachments")
public class Attachment {

    @Id
    @SequenceGenerator(name="admin_attachments_id_seq", schema = "ldapadmin", sequenceName="admin_attachments_id_seq", initialValue=1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "admin_attachments_id_seq")
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
