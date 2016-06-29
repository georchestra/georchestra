package org.georchestra.ldapadmin.dto;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Org {

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
        res.put("id", this.getId());
        res.put("name", this.getName());
        res.put("shortName", this.getShortName());
        JSONArray cities = new JSONArray();
        for(String city : this.getCities())
            cities.put(city);
        res.put("cities", cities);
        res.put("status", this.getStatus());
        JSONArray members = new JSONArray();
        for(String member : this.getMembers())
            members.put(member);
        res.put("members", members);
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