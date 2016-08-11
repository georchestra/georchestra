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

package org.georchestra.ldapadmin.dto;


import org.json.JSONException;
import org.json.JSONObject;

public class OrgExt {

    public static final String JSON_ADDRESS = "address";
    public static final String JSON_ID = "id";
    public static final String JSON_ORG_TYPE = "orgType";

    private String id;
    private String orgType;
    private String address;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject res = new JSONObject();
        res.put(JSON_ID, this.getId());
        res.put(JSON_ORG_TYPE, this.getOrgType());
        res.put(JSON_ADDRESS, this.getAddress());
        return res;
    }

    @Override
    public String toString() {
        return "OrgExt{" +
                "id='" + id + '\'' +
                ", orgType='" + orgType + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}