/**
 * 
 */
package org.georchestra.ldapadmin.ws.edituserdetails;

/**
 * @author Mauricio Pazos
 *
 */
public class EditUserDetailsFormBean implements java.io.Serializable {
	
	private String uid; 
	private String surname; 
	private String firstName; 
	private String email;
	private String title; 
	private String phone;
	private String facsimile;
	private String org;
	private String description;
	private String postalAddress; 
	
	@Override
	public String toString() {
		return "EditUserDetailsFormBean [uid=" + uid + ", surname=" + surname
				+ ", givenName=" + firstName + ", email=" + email + ", title=" + title
				+ ", phone=" + phone + ", facsimile=" + facsimile
				+ ", org=" + org + ", description="
				+ description + ", postalAddress=" + postalAddress
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
	public String getFacsimile() {
		return facsimile;
	}
	public void setFacsimile(String facsimile) {
		this.facsimile = facsimile;
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

}
