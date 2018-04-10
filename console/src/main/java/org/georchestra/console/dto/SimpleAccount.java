package org.georchestra.console.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SimpleAccount {

    @JsonProperty(UserSchema.UID_KEY)
    private String uid;

    @JsonProperty(UserSchema.GIVEN_NAME_KEY)
    private String givenName;

    @JsonProperty(UserSchema.SURNAME_KEY)
    private String surname;

    @JsonProperty(UserSchema.ORG_KEY)
    private String orgName;

    @JsonIgnore
    private String orgId;

    @JsonProperty(UserSchema.MAIL_KEY)
    private String email;

    public SimpleAccount(Account account) {
        this.uid = account.getUid();
        this.givenName = account.getGivenName();
        this.surname = account.getSurname();
        this.orgId = account.getOrg();
        this.email = account.getEmail();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
