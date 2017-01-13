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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Org {

    public static final String JSON_ID = "id";
    public static final String JSON_NAME = "name";
    public static final String JSON_SHORT_NAME = "shortName";
    public static final String JSON_CITIES = "cities";
    public static final String JSON_STATUS = "status";
    public static final String JSON_MEMBERS = "members";

    public static final String STATUS_REGISTERED = "REGISTERED";
    public static final String STATUS_PENDING = "PENDING";

    private String id;
    private String name;
    private String shortName;
    private List<String> cities;
    private String status;
    private List<String> members;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        if(shortName != null && shortName.length() > 0)
            this.shortName = shortName;
    }

    public List<String> getCities() {
        return cities;
    }

    public void setCities(List<String> cities) {
        this.cities = cities;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject res = new JSONObject();
        res.put(JSON_ID, this.getId());
        res.put(JSON_NAME, this.getName());
        res.put(JSON_SHORT_NAME, this.getShortName());

        JSONArray cities = new JSONArray();
        if(this.getCities() != null)
            for(String city : this.getCities())
                cities.put(city);
        res.put(JSON_CITIES, cities);

        if(this.getStatus() != null)
            res.put(JSON_STATUS, this.getStatus());

        JSONArray members = new JSONArray();
        if(this.getMembers() != null)
            for(String member : this.getMembers())
                members.put(member);
        res.put(JSON_MEMBERS, members);
        return res;
    }

    public String toString(){
        return this.getName();
    }

    public static Org createBrief(String name, String shortName){
        Org res = new Org();
        res.setName(name);
        res.setShortName(shortName);
        res.setId(name.replaceAll("[^\\w]", "_"));
        return res;

    }
}