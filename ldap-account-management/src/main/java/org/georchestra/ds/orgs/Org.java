/*
 * Copyright (C) 2009-2022 by the geOrchestra PSC
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

package org.georchestra.ds.orgs;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/**
 * Represents a geOrchestra Organization unit.
 * 
 * @implNote as an implementation detail, non standard LDAP organization
 *           properties are delegated to an {@link OrgExt} instance variable.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Org extends ReferenceAware implements Comparable<Org>, Cloneable {

    public static final String JSON_UUID = "uuid";
    public static final String JSON_ID = "id";
    public static final String JSON_NAME = "name";
    public static final String JSON_SHORT_NAME = "shortName";
    public static final String JSON_CITIES = "cities";
    public static final String JSON_NOTE = "note";
    public static final String JSON_MEMBERS = "members";
    public static final String JSON_PENDING = "pending";
    public static final String JSON_DESCRIPTION = "description";
    public static final String JSON_URL = "url";
    public static final String JSON_LOGO = "logo";
    public static final String JSON_ADDRESS = "address";
    public static final String JSON_ORG_TYPE = "orgType";

    private String id;
    private String name;
    private String shortName;
    private List<String> cities = new LinkedList<String>();
    private List<String> members = new LinkedList<String>();

    @JsonIgnore
    private @NonNull OrgExt ext = new OrgExt();

    Org setOrgExt(OrgExt orgExt) {
        this.ext = orgExt == null ? new OrgExt() : orgExt;
        this.ext.setId(this.getId());
        this.ext.setPending(this.isPending());
        return this;
    }

    OrgExt getExt() {
        return this.ext;
    }

    @Override
    public void setPending(boolean pending) {
        super.setPending(pending);
        ext.setPending(pending);
    }

    public void setId(String id) {
        this.id = id;
        this.ext.setId(id);
    }

    @JsonProperty(JSON_ID)
    public String getId() {
        return id;
    }

    @JsonProperty(JSON_NAME)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty(JSON_SHORT_NAME)
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @JsonProperty(JSON_CITIES)
    public List<String> getCities() {
        return cities;
    }

    public void setCities(List<String> cities) {
        this.cities = cities;
    }

    @JsonProperty(JSON_MEMBERS)
    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    @Override
    public int compareTo(Org org) {
        return this.getName().compareToIgnoreCase(org.getName());
    }

    @Override
    @JsonProperty(JSON_PENDING)
    public boolean isPending() {
        return isPending;
    }

    @Override
    public Org clone() {
        try {
            return (Org) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    // property delegation to OrgExt, this is just an implementation detail //

    @JsonGetter(JSON_ORG_TYPE)
    public String getOrgType() {
        return this.ext.getOrgType();
    }

    public void setOrgType(String orgType) {
        this.ext.setOrgType(orgType);
    }

    @JsonGetter(JSON_ADDRESS)
    public String getAddress() {
        return this.ext.getAddress();
    }

    public void setAddress(String address) {
        this.ext.setAddress(address);
    }

    @JsonProperty(JSON_DESCRIPTION)
    public String getDescription() {
        return ext.getDescription();
    }

    public void setDescription(String description) {
        this.ext.setDescription(description);
    }

    @JsonProperty(JSON_NOTE)
    public String getNote() {
        return ext.getNote();
    }

    public void setNote(String note) {
        this.ext.setNote(note);
    }

    @JsonProperty(JSON_URL)
    public String getUrl() {
        return ext.getUrl();
    }

    public void setUrl(String url) {
        this.ext.setUrl(url);
    }

    @JsonProperty(JSON_LOGO)
    public String getLogo() {
        return ext.getLogo();
    }

    public void setLogo(String logo) {
        this.ext.setLogo(logo);
    }

    @JsonProperty(JSON_UUID)
    public UUID getUniqueIdentifier() {
        return ext.getUniqueIdentifier();
    }

    public void setUniqueIdentifier(UUID uuid) {
        this.ext.setUniqueIdentifier(uuid);
    }
}
