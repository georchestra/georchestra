package org.georchestra.ldapadmin.dto;

/**
 * Account data transfer object.
 * 
 * @author Mauricio Pazos
 *
 */
public interface Account {

	public   void setUid(String uid);

	public   String getUid();

	public   String getCommonName();

	public   void setCommonName(String name);

	public   String getOrg();

	public   void setOrg(String org);

	public   String getRole();

	public   void setRole(String role);

	public   String getEmail();

	public   void setEmail(String email);

	public   String getPhone();

	public   void setPhone(String phone);

	public   String getDetails();

	public   void setDetails(String details);

	public   void setPassword(String password);

	public   String getPassword();

	public   void setNewPassword(String newPassword);

	public   String getNewPassword();

	public   String getSurname();

	public   void setSurname(String surname);

	public   String getGivenName();

	public   void setGivenName(String givenName);

	public   String getTitle();

	public   void setTitle(String title);

	public   String getPostalAddress();

	public   void setPostalAddress(String postalAddress);

	public   String getPostalCode();

	public   void setPostalCode(String postalCode);

	public   String getRegisteredAddress();

	public   void setRegisteredAddress(String registeredAddress);

	public   String getPostOfficeBox();

	public   void setPostOfficeBox(String postOfficeBox);

	public   String getPhysicalDeliveryOfficeName();

	public   void setPhysicalDeliveryOfficeName( String physicalDeliveryOfficeName);

}