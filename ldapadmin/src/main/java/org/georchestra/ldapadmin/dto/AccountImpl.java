/**
 * 
 */
package org.georchestra.ldapadmin.dto;

import java.io.Serializable;

/**
 * Account this is a Data transfer Object. 
 *  
 *  
 * @author Mauricio Pazos
 *
 */
class AccountImpl implements Serializable, Account {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8022496448991887664L;
	
	
	// main data
	private String uid; // uid
	private String commonName; // cn mandatory
	private String surname; // sn  mandatory
	private String org; // o
	private String role; // 
	private String email;// mail
	private String phone;// telephoneNumber 
	private String details; // description
	private String password; // userPassword
	private String newPassword;

	// user details
	// sn, givenName, o, title, postalAddress, postalCode, registeredAddress, postOfficeBox, physicalDeliveryOfficeName
	private String givenName; // givenName (optonal)
	private String title; // title
	private String postalAddress; //postalAddress
	private String postalCode; // postalCode
	private String registeredAddress; //registeredAddress 
	private String postOfficeBox; // postOfficeBox
	private String physicalDeliveryOfficeName; //physicalDeliveryOfficeName
	
	@Override
	public String toString() {
		return "Account [uid=" + uid + ", name=" + commonName + ", org=" + org
				+ ", role=" + role 
				+ ", email=" + email + ", phone=" + phone + ", details="
				+ details + ", password=" + password + ", newPassword="
				+ newPassword + ", surname=" + surname + ", givenName="
				+ givenName + ", title=" + title + ", postalAddress="
				+ postalAddress + ", postalCode=" + postalCode
				+ ", registeredAddress=" + registeredAddress
				+ ", postOfficeBox=" + postOfficeBox
				+ ", physicalDeliveryOfficeName=" + physicalDeliveryOfficeName
				+ "]";
	}
	
	@Override
	public void setUid(String uid) {
		this.uid = uid;
	}
	@Override
	public String getUid(){
		return this.uid;
	}
	
	@Override
	public String getCommonName() {
		return commonName;
	}
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
	public String getRole() {
		return role;
	}
	@Override
	public void setRole(String role) {
		this.role = role;
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
	public String getDetails() {
		return details;
	}
	@Override
	public void setDetails(String details) {
		this.details = details;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
		
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

	@Override
	public String getGivenName() {
		return givenName;
	}

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
}
