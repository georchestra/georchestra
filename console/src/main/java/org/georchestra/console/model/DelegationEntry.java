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

package org.georchestra.console.model;

import org.hibernate.annotations.Type;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(schema = "console", name = "delegations")
public class DelegationEntry {

    @Id
    private String uid;
    @Type(type = "org.georchestra.commons.PostGresArrayStringType")
    @Column(name = "orgs", columnDefinition = "character varying[]")
    private String[] orgs;
    @Type(type = "org.georchestra.commons.PostGresArrayStringType")
    @Column(name = "roles", columnDefinition = "character varying[]")
    private String[] roles;

    public DelegationEntry() {}

    public DelegationEntry(String uid, String[] orgs, String[] roles) {
        this.uid = uid;
        this.orgs = orgs;
        this.roles = roles;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String[] getOrgs() {
        return orgs;
    }

    public void setOrgs(String[] orgs) {
        this.orgs = orgs;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("uid", this.uid);
        res.put("orgs", new JSONArray(this.orgs));
        res.put("roles", new JSONArray(this.roles));
        return res;
    }
}
