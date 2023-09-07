/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/**
 * Represents a geOrchestra Organization unit.
 *
 * @implNote as an implementation detail, non standard LDAP organization
 *           properties are delegated to an {@link OrgExt} instance variable.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Org extends ReferenceAware implements Comparable<Org>, Cloneable {

    private String id;
    private String name;
    private String shortName;
    private List<String> cities = new LinkedList<String>();
    private List<String> members = new LinkedList<String>();

    @JsonIgnore
    private @NonNull OrgExt ext = new OrgExt();

    @Override
    public void setPending(boolean pending) {
        super.setPending(pending);
        ext.setPending(pending);
    }

    public void setId(String id) {
        this.id = id;
        this.ext.setId(id);
    }

    @Override
    public int compareTo(Org org) {
        return this.getName().compareToIgnoreCase(org.getName());
    }

    @Override
    public Org clone() {
        try {
            Org clone = (Org) super.clone();
            clone.setCities(new LinkedList<>(cities));
            clone.setMembers(new LinkedList<>(members));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    Org setOrgExt(OrgExt orgExt) {
        this.ext = orgExt == null ? new OrgExt() : orgExt;
        this.ext.setId(this.getId());
        this.ext.setPending(this.isPending());
        return this;
    }

    OrgExt getExt() {
        return this.ext;
    }

    // property delegation to OrgExt, this is just an implementation detail //
    @JsonProperty("orgType")
    public String getOrgType() {
        return this.ext.getOrgType();
    }

    public void setOrgType(String orgType) {
        this.ext.setOrgType(orgType);
    }

    @JsonProperty("address")
    public String getAddress() {
        return this.ext.getAddress();
    }

    public void setAddress(String address) {
        this.ext.setAddress(address);
    }

    @JsonProperty("description")
    public String getDescription() {
        return ext.getDescription();
    }

    public void setDescription(String description) {
        this.ext.setDescription(description);
    }

    @JsonProperty("note")
    public String getNote() {
        return ext.getNote();
    }

    public void setNote(String note) {
        this.ext.setNote(note);
    }

    @JsonProperty("url")
    public String getUrl() {
        return ext.getUrl();
    }

    public void setUrl(String url) {
        this.ext.setUrl(url);
    }

    @JsonProperty("logo")
    public String getLogo() {
        return ext.getLogo();
    }

    public void setLogo(String logo) {
        this.ext.setLogo(logo);
    }

    @JsonProperty("uuid")
    public UUID getUniqueIdentifier() {
        return ext.getUniqueIdentifier();
    }

    public void setUniqueIdentifier(UUID uuid) {
        this.ext.setUniqueIdentifier(uuid);
    }

    @JsonProperty("mail")
    public String getMail() {
        return this.ext.getMail();
    }

    public void setMail(String mail) {
        this.ext.setMail(mail);
    }
}
