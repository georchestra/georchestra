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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.naming.ldap.LdapName;
import org.json.JSONException;
import org.json.JSONObject;
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
public class AccountImpl implements Serializable, Account, Comparable<Account>{

	private static final long serialVersionUID = -8022496448991887664L;

	// main data
	private String uid; // uid

	private String commonName; // cn: person's full name,  mandatory
	private String surname; // sn  mandatory

	private String email;// mail
	private String phone;// telephoneNumber
	private String description; // description
	private String password; // userPassword
	private String newPassword;

	// user details
	// sn, givenName, title, postalAddress, postalCode, registeredAddress, postOfficeBox, physicalDeliveryOfficeName
	private String givenName; // givenName (optional)
	private String title; // title
	private String postalAddress; //postalAddress
	private String postalCode; // postalCode
	private String registeredAddress; //registeredAddress
	private String postOfficeBox; // postOfficeBox
	private String physicalDeliveryOfficeName; //physicalDeliveryOfficeName

	private String street;

	private String locality; // l

	private String facsimile;

	private String mobile;

	private String roomNumber;

	private String stateOrProvince; // st

	private String homePostalAddress;

	private Date shadowExpire;

	private String manager;
	
	private String context;

	// Organization from ou=orgs,dc=georchestra,dc=org
	private String org;

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
	@Override
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
	public JSONObject toJSON() throws JSONException {
		JSONObject res = new JSONObject();
		res.put(UserSchema.UID_KEY, this.uid);
		res.put(UserSchema.COMMON_NAME_KEY, this.commonName);
		res.put(UserSchema.SURNAME_KEY, this.surname);
		res.put(UserSchema.MAIL_KEY, this.email);
		res.put(UserSchema.TELEPHONE_KEY, this.phone);
		res.put(UserSchema.DESCRIPTION_KEY, this.description);
		res.put(UserSchema.GIVEN_NAME_KEY, this.givenName);
		res.put(UserSchema.TITLE_KEY, this.title);
		res.put(UserSchema.POSTAL_ADDRESS_KEY, this.postalAddress);
		res.put(UserSchema.POSTAL_CODE_KEY, this.postalCode);
		res.put(UserSchema.REGISTERED_ADDRESS_KEY, this.registeredAddress);
		res.put(UserSchema.POST_OFFICE_BOX_KEY, this.postOfficeBox);
		res.put(UserSchema.PHYSICAL_DELIVERY_OFFICE_NAME_KEY, this.physicalDeliveryOfficeName);
		res.put(UserSchema.STREET_KEY, this.street);
		res.put(UserSchema.LOCALITY_KEY, this.locality);
		res.put(UserSchema.FACSIMILE_KEY, this.facsimile);
		res.put(UserSchema.MOBILE_KEY, this.mobile);
		res.put(UserSchema.ROOM_NUMBER_KEY, this.roomNumber);
		res.put(UserSchema.STATE_OR_PROVINCE_KEY, this.stateOrProvince);
		res.put(UserSchema.HOME_POSTAL_ADDRESS_KEY, this.homePostalAddress);
		if(this.shadowExpire != null) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			res.put(UserSchema.SHADOW_EXPIRE_KEY, dateFormat.format(this.shadowExpire));
		}
		if(this.manager != null)
			res.put(UserSchema.MANAGER_KEY, this.manager);
		if(this.context != null)
			res.put(UserSchema.CONTEXT_KEY, this.context);
		res.put(UserSchema.ORG_KEY, this.getOrg());
		return res;
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
	public String getManager() {
		return manager;
	}

	@Override
	public void setManager(String manager) {
		if(manager != null) {
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
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    @Override
    public boolean equals(Object obj) {
	    if (this == obj) {
		    return true;
	    }
	    if (obj == null) {
		    return false;
	    }
	    if (!(obj instanceof AccountImpl)) {
		    return false;
	    }
	    AccountImpl other = (AccountImpl) obj;
	    if (givenName == null) {
		    if (other.givenName != null) {
			    return false;
		    }
	    } else if (!givenName.equals(other.givenName)) {
		    return false;
	    }
	    if (surname == null) {
		    if (other.surname != null) {
			    return false;
		    }
	    } else if (!surname.equals(other.surname)) {
		    return false;
	    }
	    if (uid == null) {
		    if (other.uid != null) {
			    return false;
		    }
	    } else if (!uid.equals(other.uid)) {
		    return false;
	    }
	    return true;
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
	public String getOrg() {
		if(this.org == null)
			return "";
		else
			return this.org;
	}

	@Override
    public int compareTo(Account o) {
		return this.uid.compareTo(o.getUid());
    }
}
