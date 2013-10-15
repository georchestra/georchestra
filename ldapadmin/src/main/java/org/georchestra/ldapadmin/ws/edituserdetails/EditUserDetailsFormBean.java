/**
 * 
 */
package org.georchestra.ldapadmin.ws.edituserdetails;

/**
 * @author Mauricio Pazos
 *
 */
public class EditUserDetailsFormBean {
	
	private String uid; 
	private String surname; 
	private String firstName; 
	private String email;
	private String title; 
	private String phone;
	private String org;
	private String description;
	private String postalAddress; 
	private String postalCode; 
	private String registeredAddress;  
	private String postOfficeBox; 
	private String physicalDeliveryOfficeName;
	
	@Override
	public String toString() {
		return "EditUserDetailsFormBean [uid=" + uid + ", surname=" + surname
				+ ", givenName=" + firstName + ", email=" + email + ", title=" + title
				+ ", phone=" + phone + ", org=" + org + ", description="
				+ description + ", postalAddress=" + postalAddress + ", postalCode="
				+ postalCode + ", registeredAddress=" + registeredAddress
				+ ", postOfficeBox=" + postOfficeBox
				+ ", physicalDeliveryOfficeName=" + physicalDeliveryOfficeName
				+ "]";
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String givenName) {
		this.firstName = givenName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getOrg() {
		return org;
	}
	public void setOrg(String org) {
		this.org = org;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPostalAddress() {
		return postalAddress;
	}
	public void setPostalAddress(String postalAddress) {
		this.postalAddress = postalAddress;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getRegisteredAddress() {
		return registeredAddress;
	}
	public void setRegisteredAddress(String registeredAddress) {
		this.registeredAddress = registeredAddress;
	}
	public String getPostOfficeBox() {
		return postOfficeBox;
	}
	public void setPostOfficeBox(String postOfficeBox) {
		this.postOfficeBox = postOfficeBox;
	}
	public String getPhysicalDeliveryOfficeName() {
		return physicalDeliveryOfficeName;
	}
	public void setPhysicalDeliveryOfficeName(String physicalDeliveryOfficeName) {
		this.physicalDeliveryOfficeName = physicalDeliveryOfficeName;
	} 
	

}
