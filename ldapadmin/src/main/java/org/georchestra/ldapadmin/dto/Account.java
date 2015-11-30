package org.georchestra.ldapadmin.dto;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Account data transfer object.
 *
 * @author Mauricio Pazos
 *
 */
public interface Account {

	void setUid(String uid);

	String getUid();

	/**
	 * Person’s full name.
	 */
	String getCommonName();

	/**
	 * Person’s full name.
	 */
	void setCommonName(String name);

	String getOrg();

	void setOrg(String org);

	String getEmail();

	void setEmail(String email);

	String getPhone();

	void setPhone(String phone);

	String getDescription();

	void setDescription(String description);

	void setPassword(String password);

	String getPassword();

	void setNewPassword(String newPassword);

	String getNewPassword();

	String getSurname();

	void setSurname(String surname);

	/**
	 * The givenName attribute is used to hold the part of a person’s name which
	 * is not their surname nor middle name.
	 */
	String getGivenName();

	/**
	 * The givenName attribute is used to hold the part of a person’s name which
	 * is not their surname nor middle name.
	 */
	void setGivenName(String givenName);

	String getTitle();

	void setTitle(String title);

	String getPostalAddress();

	void setPostalAddress(String postalAddress);

	String getPostalCode();

	void setPostalCode(String postalCode);

	String getRegisteredAddress();

	void setRegisteredAddress(String registeredAddress);

	String getPostOfficeBox();

	void setPostOfficeBox(String postOfficeBox);

	String getPhysicalDeliveryOfficeName();

	void setPhysicalDeliveryOfficeName(String physicalDeliveryOfficeName);

	void setStreet(String street);

	String getStreet();

	void setLocality(String locality);

	String getLocality();

	void setFacsimile(String facsimile);

	String getFacsimile();

	void setMobile(String mobile);

	String getMobile();

	void setRoomNumber(String roomNumber);

	String getRoomNumber();

	void setStateOrProvince(String stateOrProvince);

	String getStateOrProvince();

	void setOrganizationalUnit(String organizationalUnit);

	String getOrganizationalUnit();

	void setHomePostalAddress(String homePostalAddress);

	String getHomePostalAddress();

	String toVcf();

	String toCsv();

	JSONObject toJSON() throws JSONException;
	
	String toFormatedString(String data);

	void setUUID(String uuid);

	String getUUID();

	void setShadowExpire(Date expireDate);

	Date getShadowExpire();

}
