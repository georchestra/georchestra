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

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

import javax.naming.ldap.LdapName;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder;

import ezvcard.VCard;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.FormattedName;

import javax.naming.InvalidNameException;

/**
 * Account this is a Data transfer Object.
 *
 *
 * @author Mauricio Pazos
 *
 */
public class AccountImpl implements Serializable, Account{

	private static final long serialVersionUID = -8022496448991887664L;

	// main data
	@JsonProperty(UserSchema.UID_KEY)
	private String uid; // uid

	@JsonProperty(UserSchema.COMMON_NAME_KEY)
	private String commonName; // cn: person's full name,  mandatory

	@JsonProperty(UserSchema.SURNAME_KEY)
	private String surname; // sn  mandatory

	@JsonProperty(UserSchema.MAIL_KEY)
	private String email;// mail

	@JsonProperty(UserSchema.TELEPHONE_KEY)
	private String phone;// telephoneNumber

	@JsonProperty(UserSchema.DESCRIPTION_KEY)
	private String description; // description

	@JsonIgnore
	private String password; // userPassword
	@JsonIgnore
	private String newPassword;

	// user details
	// sn, givenName, title, postalAddress, postalCode, registeredAddress, postOfficeBox, physicalDeliveryOfficeName
	@JsonProperty(UserSchema.GIVEN_NAME_KEY)
	private String givenName; // givenName (optional)

	@JsonProperty(UserSchema.TITLE_KEY)
	private String title; // title

	@JsonProperty(UserSchema.POSTAL_ADDRESS_KEY)
	private String postalAddress; //postalAddress

	@JsonProperty(UserSchema.POSTAL_CODE_KEY)
	private String postalCode; // postalCode

	@JsonProperty(UserSchema.REGISTERED_ADDRESS_KEY)
	private String registeredAddress; //registeredAddress

	@JsonProperty(UserSchema.POST_OFFICE_BOX_KEY)
	private String postOfficeBox; // postOfficeBox

	@JsonProperty(UserSchema.PHYSICAL_DELIVERY_OFFICE_NAME_KEY)
	private String physicalDeliveryOfficeName; //physicalDeliveryOfficeName

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
	@JsonFormat(shape = JsonFormat.Shape.STRING,
			pattern = "yyyy-MM-dd")
	private Date shadowExpire;

	@JsonProperty(UserSchema.PRIVACY_POLICY_AGREEMENT_DATE_KEY)
	@JsonSerialize(using = ToStringSerializer.class)
	private LocalDate privacyPolicyAgreementDate;

	@JsonProperty(UserSchema.MANAGER_KEY)
	private String manager;
	
	@JsonProperty(UserSchema.CONTEXT_KEY)
	private String context;

	// Organization from ou=orgs,dc=georchestra,dc=org
	// Json export is defined on the getter getOrg()
	private String org;
	private boolean pending;

	@Override
	public String toString() {
		return "AccountImpl{" +
				"manager='" + manager + '\'' +
				", uid='" + uid + '\'' +
				", commonName='" + commonName + '\'' +
				", surname='" + surname + '\'' +
				", email='" + email + '\'' +
				", phone='" + phone + '\'' +
				", description='" + description + '\'' +
				", password='" + password + '\'' +
				", newPassword='" + newPassword + '\'' +
				", givenName='" + givenName + '\'' +
				", title='" + title + '\'' +
				", postalAddress='" + postalAddress + '\'' +
				", postalCode='" + postalCode + '\'' +
				", registeredAddress='" + registeredAddress + '\'' +
				", postOfficeBox='" + postOfficeBox + '\'' +
				", physicalDeliveryOfficeName='" + physicalDeliveryOfficeName + '\'' +
				", street='" + street + '\'' +
				", locality='" + locality + '\'' +
				", facsimile='" + facsimile + '\'' +
				", mobile='" + mobile + '\'' +
				", roomNumber='" + roomNumber + '\'' +
				", stateOrProvince='" + stateOrProvince + '\'' +
				", homePostalAddress='" + homePostalAddress + '\'' +
				", shadowExpire='" + shadowExpire + '\'' +
				", privacyPolicyAgreementDate='" + privacyPolicyAgreementDate + '\'' +
				", context='" + context + '\'' +
				", org='" + org + '\'' +
				'}';
	}

	@Override
	public String toVcf() {
	    VCard v = new VCard();
	    FormattedName f = new FormattedName(givenName + " " + surname);
	    v.addFormattedName(f);
	    v.addEmail(email, EmailType.WORK);
	    v.addTelephoneNumber(phone, TelephoneType.WORK);
	    v.addTitle(title);
	    Address a = new Address();
	    a.setPostalCode(postalCode);
	    a.setStreetAddress(postalAddress);
	    a.setPoBox(postOfficeBox);
	    a.setLocality(locality);
	    v.addAddress(a);
	    v.addTelephoneNumber(mobile, TelephoneType.CELL);

	    return v.write();
	}

	public String toFormatedString(String data) {

	    String ret = new String("");
	    if (data != null) {
	        ret = data.replace(",",".");
	    }
	    return ret;
	}

	private final String CSV_DELIMITER = ",";
   
   
	@Override
	public String toCsv() {

		StringBuilder csv = new StringBuilder();

		csv.append(toFormatedString(commonName));
		csv.append(CSV_DELIMITER);
		csv.append(CSV_DELIMITER);// Middle Name
		csv.append(toFormatedString(surname));
		csv.append(CSV_DELIMITER);
		csv.append(toFormatedString(title));
		csv.append(CSV_DELIMITER);
		csv.append(CSV_DELIMITER);// Suffix
		csv.append(CSV_DELIMITER); // Initials
		csv.append(CSV_DELIMITER);// Web Page
		csv.append(CSV_DELIMITER); // Gender
		csv.append(CSV_DELIMITER);// Birthday
		csv.append(CSV_DELIMITER); // Anniversary
		csv.append(CSV_DELIMITER);// Location
		csv.append(CSV_DELIMITER); // Language
		csv.append(CSV_DELIMITER);// Internet Free Busy
		csv.append(CSV_DELIMITER); // Notes
		csv.append(toFormatedString(email));
		csv.append(CSV_DELIMITER);
		csv.append(CSV_DELIMITER);// E-mail 2 Address
		csv.append(CSV_DELIMITER); // E-mail 3 Address
		csv.append(toFormatedString(phone));// primary phone
		csv.append(CSV_DELIMITER);
		csv.append(CSV_DELIMITER);// Home Phone
		csv.append(CSV_DELIMITER); // Home Phone 2
		csv.append(toFormatedString(mobile));
		csv.append(CSV_DELIMITER); // Mobile Phone
		csv.append(CSV_DELIMITER);// Pager
		csv.append(CSV_DELIMITER);// Home Fax
		csv.append(toFormatedString(homePostalAddress));
		csv.append(CSV_DELIMITER);// Home Address
		csv.append(CSV_DELIMITER);// Home Street
		csv.append(CSV_DELIMITER);// Home Street 2
		csv.append(CSV_DELIMITER);// Home Street 3
		csv.append(CSV_DELIMITER);// Home Address PO Box
		csv.append(CSV_DELIMITER); // locality
		csv.append(CSV_DELIMITER); // Home City
		csv.append(CSV_DELIMITER);// Home State
		csv.append(CSV_DELIMITER); // Home Postal Code
		csv.append(CSV_DELIMITER);// Home Country
		csv.append(CSV_DELIMITER);// Spouse
		csv.append(CSV_DELIMITER);// Children
		csv.append(toFormatedString(manager)); // Manager's Name
		csv.append(CSV_DELIMITER);// Assistant's Name
		csv.append(CSV_DELIMITER); // Referred By
		csv.append(CSV_DELIMITER);// Company Main Phone
		csv.append(CSV_DELIMITER);// Business Phone
		csv.append(CSV_DELIMITER);// Business Phone 2
		csv.append(toFormatedString(facsimile));
		csv.append(CSV_DELIMITER); // Business Fax
		csv.append(CSV_DELIMITER);// Assistant's Phone
		csv.append(CSV_DELIMITER); // Organization
		csv.append(CSV_DELIMITER); // Company
		csv.append(toFormatedString(description));
		csv.append(CSV_DELIMITER);// Job Title
		csv.append(CSV_DELIMITER);// Department
		csv.append(CSV_DELIMITER);// Office Location
		csv.append(CSV_DELIMITER);// Organizational ID Number
		csv.append(CSV_DELIMITER);// Profession
		csv.append(CSV_DELIMITER); // Account
		csv.append(toFormatedString(postalAddress));
		csv.append(CSV_DELIMITER);// Business Address
		csv.append(toFormatedString(street));
		csv.append(CSV_DELIMITER);// Business Street
		csv.append(CSV_DELIMITER);// Business Street 2
		csv.append(CSV_DELIMITER); // Business Street 3
		csv.append(toFormatedString(postOfficeBox));
		csv.append(CSV_DELIMITER);// Business Address PO Box
		csv.append(CSV_DELIMITER);// Business City
		csv.append(CSV_DELIMITER);// Business State
		csv.append(toFormatedString(postalCode));
		csv.append(CSV_DELIMITER); // Business Postal Code
		csv.append(toFormatedString(stateOrProvince));
		csv.append(CSV_DELIMITER);// Business Country
		csv.append(CSV_DELIMITER);// Other Phone
		csv.append(CSV_DELIMITER);// Other Fax
		csv.append(toFormatedString(registeredAddress));
		csv.append(CSV_DELIMITER); // Other Address
		csv.append(toFormatedString(physicalDeliveryOfficeName));
		csv.append(CSV_DELIMITER);// Other Street
		csv.append(CSV_DELIMITER);// Other Street 2
		csv.append(CSV_DELIMITER);// Other Street 3
		csv.append(CSV_DELIMITER);// Other Address PO Box
		csv.append(CSV_DELIMITER); // Other City
		csv.append(CSV_DELIMITER);// Other State
		csv.append(CSV_DELIMITER);// Other Postal Code
		csv.append(CSV_DELIMITER);// Other Country
		csv.append(CSV_DELIMITER); // Callback
		csv.append(CSV_DELIMITER);// Car Phone
		csv.append(CSV_DELIMITER);// ISDN
		csv.append(CSV_DELIMITER);// Radio Phone
		csv.append(CSV_DELIMITER);// TTY/TDD Phone
		csv.append(CSV_DELIMITER); // Telex
		csv.append(CSV_DELIMITER);// User 1
		csv.append(CSV_DELIMITER);// User 2
		csv.append(CSV_DELIMITER);// User 3
		csv.append(CSV_DELIMITER); // User 4
		csv.append(CSV_DELIMITER);// Keywords
		csv.append(CSV_DELIMITER);// Mileage
		csv.append(CSV_DELIMITER);// Hobby
		csv.append(CSV_DELIMITER);// Billing Information
		csv.append(CSV_DELIMITER); // Directory Server
		csv.append(CSV_DELIMITER);// Sensitivity
		csv.append(CSV_DELIMITER);// Priority
		csv.append(CSV_DELIMITER);// Private
		csv.append(CSV_DELIMITER); // Categories
		csv.append("\r\n"); // CRLF
		return csv.toString();

	}

	@Override
	public void setUid(String uid) {
		this.uid = uid;
	}
	@Override
	public String getUid(){
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
		LdapShaPasswordEncoder lspe = new LdapShaPasswordEncoder();
		String encrypted = lspe.encodePassword(password,
					String.valueOf(System.currentTimeMillis()).getBytes());
		this.password = encrypted;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setNewPassword(String newPassword) {
		LdapShaPasswordEncoder lspe = new LdapShaPasswordEncoder();
		String encrypted = lspe.encodePassword(newPassword,
					String.valueOf(System.currentTimeMillis()).getBytes());
		this.newPassword = encrypted;

	}
	@Override
	public String getNewPassword() {
		return this.newPassword;

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
	 * The givenName attribute is used to hold the part of a person’s name which is not their surname nor middle name.
	 */
	@Override
	public String getGivenName() {
		return givenName;
	}

	/**
	 * The givenName attribute is used to hold the part of a person’s name which is not their surname nor middle name.
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
	public void setMobile( String mobile) {
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
	public void  setStateOrProvince(String stateOrProvince) {
		this.stateOrProvince = stateOrProvince;
	}

	@Override
	public void setShadowExpire(Date expireDate) { this.shadowExpire = expireDate; }

	@Override
	public Date getShadowExpire() { return this.shadowExpire; }

	@Override
	public void setPrivacyPolicyAgreementDate(LocalDate privacyPolicyAgreementDate) { this.privacyPolicyAgreementDate = privacyPolicyAgreementDate; }

	@Override
	public LocalDate getPrivacyPolicyAgreementDate() { return this.privacyPolicyAgreementDate; }

	@Override
	public String getManager() {
		return manager;
	}

	@Override
	public void setManager(String manager) {
		if (manager == null || manager.length() == 0){
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
	public String getContext() {
		return context;
	}

	@Override
	public void setContext(String context) {
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result
	            + ((givenName == null) ? 0 : givenName.hashCode());
	    result = prime * result + ((surname == null) ? 0 : surname.hashCode());
	    result = prime * result + ((uid == null) ? 0 : uid.hashCode());
	    return result;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AccountImpl account = (AccountImpl) o;
		return Objects.equals(uid, account.uid) &&
				Objects.equals(commonName, account.commonName) &&
				Objects.equals(surname, account.surname) &&
				Objects.equals(email, account.email) &&
				Objects.equals(phone, account.phone) &&
				Objects.equals(description, account.description) &&
				Objects.equals(givenName, account.givenName) &&
				Objects.equals(title, account.title) &&
				Objects.equals(postalAddress, account.postalAddress) &&
				Objects.equals(postalCode, account.postalCode) &&
				Objects.equals(registeredAddress, account.registeredAddress) &&
				Objects.equals(postOfficeBox, account.postOfficeBox) &&
				Objects.equals(physicalDeliveryOfficeName, account.physicalDeliveryOfficeName) &&
				Objects.equals(street, account.street) &&
				Objects.equals(locality, account.locality) &&
				Objects.equals(facsimile, account.facsimile) &&
				Objects.equals(mobile, account.mobile) &&
				Objects.equals(roomNumber, account.roomNumber) &&
				Objects.equals(stateOrProvince, account.stateOrProvince) &&
				Objects.equals(homePostalAddress, account.homePostalAddress) &&
				Objects.equals(shadowExpire, account.shadowExpire) &&
				Objects.equals(privacyPolicyAgreementDate, account.privacyPolicyAgreementDate) &&
				Objects.equals(manager, account.manager) &&
				Objects.equals(context, account.context) &&
				Objects.equals(org, account.org);
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
		if(this.org == null)
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
}
