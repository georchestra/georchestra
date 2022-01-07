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

package org.georchestra.ds.users;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Getter;
import lombok.Setter;

/**
 * Account this is a Data transfer Object.
 *
 *
 * @author Mauricio Pazos
 *
 */
@SuppressWarnings("deprecation")
public class AccountImpl implements Serializable, Account {

    private static final long serialVersionUID = -8022496448991887664L;

    private @Getter @Setter UUID uniqueIdentifier;

    // main data
    @JsonProperty(UserSchema.UID_KEY)
    private String uid; // uid

    @JsonProperty(UserSchema.COMMON_NAME_KEY)
    private String commonName; // cn: person's full name, mandatory

    @JsonProperty(UserSchema.SURNAME_KEY)
    private String surname; // sn mandatory

    @JsonProperty(UserSchema.MAIL_KEY)
    private String email;// mail

    @JsonProperty(UserSchema.TELEPHONE_KEY)
    private String phone;// telephoneNumber

    @JsonProperty(UserSchema.DESCRIPTION_KEY)
    private String description; // description

    @JsonIgnore
    private String password; // userPassword

    // user details
    // sn, givenName, title, postalAddress, postalCode, registeredAddress,
    // postOfficeBox, physicalDeliveryOfficeName
    @JsonProperty(UserSchema.GIVEN_NAME_KEY)
    private String givenName; // givenName (optional)

    @JsonProperty(UserSchema.TITLE_KEY)
    private String title; // title

    @JsonProperty(UserSchema.POSTAL_ADDRESS_KEY)
    private String postalAddress; // postalAddress

    @JsonProperty(UserSchema.POSTAL_CODE_KEY)
    private String postalCode; // postalCode

    @JsonProperty(UserSchema.REGISTERED_ADDRESS_KEY)
    private String registeredAddress; // registeredAddress

    @JsonProperty(UserSchema.POST_OFFICE_BOX_KEY)
    private String postOfficeBox; // postOfficeBox

    @JsonProperty(UserSchema.PHYSICAL_DELIVERY_OFFICE_NAME_KEY)
    private String physicalDeliveryOfficeName; // physicalDeliveryOfficeName

    @JsonProperty(UserSchema.STREET_KEY)
    private String street;

    @JsonProperty(UserSchema.LOCALITY_KEY)
    private String locality; // l

    @JsonProperty(UserSchema.FACSIMILE_KEY)
    private String facsimile;

    @JsonProperty(UserSchema.MOBILE_KEY)
    private String mobile;

    @JsonProperty(UserSchema.ROOM_NUMBER_KEY)
    private String roomNumber;

    @JsonProperty(UserSchema.STATE_OR_PROVINCE_KEY)
    private String stateOrProvince; // st

    @JsonProperty(UserSchema.HOME_POSTAL_ADDRESS_KEY)
    private String homePostalAddress;

    @JsonProperty(UserSchema.SHADOW_EXPIRE_KEY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date shadowExpire;

    @JsonProperty(UserSchema.PRIVACY_POLICY_AGREEMENT_DATE_KEY)
    @JsonSerialize(using = ToStringSerializer.class)
    private LocalDate privacyPolicyAgreementDate;

    @JsonProperty(UserSchema.MANAGER_KEY)
    private String manager;

    @JsonProperty(UserSchema.NOTE_KEY)
    private String note;

    @JsonProperty(UserSchema.CONTEXT_KEY)
    private String context;

    @JsonProperty(UserSchema.SSH_KEY)
    private String[] sshKeys;

    @JsonProperty("saslUser")
    private String saslUser;

    @JsonIgnore
    private PasswordType passwordType;

    // Organization from ou=orgs,dc=georchestra,dc=org
    // Json export is defined on the getter getOrg()
    private String org;
    private boolean pending;

    @Override
    public String toString() {
        return "AccountImpl{" + "manager='" + manager + '\'' + ", uid='" + uid + '\'' + ", commonName='" + commonName
                + '\'' + ", surname='" + surname + '\'' + ", email='" + email + '\'' + ", phone='" + phone + '\''
                + ", description='" + description + '\'' + ", password='" + password + '\'' + ", givenName='"
                + givenName + '\'' + ", title='" + title + '\'' + ", postalAddress='" + postalAddress + '\''
                + ", postalCode='" + postalCode + '\'' + ", registeredAddress='" + registeredAddress + '\''
                + ", postOfficeBox='" + postOfficeBox + '\'' + ", physicalDeliveryOfficeName='"
                + physicalDeliveryOfficeName + '\'' + ", street='" + street + '\'' + ", locality='" + locality + '\''
                + ", facsimile='" + facsimile + '\'' + ", mobile='" + mobile + '\'' + ", roomNumber='" + roomNumber
                + '\'' + ", stateOrProvince='" + stateOrProvince + '\'' + ", homePostalAddress='" + homePostalAddress
                + '\'' + ", shadowExpire='" + shadowExpire + '\'' + ", privacyPolicyAgreementDate='"
                + privacyPolicyAgreementDate + '\'' + ", context='" + context + '\'' + ", note='" + note + '\''
                + ", org='" + org + '\'' + ", sshKeys='" + Arrays.toString(sshKeys) + "', saslUser='" + saslUser + "'}";
    }

    @Override
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String getUid() {
        return this.uid;
    }

    /**
     * Person’s full name.
     */
    @Override
    public String getCommonName() {
        return commonName;
    }

    /**
     * Person’s full name.
     */
    @Override
    public void setCommonName(String name) {
        this.commonName = name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setPassword(String password) {
        if (password == null) {
            this.password = null;
        } else {
            LdapShaPasswordEncoder lspe = new LdapShaPasswordEncoder();
            String encrypted = lspe.encodePassword(password, String.valueOf(System.currentTimeMillis()).getBytes());
            this.password = encrypted;
        }
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * The givenName attribute is used to hold the part of a person’s name which is
     * not their surname nor middle name.
     */
    @Override
    public String getGivenName() {
        return givenName;
    }

    /**
     * The givenName attribute is used to hold the part of a person’s name which is
     * not their surname nor middle name.
     */
    @Override
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getPostalAddress() {
        return postalAddress;
    }

    @Override
    public void setPostalAddress(String postalAddress) {
        this.postalAddress = postalAddress;
    }

    @Override
    public String getPostalCode() {
        return postalCode;
    }

    @Override
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public String getRegisteredAddress() {
        return registeredAddress;
    }

    @Override
    public void setRegisteredAddress(String registeredAddress) {
        this.registeredAddress = registeredAddress;
    }

    @Override
    public String getPostOfficeBox() {
        return postOfficeBox;
    }

    @Override
    public void setPostOfficeBox(String postOfficeBox) {
        this.postOfficeBox = postOfficeBox;
    }

    @Override
    public String getPhysicalDeliveryOfficeName() {
        return physicalDeliveryOfficeName;
    }

    @Override
    public void setPhysicalDeliveryOfficeName(String physicalDeliveryOfficeName) {
        this.physicalDeliveryOfficeName = physicalDeliveryOfficeName;
    }

    @Override
    public void setStreet(String street) {

        this.street = street;
    }

    @Override
    public String getStreet() {
        return this.street;
    }

    @Override
    public void setLocality(String locality) {

        this.locality = locality;
    }

    @Override
    public String getLocality() {

        return this.locality;
    }

    @Override
    public void setFacsimile(String facsimile) {
        this.facsimile = facsimile;
    }

    @Override
    public String getFacsimile() {
        return this.facsimile;
    }

    @Override
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String getMobile() {
        return this.mobile;
    }

    @Override
    public void setRoomNumber(String roomNumber) {

        this.roomNumber = roomNumber;
    }

    @Override
    public String getRoomNumber() {
        return this.roomNumber;
    }

    @Override
    public void setStateOrProvince(String stateOrProvince) {
        this.stateOrProvince = stateOrProvince;
    }

    @Override
    public void setShadowExpire(Date expireDate) {
        this.shadowExpire = expireDate;
    }

    @Override
    public Date getShadowExpire() {
        return this.shadowExpire;
    }

    @Override
    public void setPrivacyPolicyAgreementDate(LocalDate privacyPolicyAgreementDate) {
        this.privacyPolicyAgreementDate = privacyPolicyAgreementDate;
    }

    @Override
    public LocalDate getPrivacyPolicyAgreementDate() {
        return this.privacyPolicyAgreementDate;
    }

    @Override
    public String getManager() {
        return manager;
    }

    @Override
    public void setManager(String manager) {
        if (manager == null || manager.length() == 0) {
            this.manager = null;
        } else {
            try {
                LdapName dn = new LdapName(manager);
                this.manager = dn.getRdn(dn.size() - 1).getValue().toString();
            } catch (InvalidNameException e) {
                this.manager = manager;
            }
        }
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public void setContext(String context) {
        this.context = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((givenName == null) ? 0 : givenName.hashCode());
        result = prime * result + ((surname == null) ? 0 : surname.hashCode());
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AccountImpl account = (AccountImpl) o;
        return Objects.equals(uniqueIdentifier, account.uniqueIdentifier) && Objects.equals(uid, account.uid)
                && Objects.equals(commonName, account.commonName) && Objects.equals(surname, account.surname)
                && Objects.equals(email, account.email) && Objects.equals(phone, account.phone)
                && Objects.equals(description, account.description) && Objects.equals(givenName, account.givenName)
                && Objects.equals(title, account.title) && Objects.equals(postalAddress, account.postalAddress)
                && Objects.equals(postalCode, account.postalCode)
                && Objects.equals(registeredAddress, account.registeredAddress)
                && Objects.equals(postOfficeBox, account.postOfficeBox)
                && Objects.equals(physicalDeliveryOfficeName, account.physicalDeliveryOfficeName)
                && Objects.equals(street, account.street) && Objects.equals(locality, account.locality)
                && Objects.equals(facsimile, account.facsimile) && Objects.equals(mobile, account.mobile)
                && Objects.equals(roomNumber, account.roomNumber)
                && Objects.equals(stateOrProvince, account.stateOrProvince)
                && Objects.equals(homePostalAddress, account.homePostalAddress)
                && Objects.equals(shadowExpire, account.shadowExpire) && Objects.equals(note, account.note)
                && Objects.equals(privacyPolicyAgreementDate, account.privacyPolicyAgreementDate)
                && Objects.equals(manager, account.manager) && Objects.equals(context, account.context)
                && Objects.equals(org, account.org) && Arrays.equals(sshKeys, account.sshKeys);
    }

    @Override
    public String getStateOrProvince() {
        return this.stateOrProvince;
    }

    @Override
    public void setHomePostalAddress(String homePostalAddress) {
        this.homePostalAddress = homePostalAddress;
    }

    @Override
    public String getHomePostalAddress() {
        return this.homePostalAddress;
    }

    @Override
    public void setOrg(String org) {
        this.org = org;
    }

    @Override
    @JsonGetter(UserSchema.ORG_KEY)
    public String getOrg() {
        if (this.org == null)
            return "";
        else
            return this.org;
    }

    public int compareTo(Account o) {
        return o.getUid().compareToIgnoreCase(this.uid);
    }

    @Override
    public boolean isPending() {
        return pending;
    }

    @Override
    public void setPending(boolean pending) {
        this.pending = pending;

    }

    @Override
    public String[] getSshKeys() {
        return this.sshKeys;
    }

    @Override
    public void setSshKeys(String[] sshKeys) {
        this.sshKeys = sshKeys;

    }

    @Override
    public String getSASLUser() {
        return this.saslUser;
    }

    @Override
    public void setSASLUser(String user) {
        this.saslUser = user;
    }

    @Override
    public PasswordType getPasswordType() {
        return passwordType;
    }

    @Override
    public void setPasswordType(PasswordType passwordType) {
        this.passwordType = passwordType;
    }
}
