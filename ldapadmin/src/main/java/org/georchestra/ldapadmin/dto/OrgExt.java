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

package org.georchestra.console.dto;


import org.json.JSONException;
import org.json.JSONObject;

public class OrgExt {

    public static final String JSON_ADDRESS = "address";
    public static final String JSON_ID = "id";
    public static final String JSON_ORG_TYPE = "orgType";
    private static final String JSON_NUMERIC_ID = "numericId";

    private String id;
    private String orgType;
    private String address;
    private Integer numericId;

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

    public Integer getNumericId() {
        return numericId;
    }

    public void setNumericId(Integer numericId) {
        this.numericId = numericId;
    }


    public JSONObject toJson() throws JSONException {
        JSONObject res = new JSONObject();
        res.put(JSON_ID, this.getId());
        if(this.getOrgType() != null)
            res.put(JSON_ORG_TYPE, this.getOrgType());
        if(this.getAddress() != null)
            res.put(JSON_ADDRESS, this.getAddress());
        if(this.getNumericId() != null)
            res.put(JSON_NUMERIC_ID, this.getNumericId());
        return res;
    }

    @Override
    public String toString() {
        return "OrgExt{" +
                "id='" + id + '\'' +
                ", orgType='" + orgType + '\'' +
                ", address='" + address + '\'' +
                ", numericId='" + numericId + '\'' +
                '}';
    }
}