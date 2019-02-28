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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ldap.core.DirContextAdapter;

import java.util.LinkedList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Org implements Comparable<Org>, ReferenceAware {

    public static final String JSON_ID = "id";
    public static final String JSON_NAME = "name";
    public static final String JSON_SHORT_NAME = "shortName";
    public static final String JSON_CITIES = "cities";
    public static final String JSON_MEMBERS = "members";
    public static final String JSON_PENDING = "pending";
    public static final String JSON_DESCRIPTION = "description";

    private String id;
    private String name;
    private String shortName;
    private List<String> cities = new LinkedList<String>();
    private String status;
    private List<String> members = new LinkedList<String>();
    private boolean isPending;
    private String description = "";

    @JsonIgnore
    private DirContextAdapter reference;

    @JsonIgnore
    private OrgExt orgExt;

    @JsonProperty(JSON_ID)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        if(shortName != null && shortName.length() > 0)
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

    public OrgExt getOrgExt() {
        return orgExt;
    }

    public void setOrgExt(OrgExt orgExt) {
        this.orgExt = orgExt;
    }

    @JsonGetter(OrgExt.JSON_ORG_TYPE)
    public String getOrgType(){
        if(this.orgExt == null)
            return null;
        else
            return this.orgExt.getOrgType();
    }

    @JsonGetter(OrgExt.JSON_ADDRESS)
    public String getOrgAddress(){
        if(this.orgExt == null)
            return null;
        else
            return this.orgExt.getAddress();
    }

    public String toString(){
        return this.getName();
    }

    public int compareTo(Org org) {
        return this.getName().compareToIgnoreCase(org.getName());
    }

    public DirContextAdapter getReference() {
        return reference;
    }

    public void setReference(DirContextAdapter reference) {
        this.reference = reference;
    }

    @JsonProperty(JSON_PENDING)
    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }

    @JsonProperty(JSON_DESCRIPTION)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}