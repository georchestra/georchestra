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

package org.georchestra.console.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.georchestra.console.ds.OrgsDao;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.ldap.core.DirContextAdapter;

public class OrgExt implements ReferenceAware {

    public static final String JSON_ADDRESS = "address";
    public static final String JSON_ORG_TYPE = "orgType";

    private String id;
    private String orgType;
    private String address;
    private String description;

    @JsonIgnore
    private boolean isPending;

    @JsonIgnore
    private DirContextAdapter reference;

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

    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "OrgExt{" +
                "id='" + id + '\'' +
                ", orgType='" + orgType + '\'' +
                ", address='" + address + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public DirContextAdapter getReference() {
        return reference;
    }

    public void setReference(DirContextAdapter reference) {
        this.reference = reference;
    }

    @Override
    public OrgsDao.Extension getExtension(OrgsDao orgDao) {
        return orgDao.getExtension(this);
    }
}