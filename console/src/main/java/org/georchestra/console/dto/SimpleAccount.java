/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

import java.time.LocalDate;

import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.UserSchema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class SimpleAccount {

    @JsonProperty(UserSchema.UID_KEY)
    private String uid;

    @JsonProperty(UserSchema.GIVEN_NAME_KEY)
    private String givenName;

    @JsonProperty(UserSchema.SURNAME_KEY)
    private String surname;

    @JsonProperty(UserSchema.ORG_KEY)
    private String orgName;

    @JsonProperty(UserSchema.ORG_ID_KEY)
    private String orgId;

    @JsonProperty(UserSchema.MAIL_KEY)
    private String email;

    @JsonProperty(UserSchema.PENDING)
    private boolean pending;

    @JsonProperty(UserSchema.PRIVACY_POLICY_AGREEMENT_DATE_KEY)
    @JsonSerialize(using = ToStringSerializer.class)
    private LocalDate privacyPolicyAgreementDate;

    public SimpleAccount(Account account) {
        this.uid = account.getUid();
        this.givenName = account.getGivenName();
        this.surname = account.getSurname();
        this.orgId = account.getOrg();
        this.email = account.getEmail();
        this.pending = account.isPending();
        this.privacyPolicyAgreementDate = account.getPrivacyPolicyAgreementDate();
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

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public LocalDate getPrivacyPolicyAgreementDate() {
        return this.privacyPolicyAgreementDate;
    }

    public void setPrivacyPolicyAgreementDate(LocalDate privacyPolicyAgreementDate) {
        this.privacyPolicyAgreementDate = privacyPolicyAgreementDate;
    }
}
