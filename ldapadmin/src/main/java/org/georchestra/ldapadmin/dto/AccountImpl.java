/**
 *
 */
package org.georchestra.ldapadmin.dto;

import java.io.Serializable;

import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder;

import ezvcard.VCard;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.FormattedName;
import ezvcard.property.Organization;

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

	private String org; // o
	private String email;// mail
	private String phone;// telephoneNumber
	private String description; // description
	private String password; // userPassword
	private String newPassword;

	// user details
	// sn, givenName, o, title, postalAddress, postalCode, registeredAddress, postOfficeBox, physicalDeliveryOfficeName
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


	private String organizationalUnit; // ou


	private String homePostalAddres;


	@Override
	public String toString() {
		return "AccountImpl [uid=" + uid + ", commonName=" + commonName
				+ ", surname=" + surname + ", org=" + org
				+ ", email=" + email + ", phone=" + phone + ", description="
				+ description + ", password=" + password + ", newPassword="
				+ newPassword + ", givenName=" + givenName + ", title=" + title
				+ ", postalAddress=" + postalAddress + ", postalCode="
				+ postalCode + ", registeredAddress=" + registeredAddress
				+ ", postOfficeBox=" + postOfficeBox
				+ ", physicalDeliveryOfficeName=" + physicalDeliveryOfficeName
				+ ", street=" + street + ", locality=" + locality
				+ ", facsimile=" + facsimile + ", mobile=" + mobile
				+ ", roomNumber=" + roomNumber + ", stateOrProvince="
				+ stateOrProvince + ", organizationalUnit="
				+ organizationalUnit + ", homePostalAddres=" + homePostalAddres
				+ "]";
	}

	@Override
	public String toVcf() {
	    VCard v = new VCard();
	    FormattedName f = new FormattedName(givenName + " " + surname);
	    v.addFormattedName(f);
	    Organization org = new Organization();
	    org.addValue(this.org);
	    org.addValue(this.organizationalUnit);
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
	public String toCsv() {
		return null;
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
	public String getOrg() {
		return org;
	}
	@Override
	public void setOrg(String org) {
		this.org = org;
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
	public void setOrganizationalUnit(String organizationalUnit) {
		this.organizationalUnit = organizationalUnit;
	}

	@Override
	public String getOrganizationalUnit() {
		return this.organizationalUnit;
	}

	@Override
	public void setHomePostalAddress(String homePostalAddres) {
		this.homePostalAddres = homePostalAddres;
	}

	@Override
	public String getHomePostalAddress() {
		return this.homePostalAddres;
	}
	@Override
    public int compareTo(Account o) {
		return this.uid.compareTo(o.getUid());
    }
}
